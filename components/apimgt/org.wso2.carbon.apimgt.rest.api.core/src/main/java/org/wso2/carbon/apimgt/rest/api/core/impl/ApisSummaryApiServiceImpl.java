/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.ApisSummaryApiService;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.dto.APISummaryListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date =
        "2017-01-16T14:58:37.369+05:30")
public class ApisSummaryApiServiceImpl extends ApisSummaryApiService {
    private static final Logger log = LoggerFactory.getLogger(ApisSummaryApiServiceImpl.class);


    @Override
    public Response apisSummaryGet(String apiContext, String apiVersion, String accept, Request request) throws
            NotFoundException {

        try {
            APIMgtAdminService adminService = RestApiUtil.getAPIMgtAdminService();
            List<UriTemplate> uriTemplates = adminService.getAllResourcesForApi(apiContext, apiVersion);
            List<SubscriptionValidationData> subscriptionValidationDataList = adminService.getAPISubscriptionsOfApi
                    (apiContext, apiVersion);
            APISummaryListDTO apiSummaryListDTO = MappingUtil.toApiSummaryListDto(uriTemplates,
                    subscriptionValidationDataList);
            return Response.ok(apiSummaryListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retreiving API summary";
            HashMap<String, String> paramList = new HashMap<String, String>();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

    }
}
