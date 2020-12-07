package com.codingzero.utilities.rlf4j.quotas;

import com.codingzero.utilities.rlf4j.ApiIdentity;
import com.codingzero.utilities.rlf4j.ApiQuotaConfig;
import com.codingzero.utilities.rlf4j.ConfigurableApiQuota;
import com.codingzero.utilities.rlf4j.ConsumptionReport;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

public abstract class ConfigurableDistributedApiQuota extends ConfigurableApiQuota {

    private DistributedBucketProvider bucketProvider;

    public ConfigurableDistributedApiQuota(ApiQuotaConfig config) {
        this(config, DistributedBucketProvider.builder().build());
    }

    public ConfigurableDistributedApiQuota(ApiQuotaConfig config,
                                           DistributedBucketProvider bucketProvider) {
        super(config);
        this.bucketProvider = bucketProvider;
    }

    private Bucket getBucket(ApiIdentity identity) {
        String key = getBucketKey(identity);
        return bucketProvider.get(key, identity, id -> getBandwidth(id));
    }

    abstract protected Bandwidth getBandwidth(ApiIdentity identity);

    abstract protected String getBucketKey(ApiIdentity identity);

    @Override
    protected boolean tryConsumeInternally(ApiIdentity identity, long token) {
        Bucket bucket = getBucket(identity);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(token);
        return (probe.isConsumed());
    }

    @Override
    protected ConsumptionReport tryConsumeAndRetuningReportInternally(ApiIdentity identity, long token) {
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

    @Override
    public void updateConfig(ApiQuotaConfig config) {
        super.updateConfig(config);
        bucketProvider.clean();
    }
}
