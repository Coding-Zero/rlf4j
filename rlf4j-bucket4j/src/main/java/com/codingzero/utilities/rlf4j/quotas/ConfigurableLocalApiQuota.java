package com.codingzero.utilities.rlf4j.quotas;

import com.codingzero.utilities.rlf4j.ApiIdentity;
import com.codingzero.utilities.rlf4j.ApiQuotaConfig;
import com.codingzero.utilities.rlf4j.ConfigurableApiQuota;
import com.codingzero.utilities.rlf4j.ConsumptionReport;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;

public abstract class ConfigurableLocalApiQuota extends ConfigurableApiQuota {

    private LocalBucketProvider bucketProvider;

    public ConfigurableLocalApiQuota(ApiQuotaConfig config,
                                     LocalBucketProvider bucketProvider) {
        super(config);
        this.bucketProvider = bucketProvider;
    }

    @Override
    public void updateConfig(ApiQuotaConfig config) {
        super.updateConfig(config);
        bucketProvider.clean();
    }

    @Override
    protected boolean tryConsumeInternally(ApiIdentity identity, long token) {
        Bucket bucket = getBucket(identity);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(token);
        return probe.isConsumed();
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
