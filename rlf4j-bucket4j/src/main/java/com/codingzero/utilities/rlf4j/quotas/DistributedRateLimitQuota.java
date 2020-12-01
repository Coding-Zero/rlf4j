package com.codingzero.utilities.rlf4j.quotas;

import com.codingzero.utilities.rlf4j.ApiIdentity;
import com.codingzero.utilities.rlf4j.ConsumptionReport;
import com.codingzero.utilities.rlf4j.RateLimitQuota;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class DistributedRateLimitQuota implements RateLimitQuota {

    private List<ProxyManager<String>> buckets;
    private final int numberOfBuckets;

    public DistributedRateLimitQuota(int numberOfBuckets, String cacheName) {
        this(numberOfBuckets, cacheName, Caching.getCachingProvider().getCacheManager());
    }

    public DistributedRateLimitQuota(int numberOfBuckets, String cacheName, CacheManager cacheManager) {
        this.numberOfBuckets = numberOfBuckets;
        this.buckets = new ArrayList<>(numberOfBuckets);
        initBuckets(cacheName, cacheManager);
    }

    private void initBuckets(String cacheName, CacheManager cacheManager) {
        for (int i = 0; i < numberOfBuckets; i ++) {
            cacheName = cacheName + "-" + i;
            this.buckets.add(
                    Bucket4j.extension(JCache.class)
                            .proxyManagerForCache(
                                    createCache(cacheName, cacheManager))
            );
        }
    }

    private Cache<String, GridBucketState> createCache(String cacheName, CacheManager cacheManager) {
        MutableConfiguration<String, GridBucketState> configuration = new MutableConfiguration<>();
        return cacheManager.createCache(cacheName, configuration);
    }

    private Bucket getBucket(ApiIdentity identity) {
        Bandwidth bandwidth = getBandwidth(identity);
        String key = getBucketKey(identity);
        int bucketIndex = getBucketIndex(key);
        return buckets.get(bucketIndex).getProxy(key, getBucketConfiguration(bandwidth));
    }

    private int getBucketIndex(String key) {
        return Math.abs(key.hashCode()) % numberOfBuckets;
    }

    private Supplier<BucketConfiguration> getBucketConfiguration(Bandwidth bandwidth) {
        return () -> Bucket4j.configurationBuilder().addLimit(bandwidth).build();
    }

    abstract protected Bandwidth getBandwidth(ApiIdentity identity);

    abstract protected String getBucketKey(ApiIdentity identity);

    @Override
    public boolean tryConsume(ApiIdentity identity, long token) {
        return false;
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

}
