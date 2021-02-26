/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.notification;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.WebhooksDAO;
import org.wso2.carbon.apimgt.impl.handlers.EventHandler;
import org.wso2.carbon.apimgt.notification.event.WebhooksSubscriptionEvent;
import org.wso2.carbon.apimgt.notification.util.NotificationUtil;
import org.wso2.carbon.databridge.commons.Event;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class implements to handle webhooks subscriptions related notification events.
 */
public class WebhooksSubscriptionEventHandler implements EventHandler {

    private static final Log log = LogFactory.getLog(WebhooksSubscriptionEventHandler.class);

    @Override
    public boolean handleEvent(String event, Map<String, List<String>> headers) throws APIManagementException {
        WebhooksSubscriptionEvent subscriptionEvent = new Gson().fromJson(event, WebhooksSubscriptionEvent.class);
        Properties properties = populateProperties(subscriptionEvent);
        boolean isSuccess = true;
        if (APIConstants.Webhooks.SUBSCRIBE_MODE.equalsIgnoreCase(subscriptionEvent.getMode())) {
            isSuccess = WebhooksDAO.getInstance().addSubscription(properties);
        } else if (APIConstants.Webhooks.UNSUBSCRIBE_MODE.equalsIgnoreCase(subscriptionEvent.getMode())) {
            WebhooksDAO.getInstance().updateUnSubscription(properties);
        } else {
            throw new APIManagementException("Error while processing subscription request: Wrong subscription mode");
        }
        sendSubscriptionNotificationOnRealtime(subscriptionEvent, isSuccess);
        if (!isSuccess) {
            throw new APIManagementException("Throttled out");
        }
        return true;
    }

    /**
     * Method to publish the subscription request on to the realtime message broker
     *
     * @param subscriptionEvent subscription event
     * @return Properties       subscription properties
     */
    private Properties populateProperties(WebhooksSubscriptionEvent subscriptionEvent) throws APIManagementException {
        Properties properties = new Properties();
        properties.put(APIConstants.Webhooks.API_UUID, subscriptionEvent.getApiUUID());
        properties.put(APIConstants.Webhooks.APP_ID, subscriptionEvent.getAppID());
        properties.put(APIConstants.Webhooks.TENANT_DOMAIN, subscriptionEvent.getTenantDomain());
        properties.put(APIConstants.Webhooks.TENANT_ID, subscriptionEvent.getTenantId());
        properties.put(APIConstants.Webhooks.CALLBACK, subscriptionEvent.getCallback());
        properties.put(APIConstants.Webhooks.TOPIC, subscriptionEvent.getTopic());
        putIfNotNull(properties, APIConstants.Webhooks.SECRET, subscriptionEvent.getSecret());
        String leaseSeconds = subscriptionEvent.getLeaseSeconds();
        putIfNotNull(properties, APIConstants.Webhooks.LEASE_SECONDS, leaseSeconds);
        Date currentTime = new Date();
        Timestamp updatedTimestamp = new Timestamp(currentTime.getTime());
        subscriptionEvent.setUpdatedTime(updatedTimestamp);
        properties.put(APIConstants.Webhooks.UPDATED_AT, updatedTimestamp);
        long expiryTime = 0;
        if (!StringUtils.isEmpty(leaseSeconds)) {
            long leaseSecondsInLong;
            try {
                leaseSecondsInLong = Long.parseLong(leaseSeconds);
            } catch (NumberFormatException e) {
                throw new APIManagementException("Error while parsing leaseSeconds param", e);
            }
            expiryTime = updatedTimestamp.toInstant().plusSeconds(leaseSecondsInLong).toEpochMilli();
        }
        subscriptionEvent.setExpiryTime(expiryTime);
        properties.put(APIConstants.Webhooks.EXPIRY_AT, "" + expiryTime);
        properties.put(APIConstants.Webhooks.TIER, "" + subscriptionEvent.getTier());
        return properties;
    }

    @Override
    public String getType() {
        return APIConstants.Webhooks.SUBSCRIPTION_EVENT_TYPE;
    }

    /**
     * Method to publish the subscription request on to the realtime message broker
     *
     * @param event realtime notification data read from the event
     */
    private void sendSubscriptionNotificationOnRealtime(WebhooksSubscriptionEvent event,
                                                        boolean isSuccess) {
        Object[] objects = new Object[]{event.getApiUUID(), event.getApiName(), event.getApiContext(),
                event.getApiVersion(), event.getAppID(), event.getTenantDomain(), event.getTenantId(),
                event.getCallback(), event.getTopic(), event.getMode(), event.getSecret(), event.getExpiryTime(),
                event.getSubscriberName(), event.getApplicationTier(), event.getTier(), event.getApiTier(),
                !isSuccess};
        Event notificationMessage = new Event(APIConstants.WEBHOOKS_SUBSCRIPTION_STREAM_ID,
                System.currentTimeMillis(), null, null, objects);
        NotificationUtil.publishEventToStreamService(notificationMessage);
        if (log.isDebugEnabled()) {
            log.debug("Successfully sent the webhooks subscription notification on realtime");
        }
    }

    /**
     * Method to insert property value
     *
     * @param properties    properties
     * @param name          name of the properties
     * @param value         value of the properties
     */
    private void putIfNotNull(Properties properties, String name, String value) {
        if (value != null) {
            properties.put(name, value);
        }
    }
}
