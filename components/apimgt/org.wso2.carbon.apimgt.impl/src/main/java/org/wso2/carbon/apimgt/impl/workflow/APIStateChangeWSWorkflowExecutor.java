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

import java.util.Collections;
import java.util.List;

import org.apache.axis2.util.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

public class APIStateChangeWSWorkflowExecutor extends WorkflowExecutor{
    
    private String clientId;
    private String clientSecret;
    private String tokenAPI;
    private String serviceEndpoint;
    
    private static final Log log = LogFactory.getLog(APIStateChangeWSWorkflowExecutor.class);
  
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

    public String getTokenAPI() {
        return tokenAPI;
    }

    public void setTokenAPI(String tokenAPI) {
        this.tokenAPI = tokenAPI;
    }
    
    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    @Override
    public String getWorkflowType() {        
        return WorkflowConstants.WF_TYPE_AM_API_STATE;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {      
        return Collections.emptyList();
    } 
    
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Executing API State change Workflow..");
        }
        
        
        
       
        APIStateWorkflowDTO apiStateWorkFlowDTO = (APIStateWorkflowDTO) workflowDTO;
        
        setOAuthApplicationInfo(apiStateWorkFlowDTO);
        //build request payload
        JSONObject payload = new JSONObject();
        payload.put("clientId", apiStateWorkFlowDTO.getClientId());
        payload.put("clientSecret", apiStateWorkFlowDTO.getClientSecret());
        payload.put("scope", apiStateWorkFlowDTO.getScope());
        payload.put("tokenAPI", apiStateWorkFlowDTO.getTokenAPI());
        payload.put("apiCurrentState", apiStateWorkFlowDTO.getApiCurrentState());
        payload.put("apiLCAction", apiStateWorkFlowDTO.getApiLCAction());
        payload.put("apiName", apiStateWorkFlowDTO.getApiName());
        payload.put("apiVersion", apiStateWorkFlowDTO.getApiVersion());
        payload.put("apiProvider", apiStateWorkFlowDTO.getApiProvider());
        payload.put("callbackUrl", apiStateWorkFlowDTO.getCallbackUrl());
        payload.put("wfReference", apiStateWorkFlowDTO.getExternalWorkflowReference());
        
        JSONArray variables = new JSONArray();
        
        JSONObject clientIdObj = new JSONObject();
        clientIdObj.put("name", "clientId");
        clientIdObj.put("value", apiStateWorkFlowDTO.getClientId());
        variables.add(clientIdObj);
        
        JSONObject clientSecretObj = new JSONObject();
        clientSecretObj.put("name", "clientSecret");
        clientSecretObj.put("value", apiStateWorkFlowDTO.getClientSecret());
        variables.add(clientSecretObj);
        
        JSONObject scopeObj = new JSONObject();
        scopeObj.put("name", "scope");
        scopeObj.put("value", apiStateWorkFlowDTO.getScope());
        variables.add(scopeObj);
        
        
        URL serviceEndpointURL = new URL(serviceEndpoint);       
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpPost httpPost = new HttpPost(serviceEndpoint);
        
        
        return super.execute(workflowDTO);
    }
    
    private void setOAuthApplicationInfo(APIStateWorkflowDTO apiStateWorkFlowDTO) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        return super.complete(workflowDTO);
    }
}
