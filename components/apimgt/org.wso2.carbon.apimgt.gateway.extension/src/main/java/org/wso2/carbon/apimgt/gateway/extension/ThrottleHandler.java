/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.gateway.extension;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleConditionEvaluator;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.constants.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.throttling.dto.AuthenticationContextDTO;
import org.wso2.carbon.apimgt.gateway.throttling.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.gateway.throttling.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.gateway.throttling.temp.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.gateway.throttling.utils.StringUtils;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.handler.MessagingHandler;

import java.util.List;
import java.util.Map;

/**
 * Messaging Handler implementation to log transport header in specified points
 */
@Component(
        name = "org.wso2.carbon.apimgt.gateway.extension.ThrottleHandler",
        immediate = true,
        service = MessagingHandler.class)

public class ThrottleHandler implements MessagingHandler {

    private static final Logger log = LoggerFactory.getLogger(ThrottleHandler.class);
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

    public ThrottleHandler() {
        if (log.isDebugEnabled()) {
            log.debug("Throttle Handler initialized");
        }

        initDataPublisher();
    }


    @Override
    /**
     * request should have a header named "hello_continue" to pass the validation
     */
    public boolean validateRequestContinuation(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        log.info("Message is inside validateRequestContinuation ");
        return true;
    }

    @Override
    public void invokeAtSourceConnectionInitiation(String s) {
    }

    @Override
    public void invokeAtSourceConnectionTermination(String s) {
    }

    @Override
    public void invokeAtSourceRequestReceiving(CarbonMessage carbonMessage) {

        //Handle incoming requests and call throttling method to perform throttling.
        long executionStartTime = System.currentTimeMillis();
        try {
            doThrottle(carbonMessage);
        } finally {
            carbonMessage.setProperty(APIThrottleConstants.THROTTLING_LATENCY, System.currentTimeMillis() -
                    executionStartTime);
        }
    }

    @Override
    public void invokeAtSourceRequestSending(CarbonMessage carbonMessage) {

    }

    @Override
    public void invokeAtTargetRequestReceiving(CarbonMessage carbonMessage) {
    }

    @Override
    public void invokeAtTargetRequestSending(CarbonMessage carbonMessage) {
    }

    @Override
    public void invokeAtTargetResponseReceiving(CarbonMessage carbonMessage) {
    }

    @Override
    public void invokeAtTargetResponseSending(CarbonMessage carbonMessage) {
    }

    @Override
    public void invokeAtSourceResponseReceiving(CarbonMessage carbonMessage) {
    }

    @Override
    public void invokeAtSourceResponseSending(CarbonMessage carbonMessage) {
    }

    @Override
    public void invokeAtTargetConnectionInitiation(String s) {
    }

    @Override
    public void invokeAtTargetConnectionTermination(String s) {
        log.info("Connection terminated :" + s);
    }

    @Override
    public String handlerName() {
        return "ThrottleHandler";
    }


