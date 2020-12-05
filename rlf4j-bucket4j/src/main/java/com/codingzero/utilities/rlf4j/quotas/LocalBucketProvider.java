package com.codingzero.utilities.rlf4j.quotas;

import com.codingzero.utilities.rlf4j.ApiIdentity;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LocalBucketProvider {

    private static final int DEFAULT_BUCKETS_SIZE = 25;

    private final int numberOfBuckets;
    private Map<String, Bucket> mainBuckets;
    private Map<String, Bucket> secondaryBuckets;
    private final Lock lock;
    private final AtomicBoolean isMainBuckets;

    private LocalBucketProvider(int numberOfBuckets) {
        this.numberOfBuckets = numberOfBuckets;
        initMainBuckets();
        initSecondaryBuckets();
        this.lock = new ReentrantLock();
        this.isMainBuckets = new AtomicBoolean(true);
    }

    public int getNumberOfBuckets() {
        return numberOfBuckets;
    }

    private void initMainBuckets() {
        this.mainBuckets = new HashMap<>(numberOfBuckets);
    }

    private void initSecondaryBuckets() {
        this.secondaryBuckets = new HashMap<>(numberOfBuckets);
    }

    public Bucket get(String key, ApiIdentity identity, BandwidthSupplier bandwidthSupplier) {
        Bucket bucket = getBucket(key);
        if (Objects.isNull(bucket)) {
            lock.lock();
            try {
                //try to get one for other thread create a bucket with the same key.
                bucket = getBucket(key);
                if (!Objects.isNull(bucket)) {
                    return bucket;
                }
                Bandwidth limit = bandwidthSupplier.get(identity);
                bucket = createBucket(limit);
                putBucket(key, bucket);
            } finally {
                lock.unlock();
            }
        }
        return bucket;
    }

    public void clean() {
        if (isMainBuckets.get()) {
            initSecondaryBuckets();
            isMainBuckets.set(false);
        } else {
            initMainBuckets();
            isMainBuckets.set(true);
        }
    }

    private Bucket getBucket(String key) {
        if (isMainBuckets.get()) {
            return mainBuckets.get(key);
        } else {
            return secondaryBuckets.get(key);
        }
    }

    private void putBucket(String key, Bucket bucket) {
        if (isMainBuckets.get()) {
            mainBuckets.put(key, bucket);
        } else {
            secondaryBuckets.put(key, bucket);
        }
    }

    private Bucket createBucket(Bandwidth limit) {
        return Bucket4j.builder().addLimit(limit).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int numberOfBuckets;

        private Builder() {
            this.numberOfBuckets = DEFAULT_BUCKETS_SIZE;
        }

        public Builder numberOfBuckets(int bucketSize) {
            this.numberOfBuckets = bucketSize;
            return this;
        }

        public int getNumberOfBuckets() {
            return numberOfBuckets;
        }

        public LocalBucketProvider build() {
            return new LocalBucketProvider(getNumberOfBuckets());
        }
    }

}
