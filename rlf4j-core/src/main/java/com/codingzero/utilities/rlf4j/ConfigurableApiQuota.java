package com.codingzero.utilities.rlf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class ConfigurableApiQuota implements ApiQuota {

    private volatile ApiQuotaConfig greenConfig;
    private volatile ApiQuotaConfig blueConfig;
    private final AtomicBoolean isGreenConfigOn;

    public ConfigurableApiQuota(ApiQuotaConfig config) {
        checkForMandatoryQuotaRules(config);
        this.greenConfig = config;
        this.blueConfig = null;
        this.isGreenConfigOn = new AtomicBoolean(true);
    }

    public void updateConfig(ApiQuotaConfig config) {
        checkForMandatoryQuotaRules(config);
        if (isGreenConfigOn.get()) {
            this.blueConfig = config;
            onBlueConfigUpdate(config);
            isGreenConfigOn.set(false);
            onBlueConfigUpdateComplete(config);
        } else {
            this.greenConfig = config;
            onGreenConfigUpdate(config);
            isGreenConfigOn.set(true);
            onGreenConfigUpdateComplete(config);
        }
    }

    protected ApiQuotaConfig getCurrentConfig() {
        if (isGreenConfigOn.get()) {
            return this.greenConfig;
        } else {
            return this.blueConfig;
        }
    }

    protected boolean isGreenConfigOn() {
        return isGreenConfigOn.get();
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

    public abstract String getName();

    protected abstract boolean tryConsumeInternally(ApiIdentity identity, long token);

    protected abstract ConsumptionReport tryConsumeAndRetuningReportInternally(ApiIdentity identity,
                                                                               long token);

    protected abstract Set<String> getMandatoryQuotaRuleNames();

    /**
     * This method is invoked as updating the blue config but before turn blue config switch on
     * (#{@link #isGreenConfigOn()} returns true)
     *
     * @param config ApiQuotaConfig
     */
    protected abstract void onBlueConfigUpdate(ApiQuotaConfig config);

    /**
     * This method is invoked after the blue config updated and turn blue config switch on
     * (#{@link #isGreenConfigOn()} returns false)
     *
     * @param config ApiQuotaConfig
     */
    protected abstract void onBlueConfigUpdateComplete(ApiQuotaConfig config);

    /**
     * This method is invoked as updating the green config but before turn green config switch on
     * (#{@link #isGreenConfigOn()} returns false)
     *
     * @param config ApiQuotaConfig
     */
    protected abstract void onGreenConfigUpdate(ApiQuotaConfig config);

    /**
     * This method is invoked after the green config updated and turn green config switch on
     * (#{@link #isGreenConfigOn()} returns true)
     *
     * @param config ApiQuotaConfig
     */
    protected abstract void onGreenConfigUpdateComplete(ApiQuotaConfig config);

}
