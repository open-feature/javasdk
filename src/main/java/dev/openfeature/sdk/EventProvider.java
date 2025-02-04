package dev.openfeature.sdk;

import dev.openfeature.sdk.internal.TriConsumer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract EventProvider. Providers must extend this class to support events.
 * Emit events with {@link #emit(ProviderEvent, ProviderEventDetails)}. Please
 * note that the SDK will automatically emit
 * {@link ProviderEvent#PROVIDER_READY } or
 * {@link ProviderEvent#PROVIDER_ERROR } accordingly when
 * {@link FeatureProvider#initialize(EvaluationContext)} completes successfully
 * or with error, so these events need not be emitted manually during
 * initialization.
 *
 * @see FeatureProvider
 */
@Slf4j
public abstract class EventProvider implements FeatureProvider {
    private EventProviderListener eventProviderListener;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 3;
    private static final ExecutorService emitterExecutor = Executors.newCachedThreadPool(runnable -> {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    void setEventProviderListener(EventProviderListener eventProviderListener) {
        this.eventProviderListener = eventProviderListener;
    }

    private TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = null;

    /**
     * "Attach" this EventProvider to an SDK, which allows events to propagate from this provider.
     * No-op if the same onEmit is already attached.
     *
     * @param onEmit the function to run when a provider emits events.
     * @throws IllegalStateException if attempted to bind a new emitter for already bound provider
     */
    void attach(TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit) {
        if (this.onEmit != null && this.onEmit != onEmit) {
            // if we are trying to attach this provider to a different onEmit, something has gone wrong
            throw new IllegalStateException("Provider " + this.getMetadata().getName() + " is already attached.");
        } else {
            this.onEmit = onEmit;
        }
    }

    /**
     * "Detach" this EventProvider from an SDK, stopping propagation of all events.
     */
    void detach() {
        this.onEmit = null;
    }

    /**
     * Stop the event emitter executor and block until either termination has completed
     * or timeout period has elapsed.
     */
    @Override
    public void shutdown() {
        emitterExecutor.shutdown();
        try {
            if (!emitterExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("Emitter executor did not terminate before the timeout period had elapsed");
                emitterExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            emitterExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Emit the specified {@link ProviderEvent}.
     *
     * @param event   The event type
     * @param details The details of the event
     */
    public void emit(ProviderEvent event, ProviderEventDetails details) {
        if (eventProviderListener != null) {
            eventProviderListener.onEmit(event, details);
        }

        final TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> localOnEmit = this.onEmit;
        if (localOnEmit != null) {
            emitterExecutor.submit(() -> localOnEmit.accept(this, event, details));
        }
    }

    /**
     * Emit a {@link ProviderEvent#PROVIDER_READY} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     */
    public void emitProviderReady(ProviderEventDetails details) {
        emit(ProviderEvent.PROVIDER_READY, details);
    }

    /**
     * Emit a
     * {@link ProviderEvent#PROVIDER_CONFIGURATION_CHANGED}
     * event. Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     */
    public void emitProviderConfigurationChanged(ProviderEventDetails details) {
        emit(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, details);
    }

    /**
     * Emit a {@link ProviderEvent#PROVIDER_STALE} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     */
    public void emitProviderStale(ProviderEventDetails details) {
        emit(ProviderEvent.PROVIDER_STALE, details);
    }

    /**
     * Emit a {@link ProviderEvent#PROVIDER_ERROR} event.
     * Shorthand for {@link #emit(ProviderEvent, ProviderEventDetails)}
     *
     * @param details The details of the event
     */
    public void emitProviderError(ProviderEventDetails details) {
        emit(ProviderEvent.PROVIDER_ERROR, details);
    }
}
