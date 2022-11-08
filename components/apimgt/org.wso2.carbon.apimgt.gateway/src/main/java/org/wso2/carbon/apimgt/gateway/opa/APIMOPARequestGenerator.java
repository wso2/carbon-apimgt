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

import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.opa.OPAConstants;
import org.apache.synapse.mediators.opa.OPARequestGenerator;
import org.apache.synapse.mediators.opa.OPASecurityException;
import org.apache.synapse.mediators.opa.OPAUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;

import java.util.Map;
import java.util.TreeMap;

/**
 * APIM implementation of the {@link OPARequestGenerator}.
 */
public class APIMOPARequestGenerator implements OPARequestGenerator {

    private static final Log log = LogFactory.getLog(APIMOPARequestGenerator.class);
    public static final String ELECTED_RESOURCE_STRING = "API_ELECTED_RESOURCE";

    @Override
    public String generateRequest(String policyName, String rule, Map<String, String> additionalParameters,
                                  MessageContext messageContext)
            throws OPASecurityException {

        JSONObject inputObject = new JSONObject();
        JSONObject opaPayload = new JSONObject();
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        TreeMap<String, String> transportHeadersMap = (TreeMap<String, String>) axis2MessageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String requestOriginIP = OPAUtils.getRequestIp(axis2MessageContext);
        String requestMethod = (String) axis2MessageContext.getProperty(OPAConstants.HTTP_METHOD_STRING);
        String requestPath = (String) axis2MessageContext.getProperty(OPAConstants.API_BASEPATH_STRING);
        String electedResource = (String) axis2MessageContext.getProperty(ELECTED_RESOURCE_STRING);

        AuthenticationContext authContext = (AuthenticationContext) messageContext.getProperty("__API_AUTH_CONTEXT");
        if (authContext != null) {
            JSONObject apiContext = new JSONObject();
            apiContext.put("apiName", authContext.getApiName());
            apiContext.put("apiVersion", authContext.getApiVersion());
            apiContext.put("subscriberOrganization", authContext.getSubscriberTenantDomain());
            apiContext.put("isAuthenticated", authContext.isAuthenticated());
            apiContext.put("issuer", authContext.getIssuer());
            apiContext.put("apiPublisher", authContext.getApiPublisher());
            apiContext.put("keyType", authContext.getKeyType());
            apiContext.put("subscriber", authContext.getSubscriber());
            apiContext.put("consumerKey", authContext.getConsumerKey());
            apiContext.put("applicationUUID", authContext.getApplicationUUID());
            apiContext.put("applicationName", authContext.getApplicationName());
            apiContext.put("username", authContext.getUsername());

            if (additionalParameters.get("sendAccessToken") != null) {
                if (JavaUtils.isTrueExplicitly(additionalParameters.get("sendAccessToken"))) {
                    apiContext.put("accessToken", authContext.getAccessToken());
                }
            }

            opaPayload.put("apiContext", apiContext);
        }

        opaPayload.put(OPAConstants.REQUEST_ORIGIN_KEY, requestOriginIP);
        opaPayload.put(OPAConstants.REQUEST_METHOD_KEY, requestMethod);
        opaPayload.put(OPAConstants.REQUEST_PATH_KEY, requestPath);
        opaPayload.put(OPAConstants.REQUEST_TRANSPORT_HEADERS_KEY, new JSONObject(transportHeadersMap));
        opaPayload.put("electedResource", electedResource);

        if (additionalParameters.get(OPAConstants.ADDITIONAL_MC_PROPERTY_PARAMETER) != null) {
            String additionalMCPropertiesString = additionalParameters.get(OPAConstants.ADDITIONAL_MC_PROPERTY_PARAMETER);
            String[] additionalMCProperties = additionalMCPropertiesString.split(OPAConstants.ADDITIONAL_MC_PROPERTY_DIVIDER);
            for (String mcProperty : additionalMCProperties) {
                if (messageContext.getProperty(mcProperty) != null) {
                    opaPayload.put(mcProperty, messageContext.getProperty(mcProperty));
                }
            }
        }

        inputObject.put(OPAConstants.INPUT_KEY, opaPayload);
        return inputObject.toString();
    }

    @Override
    public boolean handleResponse(String policyName, String rule, String opaResponse,
                                  Map<String, String> additionalParameters, MessageContext messageContext)
            throws OPASecurityException {

        if (OPAConstants.EMPTY_OPA_RESPONSE.equals(opaResponse)) {
            log.error("Empty result received for the OPA policy " + policyName + " for rule " + rule);
            throw new OPASecurityException(OPASecurityException.INTERNAL_ERROR,
                    "Empty result received for the OPA policy " + policyName + " for rule " + rule);
        } else {
            try {
                org.json.JSONObject responseObject = new org.json.JSONObject(opaResponse);
                if (rule != null) {
                    return responseObject.getBoolean(OPAConstants.OPA_RESPONSE_RESULT_KEY);
                } else {
                    // If a rule is not specified, default allow rule is considered
                    org.json.JSONObject resultObjectFromAllow =
                            (org.json.JSONObject) responseObject.get(OPAConstants.OPA_RESPONSE_DEFAULT_RULE);
                    return resultObjectFromAllow.getBoolean(OPAConstants.OPA_RESPONSE_RESULT_KEY);
                }
            } catch (JSONException e) {
                log.error("Error parsing OPA JSON response, the field \"result\" not found or not a Boolean", e);
                throw new OPASecurityException(OPASecurityException.INTERNAL_ERROR,
                        OPASecurityException.INTERNAL_ERROR_MESSAGE, e);
            }
        }
    }
}
