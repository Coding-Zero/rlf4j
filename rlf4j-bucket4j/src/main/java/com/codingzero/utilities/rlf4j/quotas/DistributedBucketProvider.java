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
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class DistributedBucketProvider {

    public final static int DEFAULT_NUMBER_OF_BUCKETS = 1000;
    public final static String DEFAULT_CACHE_NAME_PREFIX = "caches";

    private final int numberOfBuckets;
    private final String cacheNamePrefix;
    private final CacheManager cacheManager;
    private final boolean needInitialize;
    private List<ProxyManager<String>> buckets;
    private Map<String, Cache<String, GridBucketState>> caches; //<cache name, cache>

    private DistributedBucketProvider(int numberOfBuckets,
                                     String cacheNamePrefix,
                                     CacheManager cacheManager,
                                     boolean needInitialize) {
        this.numberOfBuckets = numberOfBuckets;
        this.cacheNamePrefix = cacheNamePrefix;
        this.cacheManager = cacheManager;
        this.needInitialize = needInitialize;
        this.caches = new HashMap<>(numberOfBuckets);
        this.buckets = initBuckets(this.caches);
        initCache(this.caches);
    }

    private void initCache(Map<String, Cache<String, GridBucketState>> caches) {
        if (!isNeedInitialize()) {
            return;
        }
        cleanCache(caches);
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

    private List<ProxyManager<String>> initBuckets(Map<String, Cache<String, GridBucketState>> caches) {
        List<ProxyManager<String>> buckets = new ArrayList<>(numberOfBuckets);
        for (int i = 0; i < numberOfBuckets; i ++) {
            String name = cacheNamePrefix + "-" + i;
            Cache<String, GridBucketState> cache = createCache(name, cacheManager);
            buckets.add(Bucket4j.extension(JCache.class).proxyManagerForCache(cache));
            caches.put(name, cache);
        }
        return new CopyOnWriteArrayList(buckets);
    }

    private Cache<String, GridBucketState> createCache(String cacheName, CacheManager cacheManager) {
        MutableConfiguration<String, GridBucketState> configuration = new MutableConfiguration<>();
        try {
            return cacheManager.createCache(cacheName, configuration);
        } catch (CacheException e) {
            if (e.getMessage().contains("already exists")) {
                return cacheManager.getCache(cacheName);
            }
            throw e;
        }
    }

    public Bucket get(String key, ApiIdentity identity, BandwidthSupplier bandwidthSupplier) {
        int bucketIndex = getBucketIndex(key);
        return getBucket(bucketIndex, key, identity, bandwidthSupplier);
    }

    public void clean() {
        cleanCache(this.caches);
    }

    private void cleanCache(Map<String, Cache<String, GridBucketState>> caches) {
        for (Map.Entry<String, Cache<String, GridBucketState>> entry: caches.entrySet()) {
            entry.getValue().clear();
        }
    }

    private Bucket getBucket(int index, String key, ApiIdentity identity, BandwidthSupplier bandwidthSupplier) {
        return buckets
                .get(index)
                .getProxy(key, getBucketConfiguration(identity, bandwidthSupplier));
    }

    private int getBucketIndex(String key) {
        return Math.abs(key.hashCode()) % numberOfBuckets;
    }

    private Supplier<BucketConfiguration> getBucketConfiguration(ApiIdentity identity,
                                                                 BandwidthSupplier bandwidthSupplier) {
        return () -> {
            Bandwidth bandwidth = bandwidthSupplier.get(identity);
            return Bucket4j.configurationBuilder().addLimit(bandwidth).build();
        };
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
            this.cacheManager = null;
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
            if (Objects.isNull(cacheManager)) {
                cacheManager = Caching.getCachingProvider().getCacheManager();
            }
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
