package org.wso2.carbon.apimgt.impl.workflow;

import java.util.Collections;
import java.util.List;

import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

public class TestWorkflow extends WorkflowExecutor{

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
        
        
        APIStateWorkflowDTO apiStateWorkFlowDTO = (APIStateWorkflowDTO) workflowDTO;
        if("DEPRECATED".equals(apiStateWorkFlowDTO.getApiCurrentState().toUpperCase())){
            workflowDTO.setStatus(WorkflowStatus.REJECTED);
            return super.execute(workflowDTO);
        } else if ("BLOCKED".equals(apiStateWorkFlowDTO.getApiCurrentState().toUpperCase())){
            workflowDTO.setStatus(WorkflowStatus.APPROVED);
           return super.execute(workflowDTO);
        }else {
            workflowDTO.setStatus(WorkflowStatus.APPROVED);
            HttpWorkflowResponse wf = new HttpWorkflowResponse();
            wf.setRedirectConfirmationMsg("You will be redirected to a new place");
            wf.setRedirectUrl("http://www.google.com");
            return wf;
        }        
    }
    
    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        // TODO Auto-generated method stub
        return super.complete(workflowDTO);
    }

}
