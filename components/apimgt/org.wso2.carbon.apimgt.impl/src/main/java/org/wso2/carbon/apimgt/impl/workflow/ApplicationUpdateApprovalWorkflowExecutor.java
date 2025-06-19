package org.wso2.carbon.apimgt.impl.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import java.util.Objects;

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

        workflowDTO.setMetadata("requestedApplicationName", application.getName());
        workflowDTO.setMetadata("requestedTier", application.getTier());
        workflowDTO.setMetadata("requestedDescription", application.getDescription());
        workflowDTO.setMetadata("requestedSharedOrganization", application.getDescription());
        ObjectMapper objectMapper = new ObjectMapper();

        String requestedCustomAttributes;
        try {
            requestedCustomAttributes = objectMapper.writeValueAsString(application.getApplicationAttributes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        workflowDTO.setMetadata("requestedCustomAttributes", requestedCustomAttributes);
        workflowDTO.setMetadata("requestedGroupIDs", application.getGroupId());

        workflowDTO.setProperties("applicationName", existingApplication.getName());
        workflowDTO.setProperties("userName", existingApplication.getOwner());
        workflowDTO.setProperties("applicationTier", existingApplication.getTier());

        if (!Objects.equals(application.getName(), existingApplication.getName())){

        }

        //this has to be explicit with the details that are updated
        String message = "Approve application " + application.getName() +
                " update application request from application creator -" + applicationWorkflowDTO.getUserName();
        workflowDTO.setWorkflowDescription(message);

        super.execute(workflowDTO);

        return new GeneralWorkflowResponse();
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {
        workFlowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workFlowDTO);
        ApiMgtDAO dao = ApiMgtDAO.getInstance();

        try {
            Application application = dao.getApplicationById(Integer.parseInt(workFlowDTO.getWorkflowReference()));
            if (WorkflowStatus.APPROVED.equals(workFlowDTO.getStatus())) {
                application.setStatus(APIConstants.ApplicationStatus.APPLICATION_APPROVED);
                application.setName(workFlowDTO.getMetadata("requestedApplicationName"));
                application.setTier(workFlowDTO.getMetadata("requestedTier"));
                application.setDescription(workFlowDTO.getMetadata("requestedDescription"));

                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> applicationAttributes =
                        objectMapper.readValue(workFlowDTO.getMetadata("requestedCustomAttributes"), Map.class);
                application.setApplicationAttributes(applicationAttributes);

                application.setGroupId(workFlowDTO.getMetadata("requestedGroupIDs"));
                dao.updateApplicationStatus(application.getId(),APIConstants.ApplicationStatus.APPLICATION_APPROVED);
                dao.updateApplication(application);
            }else
            {
                dao.updateApplicationStatus(application.getId(), APIConstants.ApplicationStatus.APPLICATION_REJECTED);
            }

        }catch (APIManagementException e) {
            String msg = "Error occurred when retrieving the Application creation with workflow ID :" + workFlowDTO
                    .getWorkflowReference();
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new GeneralWorkflowResponse();
    }

}

