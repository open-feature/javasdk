package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

/**
 * Util class for storing and running handlers.
 */
@Slf4j
class EventSupport {

    public static final int SHUTDOWN_TIMEOUT_SECONDS = 3;

    // we use a v4 uuid as a "placeholder" for anonymous clients, since
    // ConcurrentHashMap doesn't support nulls
    private static final String defaultClientUuid = UUID.randomUUID().toString();
    private final Map<String, HandlerStore> handlerStores = new ConcurrentHashMap<>();
    private final HandlerStore globalHandlerStore = new HandlerStore();
    private final ExecutorService taskExecutor = Executors.newCachedThreadPool(runnable -> {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Run all the event handlers associated with this domain.
     * If the domain is null, handlers attached to unnamed clients will run.
     *
     * @param domain       the domain to run event handlers for, or null
     * @param event        the event type
     * @param eventDetails the event details
     */
    public void runClientHandlers(String domain, ProviderEvent event, EventDetails eventDetails) {
        domain = Optional.ofNullable(domain).orElse(defaultClientUuid);

        // run handlers if they exist
        Optional.ofNullable(handlerStores.get(domain))
                .filter(store -> Optional.of(store).isPresent())
                .map(store -> store.handlerMap.get(event))
                .ifPresent(handlers -> handlers.forEach(handler -> runHandler(handler, eventDetails)));
    }

    /**
     * Run all the API (global) event handlers.
     *
     * @param event        the event type
     * @param eventDetails the event details
     */
    public void runGlobalHandlers(ProviderEvent event, EventDetails eventDetails) {
        globalHandlerStore.handlerMap.get(event).forEach(handler -> {
            runHandler(handler, eventDetails);
        });
    }

    /**
     * Add a handler for the specified domain, or all unnamed clients.
     *
     * @param domain  the domain to add handlers for, or else unnamed
     * @param event   the event type
     * @param handler the handler function to run
     */
    public void addClientHandler(String domain, ProviderEvent event, Consumer<EventDetails> handler) {
        final String name = Optional.ofNullable(domain).orElse(defaultClientUuid);

        // lazily create and cache a HandlerStore if it doesn't exist
        HandlerStore store = Optional.ofNullable(this.handlerStores.get(name)).orElseGet(() -> {
            HandlerStore newStore = new HandlerStore();
            this.handlerStores.put(name, newStore);
            return newStore;
        });
        store.addHandler(event, handler);
    }

    /**
     * Remove a client event handler for the specified event type.
     *
     * @param domain  the domain of the client handler to remove, or null to remove
     *                from unnamed clients
     * @param event   the event type
     * @param handler the handler ref to be removed
     */
    public void removeClientHandler(String domain, ProviderEvent event, Consumer<EventDetails> handler) {
        domain = Optional.ofNullable(domain).orElse(defaultClientUuid);
        this.handlerStores.get(domain).removeHandler(event, handler);
    }

    /**
     * Add a global event handler of the specified event type.
     *
     * @param event   the event type
     * @param handler the handler to be added
     */
    public void addGlobalHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        this.globalHandlerStore.addHandler(event, handler);
    }

    /**
     * Remove a global event handler for the specified event type.
     *
     * @param event   the event type
     * @param handler the handler ref to be removed
     */
    public void removeGlobalHandler(ProviderEvent event, Consumer<EventDetails> handler) {
        this.globalHandlerStore.removeHandler(event, handler);
    }

    /**
     * Get all domain names for which we have event handlers registered.
     *
     * @return set of domain names
     */
    public Set<String> getAllDomainNames() {
        return this.handlerStores.keySet();
    }

    /**
     * Run the passed handler on the taskExecutor.
     *
     * @param handler      the handler to run
     * @param eventDetails the event details
     */
    public void runHandler(Consumer<EventDetails> handler, EventDetails eventDetails) {
        taskExecutor.submit(() -> {
            try {
                handler.accept(eventDetails);
            } catch (Exception e) {
                log.error("Exception in event handler {}", handler, e);
            }
        });
    }

    /**
     * Stop the event handler task executor and block until either termination has completed
     * or timeout period has elapsed.
     */
    public void shutdown() {
        taskExecutor.shutdown();
        try {
            if (!taskExecutor.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                log.warn("Task executor did not terminate before the timeout period had elapsed");
                taskExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            taskExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // Handler store maintains a set of handlers for each event type.
    // Each client in the SDK gets it's own handler store, which is lazily
    // instantiated when a handler is added to that client.
    static class HandlerStore {

        private final Map<ProviderEvent, List<Consumer<EventDetails>>> handlerMap;

        HandlerStore() {
            handlerMap = new ConcurrentHashMap<>();
            handlerMap.put(ProviderEvent.PROVIDER_READY, new ArrayList<>());
            handlerMap.put(ProviderEvent.PROVIDER_CONFIGURATION_CHANGED, new ArrayList<>());
            handlerMap.put(ProviderEvent.PROVIDER_ERROR, new ArrayList<>());
            handlerMap.put(ProviderEvent.PROVIDER_STALE, new ArrayList<>());
        }

        void addHandler(ProviderEvent event, Consumer<EventDetails> handler) {
            handlerMap.get(event).add(handler);
        }

        void removeHandler(ProviderEvent event, Consumer<EventDetails> handler) {
            handlerMap.get(event).remove(handler);
        }
    }
}
