/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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


package org.wso2.carbon.apimgt.internal.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.CorrelationConfigDAO;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigPropertyDTO;
import org.wso2.carbon.apimgt.internal.service.CorrelationConfigsApiService;
import org.wso2.carbon.apimgt.internal.service.dto.CorrelationComponentDTO;
import org.wso2.carbon.apimgt.internal.service.dto.CorrelationComponentPropertyDTO;
import org.wso2.carbon.apimgt.internal.service.dto.CorrelationComponentsListDTO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;
/**
 * Correlation Configs Internal Service Implementation.
 */
public class CorrelationConfigsApiServiceImpl implements CorrelationConfigsApiService {
    private static final Log log = LogFactory.getLog(CorrelationConfigsApiServiceImpl.class);

    public Response correlationConfigsGet(MessageContext messageContext) {
        List<CorrelationComponentDTO> correlationComponentDTOList = new ArrayList<>();
        try {
            List<CorrelationConfigDTO> correlationConfigDTOList =
                    CorrelationConfigDAO.getInstance().getCorrelationConfigsList();

            for (CorrelationConfigDTO correlationConfigDTO : correlationConfigDTOList) {
                CorrelationComponentDTO correlationComponentDTO = new CorrelationComponentDTO();

                correlationComponentDTO.setName(correlationConfigDTO.getName());
                correlationComponentDTO.setEnabled(correlationConfigDTO.getEnabled());

                List<CorrelationConfigPropertyDTO> correlationConfigPropertyDTOList =
                        correlationConfigDTO.getProperties();
                List<CorrelationComponentPropertyDTO> propertyDTOList = new ArrayList<>();

                for (CorrelationConfigPropertyDTO correlationConfigPropertyDTO : correlationConfigPropertyDTOList) {
                    CorrelationComponentPropertyDTO propertyDTO = new CorrelationComponentPropertyDTO();
                    propertyDTO.setName(correlationConfigPropertyDTO.getName());
                    propertyDTO.setValue(Arrays.asList(correlationConfigPropertyDTO.getValue()));
                    propertyDTOList.add(propertyDTO);
                }

                correlationComponentDTO.setProperties(propertyDTOList);
                correlationComponentDTOList.add(correlationComponentDTO);
            }

        } catch (APIManagementException e) {
            log.error("Error while retrieving correlation configs");
        }

        CorrelationComponentsListDTO correlationComponentsListDTO = new CorrelationComponentsListDTO();
        correlationComponentsListDTO.setComponents(correlationComponentDTOList);
        Response.Status status = Response.Status.OK;
        return Response.status(status).entity(correlationComponentsListDTO).build();
    }
}
