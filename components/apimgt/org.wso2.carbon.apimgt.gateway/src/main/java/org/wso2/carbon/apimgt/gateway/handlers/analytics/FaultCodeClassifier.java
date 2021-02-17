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
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.enums.FaultCategory;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.enums.FaultSubCategories;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.enums.FaultSubCategory;

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

    private FaultSubCategory getAuthFaultSubCategory(int errorCode) {
        return FaultSubCategories.Authentication.INVALID_TOKEN;
    }

    private FaultSubCategory getTargetFaultSubCategory(int errorCode) {
        if (errorCode == 101504 || errorCode == 101508) {
            return FaultSubCategories.TargetConnectivity.CONNECTION_TIMEOUT;
        } else if (errorCode == Constants.ENDPOINT_SUSPENDED_ERROR_CODE) {
            return FaultSubCategories.TargetConnectivity.CONNECTION_SUSPENDED;
        } else {
            return FaultSubCategories.TargetConnectivity.OTHER;
        }
    }

    private FaultSubCategory getThrottledFaultSubCategory(int errorCode) {
        switch (errorCode) {
        case 900803:
            return FaultSubCategories.Throttling.APPLICATION;
        case 900804:
            return FaultSubCategories.Throttling.SUBSCRIPTION;
        default:
            return FaultSubCategories.Throttling.OTHER;
        }
    }

    private FaultSubCategory getOtherFaultSubCategory(int errorCode) {
        if (isMethodNotAllowed()) {
            return FaultSubCategories.Other.METHOD_NOT_ALLOWED;
        } else if (isResourceNotFound()) {
            return FaultSubCategories.Other.RESOURCE_NOT_FOUND;
        } else {
            return FaultSubCategories.Other.OTHER;
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
