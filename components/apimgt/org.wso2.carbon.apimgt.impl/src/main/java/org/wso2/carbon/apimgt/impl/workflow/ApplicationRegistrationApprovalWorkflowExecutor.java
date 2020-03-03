package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.List;

public class ApplicationRegistrationApprovalWorkflowExecutor extends AbstractApplicationRegistrationWorkflowExecutor{


    private static final Log log = LogFactory.getLog(ApplicationRegistrationApprovalWorkflowExecutor.class);

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Executing Application registration Workflow..");
        }

        ApplicationRegistrationWorkflowDTO appRegDTO = (ApplicationRegistrationWorkflowDTO) workflowDTO;
        Application application = appRegDTO.getApplication();
        String callBackURL = appRegDTO.getCallbackUrl();
        String applicationCallbackUrl = application.getCallbackUrl();
        String applicationDescription = application.getDescription();

        String message="Approve request to create "+appRegDTO.getKeyType()+" keys for [ "+application.getName()+
                " ] from application creator - "+appRegDTO.getUserName()+" with throttling tier - "+application.getTier() ;


        workflowDTO.setWorkflowDescription(message);

        workflowDTO.setMetadata("applicationName", application.getName());
        workflowDTO.setMetadata("applicationTier", application.getTier());
        workflowDTO.setMetadata("applicationCallbackUrl", applicationCallbackUrl != null ? applicationCallbackUrl : "?");
        workflowDTO.setMetadata("applicationDescription", applicationDescription != null ? applicationDescription : "?");
        workflowDTO.setMetadata("TenantDomain", appRegDTO.getTenantDomain());
        workflowDTO.setMetadata("UserName", appRegDTO.getUserName());
        workflowDTO.setMetadata("workflowExternalRef", appRegDTO.getExternalWorkflowReference());
        workflowDTO.setMetadata("callBackURL", callBackURL != null ? callBackURL : "?");
        workflowDTO.setMetadata("KeyType", appRegDTO.getKeyType());

        workflowDTO.setProperties("Workflow Process","Registration Creation");

        super.execute(workflowDTO);


        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the external process status.
     * Based on the workflow , we will update the status column of the
     * AM_APPLICATION_KEY_MAPPING table
     *
     * @param workFlowDTO
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {
        workFlowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workFlowDTO);
        log.info("Application Registration [Complete] Workflow Invoked. Workflow ID : " + workFlowDTO
                .getExternalWorkflowReference() + "Workflow State : " + workFlowDTO.getStatus());

        if (WorkflowStatus.APPROVED.equals(workFlowDTO.getStatus())) {
            try {
                generateKeysForApplication((ApplicationRegistrationWorkflowDTO) workFlowDTO);
            } catch (APIManagementException e) {
                String msg = "Error occurred when updating the status of the Application Registration process";
                log.error(msg, e);
                throw new WorkflowException(msg, e);
            }
        }
        return new GeneralWorkflowResponse();
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {
        super.cleanUpPendingTask(workflowExtRef);
        String errorMsg = null;
        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for ApplicationRegistrationApprovalWorkflowExecutor for :" + workflowExtRef);
        }
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            apiMgtDAO.deleteWorkflowRequest(workflowExtRef);

        } catch (APIManagementException axisFault) {
            errorMsg = "Error sending out cancel pending registration approval process message. Cause: " + axisFault
                    .getMessage();
            throw new WorkflowException(errorMsg, axisFault);
        }
    }

}
