/*
 *
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.throttling;

import org.wso2.carbon.apimgt.api.UnsupportedThrottleLimitTypeException;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.BlockingConditionListDTO;

import java.util.ArrayList;
import java.util.List;

public class BlockingConditionMappingUtil {

    
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
        listDTO.setNext(""); //todo set next and previous
        listDTO.setPrevious("");
        return listDTO;
    }

    
    public static BlockingConditionDTO fromBlockingConditionToDTO(
            BlockConditionsDTO blockCondition) throws UnsupportedThrottleLimitTypeException {
        BlockingConditionDTO dto = new BlockingConditionDTO();
        dto.setConditionId(blockCondition.getConditionId() + ""); //todo change to uuid
        dto.setConditionType(blockCondition.getConditionType());
        dto.setConditionValue(blockCondition.getConditionValue());
        dto.setEnabled(blockCondition.isEnabled());
        return dto;
    }

    public static BlockConditionsDTO fromBlockingConditionDTOToModel(BlockingConditionDTO dto)
            throws UnsupportedThrottleLimitTypeException {
        BlockConditionsDTO blockCondition = new BlockConditionsDTO();
        blockCondition.setConditionType(dto.getConditionType());
        blockCondition.setConditionValue(dto.getConditionValue());
        blockCondition.setEnabled(dto.getEnabled());
        blockCondition.setConditionId(Integer.parseInt(dto.getConditionId())); //todo set properly using uuid
        return blockCondition;
    }
}
