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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AlertConfigDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AlertTypeDTO;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
    public static AlertConfigDTO toAlertConfigDTO(Map<String, String> configProperties) {
        AlertConfigDTO alertConfigDTO = new AlertConfigDTO();
        alertConfigDTO.setConfiguration(configProperties);
        alertConfigDTO.setConfigurationId(getAlertConfigId(configProperties.get("apiName"),
                configProperties.get("apiVersion"), configProperties.get("apiCreatorTenantDomain")));
        return alertConfigDTO;
    }

    /**
     * Generates a configuration id from the given parameters.
     * Configuration id is a base64 encoded string of apiName, apiVersion, applicationName separated by '#' character.
     * eg.
     * apiName: PizzaShackAPI, apiVersion: 1.0.0, tenantDomain: carbon.super.
     * ConfigId string : PizzaShackAPI#1.0.0#carbon.super.
     * Base64 encoded: UGl6emFTaGFja0FQSSMxLjAuMCNjYXJib24uc3VwZXI
     *
     * @param apiName: Name of the api
     * @param apiVersion: Api version.
     * @param tenantDomain : The tenant domain of the user.
     * @return The configuration id
     * */
    private static String getAlertConfigId(String apiName, String apiVersion, String tenantDomain){
        String configId = apiName + "#" + apiVersion + "#" + tenantDomain;
        return Base64.getEncoder().withoutPadding().encodeToString(configId.getBytes());
    }

    /**
     * Generates a configuration parameter map from the given configuration id.
     *
     * @param configId : The configuration id(base64 encoded)
     * @return A HashMap containing the config parameters.
     * */
    public static Map<String, String> configIdToMap(String configId) throws APIManagementException {
        String decodedConfigurationId = new String(Base64.getDecoder().decode(configId.getBytes()));
        String[] parameters = decodedConfigurationId.split("#");
        Map<String, String> configMap = new HashMap<>();
        configMap.put("apiName", parameters[0]);
        configMap.put("apiVersion", parameters[1]);
        configMap.put("apiCreatorTenantDomain", parameters[2]);
        return configMap;
    }

    /**
     * Converts impl.dto.AlertTypeDTO to Store api AlertTypeDTO
     *
     * @param alertTypeDTO: An impl AlertTypeDTO object.
     * @return Api Store Alert Type DTO
     * */
    public static AlertTypeDTO alertTypeToAlertTypeDTO(org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO alertTypeDTO) {
        AlertTypeDTO storeAlertTypeDTO = new AlertTypeDTO();
        storeAlertTypeDTO.setId(alertTypeDTO.getId());
        storeAlertTypeDTO.setName(alertTypeDTO.getName());
        storeAlertTypeDTO.setRequireConfiguration(alertTypeDTO.isConfigurable());
        return storeAlertTypeDTO;
    }
}
