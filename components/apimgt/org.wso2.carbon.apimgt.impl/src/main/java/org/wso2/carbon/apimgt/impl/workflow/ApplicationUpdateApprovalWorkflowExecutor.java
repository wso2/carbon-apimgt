package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.List;

public class ApplicationUpdateApprovalWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(ApplicationUpdateApprovalWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return null;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Executing Application Update Approval Workflow.. ");
        }
        ApplicationWorkflowDTO applicationWorkflowDTO = (ApplicationWorkflowDTO) workflowDTO;
        Application application = applicationWorkflowDTO.getApplication();
        Application existingApplication = applicationWorkflowDTO.getExistingApplication();
        String message = "Approve application " + application.getName() + " creation request from application creator -"
                + applicationWorkflowDTO.getUserName();
        workflowDTO.setWorkflowDescription(message);
        workflowDTO.setProperties("existingApplicationName", existingApplication.getName());
        workflowDTO.setProperties("requestedApplicationName", application.getName());

        super.execute(workflowDTO);

        return new GeneralWorkflowResponse();
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {
        workFlowDTO.setUpdatedTime(System.currentTimeMillis());
        ApiMgtDAO dao = ApiMgtDAO.getInstance();

        try {
            if (dao.getApplicationById(Integer.parseInt(workFlowDTO.getWorkflowReference())) != null) {
                super.complete(workFlowDTO);
            }

        }catch (APIManagementException e) {
            String msg = "Error occurred when retrieving the Application creation with workflow ID :" + workFlowDTO
                    .getWorkflowReference();
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        }

        return new GeneralWorkflowResponse();
    }

}

