/*
 *
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.devops.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.devops.ConfigApiService;
import org.wso2.carbon.apimgt.rest.api.devops.DevopsAPIUtils;
import org.wso2.carbon.apimgt.rest.api.devops.dto.CorrelationComponentDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.CorrelationComponentsListDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.PropertyDTO;
import org.wso2.carbon.logging.correlation.CorrelationLogConfigurable;
import org.wso2.carbon.logging.correlation.bean.ImmutableCorrelationLogConfig;
import org.wso2.carbon.logging.correlation.internal.CorrelationLogManager;

/**
 * The type Config api service.
 */
public class ConfigApiServiceImpl implements ConfigApiService {

    public Response configCorrelationGet(MessageContext messageContext) throws APIManagementException {
        String[] components = DevopsAPIUtils.CORRELATION_DEFAULT_COMPONENTS;
        CorrelationComponentsListDTO correlationComponentsListDTO = new CorrelationComponentsListDTO();
        List<CorrelationComponentDTO> correlationComponentDTOList = new ArrayList<>();
        for (String componentName : components) {
            CorrelationComponentDTO correlationComponentDTO = DevopsAPIUtils.getCorrelationComponentDTO(componentName);
            correlationComponentDTOList.add(correlationComponentDTO);
        }
        correlationComponentsListDTO.setComponents(correlationComponentDTOList);
        Response.Status status = Response.Status.OK;
        return Response.status(status).entity(correlationComponentsListDTO).build();
    }

    public Response configCorrelationPut(CorrelationComponentsListDTO correlationComponentsListDTO,
            MessageContext messageContext) throws APIManagementException {
        String invalidComponentName = DevopsAPIUtils.validateCorrelationComponentList(correlationComponentsListDTO);
        if (invalidComponentName == null) {
            List<CorrelationComponentDTO> correlationComponentDTOList = new ArrayList<>();
            for (CorrelationComponentDTO component : correlationComponentsListDTO.getComponents()) {
                String componentName = component.getName();
                Boolean enabled = Boolean.parseBoolean(component.getEnabled());
                String[] deniedThreads = new String[0];
                if (componentName.equals(DevopsAPIUtils.JDBC_COMPONENT_NAME)) {
                    List<PropertyDTO> propertyDTOs = component.getProperties();
                    for (PropertyDTO propertyDTO : propertyDTOs) {
                        if (propertyDTO.getName().equals(DevopsAPIUtils.DENIED_THREADS_NAME)) {
                            List<String> deniedThreadsList = propertyDTO.getValue();
                            deniedThreads = deniedThreadsList.toArray(new String[deniedThreadsList.size()]);
                        }
                    }
                }
                CorrelationLogConfigurable service = CorrelationLogManager.getLogServiceInstance(componentName);
                service.onConfigure(new ImmutableCorrelationLogConfig(enabled, deniedThreads, false));

                CorrelationComponentDTO correlationComponentDTO =
                        DevopsAPIUtils.getCorrelationComponentDTO(componentName);
                correlationComponentDTOList.add(correlationComponentDTO);
            }
            correlationComponentsListDTO.setComponents(correlationComponentDTOList);
            Response.Status status = Response.Status.OK;
            return Response.status(status).entity(correlationComponentsListDTO).build();
        } else {
            ErrorDTO errorObject = new ErrorDTO();
            Response.Status status = Response.Status.BAD_REQUEST;
            errorObject.setCode((long) status.getStatusCode());
            errorObject.setMessage(status.toString());
            errorObject.setDescription("Invalid Component Name: " + invalidComponentName + ". ");
            errorObject.setMoreInfo(
                    "The valid component names : " + Arrays.toString(DevopsAPIUtils.CORRELATION_DEFAULT_COMPONENTS));
            return Response.status(status).entity(errorObject).build();
        }
    }
}
