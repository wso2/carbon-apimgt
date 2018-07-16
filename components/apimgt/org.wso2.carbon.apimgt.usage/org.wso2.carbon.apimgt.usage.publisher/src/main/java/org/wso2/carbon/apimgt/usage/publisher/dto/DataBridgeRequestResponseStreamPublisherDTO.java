/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.usage.publisher.dto;

public class DataBridgeRequestResponseStreamPublisherDTO extends RequestResponseStreamDTO {

    public DataBridgeRequestResponseStreamPublisherDTO(RequestResponseStreamDTO requestStreamDTO) {
        setApiContext(requestStreamDTO.getApiContext());
        setApiHostname(requestStreamDTO.getApiHostname());
        setApiMethod(requestStreamDTO.getApiMethod());
        setApiName(requestStreamDTO.getApiName());
        setApiProviderTenantDomain(requestStreamDTO.getApiProviderTenantDomain());
        setApiPublisher(requestStreamDTO.getApiPublisher());
        setApiResourcePath(requestStreamDTO.getApiResourcePath());
        setApiResourceTemplate(requestStreamDTO.getApiResourceTemplate());
        setApiTier(requestStreamDTO.getApiTier());
        setApiVersion(requestStreamDTO.getApiVersion());
        setApplicationConsumerKey(requestStreamDTO.getApplicationConsumerKey());
        setApplicationId(requestStreamDTO.getApplicationId());
        setApplicationName(requestStreamDTO.getApplicationName());
        setApplicationOwner(requestStreamDTO.getApplicationOwner());
        setBackendTime(requestStreamDTO.getBackendTime());
        setDestination(requestStreamDTO.getDestination());
        setExecutionTime(requestStreamDTO.getExecutionTime());
        setMetaClientType(requestStreamDTO.getMetaClientType()); 
        setProtocol(requestStreamDTO.getProtocol());
        setRequestTimestamp(requestStreamDTO.getRequestTimestamp());
        setResponseCacheHit(requestStreamDTO.isResponseCacheHit());
        setResponseCode(requestStreamDTO.getResponseCode());
        setResponseSize(requestStreamDTO.getResponseSize());
        setServiceTime(requestStreamDTO.getServiceTime());
        setThrottledOut(requestStreamDTO.isThrottledOut());
        setUserAgent(requestStreamDTO.getUserAgent());
        setUserIp(requestStreamDTO.getUserIp());
        setUsername(requestStreamDTO.getUsername());
        setUserTenantDomain(requestStreamDTO.getUserTenantDomain());
    }

    public Object createPayload() {
        return new Object[] { getMetaClientType(), getApplicationConsumerKey(), getApplicationName(),
                getApplicationId(), getApplicationOwner(), getApiContext(), getApiName(), getApiVersion(),
                getApiResourcePath(), getApiResourceTemplate(), getApiMethod(), getApiPublisher(),
                getApiProviderTenantDomain(), getApiTier(), getApiHostname(), getUsername(), getUserTenantDomain(),
                getUserIp(), getUserAgent(), getRequestTimestamp(), isThrottledOut(), getResponseTime(),
                getServiceTime(), getBackendTime(), isResponseCacheHit(), getResponseSize(), getProtocol(),
                getResponseCode(), getDestination(), getExecutionTime().getSecurityLatency(),
                getExecutionTime().getThrottlingLatency(), getExecutionTime().getRequestMediationLatency(),
                getExecutionTime().getResponseMediationLatency(), getExecutionTime().getBackEndLatency(),
                getExecutionTime().getOtherLatency() };
        //TODO add gatewayType, label and id as new properties
    }

    public Object[] createMetaData() {
        // TODO Auto-generated method stub
        return null;
    }
}
