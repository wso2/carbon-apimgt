/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.util.mapping.throttling;

import org.wso2.carbon.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.throttling.synchronizer.dto.BlockingConditionListDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for mapping Block Condition model and its sub components into REST API DTOs and vice-versa
 */
public class BlockingConditionMappingUtil {

    /**
     * Converts a List of Block Condition in to REST API LIST DTO Object
     *
     * @param blockConditionList A List of Block Conditions
     * @return REST API List DTO object derived from Block Condition list
     * @throws UnsupportedThrottleLimitTypeException
     */
    public static BlockingConditionListDTO fromBlockConditionListToListDTO(
            List<BlockConditionsDTO> blockConditionList) throws UnsupportedThrottleLimitTypeException {
        BlockingConditionListDTO listDTO = new BlockingConditionListDTO();
        List<BlockingConditionDTO> blockingConditionDTOList = new ArrayList<>();
        if (blockConditionList != null) {
            for (BlockConditionsDTO blockCondition : blockConditionList) {
                BlockingConditionDTO dto = fromBlockingConditionToDTO(blockCondition);
                blockingConditionDTOList.add(dto);
            }
        }
        listDTO.setCount(blockingConditionDTOList.size());
        listDTO.setList(blockingConditionDTOList);
        return listDTO;
    }

    /**
     * Converts a single Block Condition model object into REST API DTO object
     *
     * @param blockCondition Block condition model object
     * @return Block condition DTO object derived from block condition model object
     * @throws UnsupportedThrottleLimitTypeException
     */
    public static BlockingConditionDTO fromBlockingConditionToDTO(
            BlockConditionsDTO blockCondition) throws UnsupportedThrottleLimitTypeException {
        BlockingConditionDTO dto = new BlockingConditionDTO();
        dto.setConditionId(blockCondition.getUUID());
        dto.setConditionType(blockCondition.getConditionType());

        String conditionValue = blockCondition.getConditionValue();
        if (APIConstants.BLOCKING_CONDITIONS_IP.equals(blockCondition.getConditionType())) {
            int index = conditionValue.indexOf(":");
            if (index > -1) {
                // Removing Tenant Domain from IP
                conditionValue = conditionValue.substring(index + 1, conditionValue.length());
            }
        }
        dto.setConditionValue(conditionValue);
        return dto;
    }

}
