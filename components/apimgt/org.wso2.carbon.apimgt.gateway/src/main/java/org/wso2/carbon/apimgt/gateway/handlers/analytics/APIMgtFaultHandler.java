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
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.gateway.mediators.APIMgtCommonExecutionPublisher;
import org.wso2.carbon.apimgt.usage.publisher.dto.FaultPublisherDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class APIMgtFaultHandler extends APIMgtCommonExecutionPublisher {

    public APIMgtFaultHandler() {
        super();
    }

    public boolean mediate(MessageContext messageContext) {
        super.mediate(messageContext);
        if (publisher == null) {
            this.initializeDataPublisher();
        }
        try {
            if (!enabled) {
                return true;
            }
            long requestTime = Long.parseLong((String) messageContext.getProperty(APIMgtGatewayConstants.
                                                                         REQUEST_START_TIME));
            FaultPublisherDTO faultPublisherDTO = new FaultPublisherDTO();
            faultPublisherDTO.setConsumerKey((String) messageContext.getProperty(
                    APIMgtGatewayConstants.CONSUMER_KEY));
            faultPublisherDTO.setContext((String) messageContext.getProperty(
                    APIMgtGatewayConstants.CONTEXT));
            faultPublisherDTO.setApiVersion((String) messageContext.getProperty(
                    APIMgtGatewayConstants.API_VERSION));
            faultPublisherDTO.setApi((String) messageContext.getProperty(
                    APIMgtGatewayConstants.API));
            faultPublisherDTO.setResourcePath((String) messageContext.getProperty(
                    APIMgtGatewayConstants.RESOURCE));
            faultPublisherDTO.setMethod((String) messageContext.getProperty(
                    APIMgtGatewayConstants.HTTP_METHOD));
            faultPublisherDTO.setVersion((String) messageContext.getProperty(
                    APIMgtGatewayConstants.VERSION));
            faultPublisherDTO.setErrorCode(String.valueOf(messageContext.getProperty(
                    SynapseConstants.ERROR_CODE)));
            faultPublisherDTO.setErrorMessage((String) messageContext.getProperty(
                    SynapseConstants.ERROR_MESSAGE));
            faultPublisherDTO.setRequestTime(requestTime);
            faultPublisherDTO.setUsername((String) messageContext.getProperty(
                    APIMgtGatewayConstants.USER_ID));
            faultPublisherDTO.setHostName((String) messageContext.getProperty(
                    APIMgtGatewayConstants.HOST_NAME));
            String apiPublisher=(String) messageContext.getProperty(
                    APIMgtGatewayConstants.API_PUBLISHER);
            if (apiPublisher == null) {
                String fullRequestPath = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
                String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(fullRequestPath);
                String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
                apiPublisher = APIUtil.getAPIProviderFromRESTAPI(apiVersion, tenantDomain);
            }
            faultPublisherDTO.setApiPublisher(apiPublisher);
            faultPublisherDTO.setTenantDomain(MultitenantUtils.getTenantDomain(apiPublisher));
            faultPublisherDTO.setApplicationName((String) messageContext.getProperty(
                    APIMgtGatewayConstants.APPLICATION_NAME));
            faultPublisherDTO.setApplicationId((String) messageContext.getProperty(
                    APIMgtGatewayConstants.APPLICATION_ID));
            String protocol = (String) messageContext.getProperty(
                    SynapseConstants.TRANSPORT_IN_NAME);
            faultPublisherDTO.setProtocol(protocol);

            publisher.publishEvent(faultPublisherDTO);

        } catch (Exception e) {
            log.error("Cannot publish event. " + e.getMessage(), e);
        }
        return true; // Should never stop the message flow
    }

    public boolean isContentAware() {
        return false;
    }
}
