/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.apimgt.rest.api.store.utils.mappings;

import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.TierPermission;
import org.wso2.carbon.apimgt.impl.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierPermissionInfoDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** This class is responsible for mapping APIM core tier related objects into REST API Tier related DTOs 
 *
 */
public class TierMappingUtil {

    /**
     * Converts a List object of Tiers into a DTO
     *
     * @param tiers  a list of Tier objects
     * @param limit  max number of objects returned
     * @param offset starting index
     * @return TierListDTO object containing TierDTOs
     */
    public static TierListDTO fromTierListToDTO(List<Tier> tiers, String tierLevel, int limit,
            int offset) {
        TierListDTO tierListDTO = new TierListDTO();
        List<TierDTO> tierDTOs = tierListDTO.getList();
        if (tierDTOs == null) {
            tierDTOs = new ArrayList<>();
            tierListDTO.setList(tierDTOs);
        }

        //identifying the proper start and end indexes
        int size = tiers.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= size - 1 ? offset + limit - 1 : size - 1;

        for (int i = start; i <= end; i++) {
            Tier tier = tiers.get(i);
            tierDTOs.add(fromTierToDTO(tier, tierLevel));
        }
        tierListDTO.setCount(tierDTOs.size());
        return tierListDTO;
    }

    /**
     * Sets pagination urls for a TierListDTO object given pagination parameters and url parameters
     *
     * @param tierListDTO a TierListDTO object
     * @param limit       max number of objects returned
     * @param offset      starting index
     * @param size        max offset
     */
    public static void setPaginationParams(TierListDTO tierListDTO, String tierLevel, int limit, int offset, int size) {

        String paginatedPrevious = "";
        String paginatedNext = "";

        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getTiersPaginatedURL(tierLevel,
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getTiersPaginatedURL(tierLevel,
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }

        tierListDTO.setNext(paginatedNext);
        tierListDTO.setPrevious(paginatedPrevious);
    }

    /**
     * Converts a Tier object into TierDTO
     *
     * @param tier Tier object
     * @param tierLevel tier level (api/application or resource)
     * @return TierDTO corresponds to Tier object
     */
    public static TierDTO fromTierToDTO(Tier tier, String tierLevel) {
        TierDTO dto = new TierDTO();
        dto.setName(tier.getName());
        dto.setDescription(tier.getDescription());
        dto.setRequestCount(tier.getRequestCount());
        dto.setUnitTime(tier.getUnitTime());
        dto.setStopOnQuotaReach(tier.isStopOnQuotaReached());
        dto.setTierLevel(TierDTO.TierLevelEnum.valueOf(tierLevel));
        dto = setTierPermissions(dto, tier);
        if (tier.getTierPlan() != null) {
            dto.setTierPlan(TierDTO.TierPlanEnum.valueOf(tier.getTierPlan()));
        }
        if (tier.getTierAttributes() != null) {
            Map<String, String> additionalProperties = new HashMap<>();
            for (String key : tier.getTierAttributes().keySet()) {
                additionalProperties.put(key, tier.getTierAttributes().get(key).toString());
            }
            dto.setAttributes(additionalProperties);
        }
        return dto;
    }

    /**
     * Fills the tier information on TierDTO
     *
     * @param tierDto
     * @param tier
     * @return TierDTO with permission info
     */
    public static TierDTO setTierPermissions(TierDTO tierDto, Tier tier) {

        TierPermissionInfoDTO tierPermission = new TierPermissionInfoDTO();

        // If no permission found for the tier, the default permission will be applied
        if (tier.getTierPermission() == null || tier.getTierPermission().getPermissionType() == null) {
            tierPermission.setType(TierPermissionInfoDTO.TypeEnum.valueOf("allow"));
            List roles = new ArrayList<String>();
            roles.add("Internal/everyone");
            tierPermission.setRoles(roles);
        } else {
            String permissionType = tier.getTierPermission().getPermissionType();
            tierPermission.setType(TierPermissionInfoDTO.TypeEnum.valueOf(permissionType));
            tierPermission.setRoles(Arrays.asList(tier.getTierPermission().getRoles()));
        }
        tierDto.setTierPermissions(tierPermission);

        return tierDto;
    }

}
