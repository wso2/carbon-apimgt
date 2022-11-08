/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.apimgt.solace.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.SolaceAdminApis;
import org.wso2.carbon.apimgt.solace.dtos.SolaceDeployedEnvironmentDTO;
import org.wso2.carbon.apimgt.solace.dtos.SolaceTopicsDTO;
import org.wso2.carbon.apimgt.solace.dtos.SolaceTopicsObjectDTO;
import org.wso2.carbon.apimgt.solace.dtos.SolaceURLsDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class controls the util methods of Solace related Developer Portal implementations
 */
public class SolaceStoreUtils {

    private static final Log log = LogFactory.getLog(SolaceStoreUtils.class);

    /**
     * Get SolaceUrlsInfo using admin APIs and map into DTOs to parse Devportal
     *
     * @param solaceEnvironment Solace environment Object
     * @param organizationName Solace broker organization name
     * @param environmentName      Name of the  Solace environment
     * @param availableProtocols List of available protocols
     * @return List of SolaceURLsDTO
     * @throws APIManagementException if error occurred when creating
     */
    public static List<SolaceURLsDTO> getSolaceURLsInfo(Environment solaceEnvironment, String organizationName,
             String environmentName, List<String> availableProtocols) throws APIManagementException {
        // Create solace admin APIs instance
        SolaceAdminApis solaceAdminApis = new SolaceAdminApis(solaceEnvironment.getServerURL(), solaceEnvironment.
                getUserName(), solaceEnvironment.getPassword(), solaceEnvironment.getAdditionalProperties().
                get(SolaceConstants.SOLACE_ENVIRONMENT_DEV_NAME));
        List<SolaceURLsDTO> solaceURLsDTOs = new ArrayList<>();
        HttpResponse response = solaceAdminApis.environmentGET(organizationName, environmentName);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String responseString = null;
            try {
                responseString = EntityUtils.toString(response.getEntity());
                org.json.JSONObject jsonObject = new org.json.JSONObject(responseString);
                JSONArray protocols = jsonObject.getJSONArray("messagingProtocols");
                for (int i = 0; i < protocols.length(); i++) {
                    org.json.JSONObject protocolDetails = protocols.getJSONObject(i);
                    String protocolName = protocolDetails.getJSONObject("protocol").getString("name");
                    // Get solace protocol URLs for available protocols
                    if (availableProtocols.contains(protocolName)) {
                        String endpointURI = protocolDetails.getString("uri");
                        SolaceURLsDTO subscriptionInfoSolaceProtocolURLsDTO =
                                new SolaceURLsDTO();
                        subscriptionInfoSolaceProtocolURLsDTO.setProtocol(protocolName);
                        subscriptionInfoSolaceProtocolURLsDTO.setEndpointURL(endpointURI);
                        solaceURLsDTOs.add(subscriptionInfoSolaceProtocolURLsDTO);
                    }
                }
            } catch (IOException e) {
                throw new APIManagementException("Error occurred when retrieving protocols URLs from Solace " +
                        "admin apis");
            }
        }
        return solaceURLsDTOs;
    }

    /**
     * Get SolaceDeployedEnvironmentDTO using admin APIs and map into DTOs to parse Devportal
     *
     * @param solaceEnvironment Solace environment Object
     * @param solaceOrganization Solace broker organization name
     * @param applicationUuid      Subscribed Application UUID
     * @return List of SolaceDeployedEnvironmentDTO to use in Devportal
     * @throws APIManagementException if error occurred when creating SolaceDeployedEnvironmentDTO
     */
    public static List<SolaceDeployedEnvironmentDTO> getSolaceDeployedEnvsInfo(Environment solaceEnvironment,
                 String solaceOrganization, String applicationUuid) throws APIManagementException {
        Map<String, Environment> gatewayEnvironmentMap = APIUtil.getReadOnlyGatewayEnvironments();

        // Create solace admin APIs instance
        SolaceAdminApis solaceAdminApis = new SolaceAdminApis(solaceEnvironment.getServerURL(),
                solaceEnvironment.getUserName(), solaceEnvironment.getPassword(),
                solaceEnvironment.getAdditionalProperties().get(SolaceConstants.SOLACE_ENVIRONMENT_DEV_NAME));
        HttpResponse response = solaceAdminApis.applicationGet(solaceOrganization, applicationUuid, "default");

        List<SolaceDeployedEnvironmentDTO> solaceEnvironments = new ArrayList<>();
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                String responseString = EntityUtils.toString(response.getEntity());
                org.json.JSONObject jsonObject = new org.json.JSONObject(responseString);
                // Get solace environments attached with the Solace application
                if (jsonObject.getJSONArray("environments") != null) {
                    JSONArray environmentsArray = jsonObject.getJSONArray("environments");
                    for (int i = 0; i < environmentsArray.length(); i++) {
                        SolaceDeployedEnvironmentDTO solaceDeployedEnvironmentsDTO =
                                new SolaceDeployedEnvironmentDTO();
                        org.json.JSONObject environmentObject = environmentsArray.getJSONObject(i);
                        // Get details of Solace environment attached to the solace application
                        if (environmentObject.getString("name") != null) {
                            String environmentName = environmentObject.getString("name");
                            Environment gatewayEnvironment = gatewayEnvironmentMap.get(environmentName);
                            if (gatewayEnvironment != null) {
                                // Set Solace environment details
                                solaceDeployedEnvironmentsDTO.setEnvironmentName(gatewayEnvironment.getName());
                                solaceDeployedEnvironmentsDTO.setEnvironmentDisplayName(gatewayEnvironment.
                                        getDisplayName());
                                solaceDeployedEnvironmentsDTO.setOrganizationName(gatewayEnvironment.
                                        getAdditionalProperties().get(SolaceConstants.
                                                SOLACE_ENVIRONMENT_ORGANIZATION));

                                boolean containsMQTTProtocol = false;
                                // Get messaging protocols from the response body
                                if (environmentObject.getJSONArray("messagingProtocols") != null) {
                                    List<SolaceURLsDTO> endpointUrls = new ArrayList<>();
                                    JSONArray protocolsArray = environmentObject.
                                            getJSONArray("messagingProtocols");
                                    for (int j = 0; j < protocolsArray.length(); j++) {
                                        SolaceURLsDTO solaceURLsDTO = new SolaceURLsDTO();
                                        String protocol = protocolsArray.getJSONObject(j).getJSONObject
                                                ("protocol").getString("name");
                                        if (SolaceConstants.MQTT_TRANSPORT_PROTOCOL_NAME.
                                                equalsIgnoreCase(protocol)) {
                                            containsMQTTProtocol = true;
                                        }
                                        String uri = protocolsArray.getJSONObject(j).getString("uri");
                                        solaceURLsDTO.setProtocol(protocol);
                                        solaceURLsDTO.setEndpointURL(uri);
                                        endpointUrls.add(solaceURLsDTO);
                                    }
                                    solaceDeployedEnvironmentsDTO.setSolaceURLs(endpointUrls);
                                }

                                // Get topic permissions from the solace application response body
                                if (environmentObject.getJSONObject("permissions") != null) {
                                    org.json.JSONObject permissionsObject = environmentObject.getJSONObject
                                            ("permissions");
                                    SolaceTopicsObjectDTO solaceTopicsObjectDTO =
                                            new SolaceTopicsObjectDTO();
                                    populateSolaceTopics(solaceTopicsObjectDTO, permissionsObject,
                                            "default");
                                    // Handle the special case of MQTT protocol
                                    if (containsMQTTProtocol) {
                                        HttpResponse responseForMqtt = solaceAdminApis.applicationGet
                                                (solaceOrganization, applicationUuid, SolaceConstants.
                                                                MQTT_TRANSPORT_PROTOCOL_NAME.toUpperCase());

                                        org.json.JSONObject permissionsObjectForMqtt =
                                                extractPermissionsFromSolaceApplicationGetResponse(
                                                        responseForMqtt, i, gatewayEnvironmentMap);

                                        if (permissionsObjectForMqtt != null) {
                                            populateSolaceTopics(solaceTopicsObjectDTO,
                                                    permissionsObjectForMqtt,
                                                    SolaceConstants.MQTT_TRANSPORT_PROTOCOL_NAME.toUpperCase());
                                        }
                                    }
                                    solaceDeployedEnvironmentsDTO.setSolaceTopicsObject(solaceTopicsObjectDTO);
                                }
                            }
                        }
                        solaceEnvironments.add(solaceDeployedEnvironmentsDTO);
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            return  solaceEnvironments;
        } else {
            throw new APIManagementException("Solace Environment configurations are not provided properly");
        }
    }

    /**
     * Populate Solace Topics from the response body
     *
     * @param solaceTopicsObjectDTO Solace Topic Object DTO
     * @param permissionsObject Json Object of the body
     * @param syntax      Protocol Syntax
     */
    private static void populateSolaceTopics(SolaceTopicsObjectDTO solaceTopicsObjectDTO,
             org.json.JSONObject permissionsObject, String syntax) {

        SolaceTopicsDTO topicsDTO = new SolaceTopicsDTO();
        if (permissionsObject.getJSONArray("publish") != null) {
            List<String> publishTopics = new ArrayList<>();
            for (int j = 0; j < permissionsObject.getJSONArray("publish").length(); j++) {
                org.json.JSONObject channelObject = permissionsObject.getJSONArray("publish").getJSONObject(j);
                for (Object x : channelObject.keySet()) {
                    org.json.JSONObject channel = channelObject.getJSONObject(x.toString());
                    JSONArray channelPermissions = channel.getJSONArray("permissions");
                    for (int k = 0; k < channelPermissions.length(); k++) {
                        if (!publishTopics.contains(channelPermissions.getString(k))) {
                            publishTopics.add(channelPermissions.getString(k));
                        }
                    }
                }
            }
            topicsDTO.setPublishTopics(publishTopics);
        }
        if (permissionsObject.getJSONArray("subscribe") != null) {
            List<String> subscribeTopics = new ArrayList<>();
            for (int j = 0; j < permissionsObject.getJSONArray("subscribe").length(); j++) {
                org.json.JSONObject channelObject = permissionsObject.getJSONArray("subscribe").getJSONObject(j);
                for (Object x : channelObject.keySet()) {
                    org.json.JSONObject channel = channelObject.getJSONObject(x.toString());
                    JSONArray channelPermissions = channel.getJSONArray("permissions");
                    for (int k = 0; k < channelPermissions.length(); k++) {
                        if (!subscribeTopics.contains(channelPermissions.getString(k))) {
                            subscribeTopics.add(channelPermissions.getString(k));
                        }
                    }
                }
            }
            topicsDTO.setSubscribeTopics(subscribeTopics);
        }
        if (SolaceConstants.MQTT_TRANSPORT_PROTOCOL_NAME.equalsIgnoreCase(syntax)) {
            solaceTopicsObjectDTO.setMqttSyntax(topicsDTO);
        } else {
            solaceTopicsObjectDTO.setDefaultSyntax(topicsDTO);
        }
    }

    /**
     * Populate Solace permissions related to Topics from the response body
     *
     * @param response Response of the admin request
     * @param environmentIndex index value of registered environment
     * @param gatewayEnvironmentMap registered gateway envs map
     * @return org.json.JSONObject of permissions
     */
    private static org.json.JSONObject extractPermissionsFromSolaceApplicationGetResponse
            (HttpResponse response, int environmentIndex, Map<String, Environment> gatewayEnvironmentMap)
            throws IOException {

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String responseString = EntityUtils.toString(response.getEntity());
            org.json.JSONObject jsonObject = new org.json.JSONObject(responseString);
            if (jsonObject.getJSONArray("environments") != null) {
                JSONArray environmentsArray = jsonObject.getJSONArray("environments");
                org.json.JSONObject environmentObject = environmentsArray.getJSONObject(environmentIndex);
                if (environmentObject.getString("name") != null) {
                    String environmentName = environmentObject.getString("name");
                    Environment gatewayEnvironment = gatewayEnvironmentMap.get(environmentName);
                    if (gatewayEnvironment != null) {
                        if (environmentObject.getJSONObject("permissions") != null) {
                            return environmentObject.getJSONObject("permissions");
                        }
                    }
                }
            }
        }
        return null;
    }

}
