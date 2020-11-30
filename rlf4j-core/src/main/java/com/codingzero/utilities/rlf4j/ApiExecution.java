package com.codingzero.utilities.rlf4j;

/**
 * This interface represents a API method reference
 *
 * @param <R> return type
 */
@FunctionalInterface
public interface ApiExecution<R> {

    R execute();

}
