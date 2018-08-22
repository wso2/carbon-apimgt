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
import org.wso2.carbon.apimgt.gateway.mediators.APIMgtCommonExecutionPublisher;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.FaultPublisherDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class APIMgtFaultHandler extends APIMgtCommonExecutionPublisher {

    public APIMgtFaultHandler() {
        super();
    }

    public boolean mediate(MessageContext messageContext) {
        
        if (publisher == null) {
            initDataPublisher();
        }
        try {
            if (!enabled) {
                return true;
            }
            long requestTime = Long.parseLong((String) messageContext.getProperty(APIMgtGatewayConstants.
                    REQUEST_START_TIME));
            String keyType = (String) messageContext.getProperty(APIConstants.API_KEY_TYPE);
            String correlationID = GatewayUtils.getAndSetCorrelationID(messageContext);

            FaultPublisherDTO faultPublisherDTO = new FaultPublisherDTO();
            faultPublisherDTO.setApplicationConsumerKey((String) messageContext.getProperty(
                    APIMgtGatewayConstants.CONSUMER_KEY));
            faultPublisherDTO.setApiContext((String) messageContext.getProperty(
                    APIMgtGatewayConstants.CONTEXT));
            faultPublisherDTO.setApiVersion(((String) messageContext.getProperty(
                    APIMgtGatewayConstants.API_VERSION)).split(":v")[1]);
            faultPublisherDTO.setApiName((String) messageContext.getProperty(
                    APIMgtGatewayConstants.API));
            faultPublisherDTO.setApiResourcePath((String) messageContext.getProperty(
                    APIMgtGatewayConstants.RESOURCE));
            faultPublisherDTO.setApiMethod((String) messageContext.getProperty(
                    APIMgtGatewayConstants.HTTP_METHOD));
            faultPublisherDTO.setApiVersion((String) messageContext.getProperty(
                    APIMgtGatewayConstants.VERSION));
            faultPublisherDTO.setErrorCode(String.valueOf(messageContext.getProperty(
                    SynapseConstants.ERROR_CODE)));
            faultPublisherDTO.setErrorMessage((String) messageContext.getProperty(
                    SynapseConstants.ERROR_MESSAGE));
            faultPublisherDTO.setRequestTimestamp(requestTime);
            faultPublisherDTO.setUsername((String) messageContext.getProperty(
                    APIMgtGatewayConstants.USER_ID));
            faultPublisherDTO.setUserTenantDomain(MultitenantUtils.getTenantDomain(faultPublisherDTO.getUsername()));
            faultPublisherDTO.setHostname((String) messageContext.getProperty(
                    APIMgtGatewayConstants.HOST_NAME));
            String apiPublisher = (String) messageContext.getProperty(
                    APIMgtGatewayConstants.API_PUBLISHER);
            if (apiPublisher == null) {
                String fullRequestPath = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
                String tenantDomain = getTenantDomainFromRequestURL(fullRequestPath);
                String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
                apiPublisher = APIUtil.getAPIProviderFromRESTAPI(apiVersion, tenantDomain);
            }
            faultPublisherDTO.setApiCreator(apiPublisher);
            faultPublisherDTO.setApiCreatorTenantDomain(MultitenantUtils.getTenantDomain(apiPublisher));
            faultPublisherDTO.setApplicationName((String) messageContext.getProperty(
                    APIMgtGatewayConstants.APPLICATION_NAME));
            faultPublisherDTO.setApplicationId((String) messageContext.getProperty(
                    APIMgtGatewayConstants.APPLICATION_ID));
            String protocol = (String) messageContext.getProperty(
                    SynapseConstants.TRANSPORT_IN_NAME);
            faultPublisherDTO.setProtocol(protocol);
            faultPublisherDTO.setMetaClientType(keyType);
            faultPublisherDTO.setGatewaType(APIMgtGatewayConstants.SYNAPDE_GW_LABEL);
            if (log.isDebugEnabled()) {
                log.debug("Publishing fault event from gateway to analytics for: " + messageContext.getProperty(
                        APIMgtGatewayConstants.CONTEXT) + " with ID: " + messageContext.getMessageID() + " started"
                        + " at " + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
            }
            publisher.publishEvent(faultPublisherDTO);
            if (log.isDebugEnabled()) {
                log.debug("Publishing fault event from gateway to analytics for: " + messageContext.getProperty(
                        APIMgtGatewayConstants.CONTEXT) + " with ID: " + messageContext.getMessageID() + " ended"
                        + " at " + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
            }

        } catch (Exception e) {
            log.error("Cannot publish event. " + e.getMessage(), e);
        }
        return true; // Should never stop the message flow
    }

    protected String getTenantDomainFromRequestURL(String fullRequestPath) {
        return MultitenantUtils.getTenantDomainFromRequestURL(fullRequestPath);
    }

    protected void initDataPublisher() {
        this.initializeDataPublisher();
    }

    public boolean isContentAware() {
        return false;
    }
}
