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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtTokenInfoDTO implements Serializable {

    private String subscriber;
    private long expirationTime;
    private long issuedTime;
    private String endUserName;
    private boolean contentAware;
    private String[] audience;
    private List<SubscribedApiDTO> subscribedApiDTOList = new ArrayList<SubscribedApiDTO>();
    private Map<String, SubscriptionPolicyDTO> subscriptionPolicyDTOList = new HashMap<String, SubscriptionPolicyDTO>();

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

    public String[] getAudience() {
        return audience;
    }

    public void setAudience(String[] audience) {
        this.audience = audience;
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

}
