/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.impl.alertmgt;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for alert management.
 * */
public class AlertMgtUtils {

    /**
     * Convert the alert types to alert type dtos.
     *
     * @param alertTypes: The alert types map.
     * @return A list of AlertTypeDTOs.
     * */
    static List<AlertTypeDTO> toAlertTypeDTO(Map<Integer, String> alertTypes) {
        List<AlertTypeDTO> alertTypeDTOList = new ArrayList<>();
        if (alertTypes != null) {
            for (Map.Entry entry : alertTypes.entrySet()) {
                AlertTypeDTO alertTypeDTO = new AlertTypeDTO();
                alertTypeDTO.setId((Integer)entry.getKey());
                alertTypeDTO.setName(entry.getValue().toString());
                alertTypeDTOList.add(alertTypeDTO);
            }
        }
        return alertTypeDTOList;
    }

    static Map<String, String> alertTypesToMap(List<AlertTypeDTO> alertTypes) {
        List<Integer> alertTypeIds = new ArrayList<>();
        List<String> alertTypeNames = new ArrayList<>();
        Map<String, String> alertTypesMap = new HashMap<>();

        for (AlertTypeDTO alertTypeDTO : alertTypes) {
            alertTypeIds.add(alertTypeDTO.getId());
            alertTypeNames.add(alertTypeDTO.getName());
        }

        alertTypesMap.put("ids", StringUtils.join(alertTypeIds, ","));
        alertTypesMap.put("names", StringUtils.join(alertTypeNames, ","));
        return alertTypesMap;
    }

}
