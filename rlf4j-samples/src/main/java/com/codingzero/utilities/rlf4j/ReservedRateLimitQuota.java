package com.codingzero.utilities.rlf4j;

import com.codingzero.utilities.rlf4j.quotas.LocalRateLimitQuota;
import io.github.bucket4j.Bandwidth;

public class ReservedRateLimitQuota extends LocalRateLimitQuota {

    @Override
    protected Bandwidth getBandwidth(ApiIdentity identity) {
        return null;
    }

}
