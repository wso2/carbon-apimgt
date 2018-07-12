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
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response;

public class ApplicationsApiServiceImpl extends ApplicationsApiService {
    private APIMgtAdminService apiMgtAdminService;
    private static final Logger log = LoggerFactory.getLogger(ApplicationsApiServiceImpl.class);

    public ApplicationsApiServiceImpl(APIMgtAdminService apiMgtAdminService) {
        this.apiMgtAdminService = apiMgtAdminService;
    }

    @Override
    public Response applicationsGet(String accept
            , Request request) throws NotFoundException {
        try {
            List<Application> applicationList = apiMgtAdminService.getAllApplications();
            ApplicationListDTO applicationListDTO = new ApplicationListDTO();
            applicationListDTO.setList(MappingUtil.convertToApplicationDtoList(applicationList));
            return Response.ok(applicationListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Applications.";
            HashMap<String, String> paramList = new HashMap<String, String>();
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler
                    (), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
