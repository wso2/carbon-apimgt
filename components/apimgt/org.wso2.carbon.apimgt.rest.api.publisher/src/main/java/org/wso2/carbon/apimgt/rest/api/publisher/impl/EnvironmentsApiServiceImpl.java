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

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.publisher.EnvironmentsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.EnvironmentMappingUtils;

import java.util.Map;
import javax.ws.rs.core.Response;

/** This is the service implementation class for gateway environment related operations
 *
 */
public class EnvironmentsApiServiceImpl extends EnvironmentsApiService {

    /** Get all gateway environments or applied gateway environments for a given API specified by id
     * 
     * @param apiId id of the API
     * @return Response object containing resulted gateway environments
     */
    @Override
    public Response environmentsGet(String apiId) {

        //todo : need to get environments of API if apiId is specified 
        APIManagerConfiguration config =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();
        if (environments != null) {
            EnvironmentListDTO environmentListDTO = EnvironmentMappingUtils.fromEnvironmentCollectionToDTO(
                    environments.values());
            return Response.ok().entity(environmentListDTO).build();
        } else {
            return Response.noContent().build();
        }
    }
}
