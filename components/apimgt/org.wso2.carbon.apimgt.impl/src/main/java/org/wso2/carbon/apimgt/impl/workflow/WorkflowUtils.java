/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIInfo;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationRegistrationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.LifeCycleUtils;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class used to handle notifications in Workflow.
 */
public class WorkflowUtils {

    private static final Log log = LogFactory.getLog(WorkflowUtils.class);
    public static void sendNotificationAfterWFComplete(WorkflowDTO workflowDTO, String wfType)
            throws APIManagementException {

        if (WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION.equalsIgnoreCase(wfType)) {
            String applicationId = workflowDTO.getWorkflowReference();
            int appId = Integer.parseInt(applicationId);
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            Application application = apiMgtDAO.getApplicationById(appId);
            String orgId = application.getOrganization();
            ApplicationWorkflowDTO appWFDto = (ApplicationWorkflowDTO) workflowDTO;
            appWFDto.setApplication(application);
            ApplicationEvent applicationEvent = new ApplicationEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.APPLICATION_CREATE.name(),
                    appWFDto.getTenantId(), orgId, appWFDto.getApplication().getId(),
                    appWFDto.getApplication().getUUID(),
                    appWFDto.getApplication().getName(), appWFDto.getApplication().getTokenType(),
                    appWFDto.getApplication().getTier(), appWFDto.getApplication().getGroupId(),
                    appWFDto.getApplication().getApplicationAttributes(), application.getSubscriber().getName());
            APIUtil.sendNotification(applicationEvent, APIConstants.NotifierType.APPLICATION.name());
        } else if (WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION.equalsIgnoreCase(wfType)) {
            ApplicationWorkflowDTO appWFDto = (ApplicationWorkflowDTO) workflowDTO;
            String orgId = appWFDto.getApplication().getOrganization();
            ApplicationEvent applicationEvent = new ApplicationEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.APPLICATION_DELETE.name(),
                    appWFDto.getTenantId(), orgId, appWFDto.getApplication().getId(),
                    appWFDto.getApplication().getUUID(),
                    appWFDto.getApplication().getName(), appWFDto.getApplication().getTokenType(),
                    appWFDto.getApplication().getTier(), appWFDto.getApplication().getGroupId(),
                    appWFDto.getApplication().getApplicationAttributes(), "");
            APIUtil.sendNotification(applicationEvent, APIConstants.NotifierType.APPLICATION.name());
        } else if (WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION.equalsIgnoreCase(wfType)) {
            SubscriptionWorkflowDTO subWFDto = (SubscriptionWorkflowDTO) workflowDTO;
            SubscribedAPI sub = ApiMgtDAO.getInstance()
                    .getSubscriptionById(Integer.parseInt(subWFDto.getWorkflowReference()));
            SubscriptionEvent subscriptionEvent;
            String orgId = sub.getOrganization();
            if (sub.getAPIIdentifier() != null) {
                subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                        System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_CREATE.name(),
                        subWFDto.getTenantId(), orgId,
                        Integer.parseInt(subWFDto.getWorkflowReference()), sub.getUUID(), sub.getIdentifier().getId(),
                        sub.getIdentifier().getUUID(), sub.getApplication().getId(), sub.getApplication().getUUID(),
                        sub.getTier().getName(), sub.getSubCreatedStatus(), sub.getIdentifier().getName(),
                        sub.getIdentifier().getVersion());
            } else {
                subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                        System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_CREATE.name(),
                        subWFDto.getTenantId(), orgId,
                        Integer.parseInt(subWFDto.getWorkflowReference()), sub.getUUID(), sub.getProductId().getId(),
                        sub.getProductId().getUUID(), sub.getApplication().getId(), sub.getApplication().getUUID(),
                        sub.getTier().getName(), sub.getSubCreatedStatus(), sub.getIdentifier().getName(),
                        sub.getIdentifier().getVersion());
            }
            APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
        } else if (WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE.equalsIgnoreCase(wfType)) {
            SubscriptionWorkflowDTO subWFDto = (SubscriptionWorkflowDTO) workflowDTO;
            SubscribedAPI sub = ApiMgtDAO.getInstance()
                    .getSubscriptionById(Integer.parseInt(subWFDto.getWorkflowReference()));
            String orgId = sub.getOrganization();
            SubscriptionEvent subscriptionEvent;
            if (sub.getAPIIdentifier() != null) {
                subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                        System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_UPDATE.name(),
                        subWFDto.getTenantId(), subWFDto.getTenantDomain(),
                        Integer.parseInt(subWFDto.getWorkflowReference()), sub.getUUID(), sub.getIdentifier().getId(),
                        sub.getIdentifier().getUUID(), sub.getApplication().getId(), sub.getApplication().getUUID(),
                        sub.getTier().getName(), sub.getSubCreatedStatus(), sub.getIdentifier().getName(),
                        sub.getIdentifier().getVersion());
            } else {
                subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                        System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_UPDATE.name(),
                        subWFDto.getTenantId(), subWFDto.getTenantDomain(),
                        Integer.parseInt(subWFDto.getWorkflowReference()), sub.getUUID(), sub.getProductId().getId(),
                        sub.getProductId().getUUID(), sub.getApplication().getId(), sub.getApplication().getUUID(),
                        sub.getTier().getName(), sub.getSubCreatedStatus(), sub.getIdentifier().getName(),
                        sub.getIdentifier().getVersion());
            }

            APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
        } else if (WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION.equalsIgnoreCase(wfType)) {
            SubscriptionWorkflowDTO subWFDto = (SubscriptionWorkflowDTO) workflowDTO;
            SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_DELETE.name(),
                    subWFDto.getTenantId(), subWFDto.getTenantDomain(),
                    Integer.parseInt(subWFDto.getWorkflowReference()), subWFDto.getExternalWorkflowReference(), 0,
                    "", 0, "", "", "","", "");
            APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
        } else if (WorkflowConstants.WF_TYPE_AM_API_STATE.equalsIgnoreCase(wfType) ||
                WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE.equalsIgnoreCase(wfType)) {
            APIStateWorkflowDTO apiStateWFDto = (APIStateWorkflowDTO) workflowDTO;
            APIInfo apiInfo = ApiMgtDAO.getInstance().getAPIInfoByUUID(apiStateWFDto.getApiUUID());
            String orgId = null;
            if (apiInfo != null) {
                orgId = apiInfo.getOrganization();
            }
            APIEvent apiEvent = new APIEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                    APIConstants.EventType.API_LIFECYCLE_CHANGE.name(), apiStateWFDto.getTenantId(),
                    (orgId != null) ? orgId : apiStateWFDto.getTenantDomain(), apiStateWFDto.getMetadata("ApiName"),
                    Integer.parseInt(apiStateWFDto.getWorkflowReference()), apiStateWFDto.getApiUUID(),
                    apiStateWFDto.getMetadata("ApiVersion"),
                    apiStateWFDto.getApiType(), apiStateWFDto.getMetadata( "ApiContext"),
                    apiStateWFDto.getMetadata("ApiProvider"),
                    apiStateWFDto.getMetadata("Action"), null);
            APIUtil.sendNotification(apiEvent, APIConstants.NotifierType.API.name());
        } else if (WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION.equalsIgnoreCase(wfType)) {
            ApplicationRegistrationWorkflowDTO appRegWFDto = (ApplicationRegistrationWorkflowDTO) workflowDTO;
            String orgId = appRegWFDto.getApplication().getOrganization();
            ApplicationRegistrationEvent applicationRegistrationEvent = new ApplicationRegistrationEvent(
                    UUID.randomUUID().toString(), System.currentTimeMillis(),
                    APIConstants.EventType.APPLICATION_REGISTRATION_CREATE.name(), appRegWFDto.getTenantId(),
                    orgId, appRegWFDto.getApplication().getId(),
                    appRegWFDto.getApplication().getUUID(), appRegWFDto.getApplicationInfo().getClientId(),
                    appRegWFDto.getApplication().getTokenType(), appRegWFDto.getKeyManager());
            APIUtil.sendNotification(applicationRegistrationEvent,
                    APIConstants.NotifierType.APPLICATION_REGISTRATION.name());
        } else if (WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX.equalsIgnoreCase(wfType)) {
            ApplicationRegistrationWorkflowDTO appRegWFDto = (ApplicationRegistrationWorkflowDTO) workflowDTO;
            String orgId = appRegWFDto.getApplication().getOrganization();
            ApplicationRegistrationEvent applicationRegistrationEvent = new ApplicationRegistrationEvent(
                    UUID.randomUUID().toString(), System.currentTimeMillis(),
                    APIConstants.EventType.APPLICATION_REGISTRATION_CREATE.name(), appRegWFDto.getTenantId(),
                    orgId, appRegWFDto.getApplication().getId(),
                    appRegWFDto.getApplication().getUUID(), appRegWFDto.getApplicationInfo().getClientId(),
                    appRegWFDto.getApplication().getTokenType(), appRegWFDto.getKeyManager());
            APIUtil.sendNotification(applicationRegistrationEvent,
                    APIConstants.NotifierType.APPLICATION_REGISTRATION.name());
        } else if (WorkflowConstants.WF_TYPE_AM_REVISION_DEPLOYMENT.equalsIgnoreCase(wfType)) {
            completeDeploymentWorkFlow(workflowDTO);
        }
    }

    /**
     * Handle cleanup task for api and api product state change workflow Approval executor.
     *
     * @param workflowExtRef External Workflow Reference of pending workflow process
     * @throws WorkflowException Exception when deleting the workflow task
     */
    protected static void cleanupPendingTasksByWorkflowReference(String workflowExtRef) throws WorkflowException {

        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            apiMgtDAO.deleteWorkflowRequest(workflowExtRef);
        } catch (APIManagementException axisFault) {
            String errorMsg =
                    "Error sending out cancel pending application approval process message. cause: " + axisFault.getMessage();
            throw new WorkflowException(errorMsg, axisFault);
        }
    }

    /**
     * Read the user provided lifecycle states for the approval task. These are provided in the workflow-extension.xml
     */
    protected static Map<String, List<String>> getSelectedStatesToApprove(String stateList) {
        Map<String, List<String>> stateAction = new HashMap<String, List<String>>();
        // exract selected states from stateList and populate the map
        if (stateList != null) {
            // list will be something like ' Created:Publish,Created:Deploy as a Prototype,Published:Block ' String
            // It will have State:action pairs
            String[] stateListArray = stateList.split(",");
            for (int i = 0; i < stateListArray.length; i++) {
                String[] stateActionArray = stateListArray[i].split(":");
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
        if (log.isDebugEnabled()) {
            log.debug("selected states: " + stateAction);
        }
        return stateAction;
    }

    /**
     * Set state change workflow parameters
     *
     * @param apiStateWorkFlowDTO APIStateWorkflowDTO object
     */
    protected static void setWorkflowParameters(APIStateWorkflowDTO apiStateWorkFlowDTO) {

        String callBackURL = apiStateWorkFlowDTO.getCallbackUrl();
        String message = "Approval request for API state change action " + apiStateWorkFlowDTO.getApiLCAction() + " " +
                "from " + apiStateWorkFlowDTO.getApiCurrentState() + " state for the API "
                + apiStateWorkFlowDTO.getApiName() + " : " + apiStateWorkFlowDTO.getApiVersion() + " by "
                + apiStateWorkFlowDTO.getApiProvider() + "";
        apiStateWorkFlowDTO.setWorkflowDescription(message);
        apiStateWorkFlowDTO.setMetadata("CurrentState", apiStateWorkFlowDTO.getApiCurrentState());
        apiStateWorkFlowDTO.setMetadata("Action", apiStateWorkFlowDTO.getApiLCAction());
        apiStateWorkFlowDTO.setMetadata("ApiName", apiStateWorkFlowDTO.getApiName());
        apiStateWorkFlowDTO.setMetadata("ApiContext", apiStateWorkFlowDTO.getApiContext());
        apiStateWorkFlowDTO.setMetadata("ApiVersion", apiStateWorkFlowDTO.getApiVersion());
        apiStateWorkFlowDTO.setMetadata("ApiProvider", apiStateWorkFlowDTO.getApiProvider());
        apiStateWorkFlowDTO.setMetadata("Invoker", apiStateWorkFlowDTO.getInvoker());
        apiStateWorkFlowDTO.setMetadata("TenantId", String.valueOf(apiStateWorkFlowDTO.getTenantId()));

        apiStateWorkFlowDTO.setProperties("action", apiStateWorkFlowDTO.getApiLCAction());
        apiStateWorkFlowDTO.setProperties("apiName", apiStateWorkFlowDTO.getApiName());
        apiStateWorkFlowDTO.setProperties("apiVersion", apiStateWorkFlowDTO.getApiVersion());
        apiStateWorkFlowDTO.setProperties("apiProvider", apiStateWorkFlowDTO.getApiProvider());
        apiStateWorkFlowDTO.setProperties("currentState", apiStateWorkFlowDTO.getApiCurrentState());
    }

    /**
     * Complete the lifecycle state change workflow
     *
     * @param workflowDTO Workflow DTO object
     * @throws WorkflowException Exception when completing the workflow
     */
    protected static void completeStateChangeWorkflow(WorkflowDTO workflowDTO) throws WorkflowException {

        String externalWorkflowRef = workflowDTO.getExternalWorkflowReference();
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            Workflow workflow = apiMgtDAO.getworkflowReferenceByExternalWorkflowReference(externalWorkflowRef);
            String apiName = workflow.getMetadata("ApiName");
            String action = workflow.getMetadata("Action");
            String providerName = workflow.getMetadata("ApiProvider");
            String version = workflow.getMetadata("ApiVersion");
            String invoker = workflow.getMetadata("Invoker");
            int tenantId = workflowDTO.getTenantId();
            //tenant flow is already started from the rest api service impl. no need to start from here
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(invoker);
            APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(providerName);
            String tenantDomain = APIUtil.getTenantDomainFromTenantId(tenantId);
            String uuid = apiMgtDAO.getUUIDFromIdentifier(apiIdentifier, tenantDomain);
            if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
                if (StringUtils.isNotEmpty(uuid)) {
                    ApiTypeWrapper apIorAPIProductByUUID = apiProvider.getAPIorAPIProductByUUID(uuid, tenantDomain);
                    LifeCycleUtils.changeLifecycle(invoker, apiProvider, tenantDomain, apIorAPIProductByUUID, action, Collections.emptyMap());
                    if (log.isDebugEnabled()) {
                        String logMessage = "API Status changed successfully. API Name: " + apiIdentifier.getApiName()
                                + ", API Version " + apiIdentifier.getVersion() + ", New Status : " + action;
                        log.debug(logMessage);
                    }
                }
                if (log.isDebugEnabled()) {
                    String logMessage =
                            "API Status changed successfully. API Name: " + apiIdentifier.getApiName() + ", API "
                                    + "Version " + apiIdentifier.getVersion() + ", New Status : " + action;
                    log.debug(logMessage);
                }
            }
        } catch (APIManagementException e) {
            String errorMsg = "Could not complete api state change workflow";
            log.error(errorMsg, e);
        } catch (APIPersistenceException e) {
            log.error("Error while accessing lifecycle information ", e);
        }
    }

    /**
     * Complete the revision deployment workflow
     *
     * @param workflowDTO Workflow DTO object
     */
    protected static void completeDeploymentWorkFlow(WorkflowDTO workflowDTO) {

        String externalWorkflowRef = workflowDTO.getExternalWorkflowReference();
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            Workflow workflow = apiMgtDAO.getworkflowReferenceByExternalWorkflowReference(externalWorkflowRef);
            String revisionId = workflow.getMetadata("revisionId");
            String apiId = workflow.getMetadata("apiId");
            String providerName = workflow.getMetadata("apiProvider");
            String environment = workflow.getMetadata("environment");
            String organization = workflow.getTenantDomain();
            String invoker = workflow.getMetadata("userName");
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(invoker);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(providerName);
            apiProvider.resumeDeployedAPIRevision(apiId, organization, workflow.getWorkflowReference(), revisionId,
                    environment);

            //Set displayOnDevportal to true
            APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
            apiRevisionDeployment.setRevisionUUID(workflow.getWorkflowReference());
            apiRevisionDeployment.setDeployment(environment);
            apiRevisionDeployment.setDisplayOnDevportal(true);
            apiMgtDAO.updateAPIRevisionDeployment (apiId,Collections.singleton(apiRevisionDeployment));
        } catch (APIManagementException e) {
            String errorMsg = "Could not get workflow details for workflow reference id " + externalWorkflowRef;
            log.error(errorMsg, e);
        }
    }
}
