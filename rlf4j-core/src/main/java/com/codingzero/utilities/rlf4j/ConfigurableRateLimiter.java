package com.codingzero.utilities.rlf4j;

public interface ConfigurableRateLimiter<T> extends RateLimiter<T> {

    void updateQuotaConfig(String name, ApiQuotaConfig config);

    ApiQuotaConfig getCurrentQuotaConfig(String name);

}
