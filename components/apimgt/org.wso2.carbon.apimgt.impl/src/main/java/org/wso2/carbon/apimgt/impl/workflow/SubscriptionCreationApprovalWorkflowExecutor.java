package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.List;

public class SubscriptionCreationApprovalWorkflowExecutor extends WorkflowExecutor{

    private static final Log log = LogFactory.getLog(SubscriptionCreationApprovalWorkflowExecutor.class);


    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    /**
     * This method is used to execute the workflow without giving a workflow response back to the caller to execute
     * some other task after completing the workflow
     *
     * @param workflowDTO - The WorkflowDTO which contains workflow contextual information related to the workflow.
     * @throws WorkflowException
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Executing Subscription Creation Webservice Workflow.. ");
        }

        SubscriptionWorkflowDTO subsWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        String callBackURL = subsWorkflowDTO.getCallbackUrl();

        String message="Approve API ["+ subsWorkflowDTO.getApiName()+" - "+subsWorkflowDTO.getApiVersion()+
                "] subscription creation request from subscriber - "+subsWorkflowDTO.getSubscriber()+
                " for the application - "+subsWorkflowDTO.getApplicationName();

        workflowDTO.setWorkflowDescription(message);

        workflowDTO.setMetadata("apiName", subsWorkflowDTO.getApiName());
        workflowDTO.setMetadata("apiVersion", subsWorkflowDTO.getApiVersion());
        workflowDTO.setMetadata("apiContext", subsWorkflowDTO.getApiContext());
        workflowDTO.setMetadata("apiProvider", subsWorkflowDTO.getApiProvider());
        workflowDTO.setMetadata("apiSubscriber", subsWorkflowDTO.getSubscriber());
        workflowDTO.setMetadata("applicationName", subsWorkflowDTO.getApplicationName());
        workflowDTO.setMetadata("TierName", subsWorkflowDTO.getTierName());
        workflowDTO.setMetadata("workflowExternalRef", subsWorkflowDTO.getExternalWorkflowReference());
        workflowDTO.setMetadata("callBackURL", callBackURL != null ? callBackURL : "?");

        workflowDTO.setProperties("Workflow Process","Subscription Creation");

        super.execute(workflowDTO);

        return new GeneralWorkflowResponse();
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);
        log.info("Subscription Creation [Complete] Workflow Invoked. Workflow ID : " + workflowDTO
                .getExternalWorkflowReference() + "Workflow State : " + workflowDTO.getStatus());

        if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            try {
                apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                        APIConstants.SubscriptionStatus.UNBLOCKED);
            } catch (APIManagementException e) {
                log.error("Could not complete subscription creation workflow", e);
                throw new WorkflowException("Could not complete subscription creation workflow", e);
            }
        } else if (WorkflowStatus.REJECTED.equals(workflowDTO.getStatus())) {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            try {
                apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                        APIConstants.SubscriptionStatus.REJECTED);
            } catch (APIManagementException e) {
                log.error("Could not complete subscription creation workflow", e);
                throw new WorkflowException("Could not complete subscription creation workflow", e);
            }
        }
        return new GeneralWorkflowResponse();
    }

    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {
        String errorMsg = null;
        super.cleanUpPendingTask(workflowExtRef);
        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for SubscriptionCreationApprovalWorkflowExecutor for :" + workflowExtRef);
        }
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            apiMgtDAO.deleteWorkflowRequest(workflowExtRef);

        } catch (APIManagementException axisFault) {
            errorMsg = "Error sending out cancel pending subscription approval process message. cause: " + axisFault
                    .getMessage();
            throw new WorkflowException(errorMsg, axisFault);
        }
    }


}
