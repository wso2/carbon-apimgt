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

package org.wso2.carbon.apimgt.gateway.mediators;


import org.apache.commons.lang.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.usage.publisher.dto.ExecutionTimePublisherDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.internal.ServiceReferenceHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class APIMgtCommonExecutionPublisher extends AbstractMediator {
    protected boolean enabled;

    protected boolean skipEventReceiverConnection;

    protected volatile APIMgtUsageDataPublisher publisher;

    public APIMgtCommonExecutionPublisher() {
        if (ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService() != null) {
            this.initializeDataPublisher();
        }

    }

    @Override
    public boolean mediate(MessageContext messageContext) {
        if (enabled
                && !skipEventReceiverConnection) {
            Object totalTimeObject = messageContext.getProperty(APIMgtGatewayConstants
                    .REQUEST_EXECUTION_START_TIME);
            long totalTime = 0;
            if (totalTimeObject != null) {
                totalTime = Long.parseLong((String) totalTimeObject);
            }
            totalTime = System.currentTimeMillis() - totalTime;
            String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
            String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
            String apiContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
            String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(RESTUtils.getFullRequestPath
                    (messageContext));
            if(StringUtils.isEmpty(tenantDomain)){
                tenantDomain = org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            }
            String provider = APIUtil.getAPIProviderFromRESTAPI(apiName, tenantDomain);

            ExecutionTimePublisherDTO executionTimePublisherDTO = new ExecutionTimePublisherDTO();
            executionTimePublisherDTO.setApiName(APIUtil.getAPINamefromRESTAPI(apiName));
            executionTimePublisherDTO.setVersion(apiVersion);
            executionTimePublisherDTO.setContext(apiContext);
            executionTimePublisherDTO.setTenantDomain(tenantDomain);
            executionTimePublisherDTO.setApiResponseTime(totalTime);
            executionTimePublisherDTO.setProvider(provider);
            executionTimePublisherDTO.setTenantId(APIUtil.getTenantId(provider));
            Object securityLatency = messageContext.getProperty(APIMgtGatewayConstants.SECURITY_LATENCY);
            executionTimePublisherDTO.setSecurityLatency(securityLatency == null ? 0 :
                                                          ((Number) securityLatency).longValue());
            Object throttleLatency =  messageContext.getProperty(APIMgtGatewayConstants.THROTTLING_LATENCY);
            executionTimePublisherDTO.setThrottlingLatency(throttleLatency == null ? 0 :
                                                          ((Number) throttleLatency).longValue());
            Object reqMediationLatency = messageContext.getProperty(APIMgtGatewayConstants.REQUEST_MEDIATION_LATENCY);
            executionTimePublisherDTO.setRequestMediationLatency(reqMediationLatency == null ? 0 :
                    ((Number) reqMediationLatency).longValue());
            Object resMediationLatency = messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_MEDIATION_LATENCY);
            executionTimePublisherDTO.setResponseMediationLatency(resMediationLatency == null ? 0 :
                    ((Number) resMediationLatency).longValue());
            Object otherLatency = messageContext.getProperty(APIMgtGatewayConstants.OTHER_LATENCY);
            executionTimePublisherDTO.setOtherLatency(otherLatency == null ? 0 :
                    ((Number) otherLatency).longValue());
            Object backendLatency = messageContext.getProperty(APIMgtGatewayConstants.BACKEND_LATENCY);
            executionTimePublisherDTO.setBackEndLatency(backendLatency == null ? 0 :
                    ((Number) backendLatency).longValue());
            executionTimePublisherDTO.setEventTime(System.currentTimeMillis());
            publisher.publishEvent(executionTimePublisherDTO);

        }
        return true;
    }

    protected void initializeDataPublisher() {

        enabled = APIUtil.isAnalyticsEnabled();
        skipEventReceiverConnection = DataPublisherUtil.getApiManagerAnalyticsConfiguration().
                isSkipEventReceiverConnection();
        if (!enabled || skipEventReceiverConnection) {
            return;
        }
        if (publisher == null) {
            synchronized (this) {
                if (publisher == null) {
                    String publisherClass = DataPublisherUtil.getApiManagerAnalyticsConfiguration()
                            .getPublisherClass();
                    try {
                        log.debug("Instantiating Data Publisher");
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().
                                setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                        APIMgtUsageDataPublisher tempPublisher = (APIMgtUsageDataPublisher) APIUtil.getClassForName
                                (publisherClass).newInstance();
                        tempPublisher.init();
                        publisher = tempPublisher;
                    } catch (ClassNotFoundException e) {
                        log.error("Class not found " + publisherClass, e);
                    } catch (InstantiationException e) {
                        log.error("Error instantiating " + publisherClass, e);
                    } catch (IllegalAccessException e) {
                        log.error("Illegal access to " + publisherClass, e);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
        }
    }
}
