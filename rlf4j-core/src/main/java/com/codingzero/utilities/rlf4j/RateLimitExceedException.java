package com.codingzero.utilities.rlf4j;

public class RateLimitExceedException extends Exception {

    private ApiIdentity apiIdentity;
    private ConsumptionReport consumptionReport;
    private Class<? extends ApiQuota> apiQuota;

    public RateLimitExceedException(ApiIdentity apiIdentity,
                                    ConsumptionReport consumptionReport,
                                    Class<? extends ApiQuota> apiQuota) {
        super("Api " + apiIdentity.getId() + " has exceed the quota, " + apiQuota);
        this.apiIdentity = apiIdentity;
        this.consumptionReport = consumptionReport;
        this.apiQuota = apiQuota;
    }

    public ApiIdentity getApiIdentity() {
        return apiIdentity;
    }

    public ConsumptionReport getConsumptionResult() {
        return consumptionReport;
    }

    public Class<? extends ApiQuota> getRateLimitQuota() {
        return apiQuota;
    }
}
