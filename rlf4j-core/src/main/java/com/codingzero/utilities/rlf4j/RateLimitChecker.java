package com.codingzero.utilities.rlf4j;

public interface RateLimitChecker {

    boolean isThrottling(ApiIdentity identity);
}
