/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.EndpointsApiService;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.dto.EndpointListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Implementation class for Endpoints
 */
public class EndpointsApiServiceImpl extends EndpointsApiService {
    private APIMgtAdminService apiMgtAdminService;
    private static final Logger log = LoggerFactory.getLogger(EndpointsApiServiceImpl.class);

    public EndpointsApiServiceImpl(APIMgtAdminService apiMgtAdminService) {
        this.apiMgtAdminService = apiMgtAdminService;
    }

    @Override
    public Response endpointsEndpointIdGatewayConfigGet(String endpointId
            , String accept
            , Request request) throws NotFoundException {
        try {
            String endpointGatewayConfig = apiMgtAdminService.getEndpointGatewayConfig(endpointId);

            if (endpointGatewayConfig != null) {
                return Response.ok().entity(endpointGatewayConfig).build();
            } else {
                String msg = "Endpoint is not found with apiId : " + endpointId;
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900314L, msg);
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).header(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON).entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving gateway config of Endpoint : " + endpointId;
            Map<String, String> paramList = new HashMap<>();
            paramList.put(APIMgtConstants.ExceptionsConstants.ENDPOINT_ID, endpointId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).header(HttpHeaders.CONTENT_TYPE,
                    MediaType.APPLICATION_JSON).entity(errorDTO).build();
        }
    }

    @Override
    public Response endpointsGet(Integer limit
            , String accept
            , Request request) throws NotFoundException {
        EndpointListDTO endpointListDTO = new EndpointListDTO();
        try {
            List<Endpoint> endpointList = apiMgtAdminService.getAllEndpoints();
            endpointListDTO.setList(MappingUtil.toEndpointListDto(endpointList));
            endpointListDTO.setCount(endpointList.size());
            return Response.ok().entity(endpointListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving APIs";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
