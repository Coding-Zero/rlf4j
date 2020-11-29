package com.codingzero.utilities.rlf4j;

public class RateLimitExceedException extends Exception {

    private ApiIdentity apiIdentity;
    private ConsumptionReport consumptionReport;
    private RateLimitQuota rateLimitQuota;

    public RateLimitExceedException(ApiIdentity apiIdentity,
                                    ConsumptionReport consumptionReport,
                                    RateLimitQuota rateLimitQuota) {
        super("API \'" + apiIdentity.getId() + "\' has exceed the rate limit quota.");
        this.apiIdentity = apiIdentity;
        this.consumptionReport = consumptionReport;
        this.rateLimitQuota = rateLimitQuota;
    }

    public ApiIdentity getApiIdentity() {
        return apiIdentity;
    }

    public ConsumptionReport getConsumptionResult() {
        return consumptionReport;
    }

    public RateLimitQuota getRateLimitQuota() {
        return rateLimitQuota;
    }
}
