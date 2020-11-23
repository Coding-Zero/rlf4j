package com.codingzero.utilities.rlf4j;

public class RateLimitExceedException extends RuntimeException {

    public RateLimitExceedException(String message) {
        super(message);
    }
}
