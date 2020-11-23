package com.codingzero.utilities.rlf4j.checkers;

import com.codingzero.utilities.rlf4j.ApiIdentity;
import com.codingzero.utilities.rlf4j.RateLimitChecker;

public class DistributedRateLimitChecker implements RateLimitChecker {

    @Override
    public boolean isThrottling(ApiIdentity identity) {
        return false;
    }
}
