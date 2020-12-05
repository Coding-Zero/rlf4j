package com.codingzero.utilities.rlf4j;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ApiQuotaConfig {

    private List<ApiQuotaRule> rules;
    private Map<String, ApiQuotaRule> ruleMap;
    private boolean isDisabled;
    private boolean isConsumptionReportSupported;
    private boolean isSupplementRequired;

    public ApiQuotaConfig(List<ApiQuotaRule> rules,
                          boolean isDisabled,
                          boolean isConsumptionReportSupported,
                          boolean isSupplementRequired) {
        this.rules = Collections.unmodifiableList(rules);
        initRuleMap();
        this.isDisabled = isDisabled;
        this.isConsumptionReportSupported = isConsumptionReportSupported;
        this.isSupplementRequired = isSupplementRequired;
    }

    private void initRuleMap() {
        this.ruleMap = new LinkedHashMap<>(this.rules.size());
        for (ApiQuotaRule rule: rules) {
            this.ruleMap.put(rule.getName(), rule);
        }
        this.ruleMap = Collections.unmodifiableMap(this.ruleMap);
    }

    public List<ApiQuotaRule> getRules() {
        return rules;
    }

    public Map<String, ApiQuotaRule> getRuleMap() {
        return ruleMap;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public boolean isConsumptionReportSupported() {
        return isConsumptionReportSupported;
    }

    public boolean isSupplementRequired() {
        return isSupplementRequired;
    }

    @Override
    public String toString() {
        return "ApiQuotaConfig{" +
                "rules=" + rules +
                ", ruleMap=" + ruleMap +
                ", isDisabled=" + isDisabled +
                ", isConsumptionReportSupported=" + isConsumptionReportSupported +
                ", isSupplementRequired=" + isSupplementRequired +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiQuotaConfig that = (ApiQuotaConfig) o;
        return isDisabled() == that.isDisabled() &&
                isConsumptionReportSupported() == that.isConsumptionReportSupported() &&
                isSupplementRequired() == that.isSupplementRequired() &&
                Objects.equals(getRules(), that.getRules()) &&
                Objects.equals(getRuleMap(), that.getRuleMap());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRules(), getRuleMap(), isDisabled(), isConsumptionReportSupported(), isSupplementRequired());
    }
}
