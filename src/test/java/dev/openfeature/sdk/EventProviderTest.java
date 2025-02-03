package dev.openfeature.sdk;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import dev.openfeature.sdk.internal.TriConsumer;
import java.util.function.Consumer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class EventProviderTest {

    private TestEventProvider eventProvider;

    @BeforeEach
    @SneakyThrows
    void setup() {
        eventProvider = new TestEventProvider();
        eventProvider.initialize(null);
    }

    @Test
    @DisplayName("should run attached onEmit with emitters")
    void emitsEventsWhenAttached() {
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = mockOnEmit();
        eventProvider.attach(onEmit);

        ProviderEventDetails details = ProviderEventDetails.builder().build();
        eventProvider.emit(ProviderEvent.PROVIDER_READY, details);
        eventProvider.emitProviderReady(details);
        eventProvider.emitProviderConfigurationChanged(details);
        eventProvider.emitProviderStale(details);
        eventProvider.emitProviderError(details);

        verify(onEmit, times(2)).accept(eventProvider, ProviderEvent.PROVIDER_READY, details);
        verify(onEmit, times(1)).accept(eventProvider, ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, details);
        verify(onEmit, times(1)).accept(eventProvider, ProviderEvent.PROVIDER_STALE, details);
        verify(onEmit, times(1)).accept(eventProvider, ProviderEvent.PROVIDER_ERROR, details);
    }

    @Test
    @DisplayName("should do nothing with emitters if no onEmit attached")
    void doesNotEmitsEventsWhenNotAttached() {
        // don't attach this emitter
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit = mockOnEmit();

        ProviderEventDetails details = ProviderEventDetails.builder().build();
        eventProvider.emit(ProviderEvent.PROVIDER_READY, details);
        eventProvider.emitProviderReady(details);
        eventProvider.emitProviderConfigurationChanged(details);
        eventProvider.emitProviderStale(details);
        eventProvider.emitProviderError(details);

        // should not be called
        verify(onEmit, never()).accept(any(), any(), any());
    }

    @Test
    @DisplayName("should throw if second different onEmit attached")
    void throwsWhenOnEmitDifferent() {
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit1 = mockOnEmit();
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit2 = mockOnEmit();
        eventProvider.attach(onEmit1);
        assertThrows(IllegalStateException.class, () -> eventProvider.attach(onEmit2));
    }

    @Test
    @DisplayName("should not throw if second same onEmit attached")
    void doesNotThrowWhenOnEmitSame() {
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit1 = mockOnEmit();
        TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit2 = onEmit1;
        eventProvider.attach(onEmit1);
        eventProvider.attach(onEmit2); // should not throw, same instance. noop
    }

    @Test
    @SneakyThrows
    @Timeout(value = 2, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @DisplayName("should not deadlock on emit called during emit")
    void doesNotDeadlockOnEmitStackedCalls() {
        StackedEmitCallsProvider provider = new StackedEmitCallsProvider();
        OpenFeatureAPI.getInstance().setProviderAndWait(provider);
    }

    static class StackedEmitCallsProvider extends EventProvider {
        private final NestedBlockingEmitter nestedBlockingEmitter = new NestedBlockingEmitter(this::onProviderEvent);

        @Override
        public Metadata getMetadata() {
            return () -> getClass().getSimpleName();
        }

        @Override
        public void initialize(EvaluationContext evaluationContext) throws Exception {
            synchronized (nestedBlockingEmitter) {
                nestedBlockingEmitter.init();
                while (!nestedBlockingEmitter.isReady()) {
                    try {
                        nestedBlockingEmitter.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        private void onProviderEvent(ProviderEvent providerEvent) {
            synchronized (nestedBlockingEmitter) {
                if (providerEvent == ProviderEvent.PROVIDER_READY) {
                    nestedBlockingEmitter.setReady();
                    /*
                     * This line deadlocked in the original implementation without the emitterExecutor see
                     * https://github.com/open-feature/java-sdk/issues/1299
                     */
                    emitProviderReady(ProviderEventDetails.builder().build());
                }
            }
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(
                String key, Boolean defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getBooleanEvaluation'");
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getStringEvaluation'");
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(
                String key, Integer defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getIntegerEvaluation'");
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getDoubleEvaluation'");
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getObjectEvaluation'");
        }
    }

    static class NestedBlockingEmitter {

        private final Consumer<ProviderEvent> emitProviderEvent;
        private volatile boolean isReady;

        public NestedBlockingEmitter(Consumer<ProviderEvent> emitProviderEvent) {
            this.emitProviderEvent = emitProviderEvent;
        }

        public void init() {
            // run init outside monitored thread
            new Thread(() -> {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        emitProviderEvent.accept(ProviderEvent.PROVIDER_READY);
                    })
                    .start();
        }

        public boolean isReady() {
            return isReady;
        }

        public synchronized void setReady() {
            isReady = true;
            this.notifyAll();
        }
    }

    static class TestEventProvider extends EventProvider {

        private static final String NAME = "TestEventProvider";

        @Override
        public Metadata getMetadata() {
            return () -> NAME;
        }

        @Override
        public ProviderEvaluation<Boolean> getBooleanEvaluation(
                String key, Boolean defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getBooleanEvaluation'");
        }

        @Override
        public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getStringEvaluation'");
        }

        @Override
        public ProviderEvaluation<Integer> getIntegerEvaluation(
                String key, Integer defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getIntegerEvaluation'");
        }

        @Override
        public ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getDoubleEvaluation'");
        }

        @Override
        public ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx) {
            throw new UnsupportedOperationException("Unimplemented method 'getObjectEvaluation'");
        }

        @Override
        public void attach(TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> onEmit) {
            super.attach(onEmit);
        }
    }

    @SuppressWarnings("unchecked")
    private TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails> mockOnEmit() {
        return (TriConsumer<EventProvider, ProviderEvent, ProviderEventDetails>) mock(TriConsumer.class);
    }
}
