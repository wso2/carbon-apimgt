package org.wso2.carbon.apimgt.rest.api.admin.exceptions;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;

public class UnsupportedThrottleConditionTypeException extends APIManagementException {

    public UnsupportedThrottleConditionTypeException(String message) {
        super(message);
    }

    public UnsupportedThrottleConditionTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
