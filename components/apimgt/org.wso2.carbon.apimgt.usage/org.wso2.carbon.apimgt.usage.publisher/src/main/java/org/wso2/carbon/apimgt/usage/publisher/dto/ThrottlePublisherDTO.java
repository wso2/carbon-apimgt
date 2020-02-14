/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

public class ThrottlePublisherDTO {
    private String accessToken;
    private String tenantDomain;
    private String apiname;
    private String context;
    private String version;
    private String apiCreator;
    private String apiCreatorTenantDomain;
    private String applicationName;
    private String applicationId;
    private String subscriber;
    private long throttledTime; //The timestamp which throttle out event triggers
    private String gatewayType;
    private String throttledOutReason;
    private String username;
    private String keyType;
    private String correlationID;
    private String hostName;
    private Map<String, String> properties;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getApiname() {
        return apiname;
    }

    public void setApiname(String apiname) {
        this.apiname = apiname;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContext() {
        return context;
    }

    public void setThrottledTime(long throttledTime) {
        this.throttledTime = throttledTime;
    }

    public long getThrottledTime() {
        return throttledTime;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getApiCreator() {
        return apiCreator;
    }

    public void setApiCreator(String creator) {
        this.apiCreator = creator;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public void setApplicationName(String app) {
        this.applicationName = app;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationId(String appId) {
        this.applicationId = appId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getThrottledOutReason() {
        return throttledOutReason;
    }

    public void setThrottledOutReason(String throttledOutReason) {
        this.throttledOutReason = throttledOutReason;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }

    public String getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(String subscriber) {
        this.subscriber = subscriber;
    }

    public String getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }

    public String getApiCreatorTenantDomain() {
        return apiCreatorTenantDomain;
    }

    public void setApiCreatorTenantDomain(String apiProviderTenantDomain) {
        this.apiCreatorTenantDomain = apiProviderTenantDomain;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
