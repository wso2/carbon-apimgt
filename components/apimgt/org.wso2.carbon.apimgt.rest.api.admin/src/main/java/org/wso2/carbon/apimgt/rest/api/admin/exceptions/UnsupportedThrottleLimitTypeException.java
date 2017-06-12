package org.wso2.carbon.apimgt.rest.api.admin.exceptions;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;

public class UnsupportedThrottleLimitTypeException extends APIManagementException {

    public UnsupportedThrottleLimitTypeException(String message) {
        super(message);
    }

    public UnsupportedThrottleLimitTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedThrottleLimitTypeException(String msg, Throwable e, ExceptionCodes code) {
        super(msg, e, code);
    }

    public UnsupportedThrottleLimitTypeException(String msg, ExceptionCodes code) {
        super(msg, code);
    }
}
