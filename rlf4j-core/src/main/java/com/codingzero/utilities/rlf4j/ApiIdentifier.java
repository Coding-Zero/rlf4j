package com.codingzero.utilities.rlf4j;

public interface ApiIdentifier<T> {

    boolean isSupported(Object input);

    ApiIdentity parse(T input);

}
