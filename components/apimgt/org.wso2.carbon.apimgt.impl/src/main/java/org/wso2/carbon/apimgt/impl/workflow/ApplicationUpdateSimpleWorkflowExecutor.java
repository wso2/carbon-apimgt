package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.List;

public class ApplicationUpdateSimpleWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(ApplicationUpdateSimpleWorkflowExecutor.class);


    @Override
    public String getWorkflowType() {
        return null;
    }

    public WorkflowResponse execute(WorkflowDTO workFlowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.info("Executing Application Update Workflow..");
        }
        workFlowDTO.setStatus(WorkflowStatus.APPROVED);
        complete(workFlowDTO);
        return new GeneralWorkflowResponse();
    }

    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.info("Complete  Application Update Workflow..");
        }
        String status = null;
        //To DO
        if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
            status = APIConstants.ApplicationStatus.APPLICATION_APPROVED;
        }
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        try {
            dao.updateApplicationStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),status);
        } catch (APIManagementException e) {
            String msg = "Error occurred when updating the status of the Application update process";
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        }
        return new GeneralWorkflowResponse();
    }
    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }
}
