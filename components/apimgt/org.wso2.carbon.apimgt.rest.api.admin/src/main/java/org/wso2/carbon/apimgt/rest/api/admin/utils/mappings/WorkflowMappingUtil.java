package org.wso2.carbon.apimgt.rest.api.admin.utils.mappings;

import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkflowMappingUtil {

    private static Object WorkflowTypeEnum;



    public enum WorkflowTypeEnum {
        AM_APPLICATION_CREATION,  AM_SUBSCRIPTION_CREATION,  AM_USER_SIGNUP,  AM_APPLICATION_REGISTRATION_PRODUCTION,  AM_APPLICATION_REGISTRATION_SANDBOX,  AM_APPLICATION_DELETION,  AM_API_STATE,  AM_SUBSCRIPTION_DELETION,
    };

    private WorkflowInfoDTO.WorkflowTypeEnum workflowType = null;

    public static WorkflowListDTO fromWorkflowsToDTO(Workflow[] workflows, int limit, int offset) {
        WorkflowListDTO workflowListDTO = new WorkflowListDTO();
        List<WorkflowInfoDTO> workflowInfoDTOs = workflowListDTO.getList();

        if (workflowInfoDTOs == null) {
            workflowInfoDTOs = new ArrayList<>();
            workflowListDTO.setList(workflowInfoDTOs);
        }

        //identifying the proper start and end indexes
        int start = offset < workflows.length && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= workflows.length - 1 ? offset + limit - 1 : workflows.length - 1;

        for (int i = start; i <= end; i++) {
            workflowInfoDTOs.add(fromWorkflowsToInfoDTO(workflows[i]));
        }
        workflowListDTO.setCount(workflowInfoDTOs.size());
        return workflowListDTO;
    }

    public static WorkflowInfoDTO fromWorkflowsToInfoDTO(Workflow workflow) {

        WorkflowInfoDTO workflowInfoDTO = new WorkflowInfoDTO();
        workflowInfoDTO.setWorkflowType(WorkflowInfoDTO.WorkflowTypeEnum.valueOf(workflow.getWorkflowType()));
        workflowInfoDTO.setWorkflowStatus(WorkflowInfoDTO.WorkflowStatusEnum.valueOf(workflow.getStatus().toString()));
        workflowInfoDTO.setCreatedTime(workflow.getCreatedTime());
        workflowInfoDTO.setUpdatedTime(workflow.getUpdatedTime());
        workflowInfoDTO.setReferenceId(workflow.getExternalWorkflowReference());
        workflowInfoDTO.setDescription(workflow.getWorkflowDescription());
        workflowInfoDTO.setProperties(workflow.getProperties());

        return workflowInfoDTO;


    }

    public static void setPaginationParams(WorkflowListDTO workflowListDTO, Integer limit, Integer offset, int length) {

        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, length);

        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getApplicationPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getApplicationPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }
        workflowListDTO.setNext(paginatedNext);
        workflowListDTO.setPrevious(paginatedPrevious);

    }
}
