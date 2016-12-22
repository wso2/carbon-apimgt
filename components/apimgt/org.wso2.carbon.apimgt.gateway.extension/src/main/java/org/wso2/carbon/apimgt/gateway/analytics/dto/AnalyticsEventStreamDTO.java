package org.wso2.carbon.apimgt.gateway.analytics.dto;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Schema definition of the analytics event Stream. APIM publish this type of event stream to the analyzer.
 */
public class AnalyticsEventStreamDTO {
    //    headers
    private String userAgent;
    private String hostName;
    private String method;
    private String clientIp;
    private String protocol;

    //    API metadata
    private String apiName;
    private String context;
    private String version;
    private String creator;
    private String endpoint;
    private String resourcePath;
    private String uriTemplate;

    //    Application meta data
    private String consumerKey;
    private String applicationName;
    private String applicationId;
    private String applicationOwner;
    private String userId;
    private String subscriber;

    //    Gateway/Publisher metadata
    private String gatewayIp;
    private String gatewayDomain;

    //    Throttle config
    private String isThrottled;
    private String throttledReason;
    private String throttledPolicy;
    private String subscriptionPolicy;

    //    Request data
    private boolean isRequestDataExist;
    private String requestTime;
    private String requestCount;

    //    Response data
    private boolean isResponseDataExist;
    private String responseTime;
    private String serviceTime;
    private String backendTime;
    private String backendLatency;
    private String securityLatency;
    private String throttlingLatency;
    private String requestMediationLatency;
    private String responseMediationLatency;
    private String otherLatency;
    private String responseCount;
    private String cacheHit;
    private String responseSize;
    private String responseCode;

    //    Fault data
    private boolean isFaultDataExist;
    private String errorCode;
    private String errorMessage;
    private String faultCount;

    //    Throttle data
    private boolean isThrottleDataExist;
    private String throttledTime;
    private String throttledCount;
    //    private String throttledReason;
    //    private String throttledPolicy;

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(String subscriber) {
        this.subscriber = subscriber;
    }

    public String getGatewayIp() {
        return gatewayIp;
    }

    public void setGatewayIp(String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }

    public String getGatewayDomain() {
        return gatewayDomain;
    }

    public void setGatewayDomain(String gatewayDomain) {
        this.gatewayDomain = gatewayDomain;
    }

    public String getIsThrottled() {
        return isThrottled;
    }

    public void setIsThrottled(String isThrottled) {
        this.isThrottled = isThrottled;
    }

    public String getThrottledReason() {
        return throttledReason;
    }

    public void setThrottledReason(String throttledReason) {
        this.throttledReason = throttledReason;
    }

    public String getThrottledPolicy() {
        return throttledPolicy;
    }

    public void setThrottledPolicy(String throttledPolicy) {
        this.throttledPolicy = throttledPolicy;
    }

    public String getSubscriptionPolicy() {
        return subscriptionPolicy;
    }

    public void setSubscriptionPolicy(String subscriptionPolicy) {
        this.subscriptionPolicy = subscriptionPolicy;
    }

    public boolean getIsRequestDataExist() {
        return isRequestDataExist;
    }

    public void setIsRequestDataExist(boolean isRequestDataExist) {
        this.isRequestDataExist = isRequestDataExist;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(String requestCount) {
        this.requestCount = requestCount;
    }

    public boolean getIsResponseDataExist() {
        return isResponseDataExist;
    }

    public void setIsResponseDataExist(boolean isResponseDataExist) {
        this.isResponseDataExist = isResponseDataExist;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }

    public String getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(String serviceTime) {
        this.serviceTime = serviceTime;
    }

    public String getBackendTime() {
        return backendTime;
    }

    public void setBackendTime(String backendTime) {
        this.backendTime = backendTime;
    }

    public String getBackendLatency() {
        return backendLatency;
    }

    public void setBackendLatency(String backendLatency) {
        this.backendLatency = backendLatency;
    }

    public String getSecurityLatency() {
        return securityLatency;
    }

    public void setSecurityLatency(String securityLatency) {
        this.securityLatency = securityLatency;
    }

    public String getThrottlingLatency() {
        return throttlingLatency;
    }

    public void setThrottlingLatency(String throttlingLatency) {
        this.throttlingLatency = throttlingLatency;
    }

    public String getRequestMediationLatency() {
        return requestMediationLatency;
    }

    public void setRequestMediationLatency(String requestMediationLatency) {
        this.requestMediationLatency = requestMediationLatency;
    }

    public String getResponseMediationLatency() {
        return responseMediationLatency;
    }

    public void setResponseMediationLatency(String responseMediationLatency) {
        this.responseMediationLatency = responseMediationLatency;
    }

    public String getOtherLatency() {
        return otherLatency;
    }

    public void setOtherLatency(String otherLatency) {
        this.otherLatency = otherLatency;
    }

    public String getResponseCount() {
        return responseCount;
    }

    public void setResponseCount(String responseCount) {
        this.responseCount = responseCount;
    }

    public String getCacheHit() {
        return cacheHit;
    }

    public void setCacheHit(String cacheHit) {
        this.cacheHit = cacheHit;
    }

    public String getResponseSize() {
        return responseSize;
    }

    public void setResponseSize(String responseSize) {
        this.responseSize = responseSize;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public boolean getIsFaultDataExist() {
        return isFaultDataExist;
    }

    public void setIsFaultDataExist(boolean isFaultDataExist) {
        this.isFaultDataExist = isFaultDataExist;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean getIsThrottleDataExist() {
        return isThrottleDataExist;
    }

    public void setIsThrottleDataExist(boolean isThrottleDataExist) {
        this.isThrottleDataExist = isThrottleDataExist;
    }

    public String getThrottledTime() {
        return throttledTime;
    }

    public void setThrottledTime(String throttledTime) {
        this.throttledTime = throttledTime;
    }

    public String getFaultCount() {
        return faultCount;
    }

    public void setFaultCount(String faultCount) {
        this.faultCount = faultCount;
    }

    public String getThrottledCount() {
        return throttledCount;
    }

    public void setThrottledCount(String throttledCount) {
        this.throttledCount = throttledCount;
    }
}
