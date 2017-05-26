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
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.lcm.core.beans.CheckItemBean;
import org.wso2.carbon.lcm.core.exception.LifecycleException;
import org.wso2.carbon.lcm.core.impl.LifecycleState;

import java.time.LocalDateTime;
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
    private APISubscriptionDAO apiSubscriptionDAO;
    private APILifecycleManager apiLifecycleManager;

    public APIStateChangeWorkflow(ApiDAO apiDAO, APISubscriptionDAO apiSubscriptionDAO, WorkflowDAO workflowDAO,
                                  APILifecycleManager apiLifecycleManager) {
        super(workflowDAO, Category.PUBLISHER);
        this.apiDAO = apiDAO;
        this.apiLifecycleManager = apiLifecycleManager;
        this.apiSubscriptionDAO = apiSubscriptionDAO;
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
            String targetState = getAttribute(APIMgtConstants.WorkflowConstants.ATTRIBUTE_API_TARGET_STATE);
            String localTime = getAttribute(APIMgtConstants.WorkflowConstants.ATTRIBUTE_API_LAST_UPTIME);
            LocalDateTime time = LocalDateTime.parse(localTime);
            updateAPIStatusForWorkflowComplete(getWorkflowReference(), targetState, invoker, time);
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

    private void updateAPIStatusForWorkflowComplete(String apiId, String status, String updatedBy, LocalDateTime time)
            throws APIManagementException {
        boolean requireReSubscriptions = false;
        boolean deprecateOlderVersion = false;
        try {
            API api = apiDAO.getAPI(apiId);
            if (api != null) {
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
                            if (api.getPolicies().contains(subscription.getSubscriptionTier())) {
                                if (!APIMgtConstants.SubscriptionStatus.ON_HOLD.equals(subscription.getStatus())) {
                                    subscriptionList.add(new Subscription(UUID.randomUUID().toString(),
                                            subscription.getApplication(), subscription.getApi(),
                                            subscription.getSubscriptionTier()));
                                }
                            }
                            apiSubscriptionDAO.copySubscriptions(subscriptionList);
                        }
                    }
                }
            } else {
                throw new APIMgtResourceNotFoundException("Requested API " + apiId + " Not Available");
            }
        } catch (APIMgtDAOException e) {
            String errorMsg = "Couldn't change the status of api ID " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } catch (LifecycleException e) {
            String errorMsg = "Couldn't change the status of api ID " + apiId;
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
        }
    }

}
