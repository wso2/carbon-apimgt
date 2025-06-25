package org.wso2.carbon.apimgt.impl.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.util.*;

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

        workflowDTO.setProperties("applicationName", existingApplication.getName());
        workflowDTO.setProperties("userName", existingApplication.getOwner());
        workflowDTO.setProperties("applicationTier", existingApplication.getTier());

        List<Map<String, String>> changes = new ArrayList<>();

        if (!Objects.equals(application.getName(), existingApplication.getName())){
            changes.add(createChangeObject("Application Name",
                    existingApplication.getName(), application.getName()));
        }
        if (!Objects.equals(application.getTier(), existingApplication.getTier())){
            changes.add(createChangeObject("Tier",
                    existingApplication.getTier(), application.getTier()));
        }
        if (!Objects.equals(application.getDescription(), existingApplication.getDescription())){
            changes.add(createChangeObject("Description",
                    existingApplication.getDescription(), application.getDescription()));
        }
        if (!Objects.equals(application.getGroupId(), existingApplication.getGroupId())){
            changes.add(createChangeObject("Groups",
                    existingApplication.getGroupId(), application.getGroupId()));
        }
        if (!Objects.equals(application.getSharedOrganization(), existingApplication.getSharedOrganization())){
            changes.add(createChangeObject("Shared Organization",
                    existingApplication.getSharedOrganization(), application.getSharedOrganization()));
        }

        changes.addAll(compareAndUpdateCustomAttributes
                (existingApplication.getApplicationAttributes(),application.getApplicationAttributes()));

        String changesDiff;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            changesDiff = objectMapper.writeValueAsString(changes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        workflowDTO.setProperties("changes", changesDiff);

        workflowDTO.setMetadata("requestedApplicationName", application.getName());
        workflowDTO.setMetadata("requestedTier", application.getTier());
        workflowDTO.setMetadata("requestedDescription", application.getDescription());
        workflowDTO.setMetadata("requestedSharedOrganization", application.getSharedOrganization());
        if (application.getGroupId() != null){
            workflowDTO.setMetadata("requestedGroupIDs", application.getGroupId());
        }

        String requestedCustomAttributes;
        try {
            requestedCustomAttributes = objectMapper.writeValueAsString(application.getApplicationAttributes());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        workflowDTO.setMetadata("requestedCustomAttributes", requestedCustomAttributes);

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

                if (workFlowDTO.getMetadata().containsKey("requestedGroupIDs")){
                    application.setGroupId(workFlowDTO.getMetadata("requestedGroupIDs"));
                }

                application.setSharedOrganization(workFlowDTO.getMetadata("requestedSharedOrganization"));
                dao.updateApplicationStatus(application.getId(), APIConstants.ApplicationStatus.APPLICATION_APPROVED);
                dao.updateApplication(application);
            } else {
                dao.updateApplicationStatus(application.getId(), APIConstants.ApplicationStatus.APPLICATION_APPROVED);
            }

        } catch (APIManagementException e) {
            String msg = "Error occurred when retrieving the Application creation with workflow ID :" + workFlowDTO
                    .getWorkflowReference();
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new GeneralWorkflowResponse();
    }

    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {

        String errorMsg;
        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for ApplicationUpdateApprovalWorkflowExecutor for :" + workflowExtRef);
        }
        super.cleanUpPendingTask(workflowExtRef);
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            apiMgtDAO.deleteWorkflowRequest(workflowExtRef);
        } catch (APIManagementException axisFault) {
            errorMsg = "Error sending out cancel pending application update approval process message. cause: " +
                    axisFault.getMessage();
            throw new WorkflowException(errorMsg, axisFault);
        }
    }

    private static Map<String, String> createChangeObject(String attributeName, String current, String expected) {
        return Map.of(
                "attributeName", attributeName,
                "current", current == null ? "" : current,
                "expected", expected == null ? "" : expected
        );
    }


    public static List<Map<String, String>> compareAndUpdateCustomAttributes(Map<String, String> oldMap, Map<String,
            String> newMap) {

        List<Map<String, String>> attribChanges = new ArrayList<>();
        for (String key : newMap.keySet()) {
            if (!oldMap.containsKey(key)) {
                System.out.println("Added key: " + key + ", value: " + newMap.get(key));
                attribChanges.add(createChangeObject(key, "N/A", newMap.get(key)));
            } else if (!Objects.equals(oldMap.get(key), newMap.get(key))) {
                System.out.println("Changed key: " + key + ", from: " + oldMap.get(key) + " to: " + newMap.get(key));
                attribChanges.add(createChangeObject(key, oldMap.get(key), newMap.get(key)));
            }
        }

        for (String key : oldMap.keySet()) {
            if (!newMap.containsKey(key)) {
                System.out.println("Removed key: " + key + ", value was: " + oldMap.get(key));
                attribChanges.add(createChangeObject(key, oldMap.get(key), "Removed"));
            }
        }

        return attribChanges;
    }
}

