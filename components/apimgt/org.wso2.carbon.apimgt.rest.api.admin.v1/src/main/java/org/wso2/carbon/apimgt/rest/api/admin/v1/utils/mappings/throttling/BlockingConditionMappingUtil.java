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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.BlockConditionsDTO;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BlockingConditionListDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for mapping Block Condition model and its sub components into REST API DTOs and vice-versa
 */
public class BlockingConditionMappingUtil {

    private static final Log log = LogFactory.getLog(BlockingConditionMappingUtil.class);

    /**
     * Converts a List of Block Condition in to REST API LIST DTO Object
     *
     * @param blockConditionList A List of Block Conditions
     * @return REST API List DTO object derived from Block Condition list
     */
    public static BlockingConditionListDTO fromBlockConditionListToListDTO(
            List<BlockConditionsDTO> blockConditionList) throws APIManagementException {
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
            BlockConditionsDTO blockCondition) throws APIManagementException {

        BlockingConditionDTO dto = new BlockingConditionDTO();
        dto.setConditionId(blockCondition.getUUID());
        dto.setConditionType(BlockingConditionDTO.ConditionTypeEnum.fromValue(blockCondition.getConditionType()));
        if (APIConstants.BLOCKING_CONDITIONS_API.equals(blockCondition.getConditionType()) ||
                APIConstants.BLOCKING_CONDITIONS_APPLICATION.equals(blockCondition.getConditionType()) ||
                APIConstants.BLOCKING_CONDITIONS_USER.equals(blockCondition.getConditionType())) {
            dto.setConditionValue(blockCondition.getConditionValue());
        } else if (APIConstants.BLOCKING_CONDITIONS_IP.equals(blockCondition.getConditionType()) ||
                APIConstants.BLOCK_CONDITION_IP_RANGE.equalsIgnoreCase(blockCondition.getConditionType())) {
            try {
                Object parse = new JSONParser().parse(blockCondition.getConditionValue());
                dto.setConditionValue(parse);
            } catch (ParseException e) {
                // Handle migrated Fixed IP blocking conditions
                if (blockCondition.getConditionValue() != null) {
                    String[] conditionsArray = blockCondition.getConditionValue().split(":");
                    if (conditionsArray.length == 2) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put(APIConstants.BLOCK_CONDITION_FIXED_IP,conditionsArray[1]);
                        // Invert condition is set to false for migrated Fixed IP conditions as it is false always
                        jsonObject.put(APIConstants.BLOCK_CONDITION_INVERT, Boolean.FALSE);
                        dto.setConditionValue(jsonObject);
                    } else {
                        // This is a true parsing exception. Hence, it will be thrown without handling.
                        log.error("Error parsing IP blocking condition value", e);
                        throw new APIManagementException(ExceptionCodes.INVALID_BLOCK_CONDITION_VALUES);
                    }
                } else {
                    // This is a true parsing exception. Hence, it will be thrown without handling.
                    log.error("Error parsing IP blocking condition value. The value is null.", e);
                    throw new APIManagementException(ExceptionCodes.INVALID_BLOCK_CONDITION_VALUES);
                }
            }
        }
        dto.setConditionStatus(blockCondition.isEnabled());
        return dto;
    }

    /**
     * Get query parameter values for conditionType and conditionValue from the query string.
     *
     * @param query Request query
     * @return map of conditionType and conditionValue values
     */
    public static Map<String, String> getQueryParams(String query) {
        if (query == null || StringUtils.isBlank(query)) {
            return Collections.emptyMap();
        }
        Map<String, String> parameters = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = keyValue[1];
                if (key.equals(APIConstants.BLOCK_CONDITION_TYPE) || key.equals(APIConstants.BLOCK_CONDITION_VALUE)) {
                    parameters.put(key, value);
                }
            }
        }
        return parameters;
    }
}
