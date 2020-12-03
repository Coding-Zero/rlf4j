package com.codingzero.utilities.rlf4j;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class RateLimiter<T> {

    private static final Logger LOG = Logger.getLogger(RateLimiter.class.getName());

    private static final long DEFAULT_CONSUMING_TOKEN = 1;

    private RateLimitRule<T> currentRule;
    private List<RateLimitRule<T>> rules;

    private RateLimiter() {
        this.rules = new LinkedList<>();
    }

    public RateLimitRule<T> newRule() {
        this.currentRule = new RateLimitRule<>();
        this.rules.add(currentRule);
        return this.currentRule;
    }

    public <R> R tryLimit(T apiInstance, ApiExecution<R> execution) throws RateLimitExceedException {
        checkForIllegalApiInstance(apiInstance);
        Map<ApiIdentity, ApiQuota> supplementRequiredQuotas = new LinkedHashMap<>();
        RateLimitExceedException exceedException = tryLimitWithRules(apiInstance, supplementRequiredQuotas);
        return processApiExecution(execution, exceedException, supplementRequiredQuotas);
    }

    public void tryLimitWithoutReturn(T apiInstance, ApiExecutionWithoutReturn execution) throws RateLimitExceedException {
        checkForIllegalApiInstance(apiInstance);
        Map<ApiIdentity, ApiQuota> supplementRequiredQuotas = new LinkedHashMap<>();
        RateLimitExceedException exceedException = tryLimitWithRules(apiInstance, supplementRequiredQuotas);
        processApiExecution(execution, exceedException, supplementRequiredQuotas);
    }

    private void checkForIllegalApiInstance(Object apiInstance) {
        if (Objects.isNull(apiInstance)) {
            throw new IllegalArgumentException("API instance cannot be null value");
        }
    }

    private RateLimitExceedException tryLimitWithRules(T apiInstance,
                                                       Map<ApiIdentity, ApiQuota> supplementRequiredQuotas) {
        for (RateLimitRule<T> rule: rules) {
            ApiIdentifier<T> identifier = rule.getIdentifier();
            ApiIdentity identity = identifyApiWithValidation(identifier, apiInstance);
            for (ApiQuota quota: rule.getRateLimitQuotas()) {
                ConsumptionReport report = tryConsume(identity, quota, DEFAULT_CONSUMING_TOKEN);
                if (!report.isConsumed()) {
                    return new RateLimitExceedException(identity, report, quota);
                }
                if (quota.isSupplementRequired()) {
                    supplementRequiredQuotas.put(identity, quota);
                }
            }
        }
        return null;
    }

    private ApiIdentity identifyApiWithValidation(ApiIdentifier<T> apiIdentifier,
                                                  T apiInstance) {
        ApiIdentity identity = apiIdentifier.identify(apiInstance);
        if (identity.getId().trim().length() == 0) {
            throw new IllegalArgumentException("API identity cannot be empty.");
        }
        return identity;
    }

    private ConsumptionReport tryConsume(ApiIdentity identity, ApiQuota quota, long tokens) {
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
                                      Map<ApiIdentity, ApiQuota> supplementRequiredQuotas) throws RateLimitExceedException {
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

    private void processApiExecution(ApiExecutionWithoutReturn execution,
                                     RateLimitExceedException exceedException,
                                     Map<ApiIdentity, ApiQuota> supplementRequiredQuotas) throws RateLimitExceedException {
        if (!isLimited(exceedException)) {
            execution.execute();
        }
        supplementQuotas(supplementRequiredQuotas);
        if (isLimited(exceedException)) {
            throw exceedException;
        }
    }

    private boolean isLimited(RateLimitExceedException exceedException) {
        return !Objects.isNull(exceedException);
    }

    private void supplementQuotas(Map<ApiIdentity, ApiQuota> supplementRequiredQuotas) {
        for (Map.Entry<ApiIdentity, ApiQuota> entry: supplementRequiredQuotas.entrySet()) {
            ApiIdentity identity = entry.getKey();
            ApiQuota quota = entry.getValue();
            try {
                quota.supplement(identity, DEFAULT_CONSUMING_TOKEN);
            } catch (Throwable throwable) {
                LOG.warning("Try to supplement quota " + quota.getClass()
                        + " for API " + identity.getId()
                        + " failed due to " + throwable.getMessage());
            }
        }
    }

    public static <T> RateLimiter<T> newInstance() {
        return new RateLimiter<>();
    }

    public static class RateLimitRule<T> {

        private ApiIdentifier<T> identifier;
        private List<ApiQuota> apiQuotas;

        private RateLimitRule() {
            this.apiQuotas = new LinkedList<>();
        }

        public RateLimitRule<T> quota(ApiQuota apiQuota) {
            this.apiQuotas.add(apiQuota);
            return this;
        }

        public RateLimitRule<T> identifier(ApiIdentifier<T> identifier) {
            this.identifier = identifier;
            return this;
        }

        public ApiIdentifier<T> getIdentifier() {
            if (Objects.isNull(identifier)) {
                throw new IllegalArgumentException("Api identifier cannot be null for rule, " + this);
            }
            return identifier;
        }

        public List<ApiQuota> getRateLimitQuotas() {
            if (apiQuotas.isEmpty()) {
                throw new IllegalArgumentException("Rate limit quota cannot be empty for rule, " + this);
            }
            return apiQuotas;
        }

    }

}
