/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.workflow;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ContainerBasedGatewayException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.DedicatedGateway;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.lcm.core.beans.CheckItemBean;
import org.wso2.carbon.lcm.core.exception.LifecycleException;
import org.wso2.carbon.lcm.core.impl.LifecycleState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Model to get information related to api state change workflow
 */
public class APIStateChangeWorkflow extends Workflow {

    private static final Logger log = LoggerFactory.getLogger(APIStateChangeWorkflow.class);

    private String currentState;
    private String transitionState;
    private String apiName;
    private String apiProvider;
    private String apiVersion;
    private String invoker;
    private ApiDAO apiDAO;
    private LabelDAO labelDAO;
    private APISubscriptionDAO apiSubscriptionDAO;
    private APILifecycleManager apiLifecycleManager;
    private APIGateway apiGateway;
    private boolean hasOwnGateway;
    private APIMConfigurations apimConfigurations;

    public APIStateChangeWorkflow(ApiDAO apiDAO, APISubscriptionDAO apiSubscriptionDAO, WorkflowDAO workflowDAO,
                                  APILifecycleManager apiLifecycleManager, APIGateway apiGateway, LabelDAO labelDAO) {
        super(workflowDAO, Category.PUBLISHER, apiGateway);
        this.apiDAO = apiDAO;
        this.labelDAO = labelDAO;
        this.apiLifecycleManager = apiLifecycleManager;
        this.apiSubscriptionDAO = apiSubscriptionDAO;
        this.apiGateway = apiGateway;
    }

