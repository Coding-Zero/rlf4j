package com.codingzero.utilities.rlf4j;

import javax.servlet.http.HttpServletRequest;

public class ReservedApiIdentifier implements ApiIdentifier<HttpServletRequest> {

    @Override
    public ApiIdentity identify(HttpServletRequest input) {
        return null;
    }
}
