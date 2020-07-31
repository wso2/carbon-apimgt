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

package org.wso2.carbon.apimgt.impl.dto;

public class WorkflowProperties {
    private boolean enabled;
    private String serverUrl;
    private String serverUser;
    private String serverPassword;
    private String workflowCallbackAPI;
    private String tokenEndPoint;
    private String dCREndPoint;
    private String dCREndpointUser;
    private String dCREndpointPassword;
    private boolean listTasks = true; //default true
    
    public String getdCREndpointUser() {
        return dCREndpointUser;
    }
    public void setdCREndpointUser(String dCREndpointUser) {
        this.dCREndpointUser = dCREndpointUser;
    }
    public String getdCREndpointPassword() {
        return dCREndpointPassword;
    }
    public void setdCREndpointPassword(String dCREndpointPassword) {
        this.dCREndpointPassword = dCREndpointPassword;
    }
    public String getTokenEndPoint() {
        return tokenEndPoint;
    }
    public void setTokenEndPoint(String tokenEndPoint) {
        this.tokenEndPoint = tokenEndPoint;
    }
    public String getdCREndPoint() {
        return dCREndPoint;
    }
    public void setdCREndPoint(String dCREndPoint) {
        this.dCREndPoint = dCREndPoint;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public String getServerUrl() {
        return serverUrl;
    }
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    public String getServerUser() {
        return serverUser;
    }
    public void setServerUser(String serverUser) {
        this.serverUser = serverUser;
    }
    public String getServerPassword() {
        return serverPassword;
    }
    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }
    public String getWorkflowCallbackAPI() {
        return workflowCallbackAPI;
    }
    public void setWorkflowCallbackAPI(String workflowCallbackAPI) {
        this.workflowCallbackAPI = workflowCallbackAPI;
    }
    public boolean isListTasks() {
        return listTasks;
    }
    public void setListTasks(boolean listTasks) {
        this.listTasks = listTasks;
    }    
}