    /**
     * This method is responsible for throttle incoming messages. This method will perform Application, Subscription
     * and Resource level throttling.
     *
     * @param carbonMsg carbon message context that contains message details.
     * @param authCtx   Authentication context contain Authentication details
     * @return Whether the request is throttled or not
     */
    private boolean doRoleBasedAccessThrottlingWithCEP(CarbonMessage carbonMsg, AuthenticationContextDTO authCtx) {

        //Throttle Keys
        //applicationLevelThrottleKey = {applicationId}:{authorizedUser}
        String applicationLevelThrottleKey;
        //subscriptionLevelThrottleKey = {applicationId}:{apiContext}:{apiVersion}
        String subscriptionLevelThrottleKey;
        // resourceLevelThrottleKey = {apiContext}/{apiVersion}{resourceUri}:{httpMethod}
        // if policy is user level then authorized user will append at end
        String resourceLevelThrottleKey;
        //apiLevelThrottleKey key = {apiContext}:{apiVersion}
        String apiLevelThrottleKey;

        //Throttle Tiers
        String applicationLevelTier;
        String subscriptionLevelTier;
        String resourceLevelTier = "";
        String apiLevelTier;

        //Other Relevant parameters
        AuthenticationContextDTO authContext = authCtx;
        String authorizedUser;

        //Throttle decisions
        boolean isThrottled = false;
        boolean isResourceLevelThrottled = false;
        boolean isApplicationLevelThrottled;
        boolean isSubscriptionLevelThrottled;
        boolean isSubscriptionLevelSpikeThrottled = false;
        boolean isApiLevelThrottled = false;
        boolean isBlockedRequest = false;
        boolean apiLevelThrottledTriggered = false;
        boolean policyLevelUserTriggered = false;
        String ipLevelBlockingKey = "";
        String appLevelBlockingKey = "";
        String apiLevelBlockingKey = "";
        String userLevelBlockingKey = "";
        boolean stopOnQuotaReach = true;

        String apiContext = (String) carbonMsg.getProperty("REST_API_CONTEXT");
        String apiVersion = (String) carbonMsg.getProperty("REST_API_VERSION");
        apiContext = apiContext != null ? apiContext : "";
        apiVersion = apiVersion != null ? apiVersion : "";

        String subscriberTenantDomain = "";
        // TODO: get tenant from carbon message
        String apiTenantDomain = "carbon.super";
        ConditionGroupDTO[] conditionGroupDTOs;
        String applicationId = authContext.getApplicationId();
        authorizedUser = authContext.getUsername();

        //Do blocking if there are blocking conditions present
        if (ThrottleDataHolder.getInstance().isBlockingConditionsPresent()) {
            ipLevelBlockingKey = apiTenantDomain + ":" + getClientIp(carbonMsg);
            apiLevelBlockingKey = apiContext;
            userLevelBlockingKey = authorizedUser;
            appLevelBlockingKey = authContext.getSubscriber() + ":" + authContext.getApplicationName();

            isBlockedRequest = ThrottleDataHolder.getInstance().isRequestBlocked(
                    apiLevelBlockingKey, appLevelBlockingKey, userLevelBlockingKey, ipLevelBlockingKey);

            if (isBlockedRequest) {
                String msg = "Request blocked as it violates defined blocking conditions, for API: " + apiContext +
                        " ,application:" + appLevelBlockingKey + " ,user:" + authorizedUser;
                if (log.isDebugEnabled()) {
                    log.debug(msg);
                }
                carbonMsg.setProperty(APIThrottleConstants.BLOCKED_REASON, msg);
                carbonMsg.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON, APIThrottleConstants.REQUEST_BLOCKED);
                isThrottled = true;
            } else {
                subscriberTenantDomain = authContext.getSubscriberTenantDomain();
                applicationLevelThrottleKey = applicationId + ":" + authorizedUser;
                apiLevelThrottleKey = apiContext + ":" + apiVersion;
                stopOnQuotaReach = authContext.isStopOnQuotaReach();
                //If request is not blocked then only we perform throttling.
                VerbInfoDTO verbInfoDTO = (VerbInfoDTO) carbonMsg.getProperty(APIThrottleConstants.VERB_INFO_DTO);

                //If Resource information is not present we wont proceed with throttling
                if (verbInfoDTO == null) {
                    log.warn("Error while getting throttling information for resource and http verb");
                    return false;
                }

                applicationLevelTier = authContext.getApplicationTier();
                subscriptionLevelTier = authContext.getTier();
                resourceLevelThrottleKey = verbInfoDTO.getRequestKey();
                apiLevelTier = authContext.getApiTier();
                resourceLevelTier = verbInfoDTO.getThrottling();
                //If API level throttle policy is present then it will apply and no resource level policy will apply
                if (!StringUtils.isEmpty(apiLevelTier) && !APIThrottleConstants.UNLIMITED_TIER.equalsIgnoreCase
                        (apiLevelTier)) {
                    resourceLevelThrottleKey = apiLevelThrottleKey;
                    apiLevelThrottledTriggered = true;
                }

                //If verbInfo is present then only we will do resource level throttling
                if (APIThrottleConstants.UNLIMITED_TIER.equalsIgnoreCase(verbInfoDTO.getThrottling()) &&
                        !apiLevelThrottledTriggered) {
                    //If unlimited tier throttling will not apply at resource level and pass it
                    if (log.isDebugEnabled()) {
                        log.debug("Resource level throttling set as unlimited and request will pass resource level");
                    }
                } else {
                    if (APIThrottleConstants.API_POLICY_USER_LEVEL.equalsIgnoreCase(verbInfoDTO.getApplicableLevel())) {
                        resourceLevelThrottleKey = resourceLevelThrottleKey + "_" + authorizedUser;
                        policyLevelUserTriggered = true;
                    }
                    //If tier is not unlimited only throttling will apply.
                    conditionGroupDTOs = verbInfoDTO.getConditionGroups();

                    if (conditionGroupDTOs.length > 0) {

                        // Checking Applicability of Conditions is a relatively expensive operation. So we are
                        // going to check it only if the API/Resource is throttled out.
                        if (ThrottleDataHolder.getInstance().isAPIThrottled(resourceLevelThrottleKey)) {

                            if (log.isDebugEnabled()) {
                                log.debug("Evaluating Conditional Groups");
                            }
                            //Then we will apply resource level throttling
                            List<ConditionGroupDTO> applicableConditions = ThrottleConditionEvaluator.getInstance()
                                    .getApplicableConditions(carbonMsg, authContext, conditionGroupDTOs);
                            for (ConditionGroupDTO conditionGroup : applicableConditions) {
                                String combinedResourceLevelThrottleKey = resourceLevelThrottleKey +
                                        conditionGroup.getConditionGroupId();

                                if (log.isDebugEnabled()) {
                                    log.debug("Checking condition : " + combinedResourceLevelThrottleKey);
                                }

                                if (ThrottleDataHolder.getInstance().isThrottled
                                        (combinedResourceLevelThrottleKey)) {
                                    if (!apiLevelThrottledTriggered) {
                                        isResourceLevelThrottled = isThrottled = true;
                                    } else {
                                        isApiLevelThrottled = isThrottled = true;
                                    }
                                    long timestamp = ThrottleDataHolder.getInstance()
                                            .getThrottleNextAccessTimestamp(combinedResourceLevelThrottleKey);
                                    carbonMsg.setProperty(APIThrottleConstants.THROTTLED_NEXT_ACCESS_TIMESTAMP,
                                            timestamp);
                                    break;
                                }
                            }
                        }

                    } else {
                        log.warn("Unable to find throttling information for resource and http verb. Throttling "
                                + "will not apply");
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
                        isSubscriptionLevelThrottled = ThrottleDataHolder.getInstance().isThrottled
                                (subscriptionLevelThrottleKey);
                        if (!isSubscriptionLevelThrottled && authContext.getSpikeArrestLimit() > 0) {
                            isSubscriptionLevelSpikeThrottled = isSubscriptionLevelSpike(carbonMsg,
                                    subscriptionLevelThrottleKey);
                        }
                        //if subscription level not throttled then move to application level
                        //Stop on quata reach
                        if (!isSubscriptionLevelThrottled && !isSubscriptionLevelSpikeThrottled) {
                            //Application Level Throttling
                            isApplicationLevelThrottled = ThrottleDataHolder.getInstance().
                                    isThrottled(applicationLevelThrottleKey);

                            //if application level not throttled means it does not throttled at any level.
                            if (!isApplicationLevelThrottled) {
                                boolean keyTemplatesAvailable = ThrottleDataHolder.getInstance()
                                        .isKeyTemplatesPresent();
                                if (!keyTemplatesAvailable || !validateCustomPolicy(authorizedUser,
                                        resourceLevelThrottleKey, apiContext, apiVersion, subscriberTenantDomain,
                                        apiTenantDomain, applicationId, ThrottleDataHolder.getInstance()
                                                .getKeyTemplateMap(), carbonMsg)) {
                                    //Pass message context and continue to avoid performance issue.
                                    //Did not throttled at any level. So let message go and publish event.
                                    //publish event to Global Policy Server
                                    if (isHardLimitThrottled(carbonMsg, authContext, apiContext, apiVersion)) {
                                        isThrottled = true;

                                    } else {
                                        throttleDataPublisher.publishNonThrottledEvent(
                                                applicationLevelThrottleKey, applicationLevelTier,
                                                apiLevelThrottleKey, apiLevelTier,
                                                subscriptionLevelThrottleKey, subscriptionLevelTier,
                                                resourceLevelThrottleKey, resourceLevelTier,
                                                authorizedUser, apiContext, apiVersion, subscriberTenantDomain,
                                                apiTenantDomain, applicationId, carbonMsg, authContext);
                                    }
                                } else {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Request throttled at custom throttling");
                                    }
                                    carbonMsg.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                                            APIThrottleConstants.CUSTOM_POLICY_LIMIT_EXCEED);
                                    isThrottled = true;

                                }

                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Request throttled at application level for throttle key" +
                                            applicationLevelThrottleKey);
                                }
                                carbonMsg.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                                        APIThrottleConstants.APPLICATION_LIMIT_EXCEEDED);
                                long timestamp = ThrottleDataHolder.getInstance().getThrottleNextAccessTimestamp
                                        (applicationLevelThrottleKey);
                                carbonMsg.setProperty(APIThrottleConstants.THROTTLED_NEXT_ACCESS_TIMESTAMP, timestamp);
                                isThrottled = true;
                            }
                        } else {
                            if (!stopOnQuotaReach) {
                                // This means that we are allowing the requests to continue even after the throttling
                                // limit has reached.
                                if (carbonMsg.getProperty(APIThrottleConstants.API_USAGE_THROTTLE_OUT_PROPERTY_KEY)
                                        == null) {
                                    carbonMsg.setProperty(APIThrottleConstants.API_USAGE_THROTTLE_OUT_PROPERTY_KEY,
                                            Boolean
                                                    .TRUE);
                                }
                                isThrottled = false;
                                if (log.isDebugEnabled()) {
                                    log.debug("Request throttled at subscription level for throttle key" +
                                            subscriptionLevelThrottleKey + ". But subscription policy " +
                                            subscriptionLevelTier + " allows to continue to serve requests");
                                }
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug("Request throttled at subscription level for throttle key" +
                                            subscriptionLevelThrottleKey);
                                }
                                if (!isSubscriptionLevelSpikeThrottled) {
                                    long timestamp = ThrottleDataHolder.getInstance().getThrottleNextAccessTimestamp
                                            (subscriptionLevelThrottleKey);
                                    carbonMsg.setProperty(APIThrottleConstants.THROTTLED_NEXT_ACCESS_TIMESTAMP,
                                            timestamp);
                                    carbonMsg.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                                            APIThrottleConstants
                                                    .API_LIMIT_EXCEEDED);
                                    carbonMsg.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
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
                        carbonMsg.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                                APIThrottleConstants.RESOURCE_LIMIT_EXCEEDED);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Request throttled at api level for throttle key" + apiLevelThrottleKey);
                        if (policyLevelUserTriggered) {
                            log.debug("Request has throttled out in the user level for the throttle key" +
                                    apiLevelThrottleKey);
                        }
                    }
                    carbonMsg.setProperty(APIThrottleConstants.THROTTLED_OUT_REASON,
                            APIThrottleConstants.API_LIMIT_EXCEEDED);
                }
            }

        }

        //if we need to publish throttled level or some other information we can do it here. Just before return.
        return isThrottled;
    }

    private boolean isHardLimitThrottled(CarbonMessage carbonMsg, AuthenticationContextDTO authContext, String
            apiContext, String apiVersion) {
        //// TODO: Implement Hardlimit
        return false;
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
    public boolean handleResponse(CarbonMessage messageContext) {
        return true;
    }


    /**
     * Do Throttle method will initialize throttle flow.
     *
     * @param carbonMessage message context object which contains message details.
     * @return return true if message flow need to continue(message not throttled) and pass requests to next
     * handler in chain. Else return false to notify throttled message.
     */
    private boolean doThrottle(CarbonMessage carbonMessage) {

        // TODO: get authcontext from carbonmessage
        AuthenticationContextDTO authenticationContext = AuthenticationContextDTO.getInstance();

        boolean isThrottled = false;

        isThrottled = doRoleBasedAccessThrottlingWithCEP(carbonMessage, authenticationContext);

        if (isThrottled) {

            handleThrottleOut(carbonMessage);
            return false;
        }
        return true;
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

    private String getClientIp(CarbonMessage carbonMsg) {
        // // TODO: fetch from CarbonMessage
        return "10.100.5.192";
    }


    /**
     * This method will check if coming request is hitting subscription level spikes.
     *
     * @param carbonMessage carbon message context which contains message data
     * @param throttleKey   subscription level throttle key.
     * @return true if message is throttled else false
     */
    public boolean isSubscriptionLevelSpike(CarbonMessage carbonMessage, String throttleKey) {
        // TODO: Implement spike arrest or use the synapse spike arrest
        return false;
    }

    /**
     * Validate custom policy is handle by this method. This method call is an expensive operation
     * and should not enabled by default. If we enabled this policy then all APIs available in system
     * will have to go through this check.
     *
     * @return
     */
    public boolean validateCustomPolicy(String userID, String resourceKey, String apiContext, String apiVersion,
                                        String appTenant,String apiTenant, String appId,
                                        Map<String, String> keyTemplateMap,
                                        CarbonMessage messageContext) {
        if (keyTemplateMap != null && keyTemplateMap.size() > 0) {
            for (String key : keyTemplateMap.keySet()) {
                key = key.replaceAll("\\$resourceKey", resourceKey);
                key = key.replaceAll("\\$userId", userID);
                key = key.replaceAll("\\$apiContext", apiContext);
                key = key.replaceAll("\\$apiVersion", apiVersion);
                key = key.replaceAll("\\$appTenant", appTenant);
                key = key.replaceAll("\\$apiTenant", apiTenant);
                key = key.replaceAll("\\$appId", appId);
                if (ThrottleDataHolder.getInstance().isThrottled(key)) {
                    long timestamp = ThrottleDataHolder.getInstance().getThrottleNextAccessTimestamp(key);
                    messageContext.setProperty(APIThrottleConstants.THROTTLED_NEXT_ACCESS_TIMESTAMP, timestamp);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method will initialize data publisher and this data publisher will be used to push events to traffic manager
     */
    private static synchronized void initDataPublisher() {

        if (throttleDataPublisher == null) {
            throttleDataPublisher = new ThrottleDataPublisher();
        }
    }

    /**
     * Need to complete the implementation using ballerina
     * @param messageContext
     */
    private void handleThrottleOut(CarbonMessage messageContext) {

        String errorMessage = null;
        String errorDescription = null;
        int errorCode = -1;
        int httpErrorCode = -1;
        String nextAccessTimeString = "";
        if (APIThrottleConstants.HARD_LIMIT_EXCEEDED.equals(
                messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.HARD_LIMIT_EXCEEDED_ERROR_CODE;
            errorMessage = "API Limit Reached";
            errorDescription = "API not accepting requests";
            // It it's a hard limit exceeding, we tell it as service not being available.
            httpErrorCode = APIThrottleConstants.SC_SERVICE_UNAVAILABLE;
        } else if (APIThrottleConstants.REQUEST_BLOCKED.equals(
                messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.BLOCKED_ERROR_CODE;
            errorMessage = "Message blocked";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_FORBIDDEN;
            errorDescription = "You have been blocked from accessing the resource";
        } else if (APIThrottleConstants.API_LIMIT_EXCEEDED
                .equals(messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.API_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
        } else if (APIThrottleConstants.RESOURCE_LIMIT_EXCEEDED
                .equals(messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.RESOURCE_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
        } else if (APIThrottleConstants.CUSTOM_POLICY_LIMIT_EXCEED
                .equals(messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.CUSTOM_POLICY_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
        } else if (APIThrottleConstants.SUBSCRIPTION_LIMIT_EXCEEDED
                .equals(messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.SUBSCRIPTION_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
        } else if (APIThrottleConstants.SUBSCRIPTON_BURST_LIMIT_EXCEEDED
                .equals(messageContext.getProperty(APIThrottleConstants.THROTTLED_OUT_REASON))) {
            errorCode = APIThrottleConstants.SUBSCRIPTION_BURST_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
        } else {
            errorCode = APIThrottleConstants.APPLICATION_THROTTLE_OUT_ERROR_CODE;
            errorMessage = "Message throttled out";
            // By default we send a 429 response back
            httpErrorCode = APIThrottleConstants.SC_TOO_MANY_REQUESTS;
            errorDescription = "You have exceeded your quota";
        }

        messageContext.setProperty(APIThrottleConstants.ERROR_CODE, errorCode);
        messageContext.setProperty(APIThrottleConstants.ERROR_MESSAGE, errorMessage);

        String message = httpErrorCode + errorDescription + nextAccessTimeString;
        messageContext.setProperty("THROTTLE_OUT_MESSAGE", message);
        // TODO: Implement throttleout message based on new balerina
    }

    /**
     * USed to Create a meaningful error message
     * @param throttleErrorCode
     * @param message
     * @param description
     * @param nextAccessTimeValue
     * @return
     */
    public String getFaultPayload(int throttleErrorCode, String message, String description,
                                  String nextAccessTimeValue) {

        String errorMessage = message + " " + description + "Error Code" + throttleErrorCode + nextAccessTimeValue;
        return errorMessage;
    }
}
