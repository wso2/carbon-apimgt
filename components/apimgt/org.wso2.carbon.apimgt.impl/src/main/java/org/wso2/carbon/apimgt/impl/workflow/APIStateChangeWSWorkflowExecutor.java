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
import org.apache.commons.lang3.StringUtils;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants.PayloadConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * APIStateChangeWSWorkflowExecutor is used to provide approval process to API state change using external BPMN process.
 * This class is associated with the BPMN process provided with the APIStateChangeApprovalProcess.bar
 *
 */
public class APIStateChangeWSWorkflowExecutor extends WorkflowExecutor {

    private static final String RUNTIME_INSTANCE_RESOURCE_PATH = "/runtime/process-instances";
    private static final Log log = LogFactory.getLog(APIStateChangeWSWorkflowExecutor.class);
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
            log.debug("Executing API State change Workflow.");
            log.debug("Execute workflowDTO " + workflowDTO.toString());
        }

        if (stateList != null) {
            Map<String, List<String>> stateActionMap = getSelectedStatesToApprove();
            APIStateWorkflowDTO apiStateWorkFlowDTO = (APIStateWorkflowDTO) workflowDTO;

            if (stateActionMap.containsKey(apiStateWorkFlowDTO.getApiCurrentState().toUpperCase())
                    && stateActionMap.get(apiStateWorkFlowDTO.getApiCurrentState().toUpperCase())
                            .contains(apiStateWorkFlowDTO.getApiLCAction())) {
                //set the auth application related info. This will be used to call the callback service
                setOAuthApplicationInfo(apiStateWorkFlowDTO);
                // build request payload
                String jsonPayload = buildPayloadForBPMNProcess(apiStateWorkFlowDTO);
                if(log.isDebugEnabled()){
                    log.debug("APIStateChange payload: " + jsonPayload);
                }
                if (serviceEndpoint == null) {
                    // set the bps endpoint from the global configurations
                    WorkflowProperties workflowProperties = ServiceReferenceHolder.getInstance()
                            .getAPIManagerConfigurationService().getAPIManagerConfiguration().getWorkflowProperties();
                    serviceEndpoint = workflowProperties.getServerUrl();
                }

                URL serviceEndpointURL = new URL(serviceEndpoint);
                HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(),
                        serviceEndpointURL.getProtocol());
                HttpPost httpPost = new HttpPost(serviceEndpoint + RUNTIME_INSTANCE_RESOURCE_PATH);
                //Generate the basic auth header using provided user credentials
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
                    String errorMsg = "Error while creating the http client";
                    log.error(errorMsg, e);
                    throw new WorkflowException(errorMsg, e);
                } catch (IOException e) {
                    String errorMsg = "Error while connecting to the BPMN process server from the WorkflowExecutor.";
                    log.error(errorMsg, e);
                    throw new WorkflowException(errorMsg, e);
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
            throw new WorkflowException(msg);
        }

        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the API state change workflow process.
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Completing API State change Workflow..");
            log.debug("response: " + workflowDTO.toString());
        }

        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);

        String action = workflowDTO.getAttributes().get(PayloadConstants.VARIABLE_API_LC_ACTION);
        String apiName = workflowDTO.getAttributes().get(PayloadConstants.VARIABLE_APINAME);
        String providerName = workflowDTO.getAttributes().get(PayloadConstants.VARIABLE_APIPROVIDER);
        String version = workflowDTO.getAttributes().get(PayloadConstants.VARIABLE_APIVERSION);
        String invoker = workflowDTO.getAttributes().get(PayloadConstants.VARIABLE_INVOKER);

        int tenantId = workflowDTO.getTenantId();
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        try {
            // tenant flow is already started from the rest api service impl. no need to start from here
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(invoker);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(providerName);
            String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
            String uuid = apiMgtDAO.getUUIDFromIdentifier(apiIdentifier, tenantDomain);
            if (StringUtils.isNotEmpty(uuid)) {
                ApiTypeWrapper apIorAPIProductByUUID = apiProvider.getAPIorAPIProductByUUID(uuid, tenantDomain);
                apiProvider.changeLifeCycleStatus(tenantDomain, apIorAPIProductByUUID, action, Collections.emptyMap());
                if (log.isDebugEnabled()) {
                    String logMessage = "API Status changed successfully. API Name: " + apiIdentifier.getApiName()
                            + ", API Version " + apiIdentifier.getVersion() + ", New Status : " + action;
                    log.debug(logMessage);
                }
            }
        } catch (APIManagementException e) {
            String errorMsg = "Could not complete api state change workflow";
            log.error(errorMsg, e);
            throw new WorkflowException(errorMsg, e);
        }
        return new GeneralWorkflowResponse();
    }

    /**
     * Handle cleanup task for api state change workflow ws executor. This queries the BPMN process related to the given
     * workflow reference id and delete that process
     */
    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for APIStateChangeWSWorkflowExecutor for :" + workflowExtRef);
        }
        String errorMsg;
        if (serviceEndpoint == null) {
            // set the bps endpoint from the global configurations
            WorkflowProperties workflowProperties = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration().getWorkflowProperties();
            serviceEndpoint = workflowProperties.getServerUrl();
        }
        URL serviceEndpointURL = new URL(serviceEndpoint);
        HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(), serviceEndpointURL.getProtocol());

        // get the basic auth header value to connect to the bpmn process
        String authHeader = getBasicAuthHeader();
        JSONParser parser = new JSONParser();
        HttpGet httpGet = null;
        HttpDelete httpDelete = null;

        try {
            // Get the process instance details related to the given workflow reference id. If there is a process that
            // is already started with the given wf reference as the businesskey, that process needes to be deleted
            httpGet = new HttpGet(serviceEndpoint + RUNTIME_INSTANCE_RESOURCE_PATH + "?businessKey=" + workflowExtRef);
            httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            HttpResponse response = httpClient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            String processId = null;
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // already exists a process related to the given workflow reference
                String responseStr = EntityUtils.toString(entity);
                if (log.isDebugEnabled()) {
                    log.debug("Process instance details for ref : " + workflowExtRef + ": " + responseStr);
                }
                JSONObject obj = (JSONObject) parser.parse(responseStr);
                JSONArray data = (JSONArray) obj.get(PayloadConstants.DATA);
                if (data != null) {
                    JSONObject instanceDetails = (JSONObject) data.get(0);
                    // extract the id related to that process. this id is used to delete the process
                    processId = (String) instanceDetails.get(PayloadConstants.ID);
                }

                if (processId != null) {
                    // delete the process using the id
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
                    //remove entry from the db
                    ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
                    apiMgtDAO.removeWorkflowEntry(workflowExtRef, WorkflowConstants.WF_TYPE_AM_API_STATE.toString());
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
            log.error("Error while connecting to the BPMN process server from the WorkflowExecutor.", e);
            throw new WorkflowException("Error while connecting to the external service", e);
        } catch (ParseException e) {
            log.error("Error while parsing response from BPS server", e);
            throw new WorkflowException("Error while parsing response from BPS server", e);
        } catch (APIManagementException e) {
            log.error("Error removing the workflow entry", e);
            throw new WorkflowException("Error removing the workflow entry", e);            
        } finally {
            if (httpGet != null) {
                httpGet.reset();
            }
            if (httpDelete != null) {
                httpDelete.reset();
            }

        }
    }

    /**
     * get credentials that are needed to call the rest api in BPMN engine
     */
    private String getBasicAuthHeader() {

        // if credentials are not defined in the workflow config, then get the global credentials from the
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
        clientIdObj.put(PayloadConstants.VARIABLE_NAME, PayloadConstants.VARIABLE_CLIENTID);
        clientIdObj.put(PayloadConstants.VARIABLE_VALUE, apiStateWorkFlowDTO.getClientId());
        variables.add(clientIdObj);

        JSONObject clientSecretObj = new JSONObject();
        clientSecretObj.put(PayloadConstants.VARIABLE_NAME, PayloadConstants.VARIABLE_CLIENTSECRET);
        clientSecretObj.put(PayloadConstants.VARIABLE_VALUE, apiStateWorkFlowDTO.getClientSecret());
        variables.add(clientSecretObj);

        JSONObject scopeObj = new JSONObject();
        scopeObj.put(PayloadConstants.VARIABLE_NAME, PayloadConstants.VARIABLE_SCOPE);
        scopeObj.put(PayloadConstants.VARIABLE_VALUE, apiStateWorkFlowDTO.getScope());
        variables.add(scopeObj);

        JSONObject tokenAPIObj = new JSONObject();
        tokenAPIObj.put(PayloadConstants.VARIABLE_NAME, PayloadConstants.VARIABLE_TOKENAPI);
        tokenAPIObj.put(PayloadConstants.VARIABLE_VALUE, apiStateWorkFlowDTO.getTokenAPI());
        variables.add(tokenAPIObj);

        JSONObject apiCurrentStateObj = new JSONObject();
        apiCurrentStateObj.put(PayloadConstants.VARIABLE_NAME, PayloadConstants.VARIABLE_APISTATE);
        apiCurrentStateObj.put(PayloadConstants.VARIABLE_VALUE, apiStateWorkFlowDTO.getApiCurrentState());
        variables.add(apiCurrentStateObj);

        JSONObject apiLCActionObj = new JSONObject();
        apiLCActionObj.put(PayloadConstants.VARIABLE_NAME, PayloadConstants.VARIABLE_API_LC_ACTION);
        apiLCActionObj.put(PayloadConstants.VARIABLE_VALUE, apiStateWorkFlowDTO.getApiLCAction());
        variables.add(apiLCActionObj);

        JSONObject apiNameObj = new JSONObject();
        apiNameObj.put(PayloadConstants.VARIABLE_NAME, PayloadConstants.VARIABLE_APINAME);
        apiNameObj.put(PayloadConstants.VARIABLE_VALUE, apiStateWorkFlowDTO.getApiName());
        variables.add(apiNameObj);

        JSONObject apiVersionObj = new JSONObject();
        apiVersionObj.put(PayloadConstants.VARIABLE_NAME, PayloadConstants.VARIABLE_APIVERSION);
        apiVersionObj.put(PayloadConstants.VARIABLE_VALUE, apiStateWorkFlowDTO.getApiVersion());
        variables.add(apiVersionObj);

        JSONObject apiProviderObj = new JSONObject();
        apiProviderObj.put(PayloadConstants.VARIABLE_NAME, PayloadConstants.VARIABLE_APIPROVIDER);
        apiProviderObj.put(PayloadConstants.VARIABLE_VALUE, apiStateWorkFlowDTO.getApiProvider());
        variables.add(apiProviderObj);

        JSONObject callbackUrlObj = new JSONObject();
        callbackUrlObj.put(PayloadConstants.VARIABLE_NAME, PayloadConstants.VARIABLE_CALLBACKURL);
        callbackUrlObj.put(PayloadConstants.VARIABLE_VALUE, apiStateWorkFlowDTO.getCallbackUrl());
        variables.add(callbackUrlObj);

        JSONObject wfReferenceObj = new JSONObject();
        wfReferenceObj.put(PayloadConstants.VARIABLE_NAME, PayloadConstants.VARIABLE_WFREF);
        wfReferenceObj.put(PayloadConstants.VARIABLE_VALUE, apiStateWorkFlowDTO.getExternalWorkflowReference());
        variables.add(wfReferenceObj);

        JSONObject invokerObj = new JSONObject();
        invokerObj.put(PayloadConstants.VARIABLE_NAME, PayloadConstants.VARIABLE_INVOKER);
        invokerObj.put(PayloadConstants.VARIABLE_VALUE, apiStateWorkFlowDTO.getInvoker());
        variables.add(invokerObj);

        JSONObject payload = new JSONObject();
        payload.put(PayloadConstants.PROCESS_DEF_KEY, processDefinitionKey);
        payload.put(PayloadConstants.TENANT_ID, apiStateWorkFlowDTO.getTenantId());
        // set workflowreferencid to business key so we can later query the process instance using this value
        // if we want to delete the instance
        payload.put(PayloadConstants.BUSINESS_KEY, apiStateWorkFlowDTO.getExternalWorkflowReference());
        payload.put(PayloadConstants.VARIABLES, variables);

        return payload.toJSONString();
    }

    /**
     * set information that are needed to invoke callback service
     */
    private void setOAuthApplicationInfo(APIStateWorkflowDTO apiStateWorkFlowDTO) throws WorkflowException {
        // if credentials are not defined in the workflow config call dcr endpoint and generate a
        // oauth application and pass the client id and secret
        WorkflowProperties workflowProperties = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getWorkflowProperties();
        if (clientId == null || clientSecret == null) {

            String dcrUsername = workflowProperties.getdCREndpointUser();
            String dcrPassword = workflowProperties.getdCREndpointPassword();

            byte[] encodedAuth = Base64
                    .encodeBase64((dcrUsername + ":" + dcrPassword).getBytes(Charset.forName("ISO-8859-1")));

            JSONObject payload = new JSONObject();
            payload.put(PayloadConstants.KEY_OAUTH_APPNAME, WorkflowConstants.WORKFLOW_OAUTH_APP_NAME);
            payload.put(PayloadConstants.KEY_OAUTH_OWNER, dcrUsername);
            payload.put(PayloadConstants.KEY_OAUTH_SAASAPP, "true");
            payload.put(PayloadConstants.KEY_OAUTH_GRANT_TYPES, WorkflowConstants.WORKFLOW_OAUTH_APP_GRANT_TYPES);
            URL serviceEndpointURL = new URL(workflowProperties.getdCREndPoint());
            HttpClient httpClient = APIUtil.getHttpClient(serviceEndpointURL.getPort(),
                    serviceEndpointURL.getProtocol());
            HttpPost httpPost = new HttpPost(workflowProperties.getdCREndPoint());
            String authHeader = "Basic " + new String(encodedAuth);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
            StringEntity requestEntity = new StringEntity(payload.toJSONString(), ContentType.APPLICATION_JSON);

            httpPost.setEntity(requestEntity);
            try {
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity entity = response.getEntity();
                
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                        || response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                    String responseStr = EntityUtils.toString(entity);
                    if (log.isDebugEnabled()) {
                        log.debug("Workflow oauth app created: " + responseStr);
                    }
                    JSONParser parser = new JSONParser();
                    JSONObject obj = (JSONObject) parser.parse(responseStr);
                    clientId = (String) obj.get(PayloadConstants.VARIABLE_CLIENTID);
                    clientSecret = (String) obj.get(PayloadConstants.VARIABLE_CLIENTSECRET);

                } else {
                    String error = "Error while starting the process:  " + response.getStatusLine().getStatusCode()
                            + " " + response.getStatusLine().getReasonPhrase();
                    log.error(error);
                    throw new WorkflowException(error);
                }              
       
            } catch (ClientProtocolException e) {
                String errorMsg = "Error while creating the http client";
                log.error(errorMsg, e);
                throw new WorkflowException(errorMsg, e);
            } catch (IOException e) {
                String errorMsg = "Error while connecting to dcr endpoint";
                log.error(errorMsg, e);
                throw new WorkflowException(errorMsg, e);
            } catch (ParseException e) {
                String errorMsg = "Error while parsing response from DCR endpoint";
                log.error(errorMsg, e);
                throw new WorkflowException(errorMsg, e);
            } finally {
                httpPost.reset();
            }

        }
        apiStateWorkFlowDTO.setClientId(clientId);
        apiStateWorkFlowDTO.setClientSecret(clientSecret);
        apiStateWorkFlowDTO.setScope(WorkflowConstants.API_WF_SCOPE);

        apiStateWorkFlowDTO.setTokenAPI(workflowProperties.getTokenEndPoint());

    }

    /**
     * Read the user provided lifecycle states for the approval task. These are provided in the workflow config
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
        if(log.isDebugEnabled()){
            log.debug("selected states: " + stateAction.toString());
        }
        return stateAction;
    }

}
