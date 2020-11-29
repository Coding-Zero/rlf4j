package com.codingzero.utilities.rlf4j;

import com.codingzero.utilities.rlf4j.quotas.DistributedRateLimitQuota;

import javax.servlet.http.HttpServletRequest;

public class Example {

    public static void main(String[] args) {
        Example example = new Example();
        example.demoHttpServletRequestAPIRateLimit();
    }

    public void demoHttpServletRequestAPIRateLimit() {

//        RateLimitRuleRegister register = RateLimitRuleRegister
//                .register(new HttpServletApiIdentifier()).with(new DistributedRateLimitQuota())
//                .build();
//
//        RateLimiter rateLimiter = RateLimiter.builder().register(register).build();
//        HttpServletRequest httpServletRequest = new HttpServletRequestSample();
//        try {
//            String content = rateLimiter.tryLimit(httpServletRequest, () -> helloWorld());
//            System.out.println(content);
//        } catch (RateLimitExceedException e) {
//            e.printStackTrace();
//        }
    }

    public  String helloWorld() {
        System.out.println("helloWorld()");
        return "Hello World";
    }

}
