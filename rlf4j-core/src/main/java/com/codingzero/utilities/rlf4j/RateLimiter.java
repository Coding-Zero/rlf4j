package com.codingzero.utilities.rlf4j;

import java.util.List;

public interface RateLimiter<T> {

    DefaultRateLimiter<T> quota(ApiQuota quota);

    DefaultRateLimiter<T> identifier(ApiIdentifier<T> identifier);

    ApiIdentifier<T> getCurrentIdentifier();

    List<ApiQuota> getCurrentQuotas();

    <R> R tryLimit(T apiInstance, ApiExecution<R> execution) throws RateLimitExceedException;

    void tryLimitWithoutReturn(T apiInstance, ApiExecutionWithoutReturn execution) throws RateLimitExceedException;

}
