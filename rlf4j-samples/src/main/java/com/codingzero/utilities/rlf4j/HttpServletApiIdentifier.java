package com.codingzero.utilities.rlf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public final class HttpServletApiIdentifier implements ApiIdentifier<HttpServletRequest> {

    @Override
    public ApiIdentity identify(HttpServletRequest input) {
        return new ApiIdentity(Arrays.asList("/api/hello"),  CriticalLevel.TO_BE_DECIDED, ResourceUsage.TO_BE_DECIDED);
    }
}
