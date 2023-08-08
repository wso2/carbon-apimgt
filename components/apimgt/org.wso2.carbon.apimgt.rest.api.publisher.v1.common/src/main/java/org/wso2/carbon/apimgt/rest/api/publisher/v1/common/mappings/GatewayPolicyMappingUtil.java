/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.model.GatewayPolicyData;
import org.wso2.carbon.apimgt.api.model.GatewayPolicyDeployment;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for mapping Gateway Global Policy Objects into REST API Global Policy related DTOs
 * and vice versa.
 */
public class GatewayPolicyMappingUtil {
    public static Map<Boolean, List<GatewayPolicyDeployment>> fromDTOToGatewayPolicyDeploymentMap(
            String gatewayPolicyMappingId, List<GatewayPolicyDeploymentDTO> gatewayPolicyDeploymentDTO) {
        List<GatewayPolicyDeployment> gatewayPolicyDeploymentList = new ArrayList<>();
        List<GatewayPolicyDeployment> gatewayPolicyUndeploymentList = new ArrayList<>();
        for (GatewayPolicyDeploymentDTO gatewayPolicyDeploymentDTOEntry : gatewayPolicyDeploymentDTO) {
            GatewayPolicyDeployment gatewayPolicyDeployment = new GatewayPolicyDeployment();
            gatewayPolicyDeployment.setMappingUuid(gatewayPolicyMappingId);
            gatewayPolicyDeployment.setGatewayLabel(gatewayPolicyDeploymentDTOEntry.getGatewayLabel());

            if (gatewayPolicyDeploymentDTOEntry.isGatewayDeployment()) {
                gatewayPolicyDeploymentList.add(gatewayPolicyDeployment);
            } else {
                gatewayPolicyUndeploymentList.add(gatewayPolicyDeployment);
            }
        }
        Map<Boolean, List<GatewayPolicyDeployment>> gatewayPolicyDeploymentMap = new HashMap<>();
        gatewayPolicyDeploymentMap.put(true, gatewayPolicyDeploymentList);
        gatewayPolicyDeploymentMap.put(false, gatewayPolicyUndeploymentList);
        return gatewayPolicyDeploymentMap;
    }

    public static GatewayPolicyMappingDataListDTO fromGatewayPolicyDataListToDTO(
            List<GatewayPolicyData> policyDataList, int offset, int limit) {

        List<GatewayPolicyMappingsDTO> gatewayPolicyDataList = new ArrayList<>();

        if (policyDataList == null) {
            policyDataList = new ArrayList<>();
        }

        int size = policyDataList.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = Math.min(offset + limit - 1, size - 1);
        for (int i = start; i <= end; i++) {
            gatewayPolicyDataList.add(fromGatewayPolicyDataToDTO(policyDataList.get(i)));
        }

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(size);

        GatewayPolicyMappingDataListDTO dataListDTO = new GatewayPolicyMappingDataListDTO();
        dataListDTO.setList(gatewayPolicyDataList);
        dataListDTO.setCount(gatewayPolicyDataList.size());
        dataListDTO.setPagination(paginationDTO);

        return dataListDTO;
    }

    public static GatewayPolicyMappingsDTO fromGatewayPolicyDataToDTO(GatewayPolicyData policyData) {

        GatewayPolicyMappingsDTO gatewayPolicyMappingsDTO = new GatewayPolicyMappingsDTO();
        gatewayPolicyMappingsDTO.appliedGatewayLabels(new ArrayList<>(policyData.getGatewayLabels()));
        gatewayPolicyMappingsDTO.description(policyData.getPolicyMappingDescription());
        gatewayPolicyMappingsDTO.displayName(policyData.getPolicyMappingName());
        gatewayPolicyMappingsDTO.id(policyData.getPolicyMappingId());
        gatewayPolicyMappingsDTO.policyMapping(
                OperationPolicyMappingUtil.fromOperationPolicyListToDTO(policyData.getGatewayPolicies()));

        return gatewayPolicyMappingsDTO;
    }
}
