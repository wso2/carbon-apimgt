/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.dto.ai;

/**
 * This class represent the Marketplace Assistant configuration DTO.
 */
public class MarketplaceAssistantConfigurationDTO {

    private String accessToken;
    private String key;
    private String endpoint;
    private String tokenEndpoint;
    private String apiPublishResource;
    private String chatResource;
    private String apiDeleteResource;
    private String apiCountResource;
    private boolean isEnabled;
    private boolean isAuthTokenProvided;
    private boolean isKeyProvided;

    public String getAccessToken() {
        return accessToken;
    }

    public String getKey() {
        return key;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getApiPublishResource() {
        return apiPublishResource;
    }

    public void setApiPublishResource(String apiPublishResource) {
        this.apiPublishResource = apiPublishResource;
    }

    public String getChatResource() {
        return chatResource;
    }

    public void setChatResource(String chatResource) {
        this.chatResource = chatResource;
    }

    public String getApiDeleteResource() {
        return apiDeleteResource;
    }

    public void setApiDeleteResource(String apiDeleteResource) {
        this.apiDeleteResource = apiDeleteResource;
    }

    public String getApiCountResource() {
        return apiCountResource;
    }

    public void setApiCountResource(String apiCountResource) {
        this.apiCountResource = apiCountResource;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public boolean isAuthTokenProvided() {
        return isAuthTokenProvided;
    }

    public boolean isKeyProvided() {
        return isKeyProvided;
    }

    public void setAuthTokenProvided(boolean authTokenProvided) {
        this.isAuthTokenProvided = authTokenProvided;
    }

    public void setKeyProvided(boolean keyProvided) {
        this.isKeyProvided = keyProvided;
    }
}
