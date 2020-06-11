/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class manage mapping to DTO of  workflow requests
 */
public class WorkflowMappingUtil {

    private static Object WorkflowTypeEnum;

    public enum WorkflowTypeEnum {
        AM_APPLICATION_CREATION, AM_SUBSCRIPTION_CREATION, AM_USER_SIGNUP, AM_APPLICATION_REGISTRATION_PRODUCTION, AM_APPLICATION_REGISTRATION_SANDBOX, AM_APPLICATION_DELETION, AM_API_STATE, AM_SUBSCRIPTION_DELETION,
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
        if (workflow.getWorkflowType().equals("AM_APPLICATION_CREATION")) {
            workflowInfoDTO.setWorkflowType(WorkflowInfoDTO.WorkflowTypeEnum.APPLICATION_CREATION);
        } else if (workflow.getWorkflowType().equals("AM_SUBSCRIPTION_CREATION")) {
            workflowInfoDTO.setWorkflowType(WorkflowInfoDTO.WorkflowTypeEnum.SUBSCRIPTION_CREATION);
        } else if (workflow.getWorkflowType().equals("AM_USER_SIGNUP")) {
            workflowInfoDTO.setWorkflowType(WorkflowInfoDTO.WorkflowTypeEnum.USER_SIGNUP);
        } else if (workflow.getWorkflowType().equals("AM_APPLICATION_REGISTRATION_PRODUCTION")) {
            workflowInfoDTO.setWorkflowType(WorkflowInfoDTO.WorkflowTypeEnum.APPLICATION_REGISTRATION_PRODUCTION);
        } else if (workflow.getWorkflowType().equals("AM_APPLICATION_REGISTRATION_SANDBOX")) {
            workflowInfoDTO.setWorkflowType(WorkflowInfoDTO.WorkflowTypeEnum.APPLICATION_REGISTRATION_SANDBOX);
        } else if (workflow.getWorkflowType().equals("AM_API_STATE")) {
            workflowInfoDTO.setWorkflowType(WorkflowInfoDTO.WorkflowTypeEnum.API_STATE);
        }
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
