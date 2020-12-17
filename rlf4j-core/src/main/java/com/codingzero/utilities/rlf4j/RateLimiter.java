package com.codingzero.utilities.rlf4j;

public interface RateLimiter<T> {

    void setApiIdentifier(ApiIdentifier<T> identifier);

    void addApiQuota(ApiQuota quota);

    <R> R tryLimit(T apiInstance, ApiExecution<R> execution)
            throws RateLimitExceedException, RateLimitFailedException;

    void tryLimit(T apiInstance, ApiExecutionWithoutReturn execution)
            throws RateLimitExceedException, RateLimitFailedException;

}
