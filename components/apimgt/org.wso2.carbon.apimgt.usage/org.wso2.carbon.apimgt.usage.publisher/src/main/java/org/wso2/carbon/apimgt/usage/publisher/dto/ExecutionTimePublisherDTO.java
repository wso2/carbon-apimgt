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
package org.wso2.carbon.apimgt.usage.publisher.dto;

public class ExecutionTimePublisherDTO {
    private String apiName,version,tenantDomain,provider,context;
    private long apiResponseTime;
    private int tenantId;
    private long eventTime;

    private long securityLatency;

    private long throttlingLatency;

    private long requestMediationLatency;

    private long responseMediationLatency;

    private long backEndLatency;

    private long otherLatency;

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public long getApiResponseTime() {
        return apiResponseTime;
    }

    public void setApiResponseTime(long apiResponseTime) {
        this.apiResponseTime = apiResponseTime;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public long getSecurityLatency() {
        return securityLatency;
    }

    public void setSecurityLatency(long securityLatency) {
        this.securityLatency = securityLatency;
    }

    public long getThrottlingLatency() {
        return throttlingLatency;
    }

    public void setThrottlingLatency(long throttlingLatency) {
        this.throttlingLatency = throttlingLatency;
    }

    public long getRequestMediationLatency() {
        return requestMediationLatency;
    }

    public void setRequestMediationLatency(long requestMediationLatency) {
        this.requestMediationLatency = requestMediationLatency;
    }

    public long getBackEndLatency() {
        return backEndLatency;
    }

    public void setBackEndLatency(long backEndLatency) {
        this.backEndLatency = backEndLatency;
    }

    public long getOtherLatency() {
        return otherLatency;
    }

    public void setOtherLatency(long otherLatency) {
        this.otherLatency = otherLatency;
    }

    public long getResponseMediationLatency() {
        return responseMediationLatency;
    }

    public void setResponseMediationLatency(long responseMediationLatency) {
        this.responseMediationLatency = responseMediationLatency;
    }
}
