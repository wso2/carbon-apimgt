/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.model.GatewayPolicyData;
import org.wso2.carbon.apimgt.api.model.GatewayPolicyDeployment;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingDeploymentInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayPolicyMappingInfoDTO;
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
    /**
     * Converts a list of GatewayPolicyDeploymentDTO objects into a map that contains a list of
     * GatewayPolicyDeployment objects grouped by their deployment state.
     *
     * @param gatewayPolicyMappingId     The UUID of the Gateway Policy Mapping
     * @param gatewayPolicyDeploymentDTO The list of GatewayPolicyDeploymentDTO objects
     * @return A map that contains a list of GatewayPolicyDeployment objects grouped by their deployment state
     */
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

    /**
     * Converts a GatewayPolicyData list into corresponding REST API DTO object.
     *
     * @param policyDataList The list of GatewayPolicyData objects
     * @param offset         The starting index of the result set
     * @param limit          The max number of elements in the result set
     * @return A GatewayPolicyMappingDataListDTO object that contains a list of GatewayPolicyMappingDeploymentInfoDTO
     */
    public static GatewayPolicyMappingDataListDTO fromGatewayPolicyDataListToDTO(
            List<GatewayPolicyData> policyDataList, int offset, int limit) {

        List<GatewayPolicyMappingDeploymentInfoDTO> gatewayPolicyDataList = new ArrayList<>();

        if (policyDataList == null) {
            policyDataList = new ArrayList<>();
        }

        int size = policyDataList.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = Math.min(offset + limit - 1, size - 1);
        for (int i = start; i <= end; i++) {
            gatewayPolicyDataList.add(
                    fromGatewayPolicyDataToGatewayPolicyMappingDeploymentInfoDTO(policyDataList.get(i)));
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

    /**
     * Converts a GatewayPolicyData object into corresponding REST API DTO object without policy information.
     *
     * @param policyData The GatewayPolicyData object
     * @return A GatewayPolicyMappingDeploymentInfoDTO object
     */
    public static GatewayPolicyMappingDeploymentInfoDTO fromGatewayPolicyDataToGatewayPolicyMappingDeploymentInfoDTO(
            GatewayPolicyData policyData) {

        GatewayPolicyMappingDeploymentInfoDTO gatewayPolicyMappingDeploymentInfoDTO =
                new GatewayPolicyMappingDeploymentInfoDTO();
        gatewayPolicyMappingDeploymentInfoDTO.appliedGatewayLabels(new ArrayList<>(policyData.getGatewayLabels()));
        gatewayPolicyMappingDeploymentInfoDTO.description(policyData.getPolicyMappingDescription());
        gatewayPolicyMappingDeploymentInfoDTO.displayName(policyData.getPolicyMappingName());
        gatewayPolicyMappingDeploymentInfoDTO.id(policyData.getPolicyMappingId());

        return gatewayPolicyMappingDeploymentInfoDTO;
    }

    /**
     * Converts a GatewayPolicyData object into corresponding REST API DTO object.
     *
     * @param policyData The GatewayPolicyData object
     * @return A GatewayPolicyMappingsDTO object
     */
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

    /**
     * Converts a GatewayPolicyData object into corresponding REST API DTO object.
     *
     * @param policyData The GatewayPolicyData object
     * @return A GatewayPolicyMappingInfoDTO object
     */
    public static GatewayPolicyMappingInfoDTO fromGatewayPolicyDataToInfoDTO (GatewayPolicyData policyData) {

        GatewayPolicyMappingInfoDTO gatewayPolicyMappingInfoDTO = new GatewayPolicyMappingInfoDTO();
        gatewayPolicyMappingInfoDTO.description(policyData.getPolicyMappingDescription());
        gatewayPolicyMappingInfoDTO.displayName(policyData.getPolicyMappingName());
        gatewayPolicyMappingInfoDTO.id(policyData.getPolicyMappingId());

        return gatewayPolicyMappingInfoDTO;
    }

    /**
     * Get query parameter value for gateway label from the string.
     *
     * @param query Request query
     * @return String value of gateway label
     */
    public static String getQueryParams(String query) {
        String[] keyVal = query.split(" ")[0].split(":");
        if (keyVal.length == 2) {
            return keyVal[1];
        }
        return null;
    }
}
