package com.codingzero.utilities.rlf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ConfigurableApiQuota implements ApiQuota {

    private ApiQuotaConfig mainConfig;
    private ApiQuotaConfig secondaryConfig;
    private final AtomicBoolean isMainBuckets;

    public ConfigurableApiQuota(ApiQuotaConfig config) {
        checkForMandatoryQuotaRules(config);
        this.mainConfig = config;
        this.secondaryConfig = null;
        this.isMainBuckets = new AtomicBoolean(true);
    }

    public void updateConfig(ApiQuotaConfig config) {
        checkForMandatoryQuotaRules(config);
        if (isMainBuckets.get()) {
            this.secondaryConfig = config;
            isMainBuckets.set(false);
        } else {
            this.mainConfig = config;
            isMainBuckets.set(true);
        }
    }

    public ApiQuotaConfig getCurrentConfig() {
        if (isMainBuckets.get()) {
            return this.mainConfig;
        } else {
            return this.secondaryConfig;
        }
    }

    private void checkForMandatoryQuotaRules(ApiQuotaConfig config) {
        Set<String> names = new HashSet<>(getMandatoryQuotaRuleNames());
        for (ApiQuotaRule rule: config.getRules()) {
            names.remove(rule.getName());
        }
        if (names.size() > 0) {
            throw new IllegalArgumentException("Quota rule(s), " + names + " are required, please provide.");
        }
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
            return true;
        }
        return tryConsumeInternally(identity, token);
    }

    @Override
    public ConsumptionReport tryConsumeAndRetuningReport(ApiIdentity identity, long token) {
        if (getCurrentConfig().isDisabled()) {
            return ConsumptionReport.consumed(token).remainingQuota(Long.MAX_VALUE).build();
        }
        return tryConsumeAndRetuningReportInternally(identity, token);
    }

    protected abstract boolean tryConsumeInternally(ApiIdentity identity, long token);

    protected abstract ConsumptionReport tryConsumeAndRetuningReportInternally(ApiIdentity identity,
                                                                               long token);

    protected abstract Set<String> getMandatoryQuotaRuleNames();

}
