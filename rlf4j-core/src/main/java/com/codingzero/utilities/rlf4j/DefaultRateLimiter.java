package com.codingzero.utilities.rlf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DefaultRateLimiter<T> implements RateLimiter<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRateLimiter.class);

    private static final long DEFAULT_CONSUMING_TOKEN = 1;

    private ApiIdentifier<T> identifier;
    private List<ApiQuota> apiQuotas;

    public DefaultRateLimiter() {
        this.apiQuotas = new LinkedList<>();
    }

    @Override
    public void setApiIdentifier(ApiIdentifier<T> identifier) {
        this.identifier = identifier;
    }

    @Override
    public void addApiQuota(ApiQuota quota) {
        this.apiQuotas.add(quota);
    }

    @Override
    public <R> R tryLimit(T apiInstance, ApiExecution<R> execution) throws RateLimitExceedException {
        checkForIllegalApiInstance(apiInstance);
        Map<ApiIdentity, ApiQuota> supplementRequiredQuotas = new LinkedHashMap<>();
        RateLimitExceedException exceedException = tryLimitWithRules(apiInstance, supplementRequiredQuotas);
        return processApiExecution(execution, exceedException, supplementRequiredQuotas);
    }

    @Override
    public void tryLimitWithoutReturn(T apiInstance, ApiExecutionWithoutReturn execution) throws RateLimitExceedException {
        checkForIllegalApiInstance(apiInstance);
        Map<ApiIdentity, ApiQuota> supplementRequiredQuotas = new LinkedHashMap<>();
        RateLimitExceedException exceedException = tryLimitWithRules(apiInstance, supplementRequiredQuotas);
        processApiExecution(execution, exceedException, supplementRequiredQuotas);
    }

    private RateLimitExceedException tryLimitWithRules(T apiInstance,
                                                       Map<ApiIdentity, ApiQuota> supplementRequiredQuotas) {
        verifyForNullApiIdentifier(identifier);
        ApiIdentity identity = identifyApiWithValidation(identifier, apiInstance);
        verifyForNonEmptyApiQuotas(this.apiQuotas);
        for (ApiQuota quota: this.apiQuotas) {
            try {
                long start = System.currentTimeMillis();
                ConsumptionReport report = tryConsume(identity, quota, DEFAULT_CONSUMING_TOKEN);
                long totalTime = System.currentTimeMillis() - start;
                LOGGER.debug("[consumed] latency={}, quota={}, report={}, supplement={}\n\t\t\t\t Consumption report = {}",
                        totalTime,
                        quota.getClass().getName(),
                        quota.isConsumptionReportSupported(),
                        quota.isSupplementRequired(),
                        report);
                if (!report.isConsumed()) {
                    return new RateLimitExceedException(identity, report, quota);
                }
                if (quota.isSupplementRequired()) {
                    supplementRequiredQuotas.put(identity, quota);
                }
            } catch (RuntimeException e) {
                LOGGER.warn("Failed to consume token = " + DEFAULT_CONSUMING_TOKEN
                        + " from quota = " + quota + " for api = " + apiInstance + " due to " + e.getMessage());
            }
        }
        return null;
    }

    private void checkForIllegalApiInstance(Object apiInstance) {
        if (Objects.isNull(apiInstance)) {
            throw new IllegalArgumentException("API instance cannot be null value");
        }
    }

    private void verifyForNullApiIdentifier(ApiIdentifier<T> identifier) {
        if (Objects.isNull(identifier)) {
            throw new IllegalArgumentException("Api identifier cannot be null for rule, " + this);
        }
    }

    private void verifyForNonEmptyApiQuotas(List<ApiQuota> apiQuotas) {
        if (apiQuotas.isEmpty()) {
            throw new IllegalArgumentException("Rate limit quota cannot be empty for rule, " + this);
        }
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
                long start = System.currentTimeMillis();
                quota.supplement(identity, DEFAULT_CONSUMING_TOKEN);
                long totalTime = System.currentTimeMillis() - start;
                LOGGER.debug("[supplemented] latency={}, quota={}, identity={}, supplied={}",
                        totalTime,
                        quota.getClass().getName(),
                        identity,
                        DEFAULT_CONSUMING_TOKEN);
            } catch (Throwable throwable) {
                LOGGER.warn("Try to supplement quota " + quota.getClass()
                        + " for API " + identity.getId()
                        + " failed due to " + throwable.getMessage());
            }
        }
    }

}
