package dev.openfeature.sdk;

import java.util.ArrayList;
import java.util.List;

/**
 * The interface implemented by upstream flag providers to resolve flags for
 * their service. If you want to support realtime events with your provider, you
 * should extend {@link EventProvider}
 */
public interface FeatureProvider {
    Metadata getMetadata();

    default List<Hook> getProviderHooks() {
        return new ArrayList<>();
    }

    ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean defaultValue, EvaluationContext ctx);

    ProviderEvaluation<String> getStringEvaluation(String key, String defaultValue, EvaluationContext ctx);

    ProviderEvaluation<Integer> getIntegerEvaluation(String key, Integer defaultValue, EvaluationContext ctx);

    ProviderEvaluation<Double> getDoubleEvaluation(String key, Double defaultValue, EvaluationContext ctx);

    ProviderEvaluation<Value> getObjectEvaluation(String key, Value defaultValue, EvaluationContext ctx);

    /**
     * This method is called before a provider is used to evaluate flags. Providers
     * can overwrite this method, if they have special initialization needed prior 
     * being called for flag evaluation.
     * 
     * <p>
     * It is ok if the method is expensive as it is executed in the background. All
     * runtime exceptions will be caught and logged.
     * </p>
     */
    default void initialize(EvaluationContext evaluationContext) throws Exception {
        // Intentionally left blank
    }

    /**
     * This method is called when a new provider is about to be used to evaluate
     * flags, or the SDK is shut down. Providers can overwrite this method, if they 
     * have special shutdown actions needed.
     * 
     * <p>
     * It is ok if the method is expensive as it is executed in the background. All
     * runtime exceptions will be caught and logged.
     * </p>
     */
    default void shutdown() {
        // Intentionally left blank
    }

    /**
     * Returns a representation of the current readiness of the provider.
     * If the provider needs to be initialized, it should return {@link ProviderState#NOT_READY}.
     * If the provider is in an error state, it should return {@link ProviderState#ERROR}.
     * If the provider is functioning normally, it should return {@link ProviderState#READY}.
     * 
     * <p>
     * <i>Providers which do not implement this method are assumed to be ready immediately.</i>
     * </p>
     *
     * @return ProviderState
     * @deprecated The state is handled by the SDK internally. Query the state from the {@link Client} instead.
     */

    @Deprecated
    default ProviderState getState() {
        return ProviderState.READY;
    }

    /**
     * Feature provider implementations can opt in for to support Tracking by implementing this method.
     *
     * @param eventName The name of the tracking event
     * @param context   Evaluation context used in flag evaluation (Optional)
     * @param details   Data pertinent to a particular tracking event (Optional)
     */
    default void track(String eventName, EvaluationContext context, TrackingEventDetails details) {

    }
}
