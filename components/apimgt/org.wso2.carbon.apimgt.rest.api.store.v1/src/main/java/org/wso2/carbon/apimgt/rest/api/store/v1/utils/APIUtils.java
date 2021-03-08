/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.utils;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDefaultVersionURLsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIEndpointURLsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIURLsDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class APIUtils {
    /**
     * Extracts the API environment details with access url for each endpoint
     *
     * @param api          API object
     * @param tenantDomain Tenant domain of the API
     * @return the API environment details
     * @throws APIManagementException error while extracting the information
     */
    public static List<APIEndpointURLsDTO> extractEndpointURLs(API api, String tenantDomain)
            throws APIManagementException {
        List<APIEndpointURLsDTO> apiEndpointsList = new ArrayList<>();
        Map<String, Environment> environments = APIUtil.getEnvironments();

        Set<String> environmentsPublishedByAPI = new HashSet<>(api.getEnvironments());
        environmentsPublishedByAPI.remove("none");

        Set<String> apiTransports = new HashSet<>(Arrays.asList(api.getTransports().split(",")));
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();

        for (String environmentName : environmentsPublishedByAPI) {
            Environment environment = environments.get(environmentName);
            if (environment != null) {
                APIURLsDTO apiURLsDTO = new APIURLsDTO();
                APIDefaultVersionURLsDTO apiDefaultVersionURLsDTO = new APIDefaultVersionURLsDTO();
                String[] gwEndpoints = null;
                if ("WS".equalsIgnoreCase(api.getType())) {
                    gwEndpoints = environment.getWebsocketGatewayEndpoint().split(",");
                } else {
                    gwEndpoints = environment.getApiGatewayEndpoint().split(",");
                }
                Map<String, String> domains = new HashMap<>();
                if (tenantDomain != null) {
                    domains = apiConsumer.getTenantDomainMappings(tenantDomain,
                            APIConstants.API_DOMAIN_MAPPINGS_GATEWAY);
                }

                String customGatewayUrl = null;
                if (domains != null) {
                    customGatewayUrl = domains.get(APIConstants.CUSTOM_URL);
                }

                for (String gwEndpoint : gwEndpoints) {
                    StringBuilder endpointBuilder = new StringBuilder(gwEndpoint);

                    if (customGatewayUrl != null) {
                        int index = endpointBuilder.indexOf("//");
                        endpointBuilder.replace(index + 2, endpointBuilder.length(), customGatewayUrl);
                        endpointBuilder.append(api.getContext().replace("/t/" + tenantDomain, ""));
                    } else {
                        endpointBuilder.append(api.getContext());
                    }

                    if (gwEndpoint.contains("http:") && apiTransports.contains("http")) {
                        apiURLsDTO.setHttp(endpointBuilder.toString());
                    } else if (gwEndpoint.contains("https:") && apiTransports.contains("https")) {
                        apiURLsDTO.setHttps(endpointBuilder.toString());
                    } else if (gwEndpoint.contains("ws:")) {
                        apiURLsDTO.setWs(endpointBuilder.toString());
                    } else if (gwEndpoint.contains("wss:")) {
                        apiURLsDTO.setWss(endpointBuilder.toString());
                    }

                    if (api.isDefaultVersion()) {
                        int index = endpointBuilder.lastIndexOf(api.getId().getVersion());
                        endpointBuilder.replace(index, endpointBuilder.length(), "");
                        if (gwEndpoint.contains("http:") && apiTransports.contains("http")) {
                            apiDefaultVersionURLsDTO.setHttp(endpointBuilder.toString());
                        } else if (gwEndpoint.contains("https:") && apiTransports.contains("https")) {
                            apiDefaultVersionURLsDTO.setHttps(endpointBuilder.toString());
                        } else if (gwEndpoint.contains("ws:")) {
                            apiDefaultVersionURLsDTO.setWs(endpointBuilder.toString());
                        } else if (gwEndpoint.contains("wss:")) {
                            apiDefaultVersionURLsDTO.setWss(endpointBuilder.toString());
                        }
                    }
                }

                APIEndpointURLsDTO apiEndpointURLsDTO = new APIEndpointURLsDTO();
                apiEndpointURLsDTO.setDefaultVersionURLs(apiDefaultVersionURLsDTO);
                apiEndpointURLsDTO.setUrLs(apiURLsDTO);

                apiEndpointURLsDTO.setEnvironmentName(environment.getName());
                apiEndpointURLsDTO.setEnvironmentType(environment.getType());

                apiEndpointsList.add(apiEndpointURLsDTO);
            }
        }

        return apiEndpointsList;
    }


    /**
     * Extracts the API environment details with access url for each endpoint
     *
     * @param apiProduct   API object
     * @param tenantDomain Tenant domain of the API
     * @return the API environment details
     * @throws APIManagementException error while extracting the information
     */
    public static List<APIEndpointURLsDTO> extractEndpointURLs(APIProduct apiProduct, String tenantDomain)
            throws APIManagementException {
        List<APIEndpointURLsDTO> apiEndpointsList = new ArrayList<>();
        Map<String, Environment> environments = APIUtil.getEnvironments();

        Set<String> environmentsPublishedByAPI = new HashSet<>(apiProduct.getEnvironments());
        environmentsPublishedByAPI.remove("none");

        Set<String> apiTransports = new HashSet<>(Arrays.asList(apiProduct.getTransports().split(",")));
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();

        for (String environmentName : environmentsPublishedByAPI) {
            Environment environment = environments.get(environmentName);
            if (environment != null) {
                APIURLsDTO apiURLsDTO = new APIURLsDTO();
                String[] gwEndpoints = null;
                gwEndpoints = environment.getApiGatewayEndpoint().split(",");

                Map<String, String> domains = new HashMap<>();
                if (tenantDomain != null) {
                    domains = apiConsumer.getTenantDomainMappings(tenantDomain,
                            APIConstants.API_DOMAIN_MAPPINGS_GATEWAY);
                }

                String customGatewayUrl = null;
                if (domains != null) {
                    customGatewayUrl = domains.get(APIConstants.CUSTOM_URL);
                }

                for (String gwEndpoint : gwEndpoints) {
                    StringBuilder endpointBuilder = new StringBuilder(gwEndpoint);

                    if (customGatewayUrl != null) {
                        int index = endpointBuilder.indexOf("//");
                        endpointBuilder.replace(index + 2, endpointBuilder.length(), customGatewayUrl);
                        endpointBuilder.append(apiProduct.getContext().replace("/t/" + tenantDomain, ""));
                    } else {
                        endpointBuilder.append(apiProduct.getContext());
                    }

                    if (gwEndpoint.contains("http:") && apiTransports.contains("http")) {
                        apiURLsDTO.setHttp(endpointBuilder.toString());
                    } else if (gwEndpoint.contains("https:") && apiTransports.contains("https")) {
                        apiURLsDTO.setHttps(endpointBuilder.toString());
                    }
                }

                APIEndpointURLsDTO apiEndpointURLsDTO = new APIEndpointURLsDTO();
                apiEndpointURLsDTO.setUrLs(apiURLsDTO);
                apiEndpointURLsDTO.setEnvironmentName(environment.getName());
                apiEndpointURLsDTO.setEnvironmentType(environment.getType());

                apiEndpointsList.add(apiEndpointURLsDTO);
            }
        }

        return apiEndpointsList;
    }
}
