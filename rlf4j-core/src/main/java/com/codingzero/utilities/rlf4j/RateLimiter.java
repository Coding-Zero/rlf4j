package com.codingzero.utilities.rlf4j;

import java.util.List;
import java.util.Objects;

public class RateLimiter {

    private RateLimitRuleRegister register;

    public RateLimiter(RateLimitRuleRegister register) {
        this.register = register;
    }

    public <T> void tryThrottle(T request) {
        if (Objects.isNull(request)) {
            return;
        }
        List<RateLimitRuleRegister.RateLimitRule> rules = register.getRules();
        for (RateLimitRuleRegister.RateLimitRule rule: rules) {
            ApiIdentifier identifier = rule.getIdentifier();
            if (!identifier.isSupported(request)) {
                continue;
            }
            ApiIdentity identity = identifier.parse(request);
            for (RateLimitChecker checker: rule.getCheckers()) {
                if (checker.isThrottling(identity)) {
                    throw new RateLimitExceedException("");
                }
            }
//            System.out.println(rule.getIdentifierGenericClass().isInstance(request));
//            try {
//                if (!identifier.isSupported(request)) {
//                    continue;
//                }
//                ApiIdentity identity = identifier.parse(request);
//                for (RateLimitChecker checker: rule.getCheckers()) {
//                    if (checker.isThrottling(identity)) {
//                        throw new RateLimitExceedException("");
//                    }
//                }
//            } catch (ClassCastException e) {
//                continue;
//            }



//            Class<?>[] requestInterfaces = request.getClass().getInterfaces();
//            for (Class<?> requestInterface: requestInterfaces) {
//                System.out.println("Request interface: " + requestInterface.getCanonicalName());
//            }
//            Class<?> requestSuperclass = request.getClass().getSuperclass();
//            System.out.println("Request type: " + requestSuperclass.getCanonicalName());
//
//            System.out.println("Generic type is same: " + genericType.getTypeName().equals(request.getClass().getCanonicalName()));
//            if (genericType.getTypeName().equals(request.getClass().getCanonicalName())) {
//                ApiIdentity identity = identifier.parse(request);
//                System.out.println("identity: " + identity);
//                for (RateLimitChecker checker: rule.getCheckers()) {
//                    if (checker.isThrottling(identity)) {
//                        throw new RateLimitExceedException("");
//                    }
//                }
//            }
        }
    }
}
