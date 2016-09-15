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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import org.apache.axis2.util.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class APIStateChangeWSWorkflowExecutor extends WorkflowExecutor {

    private String clientId;
    private String clientSecret;
    private String tokenAPI;
    private String serviceEndpoint;
    private String username;
    private String password;
    private String processDefinitionKey;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
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
        // build request payload

        String jsonPayload = buildPayloadForBPMNProcess(apiStateWorkFlowDTO);

        URL serviceEndpointURL = new URL(serviceEndpoint);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpPost httpPost = new HttpPost(serviceEndpoint);
        String authHeader = getBasicAuthHeader();
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        StringEntity requestEntity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);

        httpPost.setEntity(requestEntity);
        try {
            httpClient.execute(httpPost);
        } catch (ClientProtocolException e) {
            log.error("Error while creating the http client", e);
            throw new WorkflowException("Error while creating the http client", e);
        } catch (IOException e) {
            log.error("Error while connecting to the external service", e);
            throw new WorkflowException("Error while connecting to the external service", e);
        } finally {
            httpPost.reset();
        }
        super.execute(workflowDTO);
        super.publishEvents(workflowDTO);
        return new GeneralWorkflowResponse();
    }

    private String getBasicAuthHeader() {

        // if credentials are not defined in the workflow-extension.xml file, then get the global credentials from the
        // api-manager.xml configuration
        if (username == null || password == null) {
            // ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration().getProperty(key);
            // TODO implement
            username = "admin";
            password = "admin";
        }
        byte[] encodedAuth = Base64.encodeBase64((username + ":" + password).getBytes(Charset.forName("ISO-8859-1")));
        return "Basic " + new String(encodedAuth);
    }

    private String buildPayloadForBPMNProcess(APIStateWorkflowDTO apiStateWorkFlowDTO) {

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

        JSONObject tokenAPIObj = new JSONObject();
        tokenAPIObj.put("name", "tokenAPI");
        tokenAPIObj.put("value", apiStateWorkFlowDTO.getTokenAPI());
        variables.add(tokenAPIObj);

        JSONObject apiCurrentStateObj = new JSONObject();
        apiCurrentStateObj.put("name", "apiCurrentState");
        apiCurrentStateObj.put("value", apiStateWorkFlowDTO.getApiCurrentState());
        variables.add(apiCurrentStateObj);

        JSONObject apiLCActionObj = new JSONObject();
        apiLCActionObj.put("name", "apiLCAction");
        apiLCActionObj.put("value", apiStateWorkFlowDTO.getApiLCAction());
        variables.add(apiLCActionObj);

        JSONObject apiNameObj = new JSONObject();
        apiNameObj.put("name", "apiName");
        apiNameObj.put("value", apiStateWorkFlowDTO.getApiName());
        variables.add(apiNameObj);

        JSONObject apiVersionObj = new JSONObject();
        apiVersionObj.put("name", "apiVersion");
        apiVersionObj.put("value", apiStateWorkFlowDTO.getApiVersion());
        variables.add(apiVersionObj);

        JSONObject apiProviderObj = new JSONObject();
        apiProviderObj.put("name", "apiProvider");
        apiProviderObj.put("value", apiStateWorkFlowDTO.getApiProvider());
        variables.add(apiProviderObj);

        JSONObject callbackUrlObj = new JSONObject();
        callbackUrlObj.put("name", "callbackUrl");
        callbackUrlObj.put("value", apiStateWorkFlowDTO.getCallbackUrl());
        variables.add(callbackUrlObj);

        JSONObject wfReferenceObj = new JSONObject();
        wfReferenceObj.put("name", "wfReference");
        wfReferenceObj.put("value", apiStateWorkFlowDTO.getExternalWorkflowReference());
        variables.add(wfReferenceObj);

        JSONObject invokerObj = new JSONObject();
        invokerObj.put("name", "invoker");
        invokerObj.put("value", apiStateWorkFlowDTO.getInvoker());
        variables.add(invokerObj);

        JSONObject payload = new JSONObject();
        payload.put("processDefinitionKey", processDefinitionKey);
        payload.put("tenantId", apiStateWorkFlowDTO.getTenantId());
        payload.put("variables", variables);

        return payload.toJSONString();
    }

    private void setOAuthApplicationInfo(APIStateWorkflowDTO apiStateWorkFlowDTO) {
        // if credentials are not defined in the workflow-extension.xml file call dcr endpoint and generate a
        // oauth application and pass the client id and secret
        if (clientId == null || clientSecret == null) {

        }
        apiStateWorkFlowDTO.setClientId(clientId);
        apiStateWorkFlowDTO.setClientSecret(clientSecret);
        apiStateWorkFlowDTO.setScope(WorkflowConstants.API_WF_SCOPE);
        apiStateWorkFlowDTO.setTokenAPI(tokenAPI);

    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);

        String action = workflowDTO.getAttributes().get("apiLCAction");
        String apiName = workflowDTO.getAttributes().get("apiName");
        String providerName = workflowDTO.getAttributes().get("apiProvider");
        String version = workflowDTO.getAttributes().get("apiVersion");
        String invoker = workflowDTO.getAttributes().get("invoker");
        int tenantId = workflowDTO.getTenantId();

        try {
            // tenant flow is already started from the rest api service impl. no need to start from here
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceUserRegistry(invoker, tenantId);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
            GenericArtifact apiArtifact = APIUtil.getAPIArtifact(apiIdentifier, registry);
            if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
                apiArtifact.setAttribute(APIConstants.API_WORKFLOW_STATE_ATTR, WorkflowStatus.APPROVED.toString());
                APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(invoker);
                apiProvider.changeLifeCycleStatus(apiIdentifier, action);
            } else if (WorkflowStatus.REJECTED.equals(workflowDTO.getStatus())) {
                apiArtifact.setAttribute(APIConstants.API_WORKFLOW_STATE_ATTR, WorkflowStatus.REJECTED.toString());
            }

        } catch (RegistryException e) {
            log.error("Could not complete api state change workflow", e);
            throw new WorkflowException("Could not complete api state change workflow", e);
        } catch (APIManagementException e) {
            log.error("Could not complete api state change workflow", e);
            throw new WorkflowException("Could not complete api state change workflow", e);
        } catch (FaultGatewaysException e) {
            log.error("Could not complete api state change workflow", e);
            throw new WorkflowException("Could not complete api state change workflow", e);
        }

        return new GeneralWorkflowResponse();
    }

    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {
        // TODO implement what should happen when api is deleted
        super.cleanUpPendingTask(workflowExtRef);
    }

}
