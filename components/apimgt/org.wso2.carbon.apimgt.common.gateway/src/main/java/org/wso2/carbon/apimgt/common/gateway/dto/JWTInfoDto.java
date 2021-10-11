/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.common.gateway.dto;

import org.apache.commons.lang3.SerializationUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds information related to jwt token.
 *
 */
public class JWTInfoDto {
    private String applicationTier;
    private String keyType;
    private String version;
    private String applicationName;
    private String endUser;
    private int endUserTenantId;
    private String applicationUUId;
    private String subscriber;
    private String subscriptionTier;
    private String applicationId;
    private String apiContext;
    private String apiName;
    private JWTValidationInfo jwtValidationInfo;
    private Map<String, String> appAttributes = new HashMap<>();
    private String sub;
    private String[] organizations;

    public JWTInfoDto() {

    }

    public JWTInfoDto(JWTInfoDto jwtInfoDto) {

        this.applicationId = jwtInfoDto.getApplicationId();
        this.keyType = jwtInfoDto.getKeyType();
        this.version = jwtInfoDto.getVersion();
        this.applicationName = jwtInfoDto.getApplicationName();
        this.endUser = jwtInfoDto.getEndUser();
        this.endUserTenantId = jwtInfoDto.getEndUserTenantId();
        this.applicationUUId = jwtInfoDto.getApplicationUUId();
        this.subscriber = jwtInfoDto.getSubscriber();
        this.subscriptionTier = jwtInfoDto.getSubscriptionTier();
        this.applicationTier = jwtInfoDto.getApplicationTier();
        this.apiContext = jwtInfoDto.getApiContext();
        this.apiName = jwtInfoDto.getApiName();
        this.jwtValidationInfo = new JWTValidationInfo(jwtInfoDto.getJwtValidationInfo());
        this.appAttributes = jwtInfoDto.getAppAttributes();
        this.sub = jwtInfoDto.getSub();
        this.organizations = SerializationUtils.clone(jwtInfoDto.getOrganizations());
    }

    public String getApplicationTier() {

        return applicationTier;
    }

    public void setApplicationTier(String applicationTier) {

        this.applicationTier = applicationTier;
    }

    public String getKeyType() {

        return keyType;
    }

    public void setKeyType(String keyType) {

        this.keyType = keyType;
    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public String getApplicationName() {

        return applicationName;
    }

    public void setApplicationName(String applicationName) {

        this.applicationName = applicationName;
    }

    public String getEndUser() {

        return endUser;
    }

    public void setEndUser(String endUser) {

        this.endUser = endUser;
    }

    public int getEndUserTenantId() {

        return endUserTenantId;
    }

    public void setEndUserTenantId(int endUserTenantId) {

        this.endUserTenantId = endUserTenantId;
    }

    public String getApplicationUUId() {

        return applicationUUId;
    }

    public void setApplicationUUId(String applicationUUId) {

        this.applicationUUId = applicationUUId;
    }

    public String getSubscriber() {

        return subscriber;
    }

    public void setSubscriber(String subscriber) {

        this.subscriber = subscriber;
    }

    public String getSubscriptionTier() {

        return subscriptionTier;
    }

    public void setSubscriptionTier(String subscriptionTier) {

        this.subscriptionTier = subscriptionTier;
    }

    public String getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(String applicationId) {

        this.applicationId = applicationId;
    }

    public String getApiContext() {

        return apiContext;
    }

    public void setApiContext(String apiContext) {

        this.apiContext = apiContext;
    }

    public JWTValidationInfo getJwtValidationInfo() {

        return jwtValidationInfo;
    }

    public void setJwtValidationInfo(JWTValidationInfo jwtValidationInfo) {

        this.jwtValidationInfo = jwtValidationInfo;
    }

    /*public MessageContext getMessageContext() {

        return messageContext;
    }

    public void setMessageContext(MessageContext messageContext) {

        this.messageContext = messageContext;
    }*/

    public String getApiName() {

        return apiName;
    }

    public void setApiName(String apiName) {

        this.apiName = apiName;
    }

    public Map<String, String> getAppAttributes() {

        return appAttributes;
    }

    public void setAppAttributes(Map<String, String> appAttributes) {

        this.appAttributes = appAttributes;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String[] getOrganizations() {

        return SerializationUtils.clone(organizations);
    }

    public void setOrganizations(String[] organizations) {

        this.organizations = SerializationUtils.clone(organizations);
    }
}
