/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.api.dto;

import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to transfer data for User/Keys UI in Provider view.
 */
public class UserApplicationAPIUsage {

    private String userId;
    private int appId;
    private String applicationName;
    private String subStatus;
    private String accessToken;
    private String accessTokenStatus;
    private List<SubscribedAPI> apiSubscriptions = new ArrayList<SubscribedAPI>();

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }
    public String getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(String subStatus) {
        this.subStatus = subStatus;
    }

    public SubscribedAPI[] getApiSubscriptions() {
        return apiSubscriptions.toArray(new SubscribedAPI[apiSubscriptions.size()]);
    }

    public void addApiSubscriptions(SubscribedAPI apiSubscription) {
        apiSubscriptions.add(apiSubscription);
    }
    public void setAccessToken(String accessToken){
        this.accessToken=accessToken;
    }
    public String getAccessToken(){
        return accessToken;
    }
    public void setAccessTokenStatus(String accessTokenStatus){
        this.accessTokenStatus=accessTokenStatus;
    }
    public String getAccessTokenStatus(){
        return accessTokenStatus;
    }
}
