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
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.throttle.core.AccessInformation;
import org.apache.synapse.commons.throttle.core.RoleBasedAccessRateController;
import org.apache.synapse.commons.throttle.core.Throttle;
import org.apache.synapse.commons.throttle.core.ThrottleConfiguration;
import org.apache.synapse.commons.throttle.core.ThrottleConstants;
import org.apache.synapse.commons.throttle.core.ThrottleContext;
import org.apache.synapse.commons.throttle.core.ThrottleException;
import org.apache.synapse.commons.throttle.core.ThrottleFactory;
import org.apache.synapse.commons.throttle.core.factory.ThrottleContextFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;

import javax.xml.stream.XMLStreamException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


/**
 * This class is Handling new throttling check. This class will use inside each API as throttle handler.
 * It will fetch some of data from incoming message and use them to take throttling decisions.
 * To execute this handler requests must go through authentication handler and auth context should be present
 * in message context.
 */
public class ThrottleHandler extends AbstractHandler implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(ThrottleHandler.class);
    private static final String HEADER_X_FORWARDED_FOR = "X-FORWARDED-FOR";
    private volatile Throttle throttle;
    private static volatile ThrottleDataPublisher throttleDataPublisher = null;
    private String policyKeyApplication = null;
    private static final String THROTTLE_MAIN = "THROTTLE_MAIN";
    private static final String INIT_SPIKE_ARREST = "INIT_SPIKE_ARREST";
    private static final String CEP_THROTTLE = "CEP_THROTTLE";
    private static final String HANDLE_THROTTLE_OUT = "HANDLE_THROTTLE_OUT";
    private static final String RESOURCE_THROTTLE = "RESOURCE_THROTTLE";
    private static final String BLOCKED_TEST = "BLOCKED_TEST";

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
    private String productionUnitTime = "1000";
    private String sandboxMaxCount;
    private String productionMaxCount;
    private RoleBasedAccessRateController roleBasedAccessController;

    public ThrottleHandler() {
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
    private boolean doRoleBasedAccessThrottlingWithCEP(MessageContext synCtx, ConfigurationContext cc,
                                                       AuthenticationContext authenticationContext) {

        //Throttle Keys
        //applicationLevelThrottleKey key is combination of {applicationId}:{authorizedUser}
        String applicationLevelThrottleKey;
        //subscriptionLevelThrottleKey key is combination of {applicationId}:{apiContext}:{apiVersion}
        String subscriptionLevelThrottleKey;
        // The key is combination of {apiContext}/ {apiVersion}{resourceUri}:{httpMethod} if policy is user level then authorized user will append at end
        String resourceLevelThrottleKey;
        //apiLevelThrottleKey key is combination of {apiContext}:{apiVersion}
        String apiLevelThrottleKey;

        //Throttle Tiers
        String applicationLevelTier;
        String subscriptionLevelTier;
        String resourceLevelTier = "";
        String apiLevelTier;

        //Other Relevant parameters
        AuthenticationContext authContext = authenticationContext;
        String authorizedUser;

        //Throttled decisions
        boolean isThrottled = false;
        boolean isResourceLevelThrottled = false;
        boolean isApplicationLevelThrottled;
        boolean isSubscriptionLevelThrottled;
        boolean isSubscriptionLevelSpikeThrottled = false;
        boolean isApiLevelThrottled = false;
        boolean isBlockedRequest = false;
        boolean apiLevelThrottledTriggered = false;
        boolean policyLevelUserTriggered = false;
        String ipLevelBlockingKey;
        String appLevelBlockingKey = "";
        boolean stopOnQuotaReach = true;
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        apiContext = apiContext != null ? apiContext : "";
        apiVersion = apiVersion != null ? apiVersion : "";

        String subscriberTenantDomain = "";
        String apiTenantDomain = getTenantDomain();
        List<String> resourceLevelThrottleConditions;
        ConditionGroupDTO[] conditionGroupDTOs;
        String applicationId = authContext.getApplicationId();
        //If Authz context is not null only we can proceed with throttling
        if (authContext != null) {
            authorizedUser = authContext.getUsername();
            //Check if request is blocked. If request is blocked then will not proceed further and
            //inform to client.

            //Do blocking if there are blocking conditions present
            if (getThrottleDataHolder().isBlockingConditionsPresent()) {
                ipLevelBlockingKey = apiTenantDomain + ":" + getClientIp(synCtx);
                appLevelBlockingKey = authContext.getSubscriber() + ":" + authContext.getApplicationName();
                Timer timer = getTimer(MetricManager.name(
                        APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), BLOCKED_TEST));
                Timer.Context context = timer.start();
                isBlockedRequest = getThrottleDataHolder().isRequestBlocked(
                        apiContext, appLevelBlockingKey, authorizedUser, ipLevelBlockingKey);
                context.stop();
            }


            if (isBlockedRequest) {
                String msg = "Request blocked as it violates defined blocking conditions, for API: " + apiContext +
                             " ,application:" + appLevelBlockingKey + " ,user:" + authorizedUser;
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                synCtx.setProperty(APIThrottleConstants.BLOCKED_REASON, msg);
                synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.REQUEST_BLOCKED);
                isThrottled = true;
            } else {
                subscriberTenantDomain = authContext.getSubscriberTenantDomain();
                applicationLevelThrottleKey = applicationId + ":" + authorizedUser;
                apiLevelThrottleKey = apiContext + ":" + apiVersion;
                stopOnQuotaReach = authContext.isStopOnQuotaReach();
                //If request is not blocked then only we perform throttling.
                VerbInfoDTO verbInfoDTO = (VerbInfoDTO) synCtx.getProperty(APIConstants.VERB_INFO_DTO);

                //If API level tier is not present only we should move to resource level tiers.
                if (verbInfoDTO == null) {
                    log.warn("Error while getting throttling information for resource and http verb");
                    return false;
                }

                applicationLevelTier = authContext.getApplicationTier();
                subscriptionLevelTier = authContext.getTier();
                resourceLevelThrottleKey = verbInfoDTO.getRequestKey();
                apiLevelTier = authContext.getApiTier();
                resourceLevelTier = verbInfoDTO.getThrottling();
                //If API level throttle policy is present then it will apply and no resource level policy will apply for it
                if (!StringUtils.isEmpty(apiLevelTier) && !APIConstants.UNLIMITED_TIER.equalsIgnoreCase(apiLevelTier)) {
                    resourceLevelThrottleKey = apiLevelThrottleKey;
                    apiLevelThrottledTriggered = true;
                }


                //If verbInfo is present then only we will do resource level throttling
                if (APIConstants.UNLIMITED_TIER.equalsIgnoreCase(verbInfoDTO.getThrottling()) && !apiLevelThrottledTriggered) {
                    //If unlimited tier throttling will not apply at resource level and pass it
                    if (log.isDebugEnabled()) {
                        log.debug("Resource level throttling set as unlimited and request will pass " +
                                "resource level");
                    }
                } else {
                    if (APIConstants.API_POLICY_USER_LEVEL.equalsIgnoreCase(verbInfoDTO.getApplicableLevel())) {
                        resourceLevelThrottleKey = resourceLevelThrottleKey + "_" + authorizedUser;
                        policyLevelUserTriggered = true;
                    }
                    //If tier is not unlimited only throttling will apply.
                    resourceLevelThrottleConditions = verbInfoDTO.getThrottlingConditions();
                    conditionGroupDTOs = verbInfoDTO.getConditionGroups();

                    Timer timer1 = getTimer(MetricManager.name(
                            APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), RESOURCE_THROTTLE));
                    Timer.Context
                            context1 = timer1.start();

                    if (conditionGroupDTOs != null && conditionGroupDTOs.length > 0) {

                        // Checking Applicability of Conditions is a relatively expensive operation. So we are
                        // going to check it only if the API/Resource is throttled out.
                        if (getThrottleDataHolder().isAPIThrottled
                                (resourceLevelThrottleKey)) {

                            if (log.isDebugEnabled()) {
                                log.debug("Evaluating Conditional Groups");
                            }
                            //Then we will apply resource level throttling
                            List<ConditionGroupDTO> applicableConditions = getThrottleConditionEvaluator()
                                    .getApplicableConditions(synCtx, authContext, conditionGroupDTOs);
                            for (ConditionGroupDTO conditionGroup : applicableConditions) {
                                String combinedResourceLevelThrottleKey = resourceLevelThrottleKey + conditionGroup.getConditionGroupId();

                                if (log.isDebugEnabled()) {
                                    log.debug("Checking condition : " + combinedResourceLevelThrottleKey);
                                }

                                if (getThrottleDataHolder().
                                        isThrottled(combinedResourceLevelThrottleKey)) {
                                    if (!apiLevelThrottledTriggered) {
                                        isResourceLevelThrottled = isThrottled = true;
                                    } else {
                                        isApiLevelThrottled = isThrottled = true;
                                    }
                                    long timestamp = getThrottleDataHolder().
                                            getThrottleNextAccessTimestamp(combinedResourceLevelThrottleKey);
                                    synCtx.setProperty(APIThrottleConstants.THROTTLED_NEXT_ACCESS_TIMESTAMP, timestamp);
                                    break;
                                }
                            }
                        }

                    } else {
                        log.warn("Unable to find throttling information for resource and http verb. Throttling "
                                + "will not apply");
                    }
                    context1.stop();
                }


                if (!isApiLevelThrottled) {

                    Timer timer2 = getTimer(MetricManager.name(
                            APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), RESOURCE_THROTTLE));
                    Timer.Context context2 = timer2.start();

                    //Here check resource level throttled. If throttled then call handler throttled and pass.
                    //Else go for subscription level and application level throttling
                    //if resource level not throttled then move to subscription level
                    if (!isResourceLevelThrottled) {
                        //Subscription Level Throttling
                        subscriptionLevelThrottleKey = authContext.getApplicationId() + ":" + apiContext + ":"
                                                       + apiVersion;
                        isSubscriptionLevelThrottled = getThrottleDataHolder().
                                isThrottled(subscriptionLevelThrottleKey);
                        if (!isSubscriptionLevelThrottled && authContext.getSpikeArrestLimit() > 0) {
                            isSubscriptionLevelSpikeThrottled = isSubscriptionLevelSpike(synCtx, subscriptionLevelThrottleKey);
                        }
                        //if subscription level not throttled then move to application level
                        //Stop on quata reach
                        if (!isSubscriptionLevelThrottled && !isSubscriptionLevelSpikeThrottled) {
                            //Application Level Throttling
                            isApplicationLevelThrottled = getThrottleDataHolder().
                                    isThrottled(applicationLevelThrottleKey);

                            //if application level not throttled means it does not throttled at any level.
                            if (!isApplicationLevelThrottled) {
                                boolean keyTemplatesAvailable = getThrottleDataHolder().isKeyTemplatesPresent();
                                if (!keyTemplatesAvailable || !validateCustomPolicy(authorizedUser, applicationLevelThrottleKey,
                                                                                    subscriptionLevelThrottleKey, apiLevelThrottleKey, subscriptionLevelThrottleKey, apiContext,
                                                                                    apiVersion, subscriberTenantDomain, apiTenantDomain, applicationId,
                                                                                    getThrottleDataHolder().getKeyTemplateMap(), synCtx)) {
                                    //Pass message context and continue to avoid performance issue.
                                    //Did not throttled at any level. So let message go and publish event.
                                    //publish event to Global Policy Server
                                    if (isHardLimitThrottled(synCtx, authContext, apiContext, apiVersion)) {
                                        isThrottled = true;

                                    } else {
                                        throttleDataPublisher.publishNonThrottledEvent(
                                                applicationLevelThrottleKey, applicationLevelTier,
                                                apiLevelThrottleKey, apiLevelTier,
                                                subscriptionLevelThrottleKey, subscriptionLevelTier,
                                                resourceLevelThrottleKey, resourceLevelTier,
                                                authorizedUser, apiContext, apiVersion, subscriberTenantDomain,
                                                apiTenantDomain, applicationId, synCtx, authContext);
                                    }
                                } else {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Request throttled at custom throttling");
                                    }
                                    synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                                                       APIThrottleConstants.CUSTOM_POLICY_LIMIT_EXCEED);
                                    isThrottled = true;

                                }

                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Request throttled at application level for throttle key" +
                                              applicationLevelThrottleKey);
                                }
                                synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                                                   APIThrottleConstants.APPLICATION_LIMIT_EXCEEDED);
                                long timestamp = getThrottleDataHolder().getThrottleNextAccessTimestamp(applicationLevelThrottleKey);
                                synCtx.setProperty(APIThrottleConstants.THROTTLED_NEXT_ACCESS_TIMESTAMP, timestamp);
                                isThrottled = isApplicationLevelThrottled = true;
                            }
                        } else {
                            if (!stopOnQuotaReach) {
                                // This means that we are allowing the requests to continue even after the throttling
                                // limit has reached.
                                if (synCtx.getProperty(APIConstants.API_USAGE_THROTTLE_OUT_PROPERTY_KEY) == null) {
                                    synCtx.setProperty(APIConstants.API_USAGE_THROTTLE_OUT_PROPERTY_KEY, Boolean.TRUE);
                                }
                                isThrottled = false;
                                if (log.isDebugEnabled()) {
                                    log.debug("Request throttled at subscription level for throttle key" +
                                              subscriptionLevelThrottleKey + ". But subscription policy " + subscriptionLevelTier + " allows to continue to serve requests");
                                }
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Request throttled at subscription level for throttle key" +
                                              subscriptionLevelThrottleKey);
                                }
                                if (!isSubscriptionLevelSpikeThrottled) {
                                    long timestamp = getThrottleDataHolder().getThrottleNextAccessTimestamp(subscriptionLevelThrottleKey);
                                    synCtx.setProperty(APIThrottleConstants.THROTTLED_NEXT_ACCESS_TIMESTAMP, timestamp);
                                    synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.API_LIMIT_EXCEEDED);
                                    synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                                                       APIThrottleConstants.SUBSCRIPTION_LIMIT_EXCEEDED);
                                }
                                isThrottled = true;
                            }
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Request throttled at resource level for throttle key" +
                                      verbInfoDTO.getRequestKey());
                        }
                        //is throttled and resource level throttling
                        synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                                           APIThrottleConstants.RESOURCE_LIMIT_EXCEEDED);
                    }
                    context2.stop();
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Request throttled at api level for throttle key" + apiLevelThrottleKey);
                        if (policyLevelUserTriggered) {
                            log.debug("Request has throttled out in the user level for the throttle key" + apiLevelThrottleKey);
                        }
                    }
                    synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                                       APIThrottleConstants.API_LIMIT_EXCEEDED);
                }
            }

        }

        //if we need to publish throttled level or some other information we can do it here. Just before return.
        return isThrottled;
    }

    protected ThrottleConditionEvaluator getThrottleConditionEvaluator() {
        return ThrottleConditionEvaluator.getInstance();
    }

    protected ThrottleDataHolder getThrottleDataHolder() {
        return ServiceReferenceHolder.getInstance().getThrottleDataHolder();
    }

    protected String getTenantDomain() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }


    /**
     * HAndle incoming requests and call throttling method to perform throttling.
     *
     * @param messageContext message context object which contains message details.
     * @return return true if message flow need to continue and pass requests to next handler in chain. Else return
     * false to notify error with handler
     */
    public boolean handleRequest(MessageContext messageContext) {

        Timer timer3 = getTimer(MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), THROTTLE_MAIN));
        Timer.Context context3 = timer3.start();
        long executionStartTime = System.currentTimeMillis();
        try {
            return doThrottle(messageContext);
        } finally {
            messageContext.setProperty(APIMgtGatewayConstants.THROTTLING_LATENCY,
                    System.currentTimeMillis() - executionStartTime);
            context3.stop();
        }
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
        return true;
    }


    /**
     * Do Throttle method will initialize throttle flow.
     *
     * @param messageContext message context object which contains message details.
     * @return return true if message flow need to continue(message not throttled) and pass requests to next
     * handler in chain. Else return false to notify throttled message.
     */
    private boolean doThrottle(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        ConfigurationContext cc = axis2MC.getConfigurationContext();
        AuthenticationContext authenticationContext = APISecurityUtils.getAuthenticationContext(messageContext);

        if (authenticationContext != null && authenticationContext.getSpikeArrestLimit() > 0) {
            Timer timer = getTimer(MetricManager.name(
                    APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), INIT_SPIKE_ARREST));
            Timer.Context context = timer.start();
            initThrottleForSubscriptionLevelSpikeArrest(messageContext, authenticationContext);
            context.stop();
        }
        boolean isThrottled = false;

        if (!messageContext.isResponse()) {
            //org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
            //      getAxis2MessageContext();
            //ConfigurationContext cc = axis2MC.getConfigurationContext();
            Timer timer = getTimer(MetricManager.name(
                    APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), CEP_THROTTLE));
            Timer.Context context = timer.start();
            isThrottled = doRoleBasedAccessThrottlingWithCEP(messageContext, cc, authenticationContext);
            context.stop();
        }
        if (isThrottled) {
            Timer timer = getTimer(MetricManager.name(
                    APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), HANDLE_THROTTLE_OUT));
            Timer.Context context = timer.start();
            handleThrottleOut(messageContext);
            context.stop();
            return false;
        }
        return true;
    }

    protected Timer getTimer(String name) {
        return MetricManager.timer(Level.INFO, name);
    }


    private OMElement getFaultPayload(int throttleErrorCode, String message, String description,
                                      String nextAccessTimeValue) {
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
        if (!StringUtils.isEmpty(nextAccessTimeValue)) {
            OMElement nextAccessTime = fac.createOMElement("nextAccessTime", ns);
            nextAccessTime.setText(nextAccessTimeValue);
            payload.addChild(nextAccessTime);
        }
        return payload;
    }

    private void handleThrottleOut(MessageContext messageContext) {

        String errorMessage = null;
        String errorDescription = null;
        int errorCode = -1;
        int httpErrorCode = -1;
        long timestamp = 0;
        String nextAccessTimeString = "";
        if (APIThrottleConstants.HARD_LIMIT_EXCEEDED.equals(
                messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.HARD_LIMIT_EXCEEDED_ERROR_CODE;
            errorMessage = "API Limit Reached";
            errorDescription = "API not accepting requests";
            // It it's a hard limit exceeding, we tell it as service not being available.
            httpErrorCode = HttpStatus.SC_SERVICE_UNAVAILABLE;
        } else if (APIThrottleConstants.REQUEST_BLOCKED.equals(
                messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.BLOCKED_ERROR_CODE;
            errorMessage = "Message blocked";
            // By default we send a 429 response back
            httpErrorCode = HttpStatus.SC_FORBIDDEN;
            errorDescription = "You have been blocked from accessing the resource";
        } else if (APIThrottleConstants.API_LIMIT_EXCEEDED
                .equals(messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.API_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
            nextAccessTimeString = getNextAccessTimeString(messageContext);
        } else if (APIThrottleConstants.RESOURCE_LIMIT_EXCEEDED
                .equals(messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.RESOURCE_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
            nextAccessTimeString = getNextAccessTimeString(messageContext);
        } else if (APIThrottleConstants.CUSTOM_POLICY_LIMIT_EXCEED
                .equals(messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.CUSTOM_POLICY_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
            nextAccessTimeString = getNextAccessTimeString(messageContext);
        } else if (APIThrottleConstants.SUBSCRIPTION_LIMIT_EXCEEDED
                .equals(messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.SUBSCRIPTION_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
            nextAccessTimeString = getNextAccessTimeString(messageContext);
        } else if (APIThrottleConstants.SUBSCRIPTON_BURST_LIMIT_EXCEEDED
                .equals(messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.SUBSCRIPTION_BURST_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
            nextAccessTimeString = getNextAccessTimeString(messageContext);
        } else {
            errorCode = APIThrottleConstants.APPLICATION_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
            nextAccessTimeString = getNextAccessTimeString(messageContext);
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
            Utils.setFaultPayload(messageContext, getFaultPayload(errorCode, errorMessage, errorDescription, nextAccessTimeString));
        } else {
            if (!StringUtils.isEmpty(nextAccessTimeString)) {
                errorDescription += errorDescription + " .You can access API after " + nextAccessTimeString;
            }
            setSOAPFault(messageContext, errorMessage, errorDescription);
        }

        sendFault(messageContext, httpErrorCode);
    }

    protected void sendFault(MessageContext messageContext, int httpErrorCode) {
        Utils.sendFault(messageContext, httpErrorCode);
    }

    protected void setSOAPFault(MessageContext messageContext, String errorMessage, String errorDescription) {
        Utils.setSOAPFault(messageContext, "Server", errorMessage, errorDescription);
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


    private OMElement createSpikeArrestSubscriptionLevelPolicy(String policyName, int maxCount, int unitTime) {

        OMElement parsedPolicy = null;
        StringBuilder policy = new StringBuilder(APIThrottleConstants.WS_THROTTLE_POLICY_HEADER);
        if (maxCount != 0 && unitTime != 0) {
            policy.append(createPolicyForRole(policyName, Integer.toString(unitTime), Integer.toString(maxCount)));
        }
        policy.append(APIThrottleConstants.WS_THROTTLE_POLICY_BOTTOM);
        try {
            parsedPolicy = AXIOMUtil.stringToOM(policy.toString());
        } catch (XMLStreamException e) {
            log.error("Error occurred while creating policy file for Hard Throttling.", e);
        }
        return parsedPolicy;
    }

    /**
     * This method will intialize subscription level throttling context and throttle object.
     * This method need to be called for each and every request of spike arrest is enabled.
     * If throttle context for incoming message is already created method will do nothing. Else
     * it will create throttle object and context.
     *
     * @param synCtx synapse messaginitThrottleForSubscriptionLevelSpikeArreste context which contains message data
     */
    private void initThrottleForSubscriptionLevelSpikeArrest(MessageContext synCtx,
                                                             AuthenticationContext authenticationContext) {
        AuthenticationContext authContext = authenticationContext;
        policyKey = authContext.getTier();
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String subscriptionLevelThrottleKey = authContext.getApplicationId() + ":" + apiContext + ":" + apiVersion;
        int maxRequestCount = authContext.getSpikeArrestLimit();
        if (maxRequestCount != 0) {
            String unitTime = authContext.getSpikeArrestUnit();
            int spikeArrestWindowUnitTime;
            if (APIThrottleConstants.MIN.equalsIgnoreCase(unitTime)) {
                spikeArrestWindowUnitTime = 60000;
            } else {
                spikeArrestWindowUnitTime = 1000;
            }
            try {
                synchronized (this) {
                    if (throttle == null) {
                        OMElement spikeArrestSubscriptionLevelPolicy = createSpikeArrestSubscriptionLevelPolicy(
                                subscriptionLevelThrottleKey, maxRequestCount, spikeArrestWindowUnitTime);
                        if (spikeArrestSubscriptionLevelPolicy != null) {
                            throttle = ThrottleFactory.createMediatorThrottle(
                                    PolicyEngine.getPolicy(spikeArrestSubscriptionLevelPolicy));
                        }
                    } else {
                        if (throttle.getThrottleContext(subscriptionLevelThrottleKey) == null) {
                            OMElement spikeArrestSubscriptionLevelPolicy = createSpikeArrestSubscriptionLevelPolicy(
                                    subscriptionLevelThrottleKey, maxRequestCount, spikeArrestWindowUnitTime);
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
                log.error("Error while initializing throttling object for subscription level spike arrest policy" +
                          e.getMessage());
            }
        }
    }

    /**
     * This method will intialize subscription level throttling context and throttle object.
     * This method need to be called for each and every request of spike arrest is enabled.
     * If throttle context for incoming message is already created method will do nothing. Else
     * it will create throttle object and context.
     */
    private void initThrottleForHardLimitThrottling() {
        OMElement hardThrottlingPolicy = createHardThrottlingPolicy();
        if (hardThrottlingPolicy != null) {
            Throttle tempThrottle;
            try {
                tempThrottle = ThrottleFactory.createMediatorThrottle(
                        PolicyEngine.getPolicy(hardThrottlingPolicy));
                ThrottleConfiguration newThrottleConfig = tempThrottle.getThrottleConfiguration(ThrottleConstants
                                                                                                        .ROLE_BASED_THROTTLE_KEY);
                ThrottleContext hardThrottling = ThrottleContextFactory.createThrottleContext(ThrottleConstants
                                                                                                      .ROLE_BASE,
                                                                                              newThrottleConfig);
                tempThrottle.addThrottleContext(APIThrottleConstants.HARD_THROTTLING_CONFIGURATION, hardThrottling);
                if (throttle != null) {
                    throttle.addThrottleContext(APIThrottleConstants.HARD_THROTTLING_CONFIGURATION, hardThrottling);
                } else {
                    throttle = tempThrottle;
                }
            } catch (ThrottleException e) {
                log.error("Error occurred while creating policy file for Hard Throttling.", e);
            }
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
        ThrottleContext subscriptionLevelSpikeArrestThrottleContext = throttle.getThrottleContext(throttleKey);
        try {
            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);

            if (subscriptionLevelSpikeArrestThrottleContext != null && authContext.getKeyType() != null) {
                AccessInformation info = null;
                if (isClusteringEnabled()) {
                    org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) synCtx).
                            getAxis2MessageContext();
                    ConfigurationContext cc = axis2MC.getConfigurationContext();
                    subscriptionLevelSpikeArrestThrottleContext.setConfigurationContext(cc);
                }

                subscriptionLevelSpikeArrestThrottleContext.setThrottleId(id + APIThrottleConstants.SUBSCRIPTION_BURST_LIMIT);
                info = getAccessInformation(subscriptionLevelSpikeArrestThrottleContext, throttleKey, throttleKey);
                if (log.isDebugEnabled()) {
                    log.debug("Throttle by subscription level burst limit " + throttleKey);
                    log.debug("Allowed = " + (info != null ? info.isAccessAllowed() : "false"));
                }

                if (info != null && !info.isAccessAllowed()) {
                    synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.SUBSCRIPTON_BURST_LIMIT_EXCEEDED);
                    log.debug("Subscription level burst control limit exceeded for key " + throttleKey);
                    return true;
                }
            }

        } catch (ThrottleException e) {
            log.warn("Exception occurred while performing role " +
                     "based throttling", e);
            synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.HARD_LIMIT_EXCEEDED);
            return false;
        }
        return false;
    }

    protected boolean isClusteringEnabled() {
        return GatewayUtils.isClusteringEnabled();
    }

    /**
     * Validate custom policy is handle by this method. This method call is an expensive operation
     * and should not enabled by default. If we enabled this policy then all APIs available in system
     * will have to go through this check.
     *
     * @return
     */
    public boolean validateCustomPolicy(String userID, String appKey, String resourceKey, String apiKey,
                                        String subscriptionKey, String apiContext, String apiVersion, String appTenant,
                                        String apiTenant, String appId, Map<String, String> keyTemplateMap,
                                        MessageContext messageContext) {
        if (keyTemplateMap != null && keyTemplateMap.size() > 0) {
            for (String key : keyTemplateMap.keySet()) {
                key = key.replaceAll("\\$resourceKey", resourceKey);
                key = key.replaceAll("\\$userId", userID);
                key = key.replaceAll("\\$apiContext", apiContext);
                key = key.replaceAll("\\$apiVersion", apiVersion);
                key = key.replaceAll("\\$appTenant", appTenant);
                key = key.replaceAll("\\$apiTenant", apiTenant);
                key = key.replaceAll("\\$appId", appId);
                if (getThrottleDataHolder().isThrottled(key)) {
                    long timestamp = getThrottleDataHolder().getThrottleNextAccessTimestamp(key);
                    messageContext.setProperty(APIThrottleConstants.THROTTLED_NEXT_ACCESS_TIMESTAMP, timestamp);
                    return true;
                }
            }
        }
        return false;
    }

    private OMElement createHardThrottlingPolicy() {

        if (StringUtils.isEmpty(productionMaxCount) &&
            StringUtils.isEmpty(sandboxMaxCount)) {
            return null;
        }

        OMElement parsedPolicy = null;
        StringBuilder policy = new StringBuilder(APIThrottleConstants.WS_THROTTLE_POLICY_HEADER);

        if (productionMaxCount != null && productionUnitTime != null) {
            policy.append(createPolicyForRole(APIThrottleConstants.PRODUCTION_HARD_LIMIT, productionUnitTime,
                                              productionMaxCount));
        }

        if (sandboxMaxCount != null && sandboxUnitTime != null) {
            policy.append(createPolicyForRole(APIThrottleConstants.SANDBOX_HARD_LIMIT, sandboxUnitTime,
                                              sandboxMaxCount));
        }

        policy.append(APIThrottleConstants.WS_THROTTLE_POLICY_BOTTOM);
        try {
            parsedPolicy = AXIOMUtil.stringToOM(policy.toString());
        } catch (XMLStreamException e) {
            log.error("Error occurred while creating policy file for Hard Throttling.", e);
        }
        return parsedPolicy;
    }

    private String createPolicyForRole(String roleId, String unitTime, String maxCount) {
        return "<wsp:Policy>\n" +
               "     <throttle:ID throttle:type=\"ROLE\">" + roleId + "</throttle:ID>\n" +
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

    private boolean isHardLimitThrottled(MessageContext synCtx, AuthenticationContext authContext, String apiContext,
                                         String apiVersion) {
        boolean status = false;
        if (StringUtils.isNotEmpty(sandboxMaxCount) || StringUtils.isNotEmpty(productionMaxCount)) {
            ThrottleContext hardThrottleContext = throttle.getThrottleContext(APIThrottleConstants.HARD_THROTTLING_CONFIGURATION);
            try {
                org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
                ConfigurationContext cc = axis2MC.getConfigurationContext();
                apiContext = apiContext != null ? apiContext : "";
                apiVersion = apiVersion != null ? apiVersion : "";

                if (hardThrottleContext != null && authContext.getKeyType() != null) {
                    String throttleKey = apiContext + ':' + apiVersion + ':' + authContext.getKeyType();
                    AccessInformation info = null;
                    if (isClusteringEnabled()) {
                        hardThrottleContext.setConfigurationContext(cc);
                    }

                    if (APIConstants.API_KEY_TYPE_PRODUCTION.equals(authContext.getKeyType())) {
                            hardThrottleContext.setThrottleId(id + APIThrottleConstants.PRODUCTION_HARD_LIMIT);
                        info = getAccessInformation(hardThrottleContext, throttleKey, APIThrottleConstants.PRODUCTION_HARD_LIMIT);
                    } else if (APIConstants.API_KEY_TYPE_SANDBOX.equals(authContext.getKeyType())) {
                        hardThrottleContext.setThrottleId(id + APIThrottleConstants.SANDBOX_HARD_LIMIT);
                        info = getAccessInformation(hardThrottleContext, throttleKey, APIThrottleConstants.SANDBOX_HARD_LIMIT);
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("Throttle by hard limit " + throttleKey);
                        log.debug("Allowed = " + (info != null ? info.isAccessAllowed() : "false"));
                    }

                    if (info != null && !info.isAccessAllowed()) {
                        synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants
                                .HARD_LIMIT_EXCEEDED);
                        log.info("Hard Throttling limit exceeded.");
                        status = true;
                    }
                }

            } catch (ThrottleException e) {
                log.warn("Exception occurred while performing role " +
                         "based throttling", e);
                synCtx.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.HARD_LIMIT_EXCEEDED);
                status = true;
            }
        }
        return status;
    }

    protected AccessInformation getAccessInformation(ThrottleContext hardThrottleContext, String throttleKey, String productionHardLimit) throws ThrottleException {
        return roleBasedAccessController.canAccess(hardThrottleContext, throttleKey,
                productionHardLimit);
    }

    public String getSandboxMaxCount() {
        return sandboxMaxCount;
    }

    public void setSandboxMaxCount(String sandboxMaxCount) {
        this.sandboxMaxCount = sandboxMaxCount;
    }

    public String getProductionMaxCount() {
        return productionMaxCount;
    }

    public void setProductionMaxCount(String productionMaxCount) {
        this.productionMaxCount = productionMaxCount;
    }

    public String getSandboxUnitTime() {
        return sandboxUnitTime;
    }

    public void setSandboxUnitTime(String sandboxUnitTime) {
        this.sandboxUnitTime = sandboxUnitTime;
    }

    public String getProductionUnitTime() {
        return productionUnitTime;
    }

    public void setProductionUnitTime(String productionUnitTime) {
        this.productionUnitTime = productionUnitTime;
    }

    public void init(SynapseEnvironment synapseEnvironment) {
        initThrottleForHardLimitThrottling();
    }

    private String getNextAccessTimeString(MessageContext messageContext) {
        Object timestampOb = messageContext.getProperty(APIThrottleConstants.THROTTLED_NEXT_ACCESS_TIMESTAMP);
        if (timestampOb != null) {
            long timestamp = (Long) timestampOb;
            SimpleDateFormat formatUTC = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ssZ");
            formatUTC.setTimeZone(TimeZone.getTimeZone(APIThrottleConstants.UTC));
            Date date = new Date(timestamp);
            String nextAccessTimeString = formatUTC.format(date) + " " + APIThrottleConstants.UTC;
            messageContext.setProperty(APIThrottleConstants.THROTTLED_NEXT_ACCESS_TIME, nextAccessTimeString);
            return nextAccessTimeString;
        }
        return null;
    }

    public void destroy() {

    }
}
