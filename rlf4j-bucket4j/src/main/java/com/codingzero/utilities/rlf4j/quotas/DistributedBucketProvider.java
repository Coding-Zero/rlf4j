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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class DistributedBucketProvider {

    public final static int DEFAULT_NUMBER_OF_BUCKETS = 1000;
    public final static String DEFAULT_CACHE_NAME_PREFIX = "caches";
    public final static String MAIN_CACHE_NAME_MARK = "M";
    public final static String SECONDARY_CACHE_NAME_MARK = "S";

    private final int numberOfBuckets;
    private final String cacheNamePrefix;
    private final CacheManager cacheManager;
    private final boolean needInitialize;
    private List<ProxyManager<String>> mainBuckets;
    private List<ProxyManager<String>> secondaryBuckets;
    private List<Cache<String, GridBucketState>> mainCaches;
    private List<Cache<String, GridBucketState>> secondaryCaches;
    private final AtomicBoolean isMainBuckets;

    private DistributedBucketProvider(int numberOfBuckets,
                                     String cacheNamePrefix,
                                     CacheManager cacheManager,
                                     boolean needInitialize) {
        this.numberOfBuckets = numberOfBuckets;
        this.cacheNamePrefix = cacheNamePrefix;
        this.cacheManager = cacheManager;
        this.needInitialize = needInitialize;
        this.mainCaches = new ArrayList<>(numberOfBuckets);
        this.secondaryCaches = new ArrayList<>(numberOfBuckets);
        this.mainBuckets = initBuckets(MAIN_CACHE_NAME_MARK, this.mainCaches);
        this.secondaryBuckets = initBuckets(SECONDARY_CACHE_NAME_MARK, this.secondaryCaches);
        this.isMainBuckets = new AtomicBoolean(true);
    }

    public int getNumberOfBuckets() {
        return numberOfBuckets;
    }

    public String getCacheNamePrefix() {
        return cacheNamePrefix;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public boolean isNeedInitialize() {
        return needInitialize;
    }

    private List<ProxyManager<String>> initBuckets(String mark,
                                                   List<Cache<String, GridBucketState>> caches) {
        List<ProxyManager<String>> buckets = new ArrayList<>(numberOfBuckets);
        for (int i = 0; i < numberOfBuckets; i ++) {
            String name = cacheNamePrefix + "-" + mark + "-" + i;
            Cache<String, GridBucketState> cache = createCache(name, cacheManager);
            buckets.add(Bucket4j.extension(JCache.class).proxyManagerForCache(cache));
            caches.add(cache);
        }
        return buckets;
    }

    private Cache<String, GridBucketState> createCache(String cacheName, CacheManager cacheManager) {
        MutableConfiguration<String, GridBucketState> configuration = new MutableConfiguration<>();
        return cacheManager.createCache(cacheName, configuration);
    }

    public Bucket get(String key, ApiIdentity identity, BandwidthSupplier bandwidthSupplier) {
        Bandwidth bandwidth = bandwidthSupplier.get(identity);
        int bucketIndex = getBucketIndex(key);
        return getBucket(bucketIndex, key, bandwidth);
    }

    public void clean() {
        if (isMainBuckets.get()) {
            initCaches(this.secondaryCaches);
            isMainBuckets.set(false);
        } else {
            initCaches(this.mainCaches);
            isMainBuckets.set(true);
        }
    }

    private void initCaches(List<Cache<String, GridBucketState>> caches) {
        for (Cache<String, GridBucketState> cache: caches) {
            cache.removeAll();
        }
    }

    private Bucket getBucket(int index, String key, Bandwidth bandwidth) {
        if (isMainBuckets.get()) {
            return mainBuckets.get(index).getProxy(key, getBucketConfiguration(bandwidth));
        } else {
            return secondaryBuckets.get(index).getProxy(key, getBucketConfiguration(bandwidth));
        }
    }

    private int getBucketIndex(String key) {
        return Math.abs(key.hashCode()) % numberOfBuckets;
    }

    private Supplier<BucketConfiguration> getBucketConfiguration(Bandwidth bandwidth) {
        return () -> Bucket4j.configurationBuilder().addLimit(bandwidth).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int numberOfBuckets;
        private String cacheNamePrefix;
        private CacheManager cacheManager;
        private boolean needInitialize;

        private Builder() {
            this.numberOfBuckets = DEFAULT_NUMBER_OF_BUCKETS;
            this.cacheNamePrefix = DEFAULT_CACHE_NAME_PREFIX;
            this.cacheManager = Caching.getCachingProvider().getCacheManager();
            this.needInitialize = false;
        }

        public int getNumberOfBuckets() {
            return numberOfBuckets;
        }

        public Builder numberOfBuckets(int numberOfBuckets) {
            this.numberOfBuckets = numberOfBuckets;
            return this;
        }

        public String getCacheNamePrefix() {
            return cacheNamePrefix;
        }

        public Builder cacheNamePrefix(String cacheNamePrefix) {
            this.cacheNamePrefix = cacheNamePrefix;
            return this;
        }

        public CacheManager getCacheManager() {
            return cacheManager;
        }

        public Builder cacheManager(CacheManager cacheManager) {
            this.cacheManager = cacheManager;
            return this;
        }

        public boolean isNeedInitialize() {
            return needInitialize;
        }

        public Builder needInitialize(boolean needInitialize) {
            this.needInitialize = needInitialize;
            return this;
        }

        public DistributedBucketProvider build() {
            return new DistributedBucketProvider(
                    getNumberOfBuckets(),
                    getCacheNamePrefix(),
                    getCacheManager(),
                    isNeedInitialize()
            );
        }
    }

}
