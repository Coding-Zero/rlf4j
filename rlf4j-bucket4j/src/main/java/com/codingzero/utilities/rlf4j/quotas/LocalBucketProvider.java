package com.codingzero.utilities.rlf4j.quotas;

import com.codingzero.utilities.rlf4j.ApiIdentity;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LocalBucketProvider {

    private static final int DEFAULT_BUCKETS_SIZE = 25;

    private Map<String, Bucket> buckets;
    private final Lock lock;

    public LocalBucketProvider() {
        this.buckets = new HashMap<>(DEFAULT_BUCKETS_SIZE);
        this.lock = new ReentrantLock();
    }

    public Bucket get(String key, ApiIdentity identity, BandwidthSupplier bandwidthSupplier) {
        Bucket bucket = buckets.get(key);
        if (Objects.isNull(bucket)) {
            lock.lock();
            try {
                //try to get one for other thread create a bucket with the same key.
                bucket = buckets.get(key);
                if (!Objects.isNull(bucket)) {
                    return bucket;
                }
                Bandwidth limit = bandwidthSupplier.get(identity);
                bucket = createBucket(limit);
                buckets.put(key, bucket);
            } finally {
                lock.unlock();
            }
        }
        return bucket;
    }

    private Bucket createBucket(Bandwidth limit) {
        return Bucket4j.builder().addLimit(limit).build();
    }

}
