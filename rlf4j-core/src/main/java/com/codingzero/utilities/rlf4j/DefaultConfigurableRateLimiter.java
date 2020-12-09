package com.codingzero.utilities.rlf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class DefaultConfigurableRateLimiter<T> extends DefaultRateLimiter<T> implements ConfigurableRateLimiter<T> {

    private static final Logger LOG = Logger.getLogger(DefaultConfigurableRateLimiter.class.getName());

    private Map<String, ConfigurableApiQuota> apiQuotaMap;

    public DefaultConfigurableRateLimiter() {
        this.apiQuotaMap = new HashMap<>();
    }

    @Override
    public void updateQuotaConfig(String name, ApiQuotaConfig config) {
        ConfigurableApiQuota quota = apiQuotaMap.get(name);
        checkForNonExistingQuota(name, quota);
        quota.updateConfig(config);
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
