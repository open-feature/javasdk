package dev.openfeature.sdk.testutils;

import dev.openfeature.sdk.EvaluationContext;
import dev.openfeature.sdk.EventProvider;
import dev.openfeature.sdk.Metadata;
import dev.openfeature.sdk.ProviderEvaluation;
import dev.openfeature.sdk.ProviderEvent;
import dev.openfeature.sdk.ProviderEventDetails;
import dev.openfeature.sdk.Value;
import java.util.function.Consumer;

public class TestStackedEmitCallsProvider extends EventProvider {
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
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx) {
        throw new UnsupportedOperationException("Unimplemented method 'getBooleanEvaluation'");
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx) {
        throw new UnsupportedOperationException("Unimplemented method 'getStringEvaluation'");
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx) {
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
}
