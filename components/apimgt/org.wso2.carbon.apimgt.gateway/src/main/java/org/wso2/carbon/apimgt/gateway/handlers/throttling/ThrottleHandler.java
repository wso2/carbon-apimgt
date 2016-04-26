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
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.neethi.PolicyEngine;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.throttle.core.*;
import org.apache.synapse.commons.throttle.core.factory.ThrottleContextFactory;
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
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;

import org.wso2.carbon.databridge.agent.DataPublisher;

import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.stream.XMLStreamException;

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
    private static final String HEADER_X_FORWARDED_FOR = "X-FORWARDED-FOR";
    private volatile Throttle throttle;

    private static volatile DataPublisher dataPublisher = null;

    private static volatile ThrottleDataPublisher throttleDataPublisher = null;

    private String policyKeyApplication = null;

    private boolean subscriptionLevelSpikeArrestEnabled;
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
    private String sandboxUnitTime = "1000";

    private String productionUnitTime = "60000";

    private String sandboxMaxCount;

    private String maxCount;
    private RoleBasedAccessRateController roleBasedAccessController;

    //Throttle Handler rename
    public ThrottleHandler() {
        subscriptionLevelSpikeArrestEnabled = ServiceReferenceHolder.getInstance().getThrottleProperties()
                .isEnabledSubscriptionLevelSpikeArrest();
        if (log.isDebugEnabled()) {
            log.debug("Throttle Handler initialized");
        }
        this.roleBasedAccessController = new RoleBasedAccessRateController();

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
        String resourceLevelThrottleKey="";
        String apiLevelThrottleKey = "";

        //Throttle Tiers
        String applicationLevelTier;
        String subscriptionLevelTier;
        String resourceLevelTier ="";
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
        boolean isBlockedRequest;
        String ipLevelBlockingKey = null;
        String appLevelBlockingKey = null;
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        apiContext = apiContext != null ? apiContext : "";
        apiVersion = apiVersion != null ? apiVersion : "";
        List<String> resourceLevelThrottleConditions;


        //If Authz context is not null only we can proceed with throttling
        if (authContext != null) {
            authorizedUser = authContext.getUsername();
            applicationLevelThrottleKey = authContext.getApplicationId() + ":" + authorizedUser;
            //Following throttle data list can be use to hold throttle data and api level throttle key
            //should be its first element.
            //if ((authContext.getThrottlingDataList() != null) && (authContext.getThrottlingDataList().get(0) != null)) {
                apiLevelThrottleKey = apiContext + ":" +apiVersion; //authContext.getApiTier();
                //Check if request is blocked. If request is blocked then will not proceed further and
                //inform to client.
                //TODO handle blocked and throttled requests separately.

                ipLevelBlockingKey = MultitenantUtils.getTenantDomain(authorizedUser) + ":" + getClientIp(synCtx);
                appLevelBlockingKey = authContext.getSubscriber() + ":" + authContext.getApplicationName();
            //}
            isBlockedRequest = ServiceReferenceHolder.getInstance().getThrottleDataHolder().isRequestBlocked(
                   apiContext, appLevelBlockingKey, authorizedUser,ipLevelBlockingKey);
            if (isBlockedRequest) {
                String msg = "Request blocked as it violates defined blocking conditions, for API:" + apiContext +
                        " ,application:" + appLevelBlockingKey + " ,user:" + authorizedUser;
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                synCtx.setProperty(APIThrottleConstants.BLOCKED_REASON, msg);
                isThrottled = true;
            } else {
                //If request is not blocked then only we perform throttling.
                VerbInfoDTO verbInfoDTO = (VerbInfoDTO) synCtx.getProperty(APIConstants.VERB_INFO_DTO);
                applicationLevelTier = authContext.getApplicationTier();
                subscriptionLevelTier = authContext.getTier();
                resourceLevelThrottleKey = verbInfoDTO.getRequestKey();
                apiLevelTier = authContext.getApiTier();
                //If API level throttle policy is present then it will apply and no resource level policy will apply
                // for it
                if (apiLevelTier != null && apiLevelTier.length() > 0 && apiLevelThrottleKey.length() > 0) {
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
                                log.debug("Resource level throttling set as unlimited and request will pass " +
                                        "resource level");
                            }
                        } else {
                            //If tier is not unlimited only throttling will apply.
                            resourceLevelThrottleConditions = verbInfoDTO.getThrottlingConditions();
                            if (resourceLevelThrottleConditions != null && resourceLevelThrottleConditions.size() > 0) {
                                //Then we will apply resource level throttling
                                resourceLevelThrottleKey = verbInfoDTO.getRequestKey();
                                for (String conditionString : resourceLevelThrottleConditions) {
                                    resourceLevelThrottleKey = verbInfoDTO.getRequestKey() + conditionString;
                                    resourceLevelTier = verbInfoDTO.getThrottling();
                                    if (ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                                            isThrottled(resourceLevelThrottleKey)) {
                                        isResourceLevelThrottled = isThrottled = true;
                                        break;
                                    }
                                }

                            } else {
                                log.warn("Unable to find throttling information for resource and http verb. Throttling "
                                        + "will not apply");
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
                        subscriptionLevelThrottleKey = authContext.getApplicationId() + ":" + apiContext + ":"
                                + apiVersion;
                        isSubscriptionLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                                isThrottled(subscriptionLevelThrottleKey);
                        if (subscriptionLevelSpikeArrestEnabled) {
                            isSubscriptionLevelThrottled = isSubscriptionLevelSpike(synCtx, subscriptionLevelThrottleKey);
                        }
                        //if subscription level not throttled then move to application level
                        //Stop on quata reach
                        if (!isSubscriptionLevelThrottled) {
                            //Application Level Throttling
                            isApplicationLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                                    isThrottled(applicationLevelThrottleKey);

                            //if application level not throttled means it does not throttled at any level.
                            if (!isApplicationLevelThrottled) {
                                //Pass message context and continue to avaoid peformance issue.
                                //Did not throttled at any level. So let message go and publish event.
                                //publish event to Global Policy Server
                                throttleDataPublisher.publishNonThrottledEvent(
                                        applicationLevelThrottleKey, applicationLevelTier,
                                        apiLevelThrottleKey, apiLevelTier,
                                        subscriptionLevelThrottleKey, subscriptionLevelTier,
                                        resourceLevelThrottleKey, resourceLevelTier,
                                        authorizedUser, synCtx);

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
                            log.debug("Request throttled at resource level for throttle key" +
                                    verbInfoDTO.getRequestKey());
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

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        ConfigurationContext cc = axis2MC.getConfigurationContext();
        if (subscriptionLevelSpikeArrestEnabled) {
            initThrottleForSubscriptionLevelSpikeArrest(messageContext);
        }
        boolean isThrottled = false;
        if (!messageContext.isResponse()) {
            //org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
            //      getAxis2MessageContext();
            //ConfigurationContext cc = axis2MC.getConfigurationContext();
            long start = System.nanoTime();
            isThrottled = doRoleBasedAccessThrottlingWithCEP(messageContext, cc);
            log.info("===============================================Time:" + (System.nanoTime() - start));

        }
        if (isThrottled) {

            handleThrottleOut(messageContext);
            return false;
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

    private String getClientIp(MessageContext synCtx) {
        String clientIp;
        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        Map headers =
                (Map) (axis2MsgContext).getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String xForwardForHeader = (String) headers.get(HEADER_X_FORWARDED_FOR);
        if (!StringUtils.isEmpty(xForwardForHeader)) {
            clientIp = xForwardForHeader;
            int idx = xForwardForHeader.indexOf(',');
            if (idx > -1) {
                clientIp = clientIp.substring(0, idx);
            }
        } else {
            clientIp = (String) axis2MsgContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }
        return clientIp;
    }


    private OMElement createSpikeArrestSubscriptionLevelPolicy(String policyName, String maxCount, String time) {
        if (maxCount == null) {
            return null;
        }

        OMElement parsedPolicy = null;

        StringBuilder policy = new StringBuilder("<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\" " +
                "xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
                "    <throttle:MediatorThrottleAssertion>\n");

        if (maxCount != null && time != null) {
            policy.append(createSpikeArrestPolicy(policyName, time, maxCount));
        }

        policy.append("    </throttle:MediatorThrottleAssertion>\n" +
                "</wsp:Policy>");
        try {
            parsedPolicy = AXIOMUtil.stringToOM(policy.toString());
        } catch (XMLStreamException e) {
            log.error("Error occurred while creating policy file for Hard Throttling.", e);
        }
        return parsedPolicy;
    }

    /**
     * This method will create policy string for given policy name, unit time and max count.
     * This will simple return policy string according to WS policy specification.
     *
     * @param policyName policy name of given policy
     * @param unitTime   unit time in milli seconds
     * @param maxCount   maximum request count within given time window
     * @return policy string for created policy.
     */
    private String createSpikeArrestPolicy(String policyName, String unitTime, String maxCount) {
        return "<wsp:Policy>\n" +
                "     <throttle:ID throttle:type=\"ROLE\">" + policyName + "</throttle:ID>\n" +
                "            <wsp:Policy>\n" +
                "                <throttle:Control>\n" +
                "                    <wsp:Policy>\n" +
                "                        <throttle:MaximumCount>" + maxCount + "</throttle:MaximumCount>\n" +
                "                        <throttle:UnitTime>" + unitTime + "</throttle:UnitTime>\n" +
                "                    </wsp:Policy>\n" +
                "                </throttle:Control>\n" +
                "            </wsp:Policy>\n" +
                " </wsp:Policy>\n";
    }

    /**
     * This method will intialize subscription level throttling context and throttle object.
     * This method need to be called for each and every request of spike arrest is enabled.
     * If throttle context for incoming message is already created method will do nothing. Else
     * it will create throttle object and context.
     *
     * @param synCtx synapse message context which contains message data
     */
    private void initThrottleForSubscriptionLevelSpikeArrest(MessageContext synCtx) {
        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);
        policyKey = authContext.getTier();
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String subscriptionLevelThrottleKey = authContext.getApplicationId() + ":" + apiContext + ":"
                + apiVersion;
        String unitTime = "30000";
        String maxRequestCount = "5";
        try {
            synchronized (this) {
                if (throttle == null) {
                    OMElement spikeArrestSubscriptionLevelPolicy = createSpikeArrestSubscriptionLevelPolicy(
                            subscriptionLevelThrottleKey, maxRequestCount, unitTime);
                    if (spikeArrestSubscriptionLevelPolicy != null) {
                        throttle = ThrottleFactory.createMediatorThrottle(
                                PolicyEngine.getPolicy(spikeArrestSubscriptionLevelPolicy));
                    }
                } else {
                    if (throttle.getThrottleContext(subscriptionLevelThrottleKey) == null) {
                        OMElement spikeArrestSubscriptionLevelPolicy = createSpikeArrestSubscriptionLevelPolicy(
                                subscriptionLevelThrottleKey, maxRequestCount, unitTime);
                        if (spikeArrestSubscriptionLevelPolicy != null) {
                            Throttle tempThrottle = ThrottleFactory.createMediatorThrottle(
                                    PolicyEngine.getPolicy(spikeArrestSubscriptionLevelPolicy));
                            ThrottleConfiguration newThrottleConfig = tempThrottle.
                                    getThrottleConfiguration(ThrottleConstants.ROLE_BASED_THROTTLE_KEY);
                            ThrottleContext subscriptionLevelSpikeThrottle = ThrottleContextFactory.
                                    createThrottleContext(ThrottleConstants.ROLE_BASE, newThrottleConfig);
                            throttle.addThrottleContext(subscriptionLevelThrottleKey, subscriptionLevelSpikeThrottle);
                        }
                    }


                }
            }
        } catch (ThrottleException e) {
            log.error("Error while initializing throttling object for subscription level spike arrest policy"
                    +e.getMessage());
        }

    }

    /**
     * This method will check if coming request is hitting subscription level spikes.
     *
     * @param synCtx      synapse message context which contains message data
     * @param throttleKey subscription level throttle key.
     * @return true if message is throttled else false
     */
    public boolean isSubscriptionLevelSpike(MessageContext synCtx, String throttleKey) {
        ThrottleContext resourceLevelSpikeArrestThrottleContext = throttle.getThrottleContext(throttleKey);
        try {
            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);

            if (resourceLevelSpikeArrestThrottleContext != null && authContext.getKeyType() != null) {
                AccessInformation info = null;
                if (GatewayUtils.isClusteringEnabled()) {
                    org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) synCtx).
                            getAxis2MessageContext();
                    ConfigurationContext cc = axis2MC.getConfigurationContext();
                    resourceLevelSpikeArrestThrottleContext.setConfigurationContext(cc);
                }

                resourceLevelSpikeArrestThrottleContext.setThrottleId(id + APIThrottleConstants.PRODUCTION_HARD_LIMIT);
                info = roleBasedAccessController.canAccess(resourceLevelSpikeArrestThrottleContext, throttleKey,
                        throttleKey);
                System.out.println(resourceLevelSpikeArrestThrottleContext.getCallerContext(throttleKey).getLocalCounter());
                System.out.println(resourceLevelSpikeArrestThrottleContext.getCallerContext(throttleKey).getGlobalCounter());
                System.out.println(resourceLevelSpikeArrestThrottleContext.getCallerContext(throttleKey).getRoleId());
                System.out.println(info.isAccessAllowed() + "     " + info.getFaultReason());
                if (log.isDebugEnabled()) {
                    log.debug("Throttle by hard limit " + throttleKey);
                    log.debug("Allowed = " + (info != null ? info.isAccessAllowed() : "false"));
                }

                if (info != null && !info.isAccessAllowed()) {
                    synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.HARD_LIMIT_EXCEEDED);
                    log.info("Hard Throttling limit exceeded.");
                    return false;
                }
            }

        } catch (ThrottleException e) {
            log.warn("Exception occurred while performing role " +
                    "based throttling", e);
            synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.HARD_LIMIT_EXCEEDED);
            return false;
        }
        return true;
    }
}
