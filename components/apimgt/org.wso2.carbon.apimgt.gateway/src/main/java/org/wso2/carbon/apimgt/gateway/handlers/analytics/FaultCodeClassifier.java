/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultCategory;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultSubCategories;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultSubCategory;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;

/**
 * Classify faulty codes
 */
public class FaultCodeClassifier {
    private static final Log log = LogFactory.getLog(FaultCodeClassifier.class);
    private MessageContext messageContext;

    public FaultCodeClassifier(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    public FaultSubCategory getFaultSubCategory(FaultCategory faultCategory, int errorCode) {
        switch (faultCategory) {
        case AUTH:
            return getAuthFaultSubCategory(errorCode);
        case TARGET_CONNECTIVITY:
            return getTargetFaultSubCategory(errorCode);
        case THROTTLED:
            return getThrottledFaultSubCategory(errorCode);
        case OTHER:
            return getOtherFaultSubCategory(errorCode);
        }
        return null;
    }

    protected FaultSubCategory getAuthFaultSubCategory(int errorCode) {
        switch (errorCode) {
        case APISecurityConstants.API_AUTH_GENERAL_ERROR:
        case APISecurityConstants.API_AUTH_INVALID_CREDENTIALS:
        case APISecurityConstants.API_AUTH_MISSING_CREDENTIALS:
        case APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED:
        case APISecurityConstants.API_AUTH_ACCESS_TOKEN_INACTIVE:
            return FaultSubCategories.Authentication.AUTHENTICATION_FAILURE;
        case APISecurityConstants.API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE:
        case APISecurityConstants.INVALID_SCOPE:
            return FaultSubCategories.Authentication.AUTHORIZATION_FAILURE;
        case APISecurityConstants.API_BLOCKED:
        case APISecurityConstants.API_AUTH_FORBIDDEN:
        case APISecurityConstants.SUBSCRIPTION_INACTIVE:
            return FaultSubCategories.Authentication.SUBSCRIPTION_VALIDATION_FAILURE;
        default:
            return FaultSubCategories.TargetConnectivity.OTHER;
        }
    }

    protected FaultSubCategory getTargetFaultSubCategory(int errorCode) {
        switch (errorCode) {
        case SynapseConstants.NHTTP_CONNECTION_TIMEOUT:
        case SynapseConstants.NHTTP_CONNECT_TIMEOUT:
            return FaultSubCategories.TargetConnectivity.CONNECTION_TIMEOUT;
        case Constants.ENDPOINT_SUSPENDED_ERROR_CODE:
            return FaultSubCategories.TargetConnectivity.CONNECTION_SUSPENDED;
        default:
            return FaultSubCategories.TargetConnectivity.OTHER;
        }
    }

    protected FaultSubCategory getThrottledFaultSubCategory(int errorCode) {
        switch (errorCode) {
        case APIThrottleConstants.API_THROTTLE_OUT_ERROR_CODE:
            return FaultSubCategories.Throttling.API_LEVEL_LIMIT_EXCEEDED;
        case APIThrottleConstants.HARD_LIMIT_EXCEEDED_ERROR_CODE:
            return FaultSubCategories.Throttling.HARD_LIMIT_EXCEEDED;
        case APIThrottleConstants.RESOURCE_THROTTLE_OUT_ERROR_CODE:
            return FaultSubCategories.Throttling.RESOURCE_LEVEL_LIMIT_EXCEEDED;
        case APIThrottleConstants.APPLICATION_THROTTLE_OUT_ERROR_CODE:
            return FaultSubCategories.Throttling.APPLICATION_LEVEL_LIMIT_EXCEEDED;
        case APIThrottleConstants.SUBSCRIPTION_THROTTLE_OUT_ERROR_CODE:
            return FaultSubCategories.Throttling.SUBSCRIPTION_LIMIT_EXCEEDED;
        case APIThrottleConstants.BLOCKED_ERROR_CODE:
            return FaultSubCategories.Throttling.BLOCKED;
        case APIThrottleConstants.CUSTOM_POLICY_THROTTLE_OUT_ERROR_CODE:
            return FaultSubCategories.Throttling.CUSTOM_POLICY_LIMIT_EXCEEDED;
        case APIThrottleConstants.SUBSCRIPTION_BURST_THROTTLE_OUT_ERROR_CODE:
            return FaultSubCategories.Throttling.BURST_CONTROL_LIMIT_EXCEEDED;
        case APIThrottleConstants.GRAPHQL_QUERY_TOO_DEEP:
            return FaultSubCategories.Throttling.QUERY_TOO_DEEP;
        case APIThrottleConstants.GRAPHQL_QUERY_TOO_COMPLEX:
            return FaultSubCategories.Throttling.QUERY_TOO_COMPLEX;
        default:
            return FaultSubCategories.Throttling.OTHER;
        }
    }

    protected FaultSubCategory getOtherFaultSubCategory(int errorCode) {
        if (isMethodNotAllowed()) {
            return FaultSubCategories.Other.METHOD_NOT_ALLOWED;
        } else if (isResourceNotFound()) {
            return FaultSubCategories.Other.RESOURCE_NOT_FOUND;
        } else {
            return FaultSubCategories.Other.UNCLASSIFIED;
        }
    }

    public boolean isResourceNotFound() {
        if (messageContext.getPropertyKeySet().contains(SynapseConstants.ERROR_CODE)) {
            int errorCode = (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
            return messageContext.getPropertyKeySet().contains(RESTConstants.PROCESSED_API)
                    && errorCode == Constants.RESOURCE_NOT_FOUND_ERROR_CODE;
        }
        return false;
    }

    public boolean isMethodNotAllowed() {
        if (messageContext.getPropertyKeySet().contains(SynapseConstants.ERROR_CODE)) {
            int errorCode = (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
            return messageContext.getPropertyKeySet().contains(RESTConstants.PROCESSED_API)
                    && errorCode == Constants.METHOD_NOT_ALLOWED_ERROR_CODE;
        }
        return false;
    }
}
