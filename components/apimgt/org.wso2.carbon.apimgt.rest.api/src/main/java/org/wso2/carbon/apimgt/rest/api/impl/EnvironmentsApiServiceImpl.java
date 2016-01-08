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

package org.wso2.carbon.apimgt.rest.api.impl;

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.EnvironmentsApiService;
import org.wso2.carbon.apimgt.rest.api.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.utils.mappings.EnvironmentMappingUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

public class EnvironmentsApiServiceImpl extends EnvironmentsApiService {
    @Override
    public Response environmentsGet(String apiId) {

        //todo : need to get environments of API if apiId is specified 
        APIManagerConfiguration config =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();
        List <EnvironmentDTO> environmentDTOs = new ArrayList<>();
        if (environments != null) {
            for (Environment environment : environments.values()) {
                EnvironmentDTO environmentDTO = EnvironmentMappingUtils.fromEnvironmentToDTO(environment);
                environmentDTOs.add(environmentDTO);
            }
        }

        return Response.ok().entity(environmentDTOs).build();
    }
}
