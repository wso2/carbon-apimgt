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
 * /
 */

package org.wso2.carbon.apimgt.rest.api.store.utils.mappings;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class APIMappingUtil {

    public static APIIdentifier getAPIIdentifierFromApiId(String apiId) throws UnsupportedEncodingException {
        //if apiId contains -AT-, that need to be replaced before splitting
        apiId = APIUtil.replaceEmailDomainBack(apiId);
        String[] apiIdDetails = apiId.split(RestApiConstants.API_ID_DELIMITER);

        if (apiIdDetails.length < 3) {
            throw RestApiUtil.buildBadRequestException("Provided API identifier '" + apiId + "' is invalid");
        }

        // apiId format: provider-apiName-version
        String providerName = URLDecoder.decode(apiIdDetails[0], "UTF-8");
        String apiName = URLDecoder.decode(apiIdDetails[1], "UTF-8");
        String version = URLDecoder.decode(apiIdDetails[2], "UTF-8");
        String providerNameEmailReplaced = APIUtil.replaceEmailDomain(providerName);
        return new APIIdentifier(providerNameEmailReplaced, apiName, version);
    }

    public static APIIdentifier getAPIIdentifierFromApiIdOrUUID(String apiId, String requestedTenantDomain)
            throws APIManagementException, UnsupportedEncodingException {
        APIIdentifier apiIdentifier;
        APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
        if (RestApiUtil.isUUID(apiId)) {
            apiIdentifier = apiConsumer.getLightweightAPIByUUID(apiId, requestedTenantDomain).getId();
        } else {
            apiIdentifier = apiConsumer.getLightweightAPI(getAPIIdentifierFromApiId(apiId)).getId();
        }
        return  apiIdentifier;
    }

    public static APIDTO fromAPItoDTO(API model) throws APIManagementException {

        APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();

        APIDTO dto = new APIDTO();
        dto.setName(model.getId().getApiName());
        dto.setVersion(model.getId().getVersion());
        String providerName = model.getId().getProviderName();
        dto.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        dto.setId(model.getUUID());
        dto.setContext(model.getContext());
        dto.setDescription(model.getDescription());
        dto.setIsDefaultVersion(model.isDefaultVersion());
        dto.setStatus(model.getStatus().getStatus());

        //Get Swagger definition which has URL templates, scopes and resource details
        String apiSwaggerDefinition;

        apiSwaggerDefinition = apiConsumer.getSwagger20Definition(model.getId());
        apiSwaggerDefinition = RestAPIStoreUtils.removeXMediationScriptsFromSwagger(apiSwaggerDefinition);
        dto.setApiDefinition(apiSwaggerDefinition);

        Set<String> apiTags = model.getTags();
        List<String> tagsToReturn = new ArrayList<>();
        tagsToReturn.addAll(apiTags);
        dto.setTags(tagsToReturn);

        Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = model.getAvailableTiers();
        List<String> tiersToReturn = new ArrayList<>();
        for (org.wso2.carbon.apimgt.api.model.Tier tier : apiTiers) {
            tiersToReturn.add(tier.getName());
        }
        dto.setTiers(tiersToReturn);

        dto.setTransport(Arrays.asList(model.getTransports().split(",")));

        dto.setEndpointURLs(extractEnpointURLs(model));

        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(model.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(model.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(model.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(model.getTechnicalOwnerEmail());
        dto.setBusinessInformation(apiBusinessInformationDTO);
        if (!StringUtils.isBlank(model.getThumbnailUrl())) {
            dto.setThumbnailUrl(getThumbnailUri(model.getUUID()));
        }
        dto.setWsdlUri(model.getWsdlUrl());
        return dto;
    }

    /** Sets pagination urls for a APIListDTO object given pagination parameters and url parameters
     * 
     * @param apiListDTO APIListDTO object to which pagination urls need to be set 
     * @param query query parameter
     * @param offset starting index
     * @param limit max number of returned objects
     * @param size max offset
     */
    public static void setPaginationParams(APIListDTO apiListDTO, String query, int offset, int limit, int size) {
        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);

        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        apiListDTO.setNext(paginatedNext);
        apiListDTO.setPrevious(paginatedPrevious);
    }

    /** Converts an API Set object into corresponding REST API DTO
     * 
     * @param apiSet Set of API objects
     * @return APIListDTO object 
     */
    public static APIListDTO fromAPISetToDTO(Set<API> apiSet) {
        APIListDTO apiListDTO = new APIListDTO();
        List<APIInfoDTO> apiInfoDTOs = apiListDTO.getList();
        if (apiInfoDTOs == null) {
            apiInfoDTOs = new ArrayList<>();
            apiListDTO.setList(apiInfoDTOs);
        }
        for (API api : apiSet) {
            apiInfoDTOs.add(fromAPIToInfoDTO(api));
        }
        apiListDTO.setCount(apiSet.size());

        return apiListDTO;
    }
    
    /**
     * Converts a List object of APIs into a DTO
     *
     * @param apiList List of APIs
     * @param limit   maximum number of APIs returns
     * @param offset  starting index
     * @return APIListDTO object containing APIDTOs
     */
    public static APIListDTO fromAPIListToDTO(List<API> apiList, int offset, int limit) {
        APIListDTO apiListDTO = new APIListDTO();
        List<APIInfoDTO> apiInfoDTOs = apiListDTO.getList();
        if (apiInfoDTOs == null) {
            apiInfoDTOs = new ArrayList<>();
            apiListDTO.setList(apiInfoDTOs);
        }

        //add the required range of objects to be returned
        int start = offset < apiList.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= apiList.size() - 1 ? offset + limit - 1 : apiList.size() - 1;
        for (int i = start; i <= end; i++) {
            apiInfoDTOs.add(fromAPIToInfoDTO(apiList.get(i)));
        }
        apiListDTO.setCount(apiInfoDTOs.size());
        return apiListDTO;
    }

    /**
     * Creates a minimal DTO representation of an API object
     *
     * @param api API object
     * @return a minimal representation DTO
     */
    public static APIInfoDTO fromAPIToInfoDTO(API api) {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setDescription(api.getDescription());
        apiInfoDTO.setContext(api.getContext());
        apiInfoDTO.setId(api.getUUID());
        APIIdentifier apiId = api.getId();
        apiInfoDTO.setName(apiId.getApiName());
        apiInfoDTO.setVersion(apiId.getVersion());
        apiInfoDTO.setProvider(apiId.getProviderName());
        apiInfoDTO.setStatus(api.getStatus().toString());
        String providerName = api.getId().getProviderName();
        apiInfoDTO.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        if (!StringUtils.isBlank(api.getThumbnailUrl())) {
            apiInfoDTO.setThumbnailUri(getThumbnailUri(api.getUUID()));
        }
        return apiInfoDTO;
    }


    private static List<APIEndpointURLsDTO> extractEnpointURLs(API api) {
        List<APIEndpointURLsDTO> apiEndpointsList = new ArrayList<>();

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();

        Set<String> environmentsPublishedByAPI = new HashSet<String>(api.getEnvironments());
        environmentsPublishedByAPI.remove("none");

        Set<String> apiTransports = new HashSet<>(Arrays.asList(api.getTransports().split(",")));

        for (String environmentName : environmentsPublishedByAPI) {
            Environment environment = environments.get(environmentName);
            if (environment != null) {
                APIEnvironmentURLsDTO environmentURLsDTO = new APIEnvironmentURLsDTO();
                String[] gwEndpoints = environment.getApiGatewayEndpoint().split(",");

                for (String gwEndpoint : gwEndpoints) {
                    StringBuilder endpointBuilder = new StringBuilder(gwEndpoint);
                    endpointBuilder.append('/');
                    endpointBuilder.append(api.getContext());

                    if (gwEndpoint.contains("http:") && apiTransports.contains("http")) {
                        environmentURLsDTO.setHttp(endpointBuilder.toString());
                    }
                    else if (gwEndpoint.contains("https:") && apiTransports.contains("https")) {
                        environmentURLsDTO.setHttps(endpointBuilder.toString());
                    }
                }

                APIEndpointURLsDTO apiEndpointURLsDTO = new APIEndpointURLsDTO();
                apiEndpointURLsDTO.setEnvironmentURLs(environmentURLsDTO);

                apiEndpointURLsDTO.setEnvironmentName(environment.getName());
                apiEndpointURLsDTO.setEnvironmentType(environment.getType());

                apiEndpointsList.add(apiEndpointURLsDTO);
            }
        }

        return apiEndpointsList;
    }

    private static String getThumbnailUri (String uuid) {
        return RestApiConstants.RESOURCE_PATH_THUMBNAIL.replace(RestApiConstants.APIID_PARAM, uuid);
    }
}
