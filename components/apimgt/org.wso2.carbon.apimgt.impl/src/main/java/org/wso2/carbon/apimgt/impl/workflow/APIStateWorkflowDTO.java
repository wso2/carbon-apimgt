/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.workflow;

import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

public class APIStateWorkflowDTO extends WorkflowDTO{
	
	private String apiCurrentState;
	private String apiLCAction;
	private String scope;
    private String tokenAPI;
    private String clientId;
    private String clientSecret;
    private String apiName;
    private String apiProvider;
    private String apiVersion;
    public String getApiCurrentState() {
        return apiCurrentState;
    }
    public void setApiCurrentState(String apiCurrentState) {
        this.apiCurrentState = apiCurrentState;
    }
    public String getApiLCAction() {
        return apiLCAction;
    }
    public void setApiLCAction(String apiLCAction) {
        this.apiLCAction = apiLCAction;
    }
    public String getScope() {
        return scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }
    public String getTokenAPI() {
        return tokenAPI;
    }
    public void setTokenAPI(String tokenAPI) {
        this.tokenAPI = tokenAPI;
    }
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public String getClientSecret() {
        return clientSecret;
    }
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    public String getApiName() {
        return apiName;
    }
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }
    public String getApiProvider() {
        return apiProvider;
    }
    public void setApiProvider(String apiProvider) {
        this.apiProvider = apiProvider;
    }
    public String getApiVersion() {
        return apiVersion;
    }
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    
    
	

}
