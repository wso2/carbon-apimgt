package org.wso2.carbon.apimgt.rest.api.admin.exceptions;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;

public class UnsupportedThrottleLimitTypeException extends APIManagementException {

    public UnsupportedThrottleLimitTypeException(String message) {
        super(message);
    }

    public UnsupportedThrottleLimitTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
