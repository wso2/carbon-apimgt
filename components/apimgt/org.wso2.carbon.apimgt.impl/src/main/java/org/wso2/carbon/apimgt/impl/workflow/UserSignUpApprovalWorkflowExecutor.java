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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.SelfSignUpUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;

/**
 * Approval workflow for User Self Sign Up.
 */
public class UserSignUpApprovalWorkflowExecutor extends UserSignUpWorkflowExecutor {

    private static final Log log = LogFactory.getLog(UserSignUpWSWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_USER_SIGNUP;
    }

    /**
     * Execute the User self sign up workflow approval process.
     *
     * @param workflowDTO
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Executing User SignUp Webservice Workflow for " + workflowDTO.getWorkflowReference());
        }
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(workflowDTO.getWorkflowReference());
        String message = "Approve APIStore signup request done by " + tenantAwareUserName + " from the tenant domain " +
                workflowDTO.getTenantDomain();
        workflowDTO.setWorkflowDescription(message);
        workflowDTO.setProperties("tenantAwareUserName", tenantAwareUserName);
        workflowDTO.setProperties("tenantDomain", workflowDTO.getTenantDomain());
        super.execute(workflowDTO);
        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the Approval workflow executor for User self sign up.
     *
     * @param workflowDTO
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {

        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        if (log.isDebugEnabled()) {
            log.debug("User Sign Up [Complete] Workflow Invoked. Workflow ID : " +
                    workflowDTO.getExternalWorkflowReference() + "Workflow State : " +
                    workflowDTO.getStatus());
        }
        super.complete(workflowDTO);
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        String serverURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        String tenantDomain = workflowDTO.getTenantDomain();
        try {
            UserRegistrationConfigDTO signupConfig = SelfSignUpUtil.getSignupConfiguration(tenantDomain);
            String adminUsername = signupConfig.getAdminUserName();
            String adminPassword = signupConfig.getAdminPassword();
            if (serverURL == null) {
                throw new WorkflowException("Can't connect to the authentication manager. serverUrl is missing");
            } else if (adminUsername == null) {
                throw new WorkflowException("Can't connect to the authentication manager. adminUsername is missing");
            } else if (adminPassword == null) {
                throw new WorkflowException("Can't connect to the authentication manager. adminPassword is missing");
            }
            String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(workflowDTO.getWorkflowReference());
            if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
                try {
                    updateRolesOfUser(serverURL, adminUsername, adminPassword, tenantAwareUserName,
                            SelfSignUpUtil.getRoleNames(signupConfig), tenantDomain);
                } catch (Exception e) {
                    // updateRolesOfUser throws generic Exception. Therefore generic Exception is caught
                    throw new WorkflowException("Error while assigning role to user", e);
                }
            } else {
                try {
                    /* Remove created user */
                    deleteUser(serverURL, adminUsername, adminPassword, tenantAwareUserName);
                } catch (Exception e) {
                    throw new WorkflowException("Error while deleting the user", e);
                }
            }
        } catch (APIManagementException e1) {
            throw new WorkflowException("Error while accessing signup configuration", e1);
        }
        return new GeneralWorkflowResponse();
    }

    /**
     * Handle cleanup task for user self sign up Approval workflow executor.
     * Use workflow external reference  to delete the pending workflow request
     *
     * @param workflowExtRef Workflow external reference of pending workflow request
     */
    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {
        String errorMsg = null;
        super.cleanUpPendingTask(workflowExtRef);
        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for UserSignUpApprovalWorkflowExecutor for :" + workflowExtRef);
        }
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            apiMgtDAO.deleteWorkflowRequest(workflowExtRef);
        } catch (APIManagementException axisFault) {
            errorMsg = "Error sending out cancel pending UserSelfSignUp approval process message. cause: " + axisFault
                    .getMessage();
            throw new WorkflowException(errorMsg, axisFault);
        }
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }
}
