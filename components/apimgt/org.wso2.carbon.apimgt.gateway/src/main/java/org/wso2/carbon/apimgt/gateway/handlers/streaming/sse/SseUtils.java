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

package org.wso2.carbon.apimgt.gateway.handlers.streaming.sse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;

/**
 * Utils methods related to SSE Api.
 */
public class SseUtils {

    private static final Log log = LogFactory.getLog(SseUtils.class);
    private static final String THROTTLE_STREAM_ID = "org.wso2.throttle.request.stream:1.0.0";

    /**
     * Check if the request is throttled
     *
     * @param resourceLevelThrottleKey     resource level key
     * @param subscriptionLevelThrottleKey subscription level key
     * @param applicationLevelThrottleKey  application level key
     * @return is throttled or not
     */
    public static boolean isThrottled(String tenantDomain, String resourceLevelThrottleKey,
                                      String subscriptionLevelThrottleKey, String applicationLevelThrottleKey) {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

            boolean isApiLevelThrottled = ServiceReferenceHolder.getInstance().getThrottleDataHolder().isAPIThrottled(
                    resourceLevelThrottleKey);
            boolean isSubscriptionLevelThrottled =
                    ServiceReferenceHolder.getInstance().getThrottleDataHolder().isThrottled(
                            subscriptionLevelThrottleKey);
            boolean isApplicationLevelThrottled =
                    ServiceReferenceHolder.getInstance().getThrottleDataHolder().isThrottled(
                            applicationLevelThrottleKey);
            if (log.isDebugEnabled()) {
                log.debug("Throttle result \n" + "isApiLevelThrottled : " + isApiLevelThrottled
                                  + "\nisSubscriptionLevelThrottled : " + isSubscriptionLevelThrottled
                                  + "\nisApplicationLevelThrottled : " + isApplicationLevelThrottled);
            }
            return (isApiLevelThrottled || isApplicationLevelThrottled || isSubscriptionLevelThrottled);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public static void publishNonThrottledEvent(int eventCount, String messageId, ThrottleInfo throttleInfo,
                                                JSONObject properties) {

        Object[] objects = new Object[] { messageId, throttleInfo.getApplicationLevelThrottleKey(),
                throttleInfo.getApplicationTier(), throttleInfo.getApiLevelThrottleKey(), throttleInfo.getApiTier(),
                throttleInfo.getSubscriptionLevelThrottleKey(), throttleInfo.getTier(),
                throttleInfo.getResourceLevelThrottleKey(), throttleInfo.getResourceTier(),
                throttleInfo.getAuthorizedUser(), throttleInfo.getApiContext(), throttleInfo.getApiVersion(),
                throttleInfo.getSubscriberTenantDomain(), throttleInfo.getSubscriberTenantDomain(),
                throttleInfo.getApplicationId(), throttleInfo.getApiName(), properties.toString() };

        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(
                THROTTLE_STREAM_ID, System.currentTimeMillis(), null, null, objects);

        ThrottleDataPublisher throttleDataPublisher = ServiceReferenceHolder.getInstance().getThrottleDataPublisher();
        if (throttleDataPublisher != null) {
            int count = 1;
            DataPublisher publisher = ThrottleDataPublisher.getDataPublisher();
            while (count <= eventCount) {
                publisher.tryPublish(event);
                count++;
            }
        } else {
            log.error("Cannot publish events to traffic manager because ThrottleDataPublisher "
                              + "has not been initialised");
        }
    }

    public static boolean isRequestBlocked(AuthenticationContext authContext, String apiContext, String apiVersion,
                                           String authorizedUser, String clientIp, String apiTenantDomain) {

        ThrottleDataHolder throttleDataHolder = ServiceReferenceHolder.getInstance().getThrottleDataHolder();
        if (throttleDataHolder.isBlockingConditionsPresent()) {
            String appLevelBlockingKey = authContext.getSubscriber() + ":" + authContext.getApplicationName();
            String subscriptionLevelBlockingKey =
                    apiContext + ":" + apiVersion + ":" + authContext.getSubscriber() + "-" + authContext
                            .getApplicationName() + ":" + authContext.getKeyType();
            return throttleDataHolder.isRequestBlocked(apiContext, appLevelBlockingKey, authorizedUser, clientIp,
                                                       apiTenantDomain, subscriptionLevelBlockingKey);
        }
        return false;
    }
}
