package dev.openfeature.sdk;

/**
 * {@link TransactionContextPropagator} is responsible for persisting a transactional context
 * for the duration of a single transaction.
 * Examples of potential transaction specific context include: a user id, user agent, IP.
 * Transaction context is merged with evaluation context prior to flag evaluation.
 * 
 * <p>
 * The precedence of merging context can be seen in
 * <a href=https://openfeature.dev/specification/sections/evaluation-context#requirement-323>the specification</a>.
 * </p>
 */
public interface TransactionContextPropagator {

    /**
     * Returns the currently defined transaction context using the registered transaction
     * context propagator.
     *
     * @return {@link EvaluationContext} The current transaction context
     */
    EvaluationContext getTransactionContext();

    /**
     * Sets the transaction context.
     */
    void setTransactionContext(EvaluationContext evaluationContext);
}
