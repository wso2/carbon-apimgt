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

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.core.models.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.core.models.policy.Limit;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierListDTO;

import java.util.ArrayList;
import java.util.List;

public class TierMappingUtil {

    /**
     * Converts a List object of Tiers into a DTO
     *
     * @param tiers  a list of Tier objects
     * @param limit  max number of objects returned
     * @param offset starting index
     * @return TierListDTO object containing TierDTOs
     */
    public static TierListDTO fromTierListToDTO(List<Policy> tiers, String tierLevel, int limit,
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
            Policy tier = tiers.get(i);
            tierDTOs.add(fromTierToDTO(tier, tierLevel));
        }
        tierListDTO.setCount(tierDTOs.size());
        return tierListDTO;
    }

    /**
     * Converts a Tier object into TierDTO
     *
     * @param tier      Tier object
     * @param tierLevel tier level (api/application or resource)
     * @return TierDTO corresponds to Tier object
     */
    public static TierDTO fromTierToDTO(Policy tier, String tierLevel) {
        TierDTO dto = new TierDTO();
        dto.setName(tier.getPolicyName());
        dto.setDescription(tier.getDescription());
        dto.setTierLevel(TierDTO.TierLevelEnum.valueOf(StringUtils.upperCase(tierLevel)));
        dto.setUnitTime((long) tier.getDefaultQuotaPolicy().getLimit().getUnitTime());

        Limit limit = tier.getDefaultQuotaPolicy().getLimit();
        if (limit instanceof RequestCountLimit) {
            dto.setRequestCount((long) (((RequestCountLimit) limit).getRequestCount()));
        } else if (limit instanceof BandwidthLimit) {
            dto.setRequestCount((long) (((BandwidthLimit) limit).getDataAmount()));
        }
        //// TODO: 08/12/16 More fields to map 
        return dto;
    }
}