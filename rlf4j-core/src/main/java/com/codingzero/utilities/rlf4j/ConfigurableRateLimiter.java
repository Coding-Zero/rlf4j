package com.codingzero.utilities.rlf4j;

public interface ConfigurableRateLimiter<T> extends RateLimiter<T> {

    void updateQuotaConfig(String name, ApiQuotaConfig config) throws ApiQuotaConfigUpdateException;

    ApiQuotaConfig getCurrentQuotaConfig(String name);

}
