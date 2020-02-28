/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.gateway.mediators.APIMgtCommonExecutionPublisher;
import org.wso2.carbon.apimgt.usage.publisher.dto.ThrottlePublisherDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/*
* This is the class mediator which will handle publishing events upon throttle out events
*/
public class APIMgtThrottleUsageHandler extends APIMgtCommonExecutionPublisher {

    public APIMgtThrottleUsageHandler() {
        super();
    }

    public boolean mediate(MessageContext messageContext) {
        
        if (publisher == null) {
            this.initializeDataPublisher();
        }

        try {
            if (!enabled) {
                return true;
            }
            // gets the access token and username
            AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext
                    (messageContext);
            if (authContext != null) {
                long currentTime = System.currentTimeMillis();
                String throttleOutReason = APIConstants.THROTTLE_OUT_REASON_SOFT_LIMIT_EXCEEDED;

                if(messageContext.getProperty(APIConstants.THROTTLE_OUT_REASON_KEY) != null){
                    throttleOutReason = (String) messageContext.getProperty(APIConstants.THROTTLE_OUT_REASON_KEY);
                }

                ThrottlePublisherDTO throttlePublisherDTO = new ThrottlePublisherDTO();
                String consumerKey = authContext.getApiKey();
                int hashCode = -1; //set -1 for non auth users
                if (consumerKey != null) {
                    hashCode = consumerKey.hashCode();
                }
                String keyType = (String) messageContext.getProperty(APIConstants.API_KEY_TYPE);
                String correlationID = GatewayUtils.getAndSetCorrelationID(messageContext);

                throttlePublisherDTO.setAccessToken(String.valueOf(hashCode));
                String username = authContext.getUsername();
                throttlePublisherDTO.setUsername(username);
                throttlePublisherDTO.setTenantDomain(MultitenantUtils.getTenantDomain(username));
                throttlePublisherDTO.setApiCreatorTenantDomain(MultitenantUtils.getTenantDomain(
                        (String) messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER)));
                throttlePublisherDTO.setApiname((String) messageContext.getProperty(
                        APIMgtGatewayConstants.API));
                throttlePublisherDTO.setVersion(
                        ((String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API)).split(":v")[1]);
                throttlePublisherDTO.setContext((String) messageContext.getProperty(
                        APIMgtGatewayConstants.CONTEXT));
                throttlePublisherDTO.setApiCreator((String) messageContext.getProperty(
                        APIMgtGatewayConstants.API_PUBLISHER));
                String applicationName = (String) messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_NAME);
                String applicationId = (String) messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_ID);
                if (applicationName == null || "".equals(applicationName)) {
                    applicationName = "None";
                    applicationId = "0";
                }
                throttlePublisherDTO.setApplicationName(applicationName);
                throttlePublisherDTO.setApplicationId(applicationId);
                throttlePublisherDTO.setThrottledTime(currentTime);
                throttlePublisherDTO.setThrottledOutReason(throttleOutReason);
                String subscriber = authContext.getSubscriber();
                if (subscriber == null || "".equals(subscriber)) {
                    subscriber = "None";
                }
                throttlePublisherDTO.setSubscriber(subscriber);
                throttlePublisherDTO.setKeyType(keyType);
                throttlePublisherDTO.setCorrelationID(correlationID);
                throttlePublisherDTO.setGatewayType(APIMgtGatewayConstants.GATEWAY_TYPE);
                throttlePublisherDTO.setHostName(GatewayUtils.getHostName(messageContext));
                Map<String, String> properties = Utils.getCustomAnalyticsProperties(messageContext);
                throttlePublisherDTO.setProperties(properties);
                
                if (log.isDebugEnabled()) {
                    log.debug("Publishing throttling event from gateway to analytics for: "
                            + messageContext.getProperty(APIMgtGatewayConstants.CONTEXT) + " with ID: "
                            + messageContext.getMessageID() + " started" + " at "
                            + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
                }
                publisher.publishEvent(throttlePublisherDTO);
                if (log.isDebugEnabled()) {
                    log.debug("Publishing throttling event from gateway to analytics for: "
                            + messageContext.getProperty(APIMgtGatewayConstants.CONTEXT) + " with ID: "
                            + messageContext.getMessageID() + " ended" + " at "
                            + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
                }
            }
        } catch (Exception e) {
            log.error("Cannot publish throttling event. " + e.getMessage(), e);
        }
        return true; // Should never stop the message flow
    }

    public boolean isContentAware() {
        return false;
    }
}

