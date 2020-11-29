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
import java.util.function.Supplier;

public abstract class DistributedRateLimitQuota implements RateLimitQuota {

    private ProxyManager<String> buckets;
    private String cacheName;

    public DistributedRateLimitQuota(String cacheName) {
        this(cacheName, Caching.getCachingProvider().getCacheManager());
    }

    public DistributedRateLimitQuota(String cacheName, CacheManager cacheManager) {
        this.cacheName = cacheName;
        this.buckets = Bucket4j.extension(JCache.class).proxyManagerForCache(getCache(cacheManager));
    }

    private Cache<String, GridBucketState> getCache(CacheManager cacheManager) {
        MutableConfiguration<String, GridBucketState> configuration = new MutableConfiguration<>();
        return cacheManager.createCache(cacheName, configuration);
    }

    private Bucket getBucket(ApiIdentity identity) {
        Bandwidth bandwidth = getBandwidth(identity);
        String key = getBucketKey(identity);
        return buckets.getProxy(key, getBucketConfiguration(bandwidth));
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
