package com.codingzero.utilities.rlf4j.quotas;

import com.codingzero.utilities.rlf4j.ApiIdentity;
import com.codingzero.utilities.rlf4j.ApiQuota;
import com.codingzero.utilities.rlf4j.ConsumptionReport;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

public abstract class LocalApiQuota implements ApiQuota {

    private LocalBucketProvider bucketProvider;

    public LocalApiQuota(LocalBucketProvider bucketProvider) {
        this.bucketProvider = bucketProvider;
    }

    @Override
    public boolean isConsumptionReportSupported() {
        return true;
    }

    @Override
    public ConsumptionReport tryConsumeAndRetuningReport(ApiIdentity identity, long token) {
        Bucket bucket = getBucket(identity);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(token);
        if (probe.isConsumed()) {
            return ConsumptionReport.consumed(token).remainingQuota(probe.getRemainingTokens()).build();
        } else {
            return ConsumptionReport.notConsumed().build();
        }
    }

    abstract protected Bandwidth getBandwidth(ApiIdentity identity);

    abstract protected String getBucketKey(ApiIdentity identity);

    private Bucket getBucket(ApiIdentity identity) {
        String key = getBucketKey(identity);
        return bucketProvider.get(key, identity, id -> getBandwidth(id));
    }

    @Override
    public void supplement(ApiIdentity identity, long token) {
        Bucket bucket = getBucket(identity);
        bucket.addTokens(token);
    }

}
