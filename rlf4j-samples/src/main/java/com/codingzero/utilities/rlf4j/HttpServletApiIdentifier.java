package com.codingzero.utilities.rlf4j;

import javax.servlet.http.HttpServletRequest;

public final class HttpServletApiIdentifier implements ApiIdentifier<HttpServletRequest> {

    @Override
    public boolean isSupported(Object input) {
        return HttpServletRequest.class.isInstance(input);
    }

    @Override
    public ApiIdentity parse(HttpServletRequest input) {
        return null;
    }
}
