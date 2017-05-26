/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.models.User;
import org.wso2.carbon.apimgt.rest.api.store.dto.UserDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.WorkflowResponseDTO.WorkflowStatusEnum;

/**
 * Mappings related to miscellaneous objects
 */
public class MiscMappingUtil {

    /**
     * Map WorkflowResponse to WorkflowResponseDTO
     *
     * @param response WorkflowResponse object
     * @return WorkflowResponseDTO mapped WorkflowResponseDTO
     */
    public static WorkflowResponseDTO fromWorkflowResponseToDTO(WorkflowResponse response) {
        WorkflowResponseDTO responseDTO = new WorkflowResponseDTO();
        responseDTO.setWorkflowStatus(WorkflowStatusEnum.valueOf(response.getWorkflowStatus().toString()));
        responseDTO.setJsonPayload(response.getJSONPayload());
        return responseDTO;
    }

    public static User fromUserDTOToUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword().toCharArray());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        return user;
    }

}
