package com.codingzero.utilities.rlf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ConfigurableApiQuota implements ApiQuota {

    private static final int CONFIGS_SIZE = 2;
    private static final int DEFAULT_CONFIG_INDEX = 0;
    private static final int CONFIG_INDEX_UPPER_BOUND = 1;

    private List<ApiQuotaConfig> configs;
    private AtomicInteger currentConfigIndex;

    public ConfigurableApiQuota(ApiQuotaConfig config) {
        this.configs = new ArrayList<>(CONFIGS_SIZE);
        this.currentConfigIndex = new AtomicInteger(DEFAULT_CONFIG_INDEX);
        this.configs.set(DEFAULT_CONFIG_INDEX, config);
    }

    public void updateConfig(ApiQuotaConfig config) {
        int nextIndex = getNextConfigIndex();
        configs.set(nextIndex, config);
        currentConfigIndex.set(nextIndex);
    }

    public ApiQuotaConfig getCurrentConfig() {
        return configs.get(currentConfigIndex.get());
    }

    private int getNextConfigIndex() {
        return CONFIG_INDEX_UPPER_BOUND - currentConfigIndex.get();
    }

    @Override
    public boolean isConsumptionReportSupported() {
        return getCurrentConfig().isConsumptionReportSupported();
    }

    @Override
    public boolean isSupplementRequired() {
        return getCurrentConfig().isSupplementRequired();
    }

    @Override
    public boolean tryConsume(ApiIdentity identity, long token) {
        if (getCurrentConfig().isDisabled()) {
            return false;
        }
        return tryConsumeInternally(identity, token);
    }

    @Override
    public ConsumptionReport tryConsumeAndRetuningReport(ApiIdentity identity, long token) {
        if (getCurrentConfig().isDisabled()) {
            return ConsumptionReport.notConsumed().build();
        }
        return tryConsumeAndRetuningReportInternally(identity, token);
    }

    protected abstract boolean tryConsumeInternally(ApiIdentity identity, long token);

    protected abstract ConsumptionReport tryConsumeAndRetuningReportInternally(ApiIdentity identity,
                                                                               long token);


}
