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
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is Handling new throttling check. This class will use inside each API as throttle handler.
 * It will fetch some of data from incoming message and use them to take throttling decisions.
 * To execute this handler requests must go through authentication handler and auth context should be present
 * in message context.
 */
public class ThrottleHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(ThrottleHandler.class);

    private static volatile DataPublisher dataPublisher = null;

    private static volatile ThrottleDataPublisher throttleDataPublisher = null;

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

    /**
     * Created throttle handler object.
     */
    //Throttle Handler rename
    public ThrottleHandler() {
        if (log.isDebugEnabled()) {
            log.debug("Throttle Handler initialized");
        }
        /**
         * This method will initialize data publisher and this data publisher will be used to push events to central policy
         * server.
         */
        if (throttleDataPublisher == null) {
            // The publisher initializes in the first request only
            synchronized (this) {
                throttleDataPublisher = new ThrottleDataPublisher();
            }
        }
    }

    /**
     * This method is responsible for throttle incoming messages. This method will perform Application, Subscription
     * and Resource level throttling.
     *
     * @param synCtx Synapse message context that contains message details.
     * @param cc     Configuration context which holds current configuration context.
     * @return
     */
    private boolean doRoleBasedAccessThrottlingWithCEP(MessageContext synCtx, ConfigurationContext cc) {

        //Throttle Keys
        String applicationLevelThrottleKey;
        String subscriptionLevelThrottleKey;
        String resourceLevelThrottleKey;
        String apiLevelThrottleKey;

        //Throttle Tiers
        String applicationLevelTier;
        String subscriptionLevelTier;
        String resourceLevelTier;
        String apiLevelTier;

        //Other Relevant parameters
        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);
        String authorizedUser;

        //Throttled decisions
        boolean isThrottled = false;
        boolean isResourceLevelThrottled = false;
        boolean isApplicationLevelThrottled = false;
        boolean isSubscriptionLevelThrottled = false;
        boolean isApiLevelThrottled = false;

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
            apiLevelTier = authContext.getApiTier();
            //Following throttle data list can be use to hold throttle data and api level throttle key
            //should be its first element.
            apiLevelThrottleKey = authContext.getThrottlingDataList().get(0);
            VerbInfoDTO verbInfoDTO = (VerbInfoDTO) synCtx.getProperty(APIConstants.VERB_INFO_DTO);
            //If API level throttle policy is present then it will apply and no resource level policy will apply for it.
            if (apiLevelTier != null && apiLevelTier.length() > 0) {
                isThrottled = isApiLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                        isThrottled(apiLevelThrottleKey);
            } else {
                //If API level tier is not present only we should move to resource level tiers.
                if (verbInfoDTO == null) {
                    log.warn("Error while getting throttling information for resource and http verb");
                    return false;
                } else {
                    //If verbInfo is present then only we will do resource level throttling
                    if (APIConstants.UNLIMITED_TIER.equalsIgnoreCase(verbInfoDTO.getThrottling())) {
                        //If unlimited tier throttling will not apply at resource level and pass it
                        if (log.isDebugEnabled()) {
                            log.debug("Resource level throttling set as unlimited and request will pass resource level");
                        }
                    } else {
                        //If tier is not unlimited only throttling will apply.
                        resourceLevelThrottleConditions = verbInfoDTO.getThrottlingConditions();
                        if (resourceLevelThrottleConditions != null && resourceLevelThrottleConditions.size() > 0) {
                            //Then we will apply resource level throttling
                            resourceLevelThrottleKey = verbInfoDTO.getRequestKey();
                            for (String conditionString : resourceLevelThrottleConditions) {
                                resourceLevelThrottleKey = verbInfoDTO.getRequestKey() + conditionString;
                                if (ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                                        isThrottled(resourceLevelThrottleKey)) {
                                    isResourceLevelThrottled = isThrottled = true;
                                    break;
                                }
                            }

                        } else {
                            log.warn("Unable to find throttling information for resource and http verb. Throttling will " +
                                    "not apply");
                        }
                    }

                }
            }
            if (!isApiLevelThrottled) {
                //Here check resource level throttled. If throttled then call handler throttled and pass.
                //Else go for subscription level and application level throttling
                //if resource level not throttled then move to subscription level
                if (!isResourceLevelThrottled) {
                    //Subscription Level Throttling
                    subscriptionLevelThrottleKey = authContext.getApplicationId() + ":" + apiContext + ":" + apiVersion;
                    isSubscriptionLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                            isThrottled(subscriptionLevelThrottleKey);

                    //if subscription level not throttled then move to application level
                    //Stop on quata reach
                    if (!isSubscriptionLevelThrottled) {
                        //Application Level Throttling
                        applicationLevelThrottleKey = authContext.getApplicationId() + ":" + authorizedUser;
                        isApplicationLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                                isThrottled(applicationLevelThrottleKey);

                        //if application level not throttled means it does not throttled at any level.
                        if (!isApplicationLevelThrottled) {
                            //Pass message context and continue to avaoid peformance issue.
                            //Did not throttled at any level. So let message go and publish event.
                            //publish event to Global Policy Server
                            throttleDataPublisher.publishNonThrottledEvent(synCtx);

                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Request throttled at application level for throttle key" +
                                        applicationLevelThrottleKey);
                            }
                            synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                                    APIThrottleConstants.APPLICATION_LIMIT_EXCEEDED);
                            isThrottled = isApplicationLevelThrottled = true;
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Request throttled at subscription level for throttle key" +
                                    subscriptionLevelThrottleKey);
                        }
                        synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                                APIThrottleConstants.SUBSCRIPTION_LIMIT_EXCEEDED);
                        isThrottled = isSubscriptionLevelThrottled = true;
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Request throttled at resource level for throttle key" + verbInfoDTO.getRequestKey());
                    }
                    synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                            APIThrottleConstants.RESOURCE_LIMIT_EXCEEDED);
                    //is throttled and resource level throttling
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Request throttled at api level for throttle key" + apiLevelThrottleKey);
                }
                synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                        APIThrottleConstants.API_LIMIT_EXCEEDED);
            }
        }

        //if we need to publish throttled level or some other information we can do it here. Just before return.
        return isThrottled;
    }


    /**
     * HAndle incoming requests and call throttling method to perform throttling.
     *
     * @param messageContext message context object which contains message details.
     * @return return true if message flow need to continue and pass requests to next handler in chain. Else return
     * false to notify error with handler
     */
    public boolean handleRequest(MessageContext messageContext) {

        return doThrottle(messageContext);
    }

    /**
     * This method will handle responses. Usually we do not perform throttling for responses going back to clients.
     * However if we consider bandwidth scenarios we may need to consider handle response and response patch as well
     * because that also contribute data amount pass through server.
     *
     * @param messageContext message context holds message details.
     * @return return true if message flow need to continue and pass requests to next handler in chain. Else return
     * false to notify error with handler
     */
    public boolean handleResponse(MessageContext messageContext) {
        return true;//return doThrottle(messageContext);
    }


    /**
     * Do Throttle method will initialize throttle flow.
     *
     * @param messageContext message context object which contains message details.
     * @return return true if message flow need to continue(message not throttled) and pass requests to next
     * handler in chain. Else return false to notify throttled message.
     */
    private boolean doThrottle(MessageContext messageContext) {
        //long start = System.currentTimeMillis();
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
        //log.info("Total-Time:" + (end - start));
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
