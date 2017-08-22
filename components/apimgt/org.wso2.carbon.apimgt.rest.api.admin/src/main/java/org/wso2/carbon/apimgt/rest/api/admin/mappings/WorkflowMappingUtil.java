/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.admin.mappings;

import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.workflow.Workflow;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowResponseDTO.WorkflowStatusEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for mapping Workflow reated models and their sub components into REST API DTOs
 * and vice-versa
 */
public class WorkflowMappingUtil {
    
    private WorkflowMappingUtil() {
    }

    /**
     * Map WorkflowResponse to WorkflowResponseDTO
     * @param response WorkflowResponse object
     * @return WorkflowResponseDTO mapped WorkflowResponseDTO
     */
    public static WorkflowResponseDTO toWorkflowResponseDTO(WorkflowResponse response) {
        WorkflowResponseDTO responseDTO = new WorkflowResponseDTO();
        responseDTO.setWorkflowStatus(WorkflowStatusEnum.valueOf(response.getWorkflowStatus().toString()));
        responseDTO.setJsonPayload(response.getJSONPayload());
        return responseDTO;
    }

    /**
     * Map Workflow list to WorkflowListDTO
     * @param workflowList list of Workflow
     * @return list of WorkflowDTO
     */
    public static WorkflowListDTO toWorkflowListDTO(List<Workflow> workflowList) {
        WorkflowListDTO workflowListDTO = new WorkflowListDTO();
        workflowListDTO.setCount(workflowList.size());
        List<WorkflowDTO> list = new ArrayList<>();
        for (Workflow item : workflowList) {
            WorkflowDTO workflowDTO = new WorkflowDTO();
            workflowDTO.setCreatedTime(item.getCreatedTime().toString());
            workflowDTO.setDescription(item.getWorkflowDescription());
            workflowDTO.setType(item.getWorkflowType());
            workflowDTO.setReferenceId(item.getExternalWorkflowReference());
            workflowDTO.setWorkflowStatus(item.getStatus().toString());    
            list.add(workflowDTO);
        }
        workflowListDTO.setList(list);

        return workflowListDTO;
    }
    
    /**
     * Map Workflow to WorkflowDTO
     * @param response WorkflowResponse object
     * @return WorkflowResponseDTO mapped WorkflowResponseDTO
     */
    public static WorkflowDTO toWorkflowDTO(Workflow response) {
        WorkflowDTO workflowDTO = new WorkflowDTO();
        if (response != null) {
            workflowDTO.setCreatedTime(response.getCreatedTime().toString());
            workflowDTO.setDescription(response.getWorkflowDescription());
            workflowDTO.setType(response.getWorkflowType());
            workflowDTO.setReferenceId(response.getExternalWorkflowReference());
            workflowDTO.setWorkflowStatus(response.getStatus().toString());
        }

        return workflowDTO;
    }
}
