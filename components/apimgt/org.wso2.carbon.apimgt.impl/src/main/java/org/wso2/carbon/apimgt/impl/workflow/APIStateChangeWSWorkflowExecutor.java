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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axis2.util.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.formula.WorkbookDependentFormula;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
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
    private String stateList;

    public String getStateList() {
        return stateList;
    }

    public void setStateList(String stateList) {
        this.stateList = stateList;
    }

    private final String RUNTIME_INSTANCE_RESOURCE_PATH = "/runtime/process-instances";

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

        if (stateList != null) {
            Map<String, List<String>> stateActionMap = getSelectedStatesToApprove();
            APIStateWorkflowDTO apiStateWorkFlowDTO = (APIStateWorkflowDTO) workflowDTO;

            if (stateActionMap.containsKey(apiStateWorkFlowDTO.getApiCurrentState().toUpperCase())
                    && stateActionMap.get(apiStateWorkFlowDTO.getApiCurrentState().toUpperCase())
                            .contains(apiStateWorkFlowDTO.getApiLCAction())) {
                setOAuthApplicationInfo(apiStateWorkFlowDTO);
                // build request payload

                String jsonPayload = buildPayloadForBPMNProcess(apiStateWorkFlowDTO);

                URL serviceEndpointURL = new URL(serviceEndpoint);
                HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(),
                        serviceEndpointURL.getProtocol());
                HttpPost httpPost = new HttpPost(serviceEndpoint + RUNTIME_INSTANCE_RESOURCE_PATH);
                String authHeader = getBasicAuthHeader();
                httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
                StringEntity requestEntity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);

                httpPost.setEntity(requestEntity);
                try {
                    HttpResponse response = httpClient.execute(httpPost);
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_CREATED) {
                        String error = "Error while starting the process:  " + response.getStatusLine().getStatusCode()
                                + " " + response.getStatusLine().getReasonPhrase();
                        log.error(error);
                        throw new WorkflowException(error);
                    }
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
            } else {
                // For any other states, act as simpleworkflow executor.
                workflowDTO.setStatus(WorkflowStatus.APPROVED);
                // calling super.complete() instead of complete() to act as the simpleworkflow executor
                super.complete(workflowDTO);
            }
        } else {
            String msg = "State change list is not provided. Please check <stateList> element in ";
            log.error(msg);
            new WorkflowException(msg);
        }

        return new GeneralWorkflowResponse();
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
        String currentStatus = workflowDTO.getAttributes().get("apiCurrentState");

        int tenantId = workflowDTO.getTenantId();
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        try {
            // tenant flow is already started from the rest api service impl. no need to start from here
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(invoker);
            Registry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceUserRegistry(invoker, tenantId);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
            GenericArtifact apiArtifact = APIUtil.getAPIArtifact(apiIdentifier, registry);
            if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
                String targetStatus = "";
                apiArtifact.invokeAction(action, APIConstants.API_LIFE_CYCLE);
                targetStatus = apiArtifact.getLifecycleState();
                if (!currentStatus.equals(targetStatus)) {
                    apiMgtDAO.recordAPILifeCycleEvent(apiIdentifier, currentStatus.toUpperCase(),
                            targetStatus.toUpperCase(), invoker, tenantId);
                }
                if (log.isDebugEnabled()) {
                    String logMessage = "API Status changed successfully. API Name: " + apiIdentifier.getApiName()
                            + ", API Version " + apiIdentifier.getVersion() + ", New Status : " + targetStatus;
                    log.debug(logMessage);
                }
            }

        } catch (RegistryException e) {
            log.error("Could not complete api state change workflow", e);
            throw new WorkflowException("Could not complete api state change workflow", e);
        } catch (APIManagementException e) {
            log.error("Could not complete api state change workflow", e);
            throw new WorkflowException("Could not complete api state change workflow", e);
        }

        return new GeneralWorkflowResponse();
    }

    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for APIStateChangeWSWorkflowExecutor for :" + workflowExtRef);
        }
        String errorMsg;
        URL serviceEndpointURL = new URL(serviceEndpoint);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());
        HttpGet httpGet = new HttpGet(
                serviceEndpoint + RUNTIME_INSTANCE_RESOURCE_PATH + "?businessKey=" + workflowExtRef);
        String authHeader = getBasicAuthHeader();
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        JSONParser parser = new JSONParser();

        HttpDelete httpDelete = null;

        try {
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String processId = null;
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String responseStr = EntityUtils.toString(entity);
                if (log.isDebugEnabled()) {
                    log.debug("Process instance details for ref : " + workflowExtRef + ": " + responseStr);
                }
                JSONObject obj = (JSONObject) parser.parse(responseStr);
                JSONArray data = (JSONArray) obj.get("data");
                if (data != null) {
                    JSONObject instanceDetails = (JSONObject) data.get(0);
                    processId = (String) instanceDetails.get("id");
                }

                if (processId != null) {
                    httpDelete = new HttpDelete(serviceEndpoint + RUNTIME_INSTANCE_RESOURCE_PATH + "/" + processId);
                    httpDelete.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
                    response = httpClient.execute(httpDelete);
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                        errorMsg = "Error while deleting process instance details for " + workflowExtRef + " code: "
                                + response.getStatusLine().getStatusCode();
                        log.error(errorMsg);
                        throw new WorkflowException(errorMsg);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully deleted process instance for  : " + workflowExtRef);
                    }
                }

            } else {
                errorMsg = "Error while getting process instance details for " + workflowExtRef + " code: "
                        + response.getStatusLine().getStatusCode();
                log.error(errorMsg);
                throw new WorkflowException(errorMsg);
            }
        } catch (ClientProtocolException e) {
            log.error("Error while creating the http client", e);
            throw new WorkflowException("Error while creating the http client", e);
        } catch (IOException e) {
            log.error("Error while connecting to the external service", e);
            throw new WorkflowException("Error while connecting to the external service", e);
        } catch (ParseException e) {
            log.error("Error while parsing response from BPS server", e);
            throw new WorkflowException("Error while parsing response from BPS server", e);
        } finally {
            httpGet.reset();
            httpDelete.reset();
        }
    }

    /**
     * get credentials that are needed to call the rest api in BPMN engine
     */
    private String getBasicAuthHeader() {

        // if credentials are not defined in the workflow-extension.xml file, then get the global credentials from the
        // api-manager.xml configuration
        if (username == null || password == null) {
            WorkflowProperties workflowProperties = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration().getWorkflowProperties();

            username = workflowProperties.getServerUser();
            password = workflowProperties.getServerPassword();
        }
        byte[] encodedAuth = Base64.encodeBase64((username + ":" + password).getBytes(Charset.forName("ISO-8859-1")));
        return "Basic " + new String(encodedAuth);
    }

    /**
     * build the payload to call the BPMN process
     */
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
        // set workflowreferencid to business key so we can later query the process instance using this value
        // if we want to delete the instance
        payload.put("businessKey", apiStateWorkFlowDTO.getExternalWorkflowReference());
        payload.put("variables", variables);

        return payload.toJSONString();
    }

    /**
     * set information that are needed to invoke callback service
     */
    private void setOAuthApplicationInfo(APIStateWorkflowDTO apiStateWorkFlowDTO) {
        // if credentials are not defined in the workflow-extension.xml file call dcr endpoint and generate a
        // oauth application and pass the client id and secret
        if (clientId == null || clientSecret == null) {
            // TODO impliment dcr enpoint calling
        }
        apiStateWorkFlowDTO.setClientId(clientId);
        apiStateWorkFlowDTO.setClientSecret(clientSecret);
        apiStateWorkFlowDTO.setScope(WorkflowConstants.API_WF_SCOPE);
        
        WorkflowProperties workflowProperties = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getWorkflowProperties();
        apiStateWorkFlowDTO.setTokenAPI(workflowProperties.getTokenEndPoint());

    }

    /**
     * Read the user provided lifecycle states for the approval task. These are provided in the workflow-extension.xml
     */
    private Map<String, List<String>> getSelectedStatesToApprove() {
        Map<String, List<String>> stateAction = new HashMap<String, List<String>>();
        // exract selected states from stateList and populate the map
        if (stateList != null) {
            // list will be something like ' Created:Publish,Created:Deploy as a Prototype,Published:Block ' String
            // It will have State:action pairs
            String[] statelistArray = stateList.split(",");
            for (int i = 0; i < statelistArray.length; i++) {
                String[] stateActionArray = statelistArray[i].split(":");
                if (stateAction.containsKey(stateActionArray[0].toUpperCase())) {
                    ArrayList<String> actionList = (ArrayList<String>) stateAction
                            .get(stateActionArray[0].toUpperCase());
                    actionList.add(stateActionArray[1]);
                } else {
                    ArrayList<String> actionList = new ArrayList<String>();
                    actionList.add(stateActionArray[1]);
                    stateAction.put(stateActionArray[0].toUpperCase(), actionList);
                }
            }
        }
        return stateAction;
    }

}
