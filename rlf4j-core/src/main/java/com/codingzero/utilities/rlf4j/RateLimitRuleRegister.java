package com.codingzero.utilities.rlf4j;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RateLimitRuleRegister {

    private List<RateLimitRule> rules;

    private RateLimitRuleRegister(List<RateLimitRule> rules) {
        this.rules = Collections.unmodifiableList(rules);
    }

    public List<RateLimitRule> getRules() {
        return rules;
    }

    public static Builder register(ApiIdentifier identifier) {
        Builder builder = new Builder();
        return builder.register(identifier);
    }

    public static class Builder {

        private RateLimitRule currentRule;
        private List<RateLimitRule> rules;

        private Builder() {
            this.rules = new LinkedList<>();
        }

        private void addRule(RateLimitRule rule) {
            rules.add(rule);
        }

        public Builder register(ApiIdentifier identifier) {
            currentRule = new RateLimitRule(identifier);
            addRule(currentRule);
            return this;
        }

        public Builder with(RateLimitQuota quota) {
            currentRule.setRateLimitQuota(quota);
            return this;
        }

        public RateLimitRuleRegister build() {
            return new RateLimitRuleRegister(rules);
        }

    }

    public static class RateLimitRule {

        private ApiIdentifier identifier;
        private RateLimitQuota rateLimitQuota;

        private RateLimitRule(ApiIdentifier identifier) {
            this.identifier = identifier;
        }

        public void setRateLimitQuota(RateLimitQuota rateLimitQuota) {
            this.rateLimitQuota = rateLimitQuota;
        }

        public ApiIdentifier getIdentifier() {
            return identifier;
        }

        public RateLimitQuota getRateLimitQuota() {
            return rateLimitQuota;
        }
    }
}
