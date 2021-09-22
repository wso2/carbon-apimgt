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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AdditionalPropertyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GatewayEnvironmentProtocolURIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.VHostDTO;
import org.wso2.carbon.apimgt.solace.SolaceAdminApis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class used to map Environment DTO to model.
 */
public class EnvironmentMappingUtil {

    /**
     * Converts an Environment object into EnvironmentDTO.
     *
     * @param environment Environment object
     * @return EnvironmentDTO object corresponding to the given Environment object
     */
    public static EnvironmentDTO fromEnvironmentToDTO(Environment environment) {

        EnvironmentDTO environmentDTO = new EnvironmentDTO();
        environmentDTO.setName(environment.getName());
        environmentDTO.setDisplayName(environment.getDisplayName());
        environmentDTO.setType(environment.getType());
        environmentDTO.setServerUrl(environment.getServerURL());
        environmentDTO.setShowInApiConsole(environment.isShowInConsole());
        environmentDTO.setVhosts(environment.getVhosts().stream().map(EnvironmentMappingUtil::fromVHostToVHostDTO)
                .collect(Collectors.toList()));
        return environmentDTO;
    }

    /**
     * Converts a List object of SubscribedAPIs into a DTO.
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
     * Converts VHost into a VHostDTO.
     *
     * @param vHost VHost object
     * @return VHostDTO
     */
    public static VHostDTO fromVHostToVHostDTO(VHost vHost) {

        VHostDTO vHostDTO = new VHostDTO();
        vHostDTO.setHost(vHost.getHost());
        vHostDTO.setHttpContext(vHost.getHttpContext());
        vHostDTO.setHttpPort(vHost.getHttpPort());
        vHostDTO.setHttpsPort(vHost.getHttpsPort());
        vHostDTO.setWsPort(vHost.getWsPort());
        vHostDTO.setWssPort(vHost.getWssPort());
        vHostDTO.setWebsubHttpPort(vHost.getWebsubHttpPort());
        vHostDTO.setWebsubHttpsPort(vHost.getWebsubHttpsPort());
        return vHostDTO;
    }

    /**
     * Check whether given url is a HTTP url.
     *
     * @param url url to check
     * @return true if the given url is HTTP, false otherwise
     */
    private static boolean isHttpURL(String url) {

        return url.matches("^http://.*");
    }

    /**
     * Check whether given url is a HTTPS url.
     *
     * @param url url to check
     * @return true if the given url is HTTPS, false otherwise
     */
    private static boolean isHttpsURL(String url) {

        return url.matches("^https://.*");
    }

    /**
     * Check whether given url is a WS url.
     *
     * @param url url to check
     * @return true if the given url is WS, false otherwise
     */
    private static boolean isWebSocketURL(String url) {

        return url.matches("^ws://.*");
    }

    /**
     * Check whether given url is a WSS url.
     *
     * @param url url to check
     * @return true if the given url is WSS, false otherwise
     */
    private static boolean isSecureWebsocketURL(String url) {

        return url.matches("^wss://.*");
    }

    /**
     * Converts a List object of Third party environments into DTO
     *
     * @param gatewayEnvironmentCollection a collection of Environment objects
     * @return EnvironmentListDTO object containing EnvironmentDTOs
     */
    public static EnvironmentListDTO fromThirdPartyEnvironmentCollectionToDTO
    (Collection<Environment> gatewayEnvironmentCollection) throws IOException {
        EnvironmentListDTO gatewayEnvironmentListDTO = new EnvironmentListDTO();
        List<EnvironmentDTO> environmentDTOS = gatewayEnvironmentListDTO.getList();
        if (environmentDTOS == null) {
            environmentDTOS = new ArrayList<>();
            gatewayEnvironmentListDTO.setList(environmentDTOS);
        }

        for (Environment thirdPartyEnvironment : gatewayEnvironmentCollection) {
            environmentDTOS.add(fromThirdPartyEnvironmentToDTO(thirdPartyEnvironment));
        }
        gatewayEnvironmentListDTO.setCount(environmentDTOS.size());
        return gatewayEnvironmentListDTO;
    }

    /**
     * Converts an ThirdPartyEnvironment object into ThirdPartyEnvironmentDTO
     *
     * @param gatewayEnvironment Environment object
     * @return ThirdPartyEnvironmentDTO object corresponding to the given ThirdPartyEnvironment object
     */
    public static EnvironmentDTO fromThirdPartyEnvironmentToDTO(Environment gatewayEnvironment)
            throws IOException {

        EnvironmentDTO thirdPartyEnvironmentDTO = new EnvironmentDTO();
        thirdPartyEnvironmentDTO.setName(gatewayEnvironment.getName());
        Map<String, String> additionalProps = gatewayEnvironment.getAdditionalProperties();
        List<AdditionalPropertyDTO> additionalPropertyDTOS = new ArrayList<>();
        for (Map.Entry<String, String> prop : additionalProps.entrySet()) {
            AdditionalPropertyDTO additionalPropertyDTO = new AdditionalPropertyDTO();
            additionalPropertyDTO.setKey(prop.getKey());
            additionalPropertyDTO.setValue(additionalPropertyDTO.getValue());
            additionalPropertyDTOS.add(additionalPropertyDTO);
        }
        thirdPartyEnvironmentDTO.setAdditionalProperties(additionalPropertyDTOS);
        thirdPartyEnvironmentDTO.setProvider(gatewayEnvironment.getProvider());
        thirdPartyEnvironmentDTO.setDisplayName(gatewayEnvironment.getDisplayName());

        if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(gatewayEnvironment.getProvider())) {

            SolaceAdminApis solaceAdminApis = new SolaceAdminApis(gatewayEnvironment.getServerURL(),
                    gatewayEnvironment.getUserName(), gatewayEnvironment.getPassword(), gatewayEnvironment.
                    getAdditionalProperties().get(APIConstants.SOLACE_ENVIRONMENT_DEV_NAME));
            HttpResponse response = solaceAdminApis.environmentGET(
                    gatewayEnvironment.getAdditionalProperties()
                            .get(APIConstants.SOLACE_ENVIRONMENT_ORGANIZATION), gatewayEnvironment.getName());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String responseString = null;
                responseString = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = new JSONObject(responseString);
                if (jsonObject.has("messagingProtocols")) {
                    JSONArray protocols = jsonObject.getJSONArray("messagingProtocols");
                    List<GatewayEnvironmentProtocolURIDTO> endpointsList = new ArrayList<>();
                    for (int i = 0; i < protocols.length(); i++) {
                        JSONObject protocolDetails = protocols.getJSONObject(i);
                        String protocolName = protocolDetails.getJSONObject("protocol").getString("name");
                        String endpointURI = protocolDetails.getString("uri");
                        GatewayEnvironmentProtocolURIDTO gatewayEnvironmentProtocolURIDTO =
                                new GatewayEnvironmentProtocolURIDTO();
                        gatewayEnvironmentProtocolURIDTO.setProtocol(protocolName);
                        gatewayEnvironmentProtocolURIDTO.setEndpointURI(endpointURI);
                        endpointsList.add(gatewayEnvironmentProtocolURIDTO);
                    }
                    thirdPartyEnvironmentDTO.setEndpointURIs(endpointsList);
                }
            }
        }

        return thirdPartyEnvironmentDTO;
    }

}
