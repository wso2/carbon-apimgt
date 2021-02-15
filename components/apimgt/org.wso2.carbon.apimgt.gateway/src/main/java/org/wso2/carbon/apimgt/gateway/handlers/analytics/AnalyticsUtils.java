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

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * Implementation of the util methods
 */
public class AnalyticsUtils {

    public static boolean isAuthFaultRequest(int errorCode) {
        return errorCode >= Constants.ERROR_CODE_RANGES.AUTH_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.AUTH_FAILURE__END;
    }

    public static boolean isThrottledFaultRequest(int errorCode) {
        return errorCode >= Constants.ERROR_CODE_RANGES.THROTTLED_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.THROTTLED_FAILURE__END;
    }

    public static boolean isTargetFaultRequest(int errorCode) {
        return (errorCode >= Constants.ERROR_CODE_RANGES.TARGET_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.TARGET_FAILURE__END)
                || errorCode == Constants.ENDPOINT_SUSPENDED_ERROR_CODE;
    }

    public static boolean isFaultRequest(MessageContext messageContext) {
        return messageContext.getPropertyKeySet().contains(SynapseConstants.ERROR_CODE);
    }

    public static boolean isSuccessRequest(MessageContext messageContext) {
        return !messageContext.getPropertyKeySet().contains(SynapseConstants.ERROR_CODE)
                && APISecurityUtils.getAuthenticationContext(messageContext) != null;
    }

    public static boolean isResourceNotFound(MessageContext messageContext) {
        if (messageContext.getPropertyKeySet().contains(SynapseConstants.ERROR_CODE)) {
            int errorCode = (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
            return messageContext.getPropertyKeySet().contains(RESTConstants.PROCESSED_API)
                    && errorCode == Constants.RESOURCE_NOT_FOUND_ERROR_CODE;
        }
        return false;
    }

    public static boolean isMethodNotAllowed(MessageContext messageContext) {
        if (messageContext.getPropertyKeySet().contains(SynapseConstants.ERROR_CODE)) {
            int errorCode = (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
            return messageContext.getPropertyKeySet().contains(RESTConstants.PROCESSED_API)
                    && errorCode == Constants.METHOD_NOT_ALLOWED_ERROR_CODE;
        }
        return false;
    }

    public static boolean isAPINotFound(MessageContext messageContext) {
        return !messageContext.getPropertyKeySet().contains(RESTConstants.PROCESSED_API);
    }

    public static boolean isProxyAPI(MessageContext messageContext) {
        return messageContext.getPropertyKeySet().contains(RESTConstants.PROCESSED_API);
    }

    public static String getUserAgent(MessageContext messageContext) {
        Map<?, ?> headers = (Map<?, ?>) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        return (String) headers.get(APIConstants.USER_AGENT);
    }

    public static String getTimeInISO(long time) {
        OffsetDateTime offsetDateTime = OffsetDateTime
                .ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC.normalized());
        return offsetDateTime.toString();
    }
}
