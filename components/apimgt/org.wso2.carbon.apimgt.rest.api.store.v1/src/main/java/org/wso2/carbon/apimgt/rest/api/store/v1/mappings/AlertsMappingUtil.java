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

package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertConfigDTO;

import java.util.Properties;

/**
 * Utility class for alert config/ dto mappings.
 * */
public class AlertsMappingUtil {

    /**
     * Converts alert config properties to a AlertConfigDTO.
     *
     * @param configProperties : The alert config properties.
     * @return An AlertConfigDTO object.
     * */
    public static AlertConfigDTO toAlertConfigDTO(Properties configProperties) {
        AlertConfigDTO alertConfigDTO = new AlertConfigDTO();
        alertConfigDTO.setApiName(configProperties.getProperty("apiName"));
        alertConfigDTO.setApiVersion(configProperties.getProperty("apiVersion"));
        alertConfigDTO.setApplicationId(configProperties.getProperty("applicationId"));
        alertConfigDTO.setRequestCount(Integer.valueOf(configProperties.getProperty("thresholdRequestCountPerMin")));
        return alertConfigDTO;
    }

    /**
     * Converts AlertConfigDTO to properties.
     *
     * @param alertConfigDTO : The alert config DTO
     * @return Parameters.
     * */
    public static Properties toAlertConfigProperties(AlertConfigDTO alertConfigDTO) {
        Properties configurationProperties = new Properties();
        configurationProperties.setProperty("apiName", alertConfigDTO.getApiName());
        configurationProperties.setProperty("apiVersion", alertConfigDTO.getApiVersion());
        configurationProperties.setProperty("applicationId", alertConfigDTO.getApplicationId());
        configurationProperties.setProperty("thresholdRequestCountPerMin",
                String.valueOf(alertConfigDTO.getRequestCount()));
        return configurationProperties;
    }

}
