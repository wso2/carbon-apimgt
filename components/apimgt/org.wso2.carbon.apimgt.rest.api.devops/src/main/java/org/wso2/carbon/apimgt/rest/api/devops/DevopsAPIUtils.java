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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log log = LogFactory.getLog(DevopsAPIUtils.class);
    public static final String[] CORRELATION_DEFAULT_COMPONENTS = { "http", "ldap", "jdbc", "synapse", "method-calls" };


    public static boolean validateLogLevel(String logLevel) {
        if (log.isDebugEnabled()) {
            log.debug("Validating log level: " + logLevel);
        }
        
        boolean isValid = (APIConstants.APILogHandler.OFF.equalsIgnoreCase(logLevel)
                || APIConstants.APILogHandler.BASIC.equalsIgnoreCase(logLevel)
                || APIConstants.APILogHandler.STANDARD.equalsIgnoreCase(logLevel)
                || APIConstants.APILogHandler.FULL.equalsIgnoreCase(logLevel));
        
        if (!isValid) {
            log.warn("Invalid log level provided: " + logLevel);
        }
        
        return isValid;
    }

    public static LoggingApiOutputListDTO getLoggingAPIList(List<APILogInfoDTO> apiLogInfoDTOList) {
        if (log.isDebugEnabled()) {
            log.debug("Converting APILogInfoDTO list to LoggingApiOutputListDTO. Input size: " +
                    (apiLogInfoDTOList != null ? apiLogInfoDTOList.size() : "null"));
        }

        LoggingApiOutputListDTO loggingApiOutputListDTO = new LoggingApiOutputListDTO();
        List<LoggingApiOutputDTO> loggingApiOutputDTOList = new ArrayList<>();
        
        if (apiLogInfoDTOList != null) {
            for (APILogInfoDTO apiLogInfoDTO : apiLogInfoDTOList) {
                LoggingApiOutputDTO loggingApiOutputDTO = new LoggingApiOutputDTO();
                loggingApiOutputDTO.setContext(apiLogInfoDTO.getContext());
                loggingApiOutputDTO.setLogLevel(apiLogInfoDTO.getLogLevel());
                loggingApiOutputDTO.setApiId(apiLogInfoDTO.getApiId());
                loggingApiOutputDTO.setResourceMethod(apiLogInfoDTO.getResourceMethod());
                loggingApiOutputDTO.setResourcePath(apiLogInfoDTO.getResourcePath());
                loggingApiOutputDTOList.add(loggingApiOutputDTO);
            }
        }
        
        loggingApiOutputListDTO.apis(loggingApiOutputDTOList);
        
        if (log.isDebugEnabled()) {
            log.debug("Successfully converted to LoggingApiOutputListDTO with " + loggingApiOutputDTOList.size() + 
                    " items");
        }
        
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
        if (log.isDebugEnabled()) {
            log.debug("Starting validation of correlation component list");
        }
        
        if (correlationComponentsListDTO == null || correlationComponentsListDTO.getComponents() == null) {
            log.error("Correlation components list or components array is null");
            throw new APIManagementException("Invalid Request",
                    ExceptionCodes.from(ExceptionCodes.CORRELATION_CONFIG_BAD_REQUEST));
        }
        
        for (CorrelationComponentDTO component : correlationComponentsListDTO.getComponents()) {
            String componentName = component.getName();
            String enabled = component.getEnabled();
            List<CorrelationComponentPropertyDTO> properties = component.getProperties();
            
            if (componentName == null || enabled == null) {
                log.error("Component name or enabled status is null for component: " + componentName);
                throw new APIManagementException("Invalid Request",
                        ExceptionCodes.from(ExceptionCodes.CORRELATION_CONFIG_BAD_REQUEST));
            }
            
            if (properties != null) {
                for (CorrelationComponentPropertyDTO property : properties) {
                    if (property.getName() == null || property.getValue() == null) {
                        log.error("Property name or value is null for component: " + componentName);
                        throw new APIManagementException("Invalid Request",
                                ExceptionCodes.from(ExceptionCodes.CORRELATION_CONFIG_BAD_REQUEST));
                    }
                }
            }
        }
        
        log.info("Correlation component list validation completed successfully");
        return true;
    }


    public static CorrelationComponentsListDTO getCorrelationComponentsList(
            List<CorrelationConfigDTO> correlationConfigDTOList) {
        if (log.isDebugEnabled()) {
            log.debug("Converting CorrelationConfigDTO list to CorrelationComponentsListDTO. Input size: " + 
                    (correlationConfigDTOList != null ? correlationConfigDTOList.size() : "null"));
        }
        
        CorrelationComponentsListDTO correlationComponentsListDTO = new CorrelationComponentsListDTO();
        List<CorrelationComponentDTO> correlationComponentDTOList = new ArrayList<>();

        if (correlationConfigDTOList != null) {
            for (CorrelationConfigDTO correlationConfigDTO : correlationConfigDTOList) {
                CorrelationComponentDTO correlationComponentDTO = new CorrelationComponentDTO();

                correlationComponentDTO.setName(correlationConfigDTO.getName());
                correlationComponentDTO.setEnabled(correlationConfigDTO.getEnabled());

                List<CorrelationConfigPropertyDTO> correlationConfigPropertyDTOList = 
                        correlationConfigDTO.getProperties();
                List<CorrelationComponentPropertyDTO> propertyDTOList = new ArrayList<>();

                if (correlationConfigPropertyDTOList != null) {
                    for (CorrelationConfigPropertyDTO correlationConfigPropertyDTO : correlationConfigPropertyDTOList) {
                        CorrelationComponentPropertyDTO propertyDTO = new CorrelationComponentPropertyDTO();
                        propertyDTO.setName(correlationConfigPropertyDTO.getName());
                        propertyDTO.setValue(Arrays.asList(correlationConfigPropertyDTO.getValue()));
                        propertyDTOList.add(propertyDTO);
                    }
                }

                correlationComponentDTO.setProperties(propertyDTOList);
                correlationComponentDTOList.add(correlationComponentDTO);
            }
        }

        correlationComponentsListDTO.setComponents(correlationComponentDTOList);
        
        if (log.isDebugEnabled()) {
            log.debug("Successfully converted to CorrelationComponentsListDTO with " + 
                    correlationComponentDTOList.size() + " components");
        }
        
        return correlationComponentsListDTO;
    }

    public static List<CorrelationConfigDTO> getCorrelationConfigDTOList(
            CorrelationComponentsListDTO correlationComponentsListDTO) {
        if (log.isDebugEnabled()) {
            log.debug("Converting CorrelationComponentsListDTO to CorrelationConfigDTO list");
        }
        
        List<CorrelationConfigDTO> correlationConfigDTOList = new ArrayList<>();
        
        if (correlationComponentsListDTO != null && correlationComponentsListDTO.getComponents() != null) {
            List<CorrelationComponentDTO> correlationComponentDTOList = correlationComponentsListDTO.getComponents();

            for (CorrelationComponentDTO correlationComponentDTO : correlationComponentDTOList) {
                CorrelationConfigDTO correlationConfigDTO = new CorrelationConfigDTO();
                correlationConfigDTO.setName(correlationComponentDTO.getName());
                correlationConfigDTO.setEnabled(Boolean.toString(
                        Boolean.parseBoolean(correlationComponentDTO.getEnabled())));
                        
                List<CorrelationConfigPropertyDTO> properties = new ArrayList<>();
                List<CorrelationComponentPropertyDTO> componentProperties = correlationComponentDTO.getProperties();
                
                if (componentProperties != null) {
                    for (CorrelationComponentPropertyDTO propertyDTO : componentProperties) {
                        CorrelationConfigPropertyDTO correlationConfigPropertyDTO = new CorrelationConfigPropertyDTO();
                        correlationConfigPropertyDTO.setName(propertyDTO.getName());
                        List<String> propertyValue = propertyDTO.getValue();
                        
                        if (propertyValue != null) {
                            propertyValue.replaceAll(String::trim);
                            correlationConfigPropertyDTO.setValue(propertyValue.toArray(new String[0]));
                        }
                        
                        properties.add(correlationConfigPropertyDTO);
                    }
                }
                
                correlationConfigDTO.setProperties(properties);
                correlationConfigDTOList.add(correlationConfigDTO);
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Successfully converted to CorrelationConfigDTO list with " + correlationConfigDTOList.size() + 
                    " items");
        }
        
        return correlationConfigDTOList;
    }
}
