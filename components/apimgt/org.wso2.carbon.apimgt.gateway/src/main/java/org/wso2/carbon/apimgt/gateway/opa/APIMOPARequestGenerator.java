/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.gateway.opa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.opa.OPARequestGenerator;
import org.apache.synapse.mediators.opa.OPASecurityException;
import org.apache.synapse.mediators.opa.OPAUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;

import java.util.Map;
import java.util.TreeMap;

public class APIMOPARequestGenerator implements OPARequestGenerator {

    private static final Log log = LogFactory.getLog(APIMOPARequestGenerator.class);

    public static final String HTTP_METHOD_STRING = "HTTP_METHOD";
    public static final String API_BASEPATH_STRING = "TransportInURL";
    public static final String API_NAME_STRING = "API_NAME";
    public static final String API_VERSION_STRING = "SYNAPSE_REST_API_VERSION";

    @Override
    public String generateRequest(String policyName, String rule, Map<String, Object> advancedProperties,
                                  MessageContext messageContext)
            throws OPASecurityException {

        JSONObject inputObject = new JSONObject();
        JSONObject opaPayload = new JSONObject();

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        TreeMap<String, String> transportHeadersMap = (TreeMap<String, String>) axis2MessageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String requestOriginIP = OPAUtils.getRequestIp(axis2MessageContext);
        String requestMethod = (String) axis2MessageContext.getProperty(HTTP_METHOD_STRING);
        String requestPath = (String) axis2MessageContext.getProperty(API_BASEPATH_STRING);
        String apiName = (String) messageContext.getProperty(API_NAME_STRING);
        String apiVersion = (String) messageContext.getProperty(API_VERSION_STRING);
        String subscriberOrganization = "";

        AuthenticationContext authContext = (AuthenticationContext) messageContext.getProperty("__API_AUTH_CONTEXT");
        if (authContext != null) {
            apiName = authContext.getApiName();
            apiVersion = authContext.getApiVersion();
            subscriberOrganization = authContext.getSubscriberTenantDomain();
            transportHeadersMap.computeIfAbsent("Authorization", k -> "Bearer " + authContext.getAccessToken());
        }

        opaPayload.put("apiName", apiName);
        opaPayload.put("apiVersion", apiVersion);
        opaPayload.put("subscriberOrganization", subscriberOrganization);
        opaPayload.put("requestBody", messageContext.getMessageString());
        opaPayload.put("requestOrigin", requestOriginIP);
        opaPayload.put("method", requestMethod);
        opaPayload.put("path", requestPath);
        opaPayload.put("transportHeaders", new JSONObject(transportHeadersMap));
        inputObject.put("input", opaPayload);
        return inputObject.toString();
    }

    @Override
    public boolean handleResponse(String policyName, String rule, String opaResponse, MessageContext messageContext)
            throws OPASecurityException {

        if (opaResponse.equals("{}")) {
            if (log.isDebugEnabled()) {
                log.debug("Empty result received for the rule " + rule + " of policy " + policyName);
            }
            throw new OPASecurityException(OPASecurityException.OPA_RESPONSE_ERROR,
                    "Empty result received for the OPA policy rule");
        } else {
            try {
                org.json.JSONObject responseObject = new org.json.JSONObject(opaResponse);
                if (rule != null) {
                    return responseObject.getBoolean("result");
                } else {
                    // If a rule is not specified, default allow rule is considered
                    org.json.JSONObject resultObjectFromAllow = (org.json.JSONObject) responseObject.get("allow");
                    return resultObjectFromAllow.getBoolean("result");
                }
            } catch (JSONException e) {
                log.error("Error parsing OPA JSON response, the field \"result\" not found or not a Boolean", e);
                throw new OPASecurityException(OPASecurityException.OPA_RESPONSE_ERROR,
                        OPASecurityException.OPA_RESPONSE_ERROR_MESSAGE, e);
            }
        }
    }
}
