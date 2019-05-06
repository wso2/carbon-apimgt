/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.EnvironmentsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.EnvironmentMappingUtils;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/** This is the service implementation class for gateway environment related operations
 *
 */
public class EnvironmentsApiServiceImpl extends EnvironmentsApiService {

    private static final Log log = LogFactory.getLog(EnvironmentsApiServiceImpl.class);

    /** Get all gateway environments or applied gateway environments for a given API specified by id
     * 
     * @param apiId id of the API
     * @return Response object containing resulted gateway environments
     */
    @Override
    public Response environmentsGet(String apiId) {

        EnvironmentListDTO environmentListDTO = new EnvironmentListDTO();
        if (StringUtils.isBlank(apiId)) {
            // if apiId is not specified this will return all the environments
            APIManagerConfiguration config =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                            .getAPIManagerConfiguration();
            Map<String, Environment> environments = config.getApiGatewayEnvironments();
            if (environments != null) {
                environmentListDTO = EnvironmentMappingUtils.fromEnvironmentCollectionToDTO(environments.values());
            }
        } else {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            try {
                API api = APIMappingUtil.getAPIInfoFromApiIdOrUUID(apiId, tenantDomain);
                List <Environment> apiEnvironments = APIUtil.getEnvironmentsOfAPI(api);
                if (apiEnvironments != null) {
                    environmentListDTO = EnvironmentMappingUtils.fromEnvironmentCollectionToDTO(apiEnvironments);
                }
            } catch (APIManagementException e) {
                //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
                if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
                } else {
                    String errorMessage = "Error while retrieving API : " + apiId;
                    RestApiUtil.handleInternalServerError(errorMessage, e, log);
                }
            }
        }
        return Response.ok().entity(environmentListDTO).build();
    }
}
