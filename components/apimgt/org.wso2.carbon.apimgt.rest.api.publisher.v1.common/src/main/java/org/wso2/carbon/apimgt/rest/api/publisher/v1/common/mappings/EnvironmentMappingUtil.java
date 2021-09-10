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
import org.wso2.carbon.apimgt.api.model.ThirdPartyEnvironment;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.solace.SolaceAdminApis;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThirdPartyEnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThirdPartyEnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThirdPartyEnvironmentProtocolURIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.VHostDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
     * @param thirdPartyEnvironmentCollection a collection of Environment objects
     * @return EnvironmentListDTO object containing EnvironmentDTOs
     */
    public static ThirdPartyEnvironmentListDTO
    fromThirdPartyEnvironmentCollectionToDTO(Collection<ThirdPartyEnvironment> thirdPartyEnvironmentCollection)
            throws IOException {
        ThirdPartyEnvironmentListDTO thirdPartyEnvironmentListDTO = new ThirdPartyEnvironmentListDTO();
        List<ThirdPartyEnvironmentDTO> thirdPartyEnvironmentDTOs = thirdPartyEnvironmentListDTO.getList();
        if (thirdPartyEnvironmentDTOs == null) {
            thirdPartyEnvironmentDTOs = new ArrayList<>();
            thirdPartyEnvironmentListDTO.setList(thirdPartyEnvironmentDTOs);
        }

        for (ThirdPartyEnvironment thirdPartyEnvironment : thirdPartyEnvironmentCollection) {
            thirdPartyEnvironmentDTOs.add(fromThirdPartyEnvironmentToDTO(thirdPartyEnvironment));
        }
        thirdPartyEnvironmentListDTO.setCount(thirdPartyEnvironmentDTOs.size());
        return thirdPartyEnvironmentListDTO;
    }

    /**
     * Converts an ThirdPartyEnvironment object into ThirdPartyEnvironmentDTO
     *
     * @param thirdPartyEnvironment Environment object
     * @return ThirdPartyEnvironmentDTO object corresponding to the given ThirdPartyEnvironment object
     */
    public static ThirdPartyEnvironmentDTO fromThirdPartyEnvironmentToDTO(ThirdPartyEnvironment thirdPartyEnvironment)
            throws IOException {

        ThirdPartyEnvironmentDTO thirdPartyEnvironmentDTO = new ThirdPartyEnvironmentDTO();
        thirdPartyEnvironmentDTO.setName(thirdPartyEnvironment.getName());
        thirdPartyEnvironmentDTO.setOrganization(thirdPartyEnvironment.getOrganization());
        thirdPartyEnvironmentDTO.setProvider(thirdPartyEnvironment.getProvider());
        thirdPartyEnvironmentDTO.setDisplayName(thirdPartyEnvironment.getDisplayName());

        if (APIConstants.SOLACE_ENVIRONMENT.equalsIgnoreCase(thirdPartyEnvironment.getProvider())) {

            SolaceAdminApis solaceAdminApis = new SolaceAdminApis(thirdPartyEnvironment.getServerURL(),
                    thirdPartyEnvironment.getUserName(), thirdPartyEnvironment.getPassword(), thirdPartyEnvironment.
                    getDeveloper());
            HttpResponse response = solaceAdminApis.environmentGET(
                    thirdPartyEnvironment.getOrganization(), thirdPartyEnvironment.getName());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String responseString = null;
                responseString = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = new JSONObject(responseString);
                if (jsonObject.has("messagingProtocols")) {
                    JSONArray protocols = jsonObject.getJSONArray("messagingProtocols");
                    List<ThirdPartyEnvironmentProtocolURIDTO> endpointsList = new ArrayList<>();
                    for (int i = 0; i < protocols.length(); i++) {
                        JSONObject protocolDetails = protocols.getJSONObject(i);
                        String protocolName = protocolDetails.getJSONObject("protocol").getString("name");
                        String endpointURI = protocolDetails.getString("uri");
                        ThirdPartyEnvironmentProtocolURIDTO thirdPartyEnvironmentProtocolURIDTO =
                                new ThirdPartyEnvironmentProtocolURIDTO();
                        thirdPartyEnvironmentProtocolURIDTO.setProtocol(protocolName);
                        thirdPartyEnvironmentProtocolURIDTO.setEndpointURI(endpointURI);
                        endpointsList.add(thirdPartyEnvironmentProtocolURIDTO);
                    }
                    thirdPartyEnvironmentDTO.setEndpointURIs(endpointsList);
                }
            }
        }

        return thirdPartyEnvironmentDTO;
    }

}
