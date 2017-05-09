package org.wso2.carbon.apimgt.rest.api.admin.exceptions;

import org.wso2.carbon.apimgt.core.exception.APIManagementException;

public class SubscriptionThrottlePolicyException extends APIManagementException {

    public SubscriptionThrottlePolicyException(String message, Throwable cause) {
        super(message, cause);
    }
}
