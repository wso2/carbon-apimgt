/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtTokenInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String subscriber;
    private long expirationTime;
    private long issuedTime;
    private String endUserName;
    private boolean contentAware;
    private List<String> audience;
    private String scopes;
    private String keyType;
    private ApplicationDTO application;
    private String consumerKey;
    private List<SubscribedApiDTO> subscribedApiDTOList = new ArrayList<SubscribedApiDTO>();
    private Map<String, SubscriptionPolicyDTO> subscriptionPolicyDTOList = new HashMap<String, SubscriptionPolicyDTO>();
    private OAuthTokenReqMessageContext tokReqMsgCtx;
    private OAuthAuthzReqMessageContext oauthAuthzMsgCtx;
    private String permittedIP;
    private String permittedReferer;

    public String getPermittedIP() {
        return permittedIP;
    }

    public void setPermittedIP(String permittedIP) {
        this.permittedIP = permittedIP;
    }

    public String getPermittedReferer() {
        return permittedReferer;
    }

    public void setPermittedReferer(String permittedReferer) {
        this.permittedReferer = permittedReferer;
    }

    public String getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(String subscriber) {
        this.subscriber = subscriber;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public long getIssuedTime() {
        return issuedTime;
    }

    public void setIssuedTime(long issuedTime) {
        this.issuedTime = issuedTime;
    }

    public String getEndUserName() {
        return endUserName;
    }

    public void setEndUserName(String endUserName) {
        this.endUserName = endUserName;
    }

    public boolean isContentAware() {
        return contentAware;
    }

    public void setContentAware(boolean contentAware) {
        this.contentAware = contentAware;
    }

    public List<String> getAudience() {
        return audience;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public void setAudience(List<String> audience) {
        this.audience = audience;
    }

    public ApplicationDTO getApplication() {
        return application;
    }

    public void setApplication(ApplicationDTO application) {
        this.application = application;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public List<SubscribedApiDTO> getSubscribedApiDTOList() {
        return subscribedApiDTOList;
    }

    public void setSubscribedApiDTOList(List<SubscribedApiDTO> subscribedApiDTOList) {
        this.subscribedApiDTOList = subscribedApiDTOList;
    }

    public Map<String, SubscriptionPolicyDTO> getSubscriptionPolicyDTOList() {
        return subscriptionPolicyDTOList;
    }

    public void setSubscriptionPolicyDTOList(Map<String, SubscriptionPolicyDTO> subscriptionPolicyDTOList) {
        this.subscriptionPolicyDTOList = subscriptionPolicyDTOList;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public void setTokenReqMessageContext(OAuthTokenReqMessageContext tokReqMsgCtx) {
        this.tokReqMsgCtx = tokReqMsgCtx;
    }

    public OAuthTokenReqMessageContext getTokReqMsgCtx() {
        return tokReqMsgCtx;
    }

    public void setOauthAuthzMsgCtx(OAuthAuthzReqMessageContext oauthAuthzMsgCtx) {
        this.oauthAuthzMsgCtx = oauthAuthzMsgCtx;
    }

    public OAuthAuthzReqMessageContext getOauthAuthzMsgCtx() {
        return oauthAuthzMsgCtx;
    }
}
