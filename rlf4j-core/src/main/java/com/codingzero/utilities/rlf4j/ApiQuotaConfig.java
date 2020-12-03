package com.codingzero.utilities.rlf4j;

import java.util.Collections;
import java.util.List;

public final class ApiQuotaConfig {

    private List<ApiQuotaRule> rules;
    private boolean isDisabled;
    private boolean isConsumptionReportSupported;
    private boolean isSupplementRequired;

    public ApiQuotaConfig(List<ApiQuotaRule> rules,
                          boolean isDisabled,
                          boolean isConsumptionReportSupported,
                          boolean isSupplementRequired) {
        this.rules = Collections.unmodifiableList(rules);
        this.isDisabled = isDisabled;
        this.isConsumptionReportSupported = isConsumptionReportSupported;
        this.isSupplementRequired = isSupplementRequired;
    }

    public List<ApiQuotaRule> getRules() {
        return rules;
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

}
