package com.codingzero.utilities.rlf4j;

import com.codingzero.utilities.rlf4j.checkers.DistributedRateLimitChecker;

import java.util.Arrays;
import java.util.List;

public class Example {

    public static void main(String[] args) {
        RateLimitRuleRegister register = RateLimitRuleRegister
                .register(new HttpServletApiIdentifier())
                .with(new DistributedRateLimitChecker())
                .build();

        RateLimiter rateLimiter = new RateLimiter(register);
        String request = "";
        Number requestNum = 1.0;
        Example example = new Example();
        List<Number> numberList = Arrays.asList(1.0, 2.0);
        List<String> strings = Arrays.asList("abc", "ccc");
        rateLimiter.tryThrottle(request);

    }


}
