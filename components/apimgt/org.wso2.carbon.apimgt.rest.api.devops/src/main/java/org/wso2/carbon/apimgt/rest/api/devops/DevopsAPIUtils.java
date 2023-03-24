/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.devops;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APILogInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigPropertyDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.CorrelationComponentDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.CorrelationComponentPropertyDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.CorrelationComponentsListDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.LoggingApiOutputDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.LoggingApiOutputListDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Devops util functions.
 */
public class DevopsAPIUtils {

    public static final String[] CORRELATION_DEFAULT_COMPONENTS = { "http", "ldap", "jdbc", "synapse", "method-calls" };


    public static boolean validateLogLevel(String logLevel) {

        return (APIConstants.APILogHandler.OFF.equalsIgnoreCase(logLevel)
                || APIConstants.APILogHandler.BASIC.equalsIgnoreCase(logLevel)
                || APIConstants.APILogHandler.STANDARD.equalsIgnoreCase(logLevel)
                || APIConstants.APILogHandler.FULL.equalsIgnoreCase(logLevel));
    }

    public static LoggingApiOutputListDTO getLoggingAPIList(List<APILogInfoDTO> apiLogInfoDTOList) {

        LoggingApiOutputListDTO loggingApiOutputListDTO = new LoggingApiOutputListDTO();
        List<LoggingApiOutputDTO> loggingApiOutputDTOList = new ArrayList<>();
        for (APILogInfoDTO apiLogInfoDTO : apiLogInfoDTOList) {
            LoggingApiOutputDTO loggingApiOutputDTO = new LoggingApiOutputDTO();
            loggingApiOutputDTO.setContext(apiLogInfoDTO.getContext());
            loggingApiOutputDTO.setLogLevel(apiLogInfoDTO.getLogLevel());
            loggingApiOutputDTO.setApiId(apiLogInfoDTO.getApiId());
            loggingApiOutputDTO.setResourceMethod(apiLogInfoDTO.getResourceMethod());
            loggingApiOutputDTO.setResourcePath(apiLogInfoDTO.getResourcePath());
            loggingApiOutputDTOList.add(loggingApiOutputDTO);
        }
        loggingApiOutputListDTO.apis(loggingApiOutputDTOList);
        return loggingApiOutputListDTO;
    }

    /**
     * Validate correlation component list string.
     *
     * @param correlationComponentsListDTO the correlation components list dto
     * @return the string with invalid component name
     */
    public static boolean validateCorrelationComponentList(CorrelationComponentsListDTO correlationComponentsListDTO)
            throws APIManagementException {
        for (CorrelationComponentDTO component : correlationComponentsListDTO.getComponents()) {
            String componentName = component.getName();
            String enabled = component.getEnabled();
            List<CorrelationComponentPropertyDTO> properties = component.getProperties();
            if (componentName == null || enabled == null) {
                throw new APIManagementException("Invalid Request",
                        ExceptionCodes.from(ExceptionCodes.CORRELATION_CONFIG_BAD_REQUEST));
            }
            for (CorrelationComponentPropertyDTO property: properties) {
                if (property.getName() == null || property.getValue() == null) {
                    throw new APIManagementException("Invalid Request",
                            ExceptionCodes.from(ExceptionCodes.CORRELATION_CONFIG_BAD_REQUEST));
                }
            }
        }
        return true;
    }


    public static CorrelationComponentsListDTO getCorrelationComponentsList(
            List<CorrelationConfigDTO> correlationConfigDTOList) {
        CorrelationComponentsListDTO correlationComponentsListDTO = new CorrelationComponentsListDTO();
        List<CorrelationComponentDTO> correlationComponentDTOList = new ArrayList<>();

        for (CorrelationConfigDTO correlationConfigDTO: correlationConfigDTOList) {
            CorrelationComponentDTO correlationComponentDTO = new CorrelationComponentDTO();

            correlationComponentDTO.setName(correlationConfigDTO.getName());
            correlationComponentDTO.setEnabled(correlationConfigDTO.getEnabled());

            List<CorrelationConfigPropertyDTO> correlationConfigPropertyDTOList = correlationConfigDTO.getProperties();
            List<CorrelationComponentPropertyDTO> propertyDTOList = new ArrayList<>();

            for (CorrelationConfigPropertyDTO correlationConfigPropertyDTO: correlationConfigPropertyDTOList) {
                CorrelationComponentPropertyDTO propertyDTO = new CorrelationComponentPropertyDTO();
                propertyDTO.setName(correlationConfigPropertyDTO.getName());
                propertyDTO.setValue(Arrays.asList(correlationConfigPropertyDTO.getValue()));
                propertyDTOList.add(propertyDTO);
            }

            correlationComponentDTO.setProperties(propertyDTOList);
            correlationComponentDTOList.add(correlationComponentDTO);
        }

        correlationComponentsListDTO.setComponents(correlationComponentDTOList);
        return correlationComponentsListDTO;
    }

    public static List<CorrelationConfigDTO> getCorrelationConfigDTOList(
            CorrelationComponentsListDTO correlationComponentsListDTO) {
        List<CorrelationConfigDTO> correlationConfigDTOList = new ArrayList<>();
        List<CorrelationComponentDTO> correlationComponentDTOList = correlationComponentsListDTO.getComponents();

        for (CorrelationComponentDTO correlationComponentDTO: correlationComponentDTOList) {
            CorrelationConfigDTO correlationConfigDTO = new CorrelationConfigDTO();
            correlationConfigDTO.setName(correlationComponentDTO.getName());
            correlationConfigDTO.setEnabled(Boolean.toString(
                    Boolean.parseBoolean(correlationComponentDTO.getEnabled())));
            List<CorrelationConfigPropertyDTO> properties = new ArrayList<>();
            for (CorrelationComponentPropertyDTO propertyDTO: correlationComponentDTO.getProperties()) {
                CorrelationConfigPropertyDTO correlationConfigPropertyDTO = new CorrelationConfigPropertyDTO();
                correlationConfigPropertyDTO.setName(propertyDTO.getName());
                List<String> propertyValue = propertyDTO.getValue();
                propertyValue.replaceAll(String::trim);
                correlationConfigPropertyDTO.setValue(propertyValue.toArray(new String[0]));
                properties.add(correlationConfigPropertyDTO);
            }
            correlationConfigDTO.setProperties(properties);
            correlationConfigDTOList.add(correlationConfigDTO);
        }
        return correlationConfigDTOList;
    }
}
