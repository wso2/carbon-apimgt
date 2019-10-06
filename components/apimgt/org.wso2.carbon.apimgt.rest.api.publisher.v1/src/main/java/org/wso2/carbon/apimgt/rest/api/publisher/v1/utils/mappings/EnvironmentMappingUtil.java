/*
 *
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentEndpointsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentListDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EnvironmentMappingUtil {

    private static final Log log = LogFactory.getLog(EnvironmentMappingUtil.class);

    /**
     * Converts an Environment object into EnvironmentDTO
     *
     * @param environment Environment object
     * @return EnvironmentDTO object corresponding to the given Environment object
     */
    public static EnvironmentDTO fromEnvironmentToDTO(Environment environment) {
        EnvironmentDTO environmentDTO = new EnvironmentDTO();
        environmentDTO.setName(environment.getName());
        environmentDTO.setType(environment.getType());
        environmentDTO.setServerUrl(environment.getServerURL());
        environmentDTO.setShowInApiConsole(environment.isShowInConsole());
        String[] gatewayEndpoints = environment.getApiGatewayEndpoint().split(",");
        String[] webSocketGatewayEndpoints = environment.getWebsocketGatewayEndpoint().split(",");
        EnvironmentEndpointsDTO environmentEndpointsDTO = new EnvironmentEndpointsDTO();
        for (String gatewayEndpoint : gatewayEndpoints) {
            if (isHttpURL(gatewayEndpoint)) {
                environmentEndpointsDTO.setHttp(gatewayEndpoint);
            } else if (isHttpsURL(gatewayEndpoint)) {
                environmentEndpointsDTO.setHttps(gatewayEndpoint);
            }
        }
        for (String websocketGatewayEndpoint : webSocketGatewayEndpoints) {
            if (isWebSocketURL(websocketGatewayEndpoint)) {
                environmentEndpointsDTO.setWs(websocketGatewayEndpoint);
            } else if (isSecureWebsocketURL(websocketGatewayEndpoint)) {
                environmentEndpointsDTO.setWss(websocketGatewayEndpoint);
            }
        }
        environmentDTO.setEndpoints(environmentEndpointsDTO);
        return environmentDTO;
    }

    /**
     * Converts a List object of SubscribedAPIs into a DTO
     *
     * @param environmentCollection a collection of Environment objects
     * @return EnvironmentListDTO object containing EnvironmentDTOs
     */
    public static EnvironmentListDTO fromEnvironmentCollectionToDTO(Collection<Environment> environmentCollection) {
        EnvironmentListDTO environmentListDTO = new EnvironmentListDTO();
        List<EnvironmentDTO> environmentDTOs = environmentListDTO.getList();
        if (environmentDTOs == null) {
            environmentDTOs = new ArrayList<>();
            environmentListDTO.setList(environmentDTOs);
        }

        for (Environment environment : environmentCollection) {
            environmentDTOs.add(fromEnvironmentToDTO(environment));
        }
        environmentListDTO.setCount(environmentDTOs.size());
        return environmentListDTO;
    }

    /**
     * Check whether given url is a HTTP url
     *
     * @param url url to check
     * @return true if the given url is HTTP, false otherwise
     */
    private static boolean isHttpURL(String url) {
        return url.matches("^http://.*");
    }

    /**
     * Check whether given url is a HTTPS url
     *
     * @param url url to check
     * @return true if the given url is HTTPS, false otherwise
     */
    private static boolean isHttpsURL(String url) {
        return url.matches("^https://.*");
    }

    /**
     * Check whether given url is a WS url
     *
     * @param url url to check
     * @return true if the given url is WS, false otherwise
     */
    private static boolean isWebSocketURL(String url) {
        return url.matches("^ws://.*");
    }

    /**
     * Check whether given url is a WSS url
     *
     * @param url url to check
     * @return true if the given url is WSS, false otherwise
     */
    private static boolean isSecureWebsocketURL(String url) {
        return url.matches("^wss://.*");
    }

}
