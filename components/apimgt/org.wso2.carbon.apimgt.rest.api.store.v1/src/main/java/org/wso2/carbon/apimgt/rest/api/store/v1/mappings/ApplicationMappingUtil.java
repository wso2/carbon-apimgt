/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationAttributeListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ScopeInfoDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApplicationMappingUtil {

    public static ApplicationDTO fromApplicationtoDTO (Application application) {
        ApplicationDTO applicationDTO = new ApplicationDTO();
        applicationDTO.setApplicationId(application.getUUID());
        applicationDTO.setThrottlingPolicy(application.getTier());
        applicationDTO.setDescription(application.getDescription());
        Map<String,String> applicationAttributes = application.getApplicationAttributes();
        applicationDTO.setAttributes(applicationAttributes);
        applicationDTO.setName(application.getName());
        applicationDTO.setStatus(application.getStatus());
        applicationDTO.setOwner(application.getOwner());

        if (StringUtils.isNotEmpty(application.getGroupId())) {
            applicationDTO.setGroups(Arrays.asList(application.getGroupId().split(",")));
        }
        applicationDTO.setTokenType(ApplicationDTO.TokenTypeEnum.OAUTH);
        applicationDTO.setSubscriptionCount(application.getSubscriptionCount());
        if (StringUtils.isNotEmpty(application.getTokenType()) && !APIConstants.DEFAULT_TOKEN_TYPE
                .equals(application.getTokenType())) {
            applicationDTO.setTokenType(ApplicationDTO.TokenTypeEnum.valueOf(application.getTokenType()));
        }

        //todo: Uncomment when this is implemented
        /*List<ApplicationKeyDTO> applicationKeyDTOs = new ArrayList<>();
        for(APIKey apiKey : application.getKeys()) {
            ApplicationKeyDTO applicationKeyDTO = ApplicationKeyMappingUtil.fromApplicationKeyToDTO(apiKey);
            applicationKeyDTOs.add(applicationKeyDTO);
        }
        applicationDTO.setKeys(applicationKeyDTOs);*/
        return applicationDTO;
    }

    public static Application fromDTOtoApplication (ApplicationDTO applicationDTO, String username) {
        //subscriber field of the body is not honored
        Subscriber subscriber = new Subscriber(username);
        Application application = new Application(applicationDTO.getName(), subscriber);
        application.setTier(applicationDTO.getThrottlingPolicy());
        application.setDescription(applicationDTO.getDescription());
        application.setUUID(applicationDTO.getApplicationId());
        application.setTokenType(APIConstants.DEFAULT_TOKEN_TYPE);

        //Check if the token type is not set in the request.
        if (StringUtils.isEmpty(applicationDTO.getTokenType().toString())) {
            //Set the default to JWT.
            application.setTokenType(APIConstants.TOKEN_TYPE_JWT);
        } else {
            //Otherwise set it to the type in the request.
            application.setTokenType(applicationDTO.getTokenType().toString());
        }
        Map <String, String> appAttributes = applicationDTO.getAttributes();
        application.setApplicationAttributes(appAttributes);
        if (applicationDTO.getGroups() != null && applicationDTO.getGroups().size() > 0) {
            application.setGroupId(String.join(",", applicationDTO.getGroups()));
        }
        return application;
    }

    /** Converts an Application[] array into a corresponding ApplicationListDTO
     *
     * @param applications array of Application objects
     * @return ApplicationListDTO object corresponding to Application[] array
     */
    public static ApplicationListDTO fromApplicationsToDTO(Application[] applications)
            throws APIManagementException {
        ApplicationListDTO applicationListDTO = new ApplicationListDTO();
        List<ApplicationInfoDTO> applicationInfoDTOs = applicationListDTO.getList();
        if (applicationInfoDTOs == null) {
            applicationInfoDTOs = new ArrayList<>();
            applicationListDTO.setList(applicationInfoDTOs);
        }

        for (Application application : applications) {
            ApplicationInfoDTO applicationInfoDTO = fromApplicationToInfoDTO(application);
            applicationInfoDTOs.add(applicationInfoDTO);
        }

        applicationListDTO.setCount(applicationInfoDTOs.size());
        return applicationListDTO;
    }

    /** Sets pagination urls for a ApplicationListDTO object given pagination parameters and url parameters
     *
     * @param applicationListDTO a SubscriptionListDTO object
     * @param groupId group id of the applications to be returned
     * @param limit max number of objects returned
     * @param offset starting index
     * @param size max offset
     */
    public static void setPaginationParams(ApplicationListDTO applicationListDTO, String groupId, int limit, int offset,
            int size) {

        setPaginationParamsWithSortParams(applicationListDTO, groupId, limit, offset,size, null, null);
    }


    /**
     * Sets pagination urls for a ApplicationListDTO object given pagination parameters and url parameters with
     * sortOrder and sortBy params.
     *
     * @param applicationListDTO a SubscriptionListDTO object
     * @param groupId            group id of the applications to be returned
     * @param limit              max number of objects returned
     * @param offset             starting index
     * @param size               max offset
     * @param sortOrder          sorting order
     * @param sortBy             specified sort param
     */
    public static void setPaginationParamsWithSortParams(ApplicationListDTO applicationListDTO, String groupId,
                                                         int limit, int offset, int size, String sortOrder,
                                                         String sortBy) {
        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);

        String paginatedPrevious = "";
        String paginatedNext = "";
        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getApplicationPaginatedURLWithSortParams(paginatedParams.get(RestApiConstants.
                            PAGINATION_PREVIOUS_OFFSET), paginatedParams.get(
                            RestApiConstants.PAGINATION_PREVIOUS_LIMIT), groupId, sortOrder, sortBy);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getApplicationPaginatedURLWithSortParams(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), groupId, sortOrder, sortBy);
        }
        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        applicationListDTO.setPagination(paginationDTO);
    }

    public static ApplicationInfoDTO fromApplicationToInfoDTO (Application application) {
        ApplicationInfoDTO applicationInfoDTO = new ApplicationInfoDTO();
        applicationInfoDTO.setApplicationId(application.getUUID());
        applicationInfoDTO.setThrottlingPolicy(application.getTier());
        applicationInfoDTO.setDescription(application.getDescription());
        applicationInfoDTO.setStatus(application.getStatus());
        applicationInfoDTO.setName(application.getName());
        if (StringUtils.isNotEmpty(application.getGroupId())) {
            applicationInfoDTO.setGroups(Arrays.asList(application.getGroupId().split(",")));
        }
        Map<String,String> applicationAttributes = application.getApplicationAttributes();
        applicationInfoDTO.setAttributes(applicationAttributes);
        applicationInfoDTO.setSubscriptionCount(application.getSubscriptionCount());
        applicationInfoDTO.setOwner(application.getOwner());
        return applicationInfoDTO;
    }

    /***
     * Converts the sort by object according to the input
     *
     * @param sortBy
     * @return Updated sort by field
     */
    public static String getApplicationSortByField (String sortBy) {
        String updatedSortBy = "";
        if (RestApiConstants.SORT_BY_NAME.equals(sortBy)) {
            updatedSortBy = APIConstants.APPLICATION_NAME;
        } else if (RestApiConstants.SORT_BY_OWNER.equals(sortBy)) {
            updatedSortBy = APIConstants.APPLICATION_CREATED_BY;
        } else if (RestApiConstants.SORT_BY_THROTTLING_TIER.equals(sortBy)) {
            updatedSortBy = APIConstants.APPLICATION_TIER;
        } else if (RestApiConstants.SORT_BY_STATUS.equals(sortBy)) {
            updatedSortBy = APIConstants.APPLICATION_STATUS;
        }

        return updatedSortBy;
    }

    /**
     * Creates a DTO representation of an Application Attribute
     *
     * @param attribute Application Attribute JSON object
     * @return an Application Attribute DTO
     */
    public static ApplicationAttributeDTO fromApplicationAttributeJsonToDTO(JSONObject attribute) {
        ApplicationAttributeDTO applicationAttributeDTO = new ApplicationAttributeDTO();
        applicationAttributeDTO.setAttribute((String) attribute.get(APIConstants.ApplicationAttributes.ATTRIBUTE));
        applicationAttributeDTO.setDescription((String) attribute.get(APIConstants.ApplicationAttributes.DESCRIPTION));
        applicationAttributeDTO.setRequired(String.valueOf(attribute.get(APIConstants.ApplicationAttributes.REQUIRED)));
        applicationAttributeDTO.setHidden(String.valueOf(attribute.get(APIConstants.ApplicationAttributes.HIDDEN)));
        applicationAttributeDTO.setDefault(String.valueOf(attribute.get(APIConstants.ApplicationAttributes.DEFAULT)));
        return applicationAttributeDTO;
    }

    /**
     * Converts an Application Attribute List object into corresponding REST API DTO
     *
     * @param attributeList List of attribute objects
     * @return ApplicationAttributeListDTO object
     */
    public static ApplicationAttributeListDTO fromApplicationAttributeListToDTO(
            List<ApplicationAttributeDTO> attributeList) {
        ApplicationAttributeListDTO applicationAttributeListDTO = new ApplicationAttributeListDTO();
        applicationAttributeListDTO.setList(attributeList);
        applicationAttributeListDTO.setCount(attributeList.size());
        return applicationAttributeListDTO;
    }

    public static List<ScopeInfoDTO> getScopeInfoDTO(Set<Scope> scopes) {
        List<ScopeInfoDTO> scopeDto = new ArrayList<ScopeInfoDTO>();
        for (Scope scope : scopes) {
            ScopeInfoDTO scopeInfoDTO = new ScopeInfoDTO();
            scopeInfoDTO.setKey(scope.getKey());
            scopeInfoDTO.setName(scope.getName());
            scopeInfoDTO.setDescription(scope.getDescription());
            if (StringUtils.isNotBlank(scope.getRoles())) {
                scopeInfoDTO.setRoles(Arrays.asList(scope.getRoles().trim().split(",")));
            }
            scopeDto.add(scopeInfoDTO);
        }
        return scopeDto;
    }
}
