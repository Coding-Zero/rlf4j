package com.codingzero.utilities.rlf4j;

public interface RateLimitQuota {

    boolean tryConsume(ApiIdentity identity, long token);

    default boolean isConsumptionReportSupported() {
        return false;
    }

    default ConsumptionReport tryConsumeAndRetuningReport(ApiIdentity identity, long token) {
        return null;
    }

    default boolean isSupplementRequired() {
        return false;
    }

    default void supplement(ApiIdentity identity, long token) { }

}
