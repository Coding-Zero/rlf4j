package com.codingzero.utilities.rlf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class RateLimiter {

    private static final Logger LOG = Logger.getLogger(RateLimiter.class.getName());

    private static final long DEFAULT_CONSUMING_TOKEN = 1;

    private RateLimitRuleRegister register;

    private RateLimiter(RateLimitRuleRegister register) {
        this.register = register;
    }

    public <R> R tryLimit(Object apiInstance, ApiExecution<R> execution) throws RateLimitExceedException {
        checkForIllegalApiInstance(apiInstance);
        Map<ApiIdentity, RateLimitQuota> supplementRequiredQuotas = new LinkedHashMap<>();
        RateLimitExceedException exceedException = tryLimitWithRules(apiInstance, supplementRequiredQuotas);
        return processApiExecution(execution, exceedException, supplementRequiredQuotas);
    }

    private void checkForIllegalApiInstance(Object apiInstance) {
        if (Objects.isNull(apiInstance)) {
            throw new IllegalArgumentException("API instance cannot be null value");
        }
    }

    private RateLimitExceedException tryLimitWithRules(Object apiInstance,
                                                       Map<ApiIdentity, RateLimitQuota> supplementRequiredQuotas) {
        List<RateLimitRuleRegister.RateLimitRule> rules = register.getRules();
        for (RateLimitRuleRegister.RateLimitRule rule: rules) {
            ApiIdentifier identifier = rule.getIdentifier();
            ApiIdentity identity = identifyApiWithValidation(identifier, apiInstance);
            if (Objects.isNull(identity)) {
                continue;
            }
            RateLimitQuota quota = rule.getRateLimitQuota();
            ConsumptionReport report = tryConsume(identity, quota, DEFAULT_CONSUMING_TOKEN);
            if (!report.isConsumed()) {
                return new RateLimitExceedException(identity, report, quota);
            }
            if (quota.isSupplementRequired()) {
                supplementRequiredQuotas.put(identity, quota);
            }
        }
        return null;
    }

    private ApiIdentity identifyApiWithValidation(ApiIdentifier apiIdentifier,
                                                  Object apiInstance) {
        try {
            ApiIdentity identity = apiIdentifier.identify(apiInstance);
            if (identity.getId().trim().length() == 0) {
                throw new IllegalArgumentException("API identity cannot be empty.");
            }
            return identity;
        } catch (ClassCastException e) {
            return null;
        }
    }

    private ConsumptionReport tryConsume(ApiIdentity identity, RateLimitQuota quota, long tokens) {
        if (quota.isConsumptionReportSupported()) {
            return quota.tryConsumeAndRetuningReport(identity, tokens);
        } else {
            boolean succeed = quota.tryConsume(identity, tokens);
            if (succeed) {
                return ConsumptionReport.consumed(tokens).remainingQuota(-1).build();
            } else {
                return ConsumptionReport.notConsumed().remainingQuota(-1).build();
            }
        }
    }

    private <R> R processApiExecution(ApiExecution<R> execution,
                                      RateLimitExceedException exceedException,
                                      Map<ApiIdentity, RateLimitQuota> supplementRequiredQuotas) throws RateLimitExceedException {
        R result = null;
        if (!isLimited(exceedException)) {
            result = execution.execute();
        }
        supplementQuotas(supplementRequiredQuotas);
        if (isLimited(exceedException)) {
            throw exceedException;
        }
        return result;
    }

    private boolean isLimited(RateLimitExceedException exceedException) {
        return !Objects.isNull(exceedException);
    }

    private void supplementQuotas(Map<ApiIdentity, RateLimitQuota> supplementRequiredQuotas) {
        for (Map.Entry<ApiIdentity, RateLimitQuota> entry: supplementRequiredQuotas.entrySet()) {
            ApiIdentity identity = entry.getKey();
            RateLimitQuota quota = entry.getValue();
            try {
                quota.supplement(identity, DEFAULT_CONSUMING_TOKEN);
            } catch (Throwable throwable) {
                LOG.warning("Try to supplement quota " + quota.getClass()
                        + " for API " + identity.getId()
                        + " failed due to " + throwable.getMessage());
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private RateLimitRuleRegister register;

        private Builder() {

        }

        public Builder register(RateLimitRuleRegister register) {
            this.register = register;
            return this;
        }

        public RateLimitRuleRegister getRegister() {
            return register;
        }

        public RateLimiter build() {
            return new RateLimiter(getRegister());
        }
    }

}
