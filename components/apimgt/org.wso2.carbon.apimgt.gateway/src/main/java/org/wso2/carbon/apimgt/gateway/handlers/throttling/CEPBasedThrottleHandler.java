/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.gateway.handlers.throttling;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.throttle.core.RoleBasedAccessRateController;
import org.apache.synapse.commons.throttle.core.Throttle;
import org.apache.synapse.commons.throttle.core.ThrottleConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is Handling new throttling check. This class will use inside each API as throttle handler.
 * It will fetch some of data from incoming message and use them to take throttling decisions.
 * To execute this handler requests must go through authentication handler and auth context should be present
 * in message context.
 */
public class CEPBasedThrottleHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(CEPBasedThrottleHandler.class);

    private String policyKeyApplication = null;

    /**
     * The key for getting the throttling policy - key refers to a/an [registry] Resource entry
     */
    private String policyKeyResource = null;
    /**
     * The key for getting the throttling policy - key refers to a/an [registry] entry
     */
    private String policyKey = null;
    /**
     * The concurrent access control group id
     */
    private String id;
    /**
     * Version number of the throttle policy
     */

    public CEPBasedThrottleHandler() {
        if(log.isDebugEnabled()){
            log.debug("Throttle Handler intialized");
        }
    }

    private boolean doRoleBasedAccessThrottlingWithCEP(MessageContext synCtx, ConfigurationContext cc) {

        //Throttle Keys
        String applicationLevelThrottleKey;
        String subscriptionLevelThrottleKey;
        String resourceLevelThrottleKey;

        //Throttle Tiers
        String applicationLevelTier;
        String subscriptionLevelTier;
        String resourceLevelTier;

        //Other Relevant parameters
        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);
        String authorizedUser;

        //Throttled decisions
        boolean isThrottled = false;
        boolean isResourceLevelThrottled = false;
        boolean isApplicationLevelThrottled = false;
        boolean isSubscriptionLevelThrottled = false;

        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        apiContext = apiContext != null ? apiContext : "";
        apiVersion = apiVersion != null ? apiVersion : "";
        List<String> resourceLevelThrottleConditions;
        //If Authz context is not null only we can proceed with throttling
        if (authContext != null) {
            authorizedUser = authContext.getUsername();
            applicationLevelTier = authContext.getApplicationTier();
            subscriptionLevelTier = authContext.getTier();
            VerbInfoDTO verbInfoDTO = (VerbInfoDTO) synCtx.getProperty(APIConstants.VERB_INFO_DTO);
            //If verbInfo is present then only we will do resource level throttling
            if (verbInfoDTO == null) {
                log.warn("Error while getting throttling information for resource and http verb");
                return false;
            } else {
                if (APIConstants.UNLIMITED_TIER.equalsIgnoreCase(verbInfoDTO.getThrottling())) {
                    //If unlimited tier throttling will not apply at resource level and pass it
                } else {
                    //If tier is not unlimited only throttling will apply.
                    resourceLevelThrottleConditions = verbInfoDTO.getThrottlingConditions();
                    if (resourceLevelThrottleConditions == null) {
                        log.warn("Unable to find throttling information for resource and http verb. Throttling will " +
                                "not apply");
                    } else {
                        //Then we will apply resource level throttling
                        resourceLevelThrottleKey = verbInfoDTO.getRequestKey();
                        for (String conditionString : resourceLevelThrottleConditions) {
                            resourceLevelThrottleKey = verbInfoDTO.getRequestKey() + conditionString;
                            isThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                                    isThrottled(resourceLevelThrottleKey);
                            if (isThrottled) {
                                isResourceLevelThrottled = true;
                                isThrottled = true;
                                break;
                            }
                        }
                    }
                }

            }
            //Here check resource level throttled. If throttled then call handler throttled and pass.
            //Else go for subscription level and application level throttling
            //if resource level not throttled then move to subscription level
            if (!isResourceLevelThrottled) {
                //Subscription Level Throttling
                subscriptionLevelThrottleKey = authContext.getApplicationId() + ":" + apiContext + ":" + apiVersion;
                isSubscriptionLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                        isThrottled(subscriptionLevelThrottleKey);

                //if subscription level not throttled then move to application level
                if (!isSubscriptionLevelThrottled) {
                    //Application Level Throttling
                    applicationLevelThrottleKey = authContext.getApplicationId() + ":" + authorizedUser;
                    isApplicationLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                            isThrottled(applicationLevelThrottleKey);

                    //if application level not throttled means it does not throttled at any level.
                    if (!isApplicationLevelThrottled) {
                        //Did not throttled at any level. So let message go and publish event.
                        //publish event to Global Policy Server
                        String remoteIP = "127.0.0.1";
                        //(String) ((TreeMap) synCtx.getProperty(org.apache.axis2.context.MessageContext
                        //.TRANSPORT_HEADERS)).get(APIMgtGatewayConstants.X_FORWARDED_FOR);
                        if (remoteIP != null && !remoteIP.isEmpty()) {
                            if (remoteIP.indexOf(",") > 0) {
                                remoteIP = remoteIP.substring(0, remoteIP.indexOf(","));
                            }
                        } else {
                            remoteIP = (String) synCtx.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
                        }

                        //todo Added some dummy parameters
                        Map propertiesMap = new HashMap<String, String>();
                        propertiesMap.put("remoteIp", remoteIP);
                        propertiesMap.put("roleID", subscriptionLevelTier);

                        //this parameter will be used to capture message size and pass it to calculation logic
                        /*int messageSizeInBytes = 0;
                        if (authContext.isContentAware()) {
                            //this request can match with with bandwidth policy. So we need to get message size.
                            httpVerb = verbInfoDTO.getHttpVerb();
                            Object obj = ((TreeMap) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                                    getProperty("TRANSPORT_HEADERS")).get("Content-Length");
                            if (obj != null) {
                                messageSizeInBytes = Integer.parseInt(obj.toString());
                            }

                        }*/

                        Object[] objects = new Object[]{synCtx.getMessageID(), applicationLevelThrottleKey,
                                subscriptionLevelThrottleKey, applicationLevelTier, subscriptionLevelTier,
                                authorizedUser, propertiesMap};
                        //After publishing events return true
                        ServiceReferenceHolder.getInstance().getThrottleDataHolder().sendToGlobalThrottler(objects);

                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Request throttled at application level for throttle key" +
                                    applicationLevelThrottleKey);
                        }
                        synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                                APIThrottleConstants.APPLICATION_LIMIT_EXCEEDED);
                        isApplicationLevelThrottled = true;
                        isThrottled = true;
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Request throttled at subscription level for throttle key" +
                                subscriptionLevelThrottleKey);
                    }
                    synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                            APIThrottleConstants.SUBSCRIPTION_LIMIT_EXCEEDED);
                    isSubscriptionLevelThrottled = true;
                    isThrottled = true;
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Request throttled at resource level for throttle key" + verbInfoDTO.getRequestKey());
                }
                synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                        APIThrottleConstants.RESOURCE_LIMIT_EXCEEDED);
            }
        }
        //if we need to publish throttled level or some other information we can do it here. Just before return.
        return isThrottled;
    }


    public boolean handleRequest(MessageContext messageContext) {
        return doThrottle(messageContext);
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;//return doThrottle(messageContext);
    }

    private boolean doThrottle(MessageContext messageContext) {
        long start = System.currentTimeMillis();
        boolean isThrottled = false;
        if (!messageContext.isResponse()) {
            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                    getAxis2MessageContext();
            ConfigurationContext cc = axis2MC.getConfigurationContext();
            isThrottled = doRoleBasedAccessThrottlingWithCEP(messageContext, cc);
        }
        if (isThrottled) {
            // return false;
        }
        long end = System.currentTimeMillis();
        log.info("Time:" + (end - start));
        return true;
    }


    private OMElement getFaultPayload(int throttleErrorCode, String message, String description) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace(APIThrottleConstants.API_THROTTLE_NS,
                APIThrottleConstants.API_THROTTLE_NS_PREFIX);
        OMElement payload = fac.createOMElement("fault", ns);

        OMElement errorCode = fac.createOMElement("code", ns);
        errorCode.setText(String.valueOf(throttleErrorCode));
        OMElement errorMessage = fac.createOMElement("message", ns);
        errorMessage.setText(message);
        OMElement errorDetail = fac.createOMElement("description", ns);
        errorDetail.setText(description);

        payload.addChild(errorCode);
        payload.addChild(errorMessage);
        payload.addChild(errorDetail);
        return payload;
    }

    private void handleThrottleOut(MessageContext messageContext) {

        String errorMessage = null;
        String errorDescription = null;
        int errorCode = -1;
        int httpErrorCode = -1;

        if (APIThrottleConstants.HARD_LIMIT_EXCEEDED.equals(
                messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.HARD_LIMIT_EXCEEDED_ERROR_CODE;
            errorMessage = "API Limit Reached";
            errorDescription = "API not accepting requests";
            // It it's a hard limit exceeding, we tell it as service not being available.
            httpErrorCode = HttpStatus.SC_SERVICE_UNAVAILABLE;
        } else {
            errorCode = 503;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
        }

        messageContext.setProperty(SynapseConstants.ERROR_CODE, errorCode);
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE, errorMessage);
        Mediator sequence = messageContext.getSequence(APIThrottleConstants.API_THROTTLE_OUT_HANDLER);

        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        // This property need to be set to avoid sending the content in pass-through pipe (request message)
        // as the response.
        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.consumeAndDiscardMessage(axis2MC);
        } catch (AxisFault axisFault) {
            //In case of an error it is logged and the process is continued because we're setting a fault message
            // in the payload.
            log.error("Error occurred while consuming and discarding the message", axisFault);
        }

        if (messageContext.isDoingPOX() || messageContext.isDoingGET()) {
            Utils.setFaultPayload(messageContext, getFaultPayload(errorCode, errorMessage, errorDescription));
        } else {
            Utils.setSOAPFault(messageContext, "Server", errorMessage, errorDescription);
        }

        Utils.sendFault(messageContext, httpErrorCode);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPolicyKey(String policyKey) {
        this.policyKey = policyKey;
    }

    public String gePolicyKey() {
        return policyKey;
    }

    public void setPolicyKeyApplication(String policyKeyApplication) {
        this.policyKeyApplication = policyKeyApplication;
    }

    public String gePolicyKeyApplication() {
        return policyKeyApplication;
    }

    public void setPolicyKeyResource(String policyKeyResource) {
        this.policyKeyResource = policyKeyResource;
    }

    public String gePolicyKeyResource() {
        return policyKeyResource;
    }
}
