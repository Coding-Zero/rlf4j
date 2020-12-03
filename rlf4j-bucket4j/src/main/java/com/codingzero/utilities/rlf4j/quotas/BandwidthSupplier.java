package com.codingzero.utilities.rlf4j.quotas;

import com.codingzero.utilities.rlf4j.ApiIdentity;
import io.github.bucket4j.Bandwidth;

@FunctionalInterface
public interface BandwidthSupplier {

    Bandwidth get(ApiIdentity identity);

}
