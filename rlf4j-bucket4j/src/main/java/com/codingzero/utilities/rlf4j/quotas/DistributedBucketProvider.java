package com.codingzero.utilities.rlf4j.quotas;

import com.codingzero.utilities.rlf4j.ApiIdentity;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.BucketConfiguration;
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

public class DistributedBucketProvider {

    public final static int DEFAULT_NUMBER_OF_BUCKETS = 1000;
    public final static String DEFAULT_CACHE_NAME_PREFIX = "caches";

    private final List<ProxyManager<String>> buckets;
    private final int numberOfBuckets;
    private final String cacheNamePrefix;
    private final CacheManager cacheManager;

    public DistributedBucketProvider() {
        this(DEFAULT_NUMBER_OF_BUCKETS, DEFAULT_CACHE_NAME_PREFIX);
    }

    public DistributedBucketProvider(int numberOfBuckets, String cacheNamePrefix) {
        this(numberOfBuckets, cacheNamePrefix, Caching.getCachingProvider().getCacheManager());
    }

    public DistributedBucketProvider(int numberOfBuckets,
                                     String cacheNamePrefix,
                                     CacheManager cacheManager) {
        this.numberOfBuckets = numberOfBuckets;
        this.buckets = new ArrayList<>(numberOfBuckets);
        this.cacheNamePrefix = cacheNamePrefix;
        this.cacheManager = cacheManager;
        initBuckets();
    }

    private void initBuckets() {
        for (int i = 0; i < numberOfBuckets; i ++) {
            String name = cacheNamePrefix + "-" + i;
            this.buckets.add(
                    Bucket4j.extension(JCache.class)
                            .proxyManagerForCache(
                                    createCache(name, cacheManager))
            );
        }
    }

    private Cache<String, GridBucketState> createCache(String cacheName, CacheManager cacheManager) {
        MutableConfiguration<String, GridBucketState> configuration = new MutableConfiguration<>();
        return cacheManager.createCache(cacheName, configuration);
    }

    public Bucket get(String key, ApiIdentity identity, BandwidthSupplier bandwidthSupplier) {
        Bandwidth bandwidth = bandwidthSupplier.get(identity);
        int bucketIndex = getBucketIndex(key);
        return buckets.get(bucketIndex).getProxy(key, getBucketConfiguration(bandwidth));
    }

    private int getBucketIndex(String key) {
        return Math.abs(key.hashCode()) % numberOfBuckets;
    }

    private Supplier<BucketConfiguration> getBucketConfiguration(Bandwidth bandwidth) {
        return () -> Bucket4j.configurationBuilder().addLimit(bandwidth).build();
    }

}
