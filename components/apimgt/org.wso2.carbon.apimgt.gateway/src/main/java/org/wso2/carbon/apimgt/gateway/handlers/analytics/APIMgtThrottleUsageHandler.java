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
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.gateway.mediators.APIMgtCommonExecutionPublisher;
import org.wso2.carbon.apimgt.usage.publisher.dto.ThrottlePublisherDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/*
* This is the class mediator which will handle publishing events upon throttle out events
*/
public class APIMgtThrottleUsageHandler extends APIMgtCommonExecutionPublisher {


    public APIMgtThrottleUsageHandler() {
        super();
    }



    public boolean mediate(MessageContext messageContext) {
        super.mediate(messageContext);
        if (publisher == null) {
            this.initializeDataPublisher();
        }

        try {
            if (!enabled || skipEventReceiverConnection) {
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
                throttlePublisherDTO.setAccessToken(String.valueOf(hashCode));
                String username = authContext.getUsername();
                throttlePublisherDTO.setUsername(username);
                throttlePublisherDTO.setTenantDomain(MultitenantUtils.getTenantDomain(
                        (String) messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER)));
                throttlePublisherDTO.setApiname((String) messageContext.getProperty(
                        APIMgtGatewayConstants.API));
                throttlePublisherDTO.setVersion((String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API));
                throttlePublisherDTO.setContext((String) messageContext.getProperty(
                        APIMgtGatewayConstants.CONTEXT));
                throttlePublisherDTO.setProvider((String) messageContext.getProperty(
                        APIMgtGatewayConstants.API_PUBLISHER));
                throttlePublisherDTO.setApplicationName((String) messageContext.getProperty(
                        APIMgtGatewayConstants.APPLICATION_NAME));
                throttlePublisherDTO.setApplicationId((String) messageContext.getProperty(
                        APIMgtGatewayConstants.APPLICATION_ID));
                throttlePublisherDTO.setThrottledTime(currentTime);
                throttlePublisherDTO.setThrottledOutReason(throttleOutReason);
                throttlePublisherDTO.setSubscriber(authContext.getSubscriber());
                publisher.publishEvent(throttlePublisherDTO);


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

