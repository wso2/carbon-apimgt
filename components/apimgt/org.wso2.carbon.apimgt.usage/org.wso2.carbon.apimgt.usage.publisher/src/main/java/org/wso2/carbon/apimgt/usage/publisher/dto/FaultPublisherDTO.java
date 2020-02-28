/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class FaultPublisherDTO {
    private String metaClientType;
    private String applicationConsumerKey;
    private String apiName;
    private String apiVersion;
    private String apiContext;
    private String apiResourcePath;
    private String apiMethod;
    private String apiCreator;
    private String apiCreatorTenantDomain;
    private String username;
    private String userTenantDomain;
    private String protocol;
    private String applicationName;
    private String applicationId;
    private String hostname;
    private String errorCode;
    private String errorMessage;
    private String gatewaType;
    private long requestTimestamp;
    private Map<String, String> properties;

    public String getUserTenantDomain() {
        return userTenantDomain;
    }

    public void setUserTenantDomain(String userTenantDomain) {
        this.userTenantDomain = userTenantDomain;
    }

    public String getMetaClientType() {
        return metaClientType;
    }

    public void setMetaClientType(String metaClientType) {
        this.metaClientType = metaClientType;
    }

    public String getApplicationConsumerKey() {
        return applicationConsumerKey;
    }

    public void setApplicationConsumerKey(String applicationConsumerKey) {
        this.applicationConsumerKey = applicationConsumerKey;
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

    public String getApiContext() {
        return apiContext;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    public String getApiResourcePath() {
        return apiResourcePath;
    }

    public void setApiResourcePath(String apiResourcePath) {
        this.apiResourcePath = apiResourcePath;
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

    public void setApiCreator(String apiProvider) {
        this.apiCreator = apiProvider;
    }

    public String getApiCreatorTenantDomain() {
        return apiCreatorTenantDomain;
    }

    public void setApiCreatorTenantDomain(String apiCreatorTenantDomain) {
        this.apiCreatorTenantDomain = apiCreatorTenantDomain;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
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

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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

    public long getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(long requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public String getGatewaType() {
        return gatewaType;
    }

    public void setGatewaType(String gatewaType) {
        this.gatewaType = gatewaType;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
