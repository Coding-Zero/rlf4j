package com.codingzero.utilities.rlf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

        public Builder with(RateLimitChecker checker) {
            currentRule.add(checker);
            return this;
        }

        public RateLimitRuleRegister build() {
            return new RateLimitRuleRegister(rules);
        }

    }

    public static class RateLimitRule {

        private ApiIdentifier identifier;
        private List<RateLimitChecker> checkers;
        private Class<?> identifierGenericClass;

        private RateLimitRule(ApiIdentifier identifier) {
            this.identifier = identifier;
            this.checkers = new LinkedList<>();
//            this.identifierGenericClass = readIdentifierGenericClass();
        }

        private Class<?> readIdentifierGenericClass() {
            Type[] genericInterfaces = identifier.getClass().getGenericInterfaces();
            for (Type genericInterface : genericInterfaces) {
                if (genericInterface instanceof ParameterizedType) {
                    Type[] genericTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
                    System.out.println(genericTypes[0]);
                    try {
                        return Class.forName(genericTypes[0].getTypeName());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            throw new IllegalArgumentException("No generic type to be assigned for API identifier, " + identifier);
        }

        private void add(RateLimitChecker checker) {
            checkers.add(checker);
        }

        public ApiIdentifier getIdentifier() {
            return identifier;
        }

        public List<RateLimitChecker> getCheckers() {
            return Collections.unmodifiableList(checkers);
        }

        public Class<?> getIdentifierGenericClass() {
            return identifierGenericClass;
        }
    }
}
