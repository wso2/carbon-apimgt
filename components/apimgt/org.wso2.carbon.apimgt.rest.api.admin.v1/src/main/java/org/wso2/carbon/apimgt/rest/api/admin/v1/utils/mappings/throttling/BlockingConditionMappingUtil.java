/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.throttling;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionListDTO;

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
     */
    public static BlockingConditionListDTO fromBlockConditionListToListDTO(
            List<BlockConditionsDTO> blockConditionList) throws ParseException {
        BlockingConditionListDTO listDTO = new BlockingConditionListDTO();
        List<BlockingConditionDTO> blockingConditionDTOList = new ArrayList<>();
        if (blockConditionList != null) {
            for (BlockConditionsDTO blockCondition : blockConditionList) {
                // Added to skip SUBSCRIPTION type blocking conditions left after migrations since subscription blocks
                // are no longer added to the AM_BLOCK_CONDITIONS table
                if (APIConstants.BLOCKING_CONDITIONS_SUBSCRIPTION.equals(blockCondition.getConditionType())) {
                    continue;
                }
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
     */
    public static BlockingConditionDTO fromBlockingConditionToDTO(
            BlockConditionsDTO blockCondition) throws ParseException {

        BlockingConditionDTO dto = new BlockingConditionDTO();
        dto.setConditionId(blockCondition.getUUID());
        dto.setConditionType(BlockingConditionDTO.ConditionTypeEnum.fromValue(blockCondition.getConditionType()));
        if (APIConstants.BLOCKING_CONDITIONS_API.equals(blockCondition.getConditionType()) ||
                APIConstants.BLOCKING_CONDITIONS_APPLICATION.equals(blockCondition.getConditionType()) ||
                APIConstants.BLOCKING_CONDITIONS_USER.equals(blockCondition.getConditionType())) {
            dto.setConditionValue(blockCondition.getConditionValue());
        } else if (APIConstants.BLOCKING_CONDITIONS_IP.equals(blockCondition.getConditionType()) ||
                APIConstants.BLOCK_CONDITION_IP_RANGE.equalsIgnoreCase(blockCondition.getConditionType())) {
            Object parse = new JSONParser().parse(blockCondition.getConditionValue());
            dto.setConditionValue(parse);
        }
        dto.setConditionStatus(blockCondition.isEnabled());
        return dto;
    }
}
