/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.notification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.eventing.EventPublisherEvent;
import org.wso2.carbon.apimgt.eventing.EventPublisherType;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.token.OpaqueAPIKeyNotifier;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.Properties;

/**
 * Implemented class for OpaqueAPIKeyNotifier interface
 * sendLastUsedTimeOnRealtime() method is implemented
 * sendApiKeyInfoOnRealtime() method is implemented
 */
public class OpaqueAPIKeyNotifierImpl implements OpaqueAPIKeyNotifier {

    private static final Log log = LogFactory.getLog(OpaqueAPIKeyNotifierImpl.class);
    protected static final String DEFAULT_TTL = "3600";
    protected Properties realTimeNotifierProperties;

    /**
     * Method to publish the api key last used time on to the realtime message broker
     *
     * @param properties details to send
     */
    @Override
    public void sendLastUsedTimeOnRealtime(Properties properties) {

        // Variables related to Realtime Notifier
        String type = null;
        long expiryTimeForJWT = 0L;
        if (properties.getProperty(APIConstants.NotificationEvent.EXPIRY_TIME) != null) {
            expiryTimeForJWT = Long.parseLong(properties.getProperty(APIConstants.NotificationEvent.EXPIRY_TIME));
        }
        String realtimeNotifierTTL = realTimeNotifierProperties.getProperty("ttl", DEFAULT_TTL);
        String eventId = properties.getProperty(APIConstants.NotificationEvent.EVENT_ID);
        if (APIConstants.NotificationEvent.API_KEY_USAGE_EVENT.equals(
                properties.getProperty(APIConstants.NotificationEvent.EVENT_TYPE))) {
            type = properties.getProperty(APIConstants.NotificationEvent.USAGE_TYPE);
        } else {
            type = properties.getProperty(APIConstants.NotificationEvent.EVENT_TYPE);
        }
        String orgId = properties.getProperty(APIConstants.NotificationEvent.ORG_ID);
        Object tenantIdObj = properties.get(APIConstants.NotificationEvent.TENANT_ID);
        int tenantId = tenantIdObj instanceof Integer ? (Integer) tenantIdObj : 0;
        Object lastUsedTimeObj = properties.get(APIConstants.NotificationEvent.LAST_USED_TIME);
        long lastUsedTime = lastUsedTimeObj instanceof Long ? (Long) lastUsedTimeObj : 0L;
        Object[] objects =
                new Object[]{eventId, properties.getProperty(APIConstants.NotificationEvent.API_KEY_HASH), lastUsedTime,
                        realtimeNotifierTTL, expiryTimeForJWT, type, tenantId};
        EventPublisherEvent apiKeyUsageEvent = new EventPublisherEvent(APIConstants.API_KEY_USAGE_STREAM_ID,
                System.currentTimeMillis(), objects);
        apiKeyUsageEvent.setOrgId(orgId);
        APIUtil.publishEvent(EventPublisherType.API_KEY_USAGE, apiKeyUsageEvent,
                apiKeyUsageEvent.toString());
    }

    /**
     * Method to publish the api key info on to the realtime message broker
     *
     * @param properties details to send
     */
    @Override
    public void sendApiKeyInfoOnRealtime(Properties properties) {

        String eventId = properties.getProperty(APIConstants.NotificationEvent.EVENT_ID);
        String validityPeriodStr = properties.getProperty(APIConstants.NotificationEvent.VALIDITY_PERIOD);
        long validityPeriod = validityPeriodStr != null ? Long.parseLong(validityPeriodStr) : 0L;
        String orgId = properties.getProperty(APIConstants.NotificationEvent.ORG_ID);
        Object tenantIdObj = properties.get(APIConstants.NotificationEvent.TENANT_ID);
        int tenantId = tenantIdObj instanceof Integer ? (Integer) tenantIdObj : 0;
        Object appIdObj = properties.get(APIConstants.NotificationEvent.APPLICATION_ID);
        int appId = appIdObj instanceof Integer ? (Integer) appIdObj : 0;
        String originUUId = null;
        String origin = null;
        if (properties.getProperty(APIConstants.NotificationEvent.APPLICATION_UUID) != null) {
            originUUId  = properties.getProperty(APIConstants.NotificationEvent.APPLICATION_UUID);
            origin = "APP";
        } else if (properties.getProperty(APIConstants.NotificationEvent.API_UUID) != null) {
            originUUId = properties.getProperty(APIConstants.NotificationEvent.API_UUID);
            origin = "API";
        }
        Object[] objects = new Object[]{eventId, properties.getProperty(APIConstants.NotificationEvent.API_KEY_HASH),
                        properties.getProperty(APIConstants.NotificationEvent.KEY_TYPE),
                        properties.getProperty(APIConstants.NotificationEvent.KEY_DISPLAY_NAME),
                        origin, originUUId, appId,
                        properties.getProperty(APIConstants.NotificationEvent.STATUS), validityPeriod,
                        properties.getProperty(APIConstants.NotificationEvent.ADDITIONAL_PROPERTIES), tenantId};
        EventPublisherEvent apiKeyInfoEvent = new EventPublisherEvent(APIConstants.API_KEY_INFO_STREAM_ID,
                System.currentTimeMillis(), objects);
        apiKeyInfoEvent.setOrgId(orgId);
        APIUtil.publishEvent(EventPublisherType.API_KEY_INFO, apiKeyInfoEvent,
                apiKeyInfoEvent.toString());
    }

    /**
     * Method to send the api key association info on realtime
     * @param properties additional properties to send
     */
    @Override
    public void sendApiKeyAssociationInfoOnRealtime(Properties properties) {
        String eventId = properties.getProperty(APIConstants.NotificationEvent.EVENT_ID);
        String orgId = properties.getProperty(APIConstants.NotificationEvent.ORG_ID);
        Object tenantIdObj = properties.get(APIConstants.NotificationEvent.TENANT_ID);
        int tenantId = tenantIdObj instanceof Integer ? (Integer) tenantIdObj : 0;
        Object appIdObj = properties.get(APIConstants.NotificationEvent.APPLICATION_ID);
        int appId = appIdObj instanceof Integer ? (Integer) appIdObj : 0;
        Object[] objects = new Object[]{eventId,
                properties.getProperty(APIConstants.NotificationEvent.KEY_DISPLAY_NAME),
                properties.getProperty(APIConstants.NotificationEvent.KEY_TYPE),
                properties.getProperty(APIConstants.NotificationEvent.API_KEY_HASH),
                properties.getProperty(APIConstants.NotificationEvent.API_UUID),
                properties.getProperty(APIConstants.NotificationEvent.APPLICATION_UUID), appId,
                properties.getProperty(APIConstants.NotificationEvent.ASSOCIATION_TYPE), tenantId};
        EventPublisherEvent apiKeyAssociationInfoEvent = new EventPublisherEvent(
                APIConstants.API_KEY_ASSOCIATION_INFO_STREAM_ID, System.currentTimeMillis(), objects);
        apiKeyAssociationInfoEvent.setOrgId(orgId);
        APIUtil.publishEvent(EventPublisherType.API_KEY_ASSOCIATION_INFO, apiKeyAssociationInfoEvent,
                apiKeyAssociationInfoEvent.toString());
    }

    @Override
    public void init(Properties realTimeNotifierProperties) {
        this.realTimeNotifierProperties = realTimeNotifierProperties != null
                ? (Properties) realTimeNotifierProperties.clone()
                : new Properties();
    }
}
