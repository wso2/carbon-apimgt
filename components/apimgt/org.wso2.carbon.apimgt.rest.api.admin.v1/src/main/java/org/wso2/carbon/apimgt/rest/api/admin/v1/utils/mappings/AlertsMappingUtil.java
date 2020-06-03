/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertTypesListDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Util class to map alerts to DTOs
 */
public class AlertsMappingUtil {

    /**
     * Map AlertTypeDTO list to AlertTypesListDTO
     *
     * @param alertTypes
     * @return
     */
    public static AlertTypesListDTO fromAlertTypesListToAlertTypeListDTO(
            List<org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> alertTypes) {

        AlertTypesListDTO alertTypesListDTO = new AlertTypesListDTO();
        List<AlertTypeDTO> alertTypeDTOList = fromAlertTypesListToAlertTypeDTOList(alertTypes);
        alertTypesListDTO.setAlerts(alertTypeDTOList);
        alertTypesListDTO.setCount(alertTypeDTOList.size());
        return alertTypesListDTO;
    }

    /**
     * Map AlertType list to AlertTypeDTO list
     *
     * @param alertTypes
     * @return
     */
    public static List<AlertTypeDTO> fromAlertTypesListToAlertTypeDTOList(
            List<org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> alertTypes) {

        List<AlertTypeDTO> alertTypeDTOList = new ArrayList<>();
        for (org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO alertType : alertTypes) {
            alertTypeDTOList.add(fromAlertTypeToAlertTypeDTO(alertType));
        }
        return alertTypeDTOList;
    }

    /**
     * Map AlertType to AlertTypeDTO
     *
     * @param alert
     * @return
     */
    public static AlertTypeDTO fromAlertTypeToAlertTypeDTO(org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO alert) {
        AlertTypeDTO alertTypeDTO = new AlertTypeDTO();
        alertTypeDTO.setId(alert.getId().toString());
        alertTypeDTO.setName(alert.getName());
        return alertTypeDTO;
    }
}
