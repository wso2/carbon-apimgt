package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.List;

public class ApplicationCreationApprovalWorkflowExecutor extends WorkflowExecutor{


    private static final Log log = LogFactory.getLog(ApplicationCreationApprovalWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION;
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Executing Application creation Workflow.");
        }

        ApplicationWorkflowDTO appWorkFlowDTO = (ApplicationWorkflowDTO) workflowDTO;
        Application application = appWorkFlowDTO.getApplication();
        String callBackURL = appWorkFlowDTO.getCallbackUrl();

        String message="Approve application ["+ application.getName() +"] creation request from application creator -"
                +appWorkFlowDTO.getUserName()+" with throttling tier - "+application.getTier();

        workflowDTO.setWorkflowDescription(message);

        workflowDTO.setMetadata("applicationName", application.getName());
        workflowDTO.setMetadata("applicationTier", application.getTier());
        workflowDTO.setMetadata("applicationCallbackUrl", application.getCallbackUrl());
        workflowDTO.setMetadata("applicationDescription", application.getDescription());
        workflowDTO.setMetadata("tenantDomain", appWorkFlowDTO.getTenantDomain());
        workflowDTO.setMetadata("userName", appWorkFlowDTO.getUserName());
        workflowDTO.setMetadata("workflowExternalRef", appWorkFlowDTO.getExternalWorkflowReference());
        workflowDTO.setMetadata("callBackURL", callBackURL != null ? callBackURL : "?");

        workflowDTO.setProperties("Workflow Process","Application Creation");

        super.execute(workflowDTO);

        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the external process status.
     * Based on the workflow , we will update the status column of the
     * Application table
     *
     * @param workFlowDTO object
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {

        workFlowDTO.setUpdatedTime(System.currentTimeMillis());
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        try {
            if (dao.getApplicationById(Integer.parseInt(workFlowDTO.getWorkflowReference())) != null) {

                super.complete(workFlowDTO);
                log.info("Application Creation [Complete] Workflow Invoked. Workflow ID : " + workFlowDTO
                        .getExternalWorkflowReference() + "Workflow State : " + workFlowDTO.getStatus());

                String status = null;
                if (WorkflowStatus.CREATED.equals(workFlowDTO.getStatus())) {
                    status = APIConstants.ApplicationStatus.APPLICATION_CREATED;
                } else if (WorkflowStatus.REJECTED.equals(workFlowDTO.getStatus())) {
                    status = APIConstants.ApplicationStatus.APPLICATION_REJECTED;
                } else if (WorkflowStatus.APPROVED.equals(workFlowDTO.getStatus())) {
                    status = APIConstants.ApplicationStatus.APPLICATION_APPROVED;
                }

                try {
                    dao.updateApplicationStatus(Integer.parseInt(workFlowDTO.getWorkflowReference()), status);
                } catch (APIManagementException e) {
                    String msg = "Error occurred when updating the status of the Application creation " + "process";
                    log.error(msg, e);
                    throw new WorkflowException(msg, e);
                }

            } else {
                String msg = "Application does not exist";
                throw new WorkflowException(msg);
            }
        } catch (APIManagementException e) {
            String msg = "Error occurred when retrieving the Application creation with workflow ID :" + workFlowDTO
                    .getWorkflowReference();
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        }

        return new GeneralWorkflowResponse();
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {

        // implemetation is not provided in this version
        return null;
    }

    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {
        String errorMsg;

        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for ApplicationCreationApprovalWorkflowExecutor for :" + workflowExtRef);
        }
        super.cleanUpPendingTask(workflowExtRef);
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            apiMgtDAO.deleteWorkflowRequest(workflowExtRef);


        } catch (APIManagementException axisFault) {
            errorMsg = "Error sending out cancel pending application approval process message. cause: " + axisFault
                    .getMessage();
            throw new WorkflowException(errorMsg, axisFault);
        }
    }
}
