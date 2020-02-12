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

import java.util.HashMap;
import java.util.Map;

public class RequestResponseStreamDTO {

    private String applicationConsumerKey;
    private String applicationName;
    private String applicationId;
    private String applicationOwner;
    private String apiContext;
    private String apiName;
    private String apiVersion;
    private String apiResourcePath;
    private String apiResourceTemplate;
    private String apiMethod;
    private String apiCreator;
    private String apiCreatorTenantDomain;
    private String apiTier;
    private String apiHostname;
    private String username;
    private String userTenantDomain;
    private String userIp;
    private String userAgent;
    private long serviceTime;
    private long requestTimestamp;
    private boolean throttledOut;
    private long backendTime;
    private boolean responseCacheHit;
    private long responseSize;
    private String protocol;
    private int responseCode;
    private String destination;
    private String metaClientType;
    private long responseTime;
    private String gatewayType;
    private String correlationID;
    private String label;
    private ExecutionTimeDTO executionTime;
    private Map<String, String> properties;

    public String getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }

    public String getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public String getApplicationConsumerKey() {
        return applicationConsumerKey;
    }

    public void setApplicationConsumerKey(String applicationConsumerKey) {
        this.applicationConsumerKey = applicationConsumerKey;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationOwner() {
        return applicationOwner;
    }

    public void setApplicationOwner(String applicationOwner) {
        this.applicationOwner = applicationOwner;
    }

    public String getApiContext() {
        return apiContext;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiResourcePath() {
        return apiResourcePath;
    }

    public void setApiResourcePath(String apiResourcePath) {
        this.apiResourcePath = apiResourcePath;
    }

    public String getApiResourceTemplate() {
        return apiResourceTemplate;
    }

    public void setApiResourceTemplate(String apiResourceTemplate) {
        this.apiResourceTemplate = apiResourceTemplate;
    }

    public String getApiMethod() {
        return apiMethod;
    }

    public void setApiMethod(String apiMethod) {
        this.apiMethod = apiMethod;
    }

    public String getApiCreator() {
        return apiCreator;
    }

    public void setApiCreator(String apiCreator) {
        this.apiCreator = apiCreator;
    }

    public String getApiCreatorTenantDomain() {
        return apiCreatorTenantDomain;
    }

    public void setApiCreatorTenantDomain(String apiCreatorTenantDomain) {
        this.apiCreatorTenantDomain = apiCreatorTenantDomain;
    }

    public String getApiTier() {
        return apiTier;
    }

    public void setApiTier(String apiTier) {
        this.apiTier = apiTier;
    }

    public String getApiHostname() {
        return apiHostname;
    }

    public void setApiHostname(String apiHostname) {
        this.apiHostname = apiHostname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserTenantDomain() {
        return userTenantDomain;
    }

    public void setUserTenantDomain(String userTenantDomain) {
        this.userTenantDomain = userTenantDomain;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public long getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(long serviceTime) {
        this.serviceTime = serviceTime;
    }

    public long getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(long requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public boolean isThrottledOut() {
        return throttledOut;
    }

    public void setThrottledOut(boolean throttledOut) {
        this.throttledOut = throttledOut;
    }

    public long getBackendTime() {
        return backendTime;
    }

    public void setBackendTime(long backendTime) {
        this.backendTime = backendTime;
    }

    public boolean isResponseCacheHit() {
        return responseCacheHit;
    }

    public void setResponseCacheHit(boolean responseCacheHit) {
        this.responseCacheHit = responseCacheHit;
    }

    public long getResponseSize() {
        return responseSize;
    }

    public void setResponseSize(long responseSize) {
        this.responseSize = responseSize;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getMetaClientType() {
        return metaClientType;
    }

    public void setMetaClientType(String metaClientType) {
        this.metaClientType = metaClientType;
    }

    public ExecutionTimeDTO getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(ExecutionTimeDTO executionTime) {
        this.executionTime = executionTime;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
