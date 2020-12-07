package com.codingzero.utilities.rlf4j.quotas;

import com.codingzero.utilities.rlf4j.ApiIdentity;
import com.codingzero.utilities.rlf4j.ApiQuota;
import com.codingzero.utilities.rlf4j.ConsumptionReport;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

public abstract class DistributedApiQuota implements ApiQuota {

    private DistributedBucketProvider bucketProvider;

    public DistributedApiQuota() {
        this(DistributedBucketProvider.builder().build());
    }

    public DistributedApiQuota(DistributedBucketProvider bucketProvider) {
        this.bucketProvider = bucketProvider;
    }

    private Bucket getBucket(ApiIdentity identity) {
        String key = getBucketKey(identity);
        return bucketProvider.get(key, identity, id -> getBandwidth(id));
    }

    abstract protected Bandwidth getBandwidth(ApiIdentity identity);

    abstract protected String getBucketKey(ApiIdentity identity);

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

    @Override
    public void supplement(ApiIdentity identity, long token) {
        Bucket bucket = getBucket(identity);
        bucket.addTokens(token);
    }

}