    public String getCurrentState() {
        return currentState;
    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    public String getTransitionState() {
        return transitionState;
    }

    public void setTransitionState(String transitionState) {
        this.transitionState = transitionState;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public boolean isHasOwnGateway() {
        return hasOwnGateway;
    }

    public void setHasOwnGateway(boolean hasOwnGateway) {
        this.hasOwnGateway = hasOwnGateway;
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

    public String getInvoker() {
        return invoker;
    }

    public void setInvoker(String invoker) {
        this.invoker = invoker;
    }

    @Override
    public WorkflowResponse completeWorkflow(WorkflowExecutor workflowExecutor) throws APIManagementException {
        WorkflowResponse response = workflowExecutor.complete(this);
        setStatus(response.getWorkflowStatus());

        if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
            if (log.isDebugEnabled()) {
                log.debug("API state change workflow complete: Approved");
            }
            String invoker = getAttribute(APIMgtConstants.WorkflowConstants.ATTRIBUTE_API_LC_INVOKER);
            String currentState = getAttribute(APIMgtConstants.WorkflowConstants.ATTRIBUTE_API_CUR_STATE);
            String targetState = getAttribute(APIMgtConstants.WorkflowConstants.ATTRIBUTE_API_TARGET_STATE);

            boolean hasOwnGateway =
                    Boolean.valueOf(getAttribute(APIMgtConstants.WorkflowConstants.ATTRIBUTE_HAS_OWN_GATEWAY));
            String label = getAttribute(APIMgtConstants.WorkflowConstants.ATTRIBUTE_API_AUTOGEN_LABEL);

            if (hasOwnGateway) {
                //assuming that there is no transition from (MAINTENANCE to  PROTOTYPED), (DEPRECATED to CREATED),
                // (CREATED to DEPRECATED)
                if ((currentState.equalsIgnoreCase(APIStatus.CREATED.getStatus()) ||
                        currentState.equalsIgnoreCase(APIStatus.MAINTENANCE.getStatus()) ||
                        currentState.equalsIgnoreCase(APIStatus.PROTOTYPED.getStatus())) &&
                        (targetState.equalsIgnoreCase(APIStatus.PUBLISHED.getStatus()) ||
                                targetState.equalsIgnoreCase(APIStatus.PROTOTYPED.getStatus()) ||
                                targetState.equalsIgnoreCase(APIStatus.DEPRECATED.getStatus()))) {

                    try {
                        // No need to auto-generate the label again As hasOwnGateway is true.
                        //create the gateway
                        API api = apiDAO.getAPI(getWorkflowReference());
                        apiGateway.createContainerBasedGateway(label, api);
                    } catch (ContainerBasedGatewayException e) {
                        // Revert already added labels
                        DedicatedGateway dedicatedGateway = new DedicatedGateway();
                        dedicatedGateway.setEnabled(false);
                        dedicatedGateway.setApiId(getWorkflowReference());
                        dedicatedGateway.setUpdatedBy(invoker);
                        List<String> labels = new ArrayList<>();
                        labels.add(labelDAO.getLabelIdByNameAndType(APIMgtConstants.DEFAULT_LABEL_NAME, APIMgtConstants
                                .LABEL_TYPE_GATEWAY));
                        labels.add(labelDAO.getLabelIdByNameAndType(APIMgtConstants.DEFAULT_LABEL_NAME, APIMgtConstants
                                .LABEL_TYPE_STORE));
                        apiDAO.updateDedicatedGateway(dedicatedGateway, labels);
                        throw new APIManagementException("Error while updating lifecycle state in Private Jet Mode", e,
                                ExceptionCodes.DEDICATED_CONTAINER_GATEWAY_CREATION_FAILED);
                    }

                }
            }

            String localTime = getAttribute(APIMgtConstants.WorkflowConstants.ATTRIBUTE_API_LAST_UPTIME);
            Instant time = Instant.parse(localTime);
            updateAPIStatusForWorkflowComplete(getWorkflowReference(), targetState, invoker, time);

            // check whether this removal of gateway cause events left in the topic.
            // After publishing the state change to the Gateway, remove the gateway for following occasions.
            if (hasOwnGateway) {
                if ((currentState.equalsIgnoreCase(APIStatus.PUBLISHED.getStatus()) ||
                        currentState.equalsIgnoreCase(APIStatus.PROTOTYPED.getStatus()) ||
                        currentState.equalsIgnoreCase(APIStatus.DEPRECATED.getStatus())) &&
                        (targetState.equalsIgnoreCase(APIStatus.CREATED.getStatus()) ||
                                targetState.equalsIgnoreCase(APIStatus.MAINTENANCE.getStatus()) ||
                                targetState.equalsIgnoreCase(APIStatus.PROTOTYPED.getStatus())) ||
                        targetState.equalsIgnoreCase(APIStatus.RETIRED.getStatus())) {

                    // remove gateway
                    API api = apiDAO.getAPI(getWorkflowReference());
                    apiGateway.removeContainerBasedGateway(label, api);
                }
            }

        } else if (WorkflowStatus.REJECTED == response.getWorkflowStatus()) {
            if (log.isDebugEnabled()) {
                log.debug("API state change workflow complete: Rejected");
            }
            apiDAO.updateAPIWorkflowStatus(getWorkflowReference(), APIMgtConstants.APILCWorkflowStatus.REJECTED);
        }
        updateWorkflowEntries(this);
        return response;
    }

    @Override
    public String toString() {
        return "APIStateChangeWorkflow [currentState=" + currentState + ", transitionState=" + transitionState
                + ", apiName=" + apiName + ", apiProvider=" + apiProvider + ", apiVersion=" + apiVersion + ", invoker="
                + invoker + ", toString()=" + super.toString() + "]";
    }

    private void updateAPIStatusForWorkflowComplete(String apiId, String status, String updatedBy, Instant time)
            throws APIManagementException {
        boolean requireReSubscriptions = false;
        boolean deprecateOlderVersion = false;
        try {
            API api = apiDAO.getAPI(apiId);

            API.APIBuilder apiBuilder = new API.APIBuilder(api);
            apiBuilder.lastUpdatedTime(time);
            apiBuilder.updatedBy(updatedBy);
            LifecycleState currentState = apiLifecycleManager.getLifecycleDataForState(
                    apiBuilder.getLifecycleInstanceId(), apiBuilder.getLifeCycleStatus());
            apiBuilder.lifecycleState(currentState);
            if (APIMgtConstants.APILCWorkflowStatus.PENDING.toString().equals(api.getWorkflowStatus())) {
                apiBuilder.workflowStatus(APIMgtConstants.APILCWorkflowStatus.APPROVED.toString());
                apiDAO.updateAPIWorkflowStatus(apiId, APIMgtConstants.APILCWorkflowStatus.APPROVED);
            }
            List<CheckItemBean> list = currentState.getCheckItemBeanList();
            for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
                CheckItemBean checkItemBean = (CheckItemBean) iterator.next();
                if (APIMgtConstants.DEPRECATE_PREVIOUS_VERSIONS.equals(checkItemBean.getName())) {
                    deprecateOlderVersion = checkItemBean.isValue();
                } else if (APIMgtConstants.REQUIRE_RE_SUBSCRIPTIONS.equals(checkItemBean.getName())) {
                    requireReSubscriptions = checkItemBean.isValue();
                }
            }
            API originalAPI = apiBuilder.build();

            apiLifecycleManager.executeLifecycleEvent(api.getLifeCycleStatus(), status,
                    apiBuilder.getLifecycleInstanceId(), updatedBy, originalAPI);
            if (deprecateOlderVersion) {
                if (StringUtils.isNotEmpty(api.getCopiedFromApiId())) {
                    API oldAPI = apiDAO.getAPI(api.getCopiedFromApiId());
                    if (oldAPI != null) {
                        API.APIBuilder previousAPI = new API.APIBuilder(oldAPI);
                        previousAPI.setLifecycleStateInfo(apiLifecycleManager.getLifecycleDataForState(
                                        previousAPI.getLifecycleInstanceId(), previousAPI.getLifeCycleStatus())
                        );
                        if (APIUtils.validateTargetState(previousAPI.getLifecycleState(),
                                APIStatus.DEPRECATED.getStatus())) {
                            apiLifecycleManager.executeLifecycleEvent(previousAPI.getLifeCycleStatus(),
                                    APIStatus.DEPRECATED.getStatus(), previousAPI.getLifecycleInstanceId(),
                                    updatedBy, previousAPI.build());
                        }
                    }
                }
            }
            if (!requireReSubscriptions) {
                if (StringUtils.isNotEmpty(api.getCopiedFromApiId())) {
                    List<Subscription> subscriptions = apiSubscriptionDAO.getAPISubscriptionsByAPI(
                            api.getCopiedFromApiId());
                    List<Subscription> subscriptionList = new ArrayList<>();
                    for (Subscription subscription : subscriptions) {
                        if (api.getPolicies().contains(subscription.getPolicy())) {
                            if (!APIMgtConstants.SubscriptionStatus.ON_HOLD.equals(subscription.getStatus())) {
                                subscriptionList.add(new Subscription(UUID.randomUUID().toString(),
                                        subscription.getApplication(), subscription.getApi(),
                                        subscription.getPolicy()));
                            }
                        }
                        apiSubscriptionDAO.copySubscriptions(subscriptionList);
                    }
                }
            }
            //publish API state change to gateway
            apiGateway.changeAPIState(originalAPI, status);
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't change the status of api ID " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, e.getErrorHandler());
        } catch (LifecycleException e) {
            String errorMsg = "Couldn't change the status of api ID " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
        } catch (GatewayException e) {
            String message = "Error occurred while changing the state of api ID: " + apiId + " to " + status
                    + "in gateway";
            log.error(message, e);
            throw new APIManagementException(message, ExceptionCodes.GATEWAY_EXCEPTION);
        }
    }

}
