package com.codingzero.utilities.rlf4j;

public class RateLimitExceedException extends Exception {

    private ApiIdentity apiIdentity;
    private ConsumptionReport consumptionReport;
    private ApiQuota apiQuota;

    public RateLimitExceedException(ApiIdentity apiIdentity,
                                    ConsumptionReport consumptionReport,
                                    ApiQuota apiQuota) {
        super("API \'" + apiIdentity.getId() + "\' has exceed the rate limit quota, " + apiQuota.getClass());
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

    public ApiQuota getRateLimitQuota() {
        return apiQuota;
    }
}
