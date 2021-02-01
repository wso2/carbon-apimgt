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
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.usage.publisher.dto.enums.EVENT_TYPE;
import org.wso2.carbon.apimgt.usage.publisher.dto.enums.FAULT_EVENT_TYPE;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Map;

/**
 * Implementation of the util methods
 */
public class AnalyticsUtils {
    private static final String ANALYTICS_REQUEST_TYPE = "analytics_request_type";
    private static final String THROTTLED_OUT_REQUEST = "throttled_out_request";

    public static void markedAsThrottled(MessageContext messageContext) {
        messageContext.setProperty(ANALYTICS_REQUEST_TYPE, THROTTLED_OUT_REQUEST);
    }

    public static boolean isRequestThrottledOut(MessageContext messageContext) {
        if (messageContext.getPropertyKeySet().contains(ANALYTICS_REQUEST_TYPE)) {
            String type = (String) messageContext.getProperty(ANALYTICS_REQUEST_TYPE);
            return THROTTLED_OUT_REQUEST.equals(type);
        } else {
            return false;
        }
    }

    public static boolean isAuthFaultRequest(int errorCode) {
        return errorCode >= Constants.ERROR_CODE_RANGES.AUTH_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.AUTH_FAILURE__END;
    }

    public static boolean isThrottledFaultRequest(int errorCode) {
        return errorCode >= Constants.ERROR_CODE_RANGES.THROTTLED_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.THROTTLED_FAILURE__END;
    }

    public static boolean isTargetFaultRequest(int errorCode) {
        return errorCode >= Constants.ERROR_CODE_RANGES.TARGET_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.TARGET_FAILURE__END;
    }

    public static FAULT_EVENT_TYPE getFaultyType(MessageContext messageContext) {
        int errorCode = (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
        if (isAuthFaultRequest(errorCode)) {
            return FAULT_EVENT_TYPE.AUTH;
        } else if (isThrottledFaultRequest(errorCode)) {
            return FAULT_EVENT_TYPE.THROTTLED;
        } else if (isTargetFaultRequest(errorCode)) {
            return FAULT_EVENT_TYPE.TARGET;
        } else {
            return FAULT_EVENT_TYPE.OTHER;
        }
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

    public static EVENT_TYPE getEventType(MessageContext messageContext) {
        if (isSuccessRequest(messageContext)) {
            return EVENT_TYPE.SUCCESS;
        } else if (isFaultRequest(messageContext)) {
            return EVENT_TYPE.FAULTY;
        } else if (isResourceNotFound(messageContext)) {
            return EVENT_TYPE.RESOURCE_NOT_FOUND;
        } else if (isMethodNotAllowed(messageContext)) {
            return EVENT_TYPE.METHOD_NOT_ALLOWED;
        } else if (isAPINotFound(messageContext)) {
            return EVENT_TYPE.API_NOT_FOUND;
        } else if (isProxyAPI(messageContext)) {
            return EVENT_TYPE.PROXY_API_INVOCATION;
        }

        return EVENT_TYPE.OTHER;
    }

    public static String getUserAgent(MessageContext messageContext) {
        Map<?, ?> headers = (Map<?, ?>) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        return (String) headers.get(APIConstants.USER_AGENT);
    }

    public static long getRequestMediationLatency(MessageContext messageContext) {

        Object reqMediationLatency = messageContext.getProperty(APIMgtGatewayConstants.REQUEST_MEDIATION_LATENCY);
        return reqMediationLatency == null ? 0 : ((Number) reqMediationLatency).longValue();
    }

    public static long getResponseMediationLatency(MessageContext messageContext) {

        Object resMediationLatency = messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_MEDIATION_LATENCY);
        return resMediationLatency == null ? 0 : ((Number) resMediationLatency).longValue();
    }

}
