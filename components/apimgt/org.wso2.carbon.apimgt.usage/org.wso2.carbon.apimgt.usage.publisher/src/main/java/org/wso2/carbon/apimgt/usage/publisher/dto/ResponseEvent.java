/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.usage.publisher.dto;

/**
 * Response event data DTO
 */
public class ResponseEvent extends AnalyticsEvent {
    private String apiMethod;
    private String apiResourceTemplate;
    private String destination;
    private String responseCacheHit;
    private String responseLatency;
    private String backendLatency;
    private String requestMediationLatency;
    private String responseMediationLatency;

    public String getApiMethod() {
        return apiMethod;
    }

    public void setApiMethod(String apiMethod) {
        this.apiMethod = apiMethod;
    }

    public String getApiResourceTemplate() {
        return apiResourceTemplate;
    }

    public void setApiResourceTemplate(String apiResourceTemplate) {
        this.apiResourceTemplate = apiResourceTemplate;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getResponseCacheHit() {
        return responseCacheHit;
    }

    public void setResponseCacheHit(String responseCacheHit) {
        this.responseCacheHit = responseCacheHit;
    }

    public String getResponseLatency() {
        return responseLatency;
    }

    public void setResponseLatency(String responseLatency) {
        this.responseLatency = responseLatency;
    }

    public String getBackendLatency() {
        return backendLatency;
    }

    public void setBackendLatency(String backendLatency) {
        this.backendLatency = backendLatency;
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
}
