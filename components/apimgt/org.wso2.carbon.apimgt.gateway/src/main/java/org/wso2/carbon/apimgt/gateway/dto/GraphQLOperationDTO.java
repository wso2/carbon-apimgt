/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.dto;

import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;

/**
 * Holds information related to GraphQL operation and its Verb Info DTO. Currently, used to hold verb information such
 * as throttle policy values of GraphQL subscription operations.
 */
public class GraphQLOperationDTO {

    VerbInfoDTO verbInfoDTO;
    String operation;

    public GraphQLOperationDTO(VerbInfoDTO verbInfoDTO, String operation) {
        this.verbInfoDTO = verbInfoDTO;
        this.operation = operation;
    }

    public VerbInfoDTO getVerbInfoDTO() {
        return verbInfoDTO;
    }

    public void setVerbInfoDTO(VerbInfoDTO verbInfoDTO) {
        this.verbInfoDTO = verbInfoDTO;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
