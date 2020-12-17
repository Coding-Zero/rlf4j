package com.codingzero.utilities.rlf4j;

public class RateLimitFailedException extends Exception {

    private ApiIdentity apiIdentity;
    private Class<? extends ApiQuota> apiQuota;

    public RateLimitFailedException(ApiIdentity apiIdentity,
                                    Class<? extends ApiQuota> apiQuota,
                                    Throwable cause) {
        super(
                "Try to throttle Api " + apiIdentity.getId()
                        + " with quota, " + apiQuota
                        + " failed due to " + cause.getMessage(),
                cause);
        this.apiIdentity = apiIdentity;
        this.apiQuota = apiQuota;
    }

    public ApiIdentity getApiIdentity() {
        return apiIdentity;
    }

    public Class<? extends ApiQuota> getRateLimitQuota() {
        return apiQuota;
    }
}
