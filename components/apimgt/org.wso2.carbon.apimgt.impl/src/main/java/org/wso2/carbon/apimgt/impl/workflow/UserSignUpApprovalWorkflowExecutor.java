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

public class UserSignUpApprovalWorkflowExecutor extends UserSignUpWorkflowExecutor{

    private static final Log log = LogFactory.getLog(UserSignUpWSWorkflowExecutor.class);


    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_USER_SIGNUP;
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException  {

        if (log.isDebugEnabled()) {
            log.debug("Executing User SignUp Webservice Workflow for " + workflowDTO.getWorkflowReference());
        }


        String callBackURL = workflowDTO.getCallbackUrl();
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(workflowDTO.getWorkflowReference());

        String message="Approve APIStore signup request done by "+tenantAwareUserName  +" from the tenant domain "+workflowDTO.getTenantDomain();

        workflowDTO.setWorkflowDescription(message);

        workflowDTO.setMetadata("TenantUserName", tenantAwareUserName);
        workflowDTO.setMetadata("TenantDomain", workflowDTO.getTenantDomain());
        workflowDTO.setMetadata("workflowExternalRef", workflowDTO.getExternalWorkflowReference());
        workflowDTO.setMetadata("callBackURL", callBackURL != null ? callBackURL : "?");

        workflowDTO.setProperties("Workflow Process","User Self Sign Up");

        super.execute(workflowDTO);


        return new GeneralWorkflowResponse();
    }

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
            } else if(adminUsername == null) {
                throw new WorkflowException("Can't connect to the authentication manager. adminUsername is missing");
            } else if(adminPassword == null) {
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
