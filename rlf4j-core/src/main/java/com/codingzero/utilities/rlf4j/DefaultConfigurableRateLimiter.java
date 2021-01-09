package com.codingzero.utilities.rlf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultConfigurableRateLimiter<T> extends DefaultRateLimiter<T> implements ConfigurableRateLimiter<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfigurableRateLimiter.class);

    private Map<String, ConfigurableApiQuota> apiQuotaMap;

    public DefaultConfigurableRateLimiter() {
        this(DEFAULT_API_QUOTA_PARKING_INTERVAL);
    }

    public DefaultConfigurableRateLimiter(long apiQuotaParkingInterval) {
        super(apiQuotaParkingInterval);
        this.apiQuotaMap = new HashMap<>();
    }

    @Override
    public void updateQuotaConfig(String name, ApiQuotaConfig config) throws ApiQuotaConfigUpdateException {
        ConfigurableApiQuota quota = apiQuotaMap.get(name);
        try {
            checkForNonExistingQuota(name, quota);
            long start = System.currentTimeMillis();
            quota.updateConfig(config);
            long totalTime = System.currentTimeMillis() - start;
            logQuotaUpdateInfo(totalTime, name, quota.isGreenConfigOn(), config);
        } catch (RuntimeException e) {
            throw new ApiQuotaConfigUpdateException(config, name, e);
        }
    }

    private void logQuotaUpdateInfo(long totalTime, String name, boolean isGreen, ApiQuotaConfig config) {
        LOGGER.debug("[updated] latency={} name={}, green={}, config={} ",
                totalTime,
                name,
                isGreen,
                config);
    }

    @Override
    public ApiQuotaConfig getCurrentQuotaConfig(String name) {
        ConfigurableApiQuota quota = apiQuotaMap.get(name);
        checkForNonExistingQuota(name, quota);
        return quota.getCurrentConfig();
    }

    @Override
    public void addApiQuota(ApiQuota quota) {
        checkForIllegalApiQuota(quota);
        super.addApiQuota(quota);
        ConfigurableApiQuota configurableApiQuota = (ConfigurableApiQuota) quota;
        apiQuotaMap.put(configurableApiQuota.getName(), configurableApiQuota);
    }

    private void checkForIllegalApiQuota(ApiQuota quota) {
        if (!ConfigurableApiQuota.class.isInstance(quota)) {
            throw new IllegalArgumentException(
                    "ApiQuota, " + quota + " must implement ConfigurableApiQuota interface.");
        }
    }

    private void checkForNonExistingQuota(String name, ConfigurableApiQuota quota) {
        if (Objects.isNull(quota)) {
            throw new IllegalStateException("Quota, " + name + " doesn't exist.");
        }
    }

}
