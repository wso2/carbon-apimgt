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

package org.wso2.carbon.apimgt.rest.api.utils.mappings;

import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.rest.api.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.dto.EnvironmentEndpointsDTO;

public class EnvironmentMappingUtils {

    public static EnvironmentDTO fromEnvironmentToDTO(Environment environment) {
        EnvironmentDTO environmentDTO = new EnvironmentDTO();
        environmentDTO.setName(environment.getName());
        environmentDTO.setType(environment.getType());
        environmentDTO.setServerUrl(environment.getServerURL());
        environmentDTO.setShowInApiConsole(environment.isShowInConsole());
        String[] gatewayEndpoints = environment.getApiGatewayEndpoint().split(",");
        EnvironmentEndpointsDTO environmentEndpointsDTO = new EnvironmentEndpointsDTO();
        for (String gatewayEndpoint : gatewayEndpoints) {
            if (isHttpURL(gatewayEndpoint)) {
                environmentEndpointsDTO.setHttp(gatewayEndpoint);
            } else if (isHttpsURL(gatewayEndpoint)) {
                environmentEndpointsDTO.setHttps(gatewayEndpoint);
            }
        }
        environmentDTO.setEndpoints(environmentEndpointsDTO);
        return environmentDTO;
    }

    private static boolean isHttpURL (String url) {
        return url.matches("^http://.*");
    }

    private static boolean isHttpsURL (String url) {
        return url.matches("^https://.*");
    }

}
