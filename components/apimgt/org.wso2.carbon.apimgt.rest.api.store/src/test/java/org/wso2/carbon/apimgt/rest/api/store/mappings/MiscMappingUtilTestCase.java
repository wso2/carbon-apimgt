/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.models.User;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.rest.api.store.dto.UserDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.WorkflowResponseDTO;
import static org.testng.Assert.assertEquals;

public class MiscMappingUtilTestCase {

    @Test
    public void testFromWorkflowResponseToDTO() {
        WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
        workflowResponse.setWorkflowStatus(WorkflowStatus.APPROVED);
        WorkflowResponseDTO workflowResponseDTO = MiscMappingUtil.fromWorkflowResponseToDTO(workflowResponse);
        assertEquals(workflowResponseDTO.getWorkflowStatus().name(), workflowResponse.getWorkflowStatus().name());
    }

    @Test
    public void testFromUserDTOToUser() {
        UserDTO userDTO = new UserDTO().email("test@gmail.com").firstName("Test1").lastName("test2").password("dummy")
                                        .username("myuser1");
        User user = MiscMappingUtil.fromUserDTOToUser(userDTO);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getFirstName(), user.getFirstName());
        assertEquals(userDTO.getLastName(), user.getLastName());
        assertEquals(userDTO.getPassword(), new String(user.getPassword()));
        assertEquals(userDTO.getUsername(), user.getUsername());
    }
}
