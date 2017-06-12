/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.mappings;

import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.IPConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.exceptions.UnsupportedThrottleLimitTypeException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for mapping Block Condition model and its sub components into REST API DTOs and vice-versa.
 */
public class BlockingConditionMappingUtil {

    /**
     * Converts a List of Block Condition in to REST API LIST DTO Object.
     *
     * @param blockConditionList A List of Block Conditions
     * @return REST API List DTO object derived from Block Condition list
     * @throws UnsupportedThrottleLimitTypeException - If error occurs
     */
    public static BlockingConditionListDTO fromBlockConditionListToListDTO(List<BlockConditions> blockConditionList)
            throws UnsupportedThrottleLimitTypeException {
        BlockingConditionListDTO listDTO = new BlockingConditionListDTO();
        List<BlockingConditionDTO> blockingConditionDTOList = new ArrayList<>();
        if (blockConditionList != null) {
            for (BlockConditions blockCondition : blockConditionList) {
                BlockingConditionDTO dto = fromBlockingConditionToDTO(blockCondition);
                blockingConditionDTOList.add(dto);
            }
        }
        listDTO.setCount(blockingConditionDTOList.size());
        listDTO.setList(blockingConditionDTOList);
        return listDTO;
    }

    /**
     * Converts a single Block Condition model object into REST API DTO object.
     *
     * @param blockCondition Block condition model object
     * @return Block condition DTO object derived from block condition model object
     * @throws UnsupportedThrottleLimitTypeException - If error occurs
     */
    public static BlockingConditionDTO fromBlockingConditionToDTO(BlockConditions blockCondition)
            throws UnsupportedThrottleLimitTypeException {
        if (blockCondition.getUuid() == null) {
            return null;
        }
        BlockingConditionDTO dto = new BlockingConditionDTO();
        dto.setConditionId(blockCondition.getUuid());
        dto.setConditionType(blockCondition.getConditionType());
        dto.setStatus(blockCondition.isEnabled());
        if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE
                .equals(blockCondition.getConditionType())) {
            dto.setIpCondition(fromBlockConditionToIpConditionDTO(blockCondition));
        }
        String conditionValue = blockCondition.getConditionValue();
        dto.setConditionValue(conditionValue);
        return dto;
    }

    /**
     * Convert BlockingConditionDTO to BlockCondition.
     *
     * @param blockingConditionDTO blockindConditionDTO to be converted
     * @return BlockCondition Object
     * @throws UnsupportedThrottleLimitTypeException - If error occurs
     */
    public static BlockConditions fromBlockingConditionDTOToBlockCondition(BlockingConditionDTO blockingConditionDTO)
            throws UnsupportedThrottleLimitTypeException {
        BlockConditions blockConditions = new BlockConditions();
        blockConditions.setUuid(blockingConditionDTO.getConditionId());
        blockConditions.setConditionType(blockingConditionDTO.getConditionType());
        blockConditions.setConditionValue(blockingConditionDTO.getConditionValue());
        blockConditions.setEnabled(blockingConditionDTO.getStatus());
        if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE
                .equals(blockConditions.getConditionType())) {
            blockConditions.setStartingIP(blockingConditionDTO.getIpCondition().getStartingIP());
            blockConditions.setEndingIP(blockingConditionDTO.getIpCondition().getEndingIP());
        }
        return blockConditions;
    }

    /**
     * Block condition IP range details to IPConditionDTO.
     *
     * @param blockConditions blockCondition to be converted into IPConditionDTO.
     * @return IPConditionDTO Object
     */
    private static IPConditionDTO fromBlockConditionToIpConditionDTO(BlockConditions blockConditions) {
        IPConditionDTO ipConditionDTO = new IPConditionDTO();
        ipConditionDTO.setStartingIP(blockConditions.getStartingIP());
        ipConditionDTO.setEndingIP(blockConditions.getEndingIP());
        return ipConditionDTO;
    }

}