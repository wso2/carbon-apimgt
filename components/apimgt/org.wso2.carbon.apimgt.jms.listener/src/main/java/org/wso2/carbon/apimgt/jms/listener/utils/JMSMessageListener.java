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

package org.wso2.carbon.apimgt.jms.listener.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.dto.ResourceCacheInvalidationDto;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.util.ThrottleConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.jms.listener.APICondition;
import org.wso2.carbon.apimgt.jms.listener.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(JMSMessageListener.class);

    // These patterns will be used to determine for which type of keys the throttling condition has occurred.
    private Pattern apiPattern = Pattern.compile("/.*/(.*):\\1_(condition_(\\d*)|default)");
    private static final int API_PATTERN_GROUPS = 3;
    private static final int API_PATTERN_CONDITION_INDEX = 2;

    private Pattern resourcePattern = Pattern.compile("/.*/(.*)/\\1(.*)?:[A-Z]{0,5}_(condition_(\\d*)|default)");
    public static final int RESOURCE_PATTERN_GROUPS = 4;
    public static final int RESOURCE_PATTERN_CONDITION_INDEX = 3;
    public static final String CONDITION_KEY = "condition";
    public static final String RESOURCE_KEY = "key";

    public void onMessage(Message message) {

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                Topic jmsDestination = (Topic) message.getJMSDestination();
                if (message instanceof MapMessage) {
                    MapMessage mapMessage = (MapMessage) message;
                    Map<String, Object> map = new HashMap<String, Object>();
                    Enumeration enumeration = mapMessage.getMapNames();
                    while (enumeration.hasMoreElements()) {
                        String key = (String) enumeration.nextElement();
                        map.put(key, mapMessage.getObject(key));
                    }
                    if (JMSConstants.TOPIC_THROTTLE_DATA.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (map.get(APIConstants.THROTTLE_KEY) != null) {
                            /*
                             * This message contains throttle data in map which contains Keys
                             * throttleKey - Key of particular throttling level
                             * isThrottled - Whether message has throttled or not
                             * expiryTimeStamp - When the throttling time window will expires
                             */

                            handleThrottleUpdateMessage(map);
                        } else if (map.get(APIConstants.BLOCKING_CONDITION_KEY) != null) {
                            /*
                             * This message contains blocking condition data
                             * blockingCondition - Blocking condition type
                             * conditionValue - blocking condition value
                             * state - State whether blocking condition is enabled or not
                             */
                            handleBlockingMessage(map);
                        } else if (map.get(APIConstants.POLICY_TEMPLATE_KEY) != null) {
                            /*
                             * This message contains key template data
                             * keyTemplateValue - Value of key template
                             * keyTemplateState - whether key template active or not
                             */
                            handleKeyTemplateMessage(map);
                        }
                    } else if (JMSConstants.TOPIC_TOKEN_REVOCATION.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (map.get(APIConstants.REVOKED_TOKEN_KEY) !=
                                null) {
                            /*
                             * This message contains revoked token data
                             * revokedToken - Revoked Token which should be removed from the cache
                             * expiryTime - ExpiryTime of the token if token is JWT, otherwise expiry is set to 0
                             */
                            handleRevokedTokenMessage((String) map.get(APIConstants.REVOKED_TOKEN_KEY),
                                    (Long) map.get(APIConstants.REVOKED_TOKEN_EXPIRY_TIME));
                        }

                    } else if (JMSConstants.TOPIC_CACHE_INVALIDATION.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (map.get(APIConstants.CACHE_INVALIDATION_TYPE) != null) {
                            if (APIConstants.RESOURCE_CACHE_NAME
                                    .equalsIgnoreCase((String) map.get(APIConstants.CACHE_INVALIDATION_TYPE))) {
                                handleResourceCacheInvalidationMessage(map);
                            } else if (APIConstants.GATEWAY_KEY_CACHE_NAME
                                    .equalsIgnoreCase((String) map.get(APIConstants.CACHE_INVALIDATION_TYPE))) {
                                handleKeyCacheInvalidationMessage(map);
                            } else if (APIConstants.GATEWAY_USERNAME_CACHE_NAME
                                    .equalsIgnoreCase((String) map.get(APIConstants.CACHE_INVALIDATION_TYPE))) {
                                handleUserCacheInvalidationMessage(map);
                            }

                        }
                    }
                } else {
                    log.warn("Event dropped due to unsupported message type " + message.getClass());
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException e) {
            log.error("JMSException occurred when processing the received message ", e);
        } catch (ParseException e) {
            log.error("Error while processing evaluatedConditions", e);
        }
    }

    private void handleUserCacheInvalidationMessage(Map<String, Object> map) throws ParseException {

        if (map.containsKey("value")) {
            String value = (String) map.get("value");
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonValue = (JSONArray) jsonParser.parse(value);

            ServiceReferenceHolder.getInstance().getCacheInvalidationService()
                    .invalidateCachedUsernames((String[]) jsonValue.toArray(new String[jsonValue.size()]));
        }
    }

    private void handleKeyCacheInvalidationMessage(Map<String, Object> map) throws ParseException {

        if (map.containsKey("value")) {
            String value = (String) map.get("value");
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonValue = (JSONArray) jsonParser.parse(value);

            ServiceReferenceHolder.getInstance().getCacheInvalidationService()
                    .invalidateCachedTokens((String[]) jsonValue.toArray(new String[jsonValue.size()]));
        }
    }

    private void handleResourceCacheInvalidationMessage(Map<String, Object> map) throws ParseException {

        if (map.containsKey("value")) {
            String value = (String) map.get("value");
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonValue = (JSONObject) jsonParser.parse(value);
            String apiContext = (String) jsonValue.get("apiContext");
            String apiVersion = (String) jsonValue.get("apiVersion");
            JSONArray resources = (JSONArray) jsonValue.get("resources");
            List<ResourceCacheInvalidationDto> resourceCacheInvalidationDtoList = new ArrayList<>();
            for (Object resource : resources) {
                JSONObject uriTemplate = (JSONObject) resource;
                String resourceURLContext = (String) uriTemplate.get("resourceURLContext");
                String httpVerb = (String) uriTemplate.get("httpVerb");
                ResourceCacheInvalidationDto uriTemplateDto = new ResourceCacheInvalidationDto();
                uriTemplateDto.setHttpVerb(httpVerb);
                uriTemplateDto.setResourceURLContext(resourceURLContext);
                resourceCacheInvalidationDtoList.add(uriTemplateDto);
            }
            ServiceReferenceHolder.getInstance().getCacheInvalidationService().invalidateResourceCache(apiContext,
                    apiVersion,
                    resourceCacheInvalidationDtoList
                            .toArray(new ResourceCacheInvalidationDto[resourceCacheInvalidationDtoList.size()]));
        }

    }

    private void handleThrottleUpdateMessage(Map<String, Object> map) throws ParseException {

        String throttleKey = map.get(APIThrottleConstants.THROTTLE_KEY).toString();
        String throttleState = map.get(APIThrottleConstants.IS_THROTTLED).toString();
        Long timeStamp = Long.parseLong(map.get(APIThrottleConstants.EXPIRY_TIMESTAMP).toString());
        Object evaluatedConditionObject = map.get(APIThrottleConstants.EVALUATED_CONDITIONS);

        if (log.isDebugEnabled()) {
            log.debug("Received Key -  throttleKey : " + throttleKey + " , " +
                    "isThrottled :" + throttleState + " , expiryTime : " + new Date(timeStamp).toString());
        }

        if (ThrottleConstants.TRUE.equalsIgnoreCase(throttleState)) {
            ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                    addThrottleData(throttleKey, timeStamp);

            APICondition extractedKey = extractAPIorResourceKey(throttleKey);

            if (extractedKey != null) {
                if (evaluatedConditionObject != null) {
                    ServiceReferenceHolder.getInstance().getThrottleDataHolder().addThrottledApiConditions
                            (extractedKey.getResourceKey(), extractedKey.getName(), APIUtil.extractConditionDto(
                                    (String) evaluatedConditionObject));
                }
                if (!ServiceReferenceHolder.getInstance().getThrottleDataHolder().isAPIThrottled(extractedKey
                        .getResourceKey())) {
                    ServiceReferenceHolder.getInstance().getThrottleDataHolder().addThrottledAPIKey(extractedKey
                            .getResourceKey(), timeStamp);
                    if (log.isDebugEnabled()) {
                        log.debug("Adding throttling key : " + extractedKey);
                    }
                }

            }
        } else {
            ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                    removeThrottleData(throttleKey);
            APICondition extractedKey = extractAPIorResourceKey(throttleKey);
            if (extractedKey != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing throttling key : " + extractedKey.getResourceKey());
                }

                ServiceReferenceHolder.getInstance().getThrottleDataHolder().removeThrottledAPIKey(extractedKey.getResourceKey());
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().removeThrottledApiConditions(extractedKey.getResourceKey(), extractedKey.getName());
            }
        }
    }

    //Synchronized due to blocking data contains or not can updated by multiple threads. Will not be a performance
    // isssue
    //as this will not happen more frequently
    private synchronized void handleBlockingMessage(Map<String, Object> map) {

        if (log.isDebugEnabled()) {
            log.debug("Received Key -  blockingCondition : " + map.get(APIConstants.BLOCKING_CONDITION_KEY).toString() + " , " +
                    "conditionValue :" + map.get(APIConstants.BLOCKING_CONDITION_VALUE).toString() + " , " +
                    "tenantDomain : " + map.get(APIConstants.BLOCKING_CONDITION_DOMAIN));
        }

        String condition = map.get(APIConstants.BLOCKING_CONDITION_KEY).toString();
        String conditionValue = map.get(APIConstants.BLOCKING_CONDITION_VALUE).toString();
        String conditionState = map.get(APIConstants.BLOCKING_CONDITION_STATE).toString();

        if (APIConstants.BLOCKING_CONDITIONS_APPLICATION.equals(condition)) {
            if (ThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().addApplicationBlockingCondition(conditionValue, conditionValue);
            } else {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().removeApplicationBlockingCondition(conditionValue);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_API.equals(condition)) {
            if (ThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().addAPIBlockingCondition(conditionValue,
                        conditionValue);
            } else {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().removeAPIBlockingCondition(conditionValue);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_USER.equals(condition)) {
            if (ThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().addUserBlockingCondition(conditionValue,
                        conditionValue);
            } else {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().removeUserBlockingCondition(conditionValue);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_IP.equals(condition)) {
            if (ThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().addIplockingCondition(conditionValue,
                        conditionValue);
            } else {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().removeIpBlockingCondition(conditionValue);
            }
        }
    }

    private APICondition extractAPIorResourceKey(String throttleKey) {

        Matcher m = resourcePattern.matcher(throttleKey);
        if (m.matches()) {
            if (m.groupCount() == RESOURCE_PATTERN_GROUPS) {
                String condition = m.group(RESOURCE_PATTERN_CONDITION_INDEX);
                String resourceKey = throttleKey.substring(0, throttleKey.indexOf("_" + condition));
                return new APICondition(resourceKey, condition);
            }
        } else {
            m = apiPattern.matcher(throttleKey);
            if (m.matches()) {
                if (m.groupCount() == API_PATTERN_GROUPS) {
                    String condition = m.group(API_PATTERN_CONDITION_INDEX);
                    String resourceKey = throttleKey.substring(0, throttleKey.indexOf("_" + condition));
                    return new APICondition(resourceKey, condition);
                }
            }
        }
        return null;
    }

    private synchronized void handleKeyTemplateMessage(Map<String, Object> map) {

        if (log.isDebugEnabled()) {
            log.debug("Received Key -  KeyTemplate : " + map.get(APIConstants.POLICY_TEMPLATE_KEY).toString());
        }
        String keyTemplateValue = map.get(APIConstants.POLICY_TEMPLATE_KEY).toString();
        String keyTemplateState = map.get(APIConstants.TEMPLATE_KEY_STATE).toString();
        if (ThrottleConstants.ADD.equals(keyTemplateState)) {
            ServiceReferenceHolder.getInstance().getThrottleDataHolder()
                    .addKeyTemplate(keyTemplateValue, keyTemplateValue);
        } else {
            ServiceReferenceHolder.getInstance().getThrottleDataHolder()
                    .removeKeyTemplate(keyTemplateValue);
        }
    }

    private void handleRevokedTokenMessage(String revokedToken, long expiryTime) {
        boolean isJwtToken = false;
        if (StringUtils.isEmpty(revokedToken)) {
            return;
        }

        //handle JWT tokens
        if (revokedToken.contains(APIConstants.DOT) && APIUtil.isValidJWT(revokedToken)) {
            revokedToken = APIUtil.getSignatureIfJWT(revokedToken); //JWT signature is the cache key
            RevokedJWTDataHolder.getInstance().addRevokedJWTToMap(revokedToken, expiryTime);  // Add revoked token to
            // revoked JWT map
            isJwtToken = true;
        }

        //Find the actual tenant domain on which the access token was cached. It is stored as a reference in
        //the super tenant cache.
        String cachedTenantDomain;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            cachedTenantDomain = Utils.getCachedTenantDomain(revokedToken);
            if (cachedTenantDomain == null) { //the token is not in cache
                return;
            }
            Utils.removeCacheEntryFromGatewayCache(revokedToken);
            Utils.putInvalidTokenEntryIntoInvalidTokenCache(revokedToken, cachedTenantDomain);
            //Clear the API Key cache if revoked token is in the JWT format
            if (isJwtToken) {
                Utils.removeCacheEntryFromGatewayAPiKeyCache(revokedToken);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        //Remove token from the token's own tenant's cache.
        Utils.removeTokenFromTenantTokenCache(revokedToken, cachedTenantDomain);
        Utils.putInvalidTokenIntoTenantInvalidTokenCache(revokedToken, cachedTenantDomain);
    }
}
