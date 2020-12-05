package com.codingzero.utilities.rlf4j;

public interface ApiQuota {

    default boolean tryConsume(ApiIdentity identity, long token) {
        throw new IllegalStateException("Please implement this method if method isConsumptionReportSupported{} return false.");
    }

    default boolean isConsumptionReportSupported() {return false;}

    default ConsumptionReport tryConsumeAndRetuningReport(ApiIdentity identity, long token) {
        throw new IllegalStateException("Please implement this method if method isConsumptionReportSupported{} return true.");
    }

    default boolean isSupplementRequired() {
        return false;
    }

    default void supplement(ApiIdentity identity, long token) {
        throw new IllegalStateException("Please implement this method if method isSupplementRequired{} return true.");
    }

}
