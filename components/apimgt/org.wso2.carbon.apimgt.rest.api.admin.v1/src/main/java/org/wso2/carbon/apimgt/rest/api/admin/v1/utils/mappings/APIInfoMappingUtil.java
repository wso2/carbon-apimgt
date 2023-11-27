/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class APIInfoMappingUtil {

    private static final String EMPTY_STRING = "";
    /**
     * Converts a APIIdentifier object into APIInfoDTO
     *
     * @param apiId APIIdentifier object
     * @return APIInfoDTO corresponds to APIIdentifier object
     */
    private static APIInfoDTO fromAPIInfoToDTO(APIIdentifier apiId)
            throws UnsupportedEncodingException {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        APIIdentifier apiIdEmailReplacedBack = new APIIdentifier(APIUtil.replaceEmailDomainBack(apiId.getProviderName
                ()).replace(RestApiConstants.API_ID_DELIMITER, RestApiConstants.URL_ENCODED_API_ID_DELIMITER),
                URLEncoder.encode(apiId.getApiName(), RestApiConstants.CHARSET).replace(RestApiConstants
                        .API_ID_DELIMITER, RestApiConstants.URL_ENCODED_API_ID_DELIMITER), apiId.getVersion().
                replace(RestApiConstants.API_ID_DELIMITER, RestApiConstants.URL_ENCODED_API_ID_DELIMITER));
        apiInfoDTO.setName(apiIdEmailReplacedBack.getApiName());
        apiInfoDTO.setVersion(apiIdEmailReplacedBack.getVersion());
        apiInfoDTO.setProvider(apiIdEmailReplacedBack.getProviderName());
        return apiInfoDTO;
    }

    /**
     * Converts a List object of APIIdentifiers into a DTO
     *
     * @param apiIds a list of APIIdentifier objects
     * @return APIInfoListDTO object containing APIInfoDTOs
     */
    public static APIInfoListDTO fromAPIInfoListToDTO(List<APIIdentifier> apiIds) throws
            UnsupportedEncodingException {
        APIInfoListDTO apiInfoListDTO = new APIInfoListDTO();
        List<APIInfoDTO> apiInfoDTOs = apiInfoListDTO.getList();
        if (apiInfoDTOs == null) {
            apiInfoDTOs = new ArrayList<>();
            apiInfoListDTO.setList(apiInfoDTOs);
        }
        for (APIIdentifier apiId : apiIds) {
            apiInfoDTOs.add(fromAPIInfoToDTO(apiId));
        }
        apiInfoListDTO.setCount(apiInfoDTOs.size());
        return apiInfoListDTO;
    }

    public static APIListDTO fromAPIListToInfoDTO(List<API> apiList) throws APIManagementException {
        APIListDTO apiListDTO = new APIListDTO();
        List<APIInfomationDTO> apiInfoDTOs = apiListDTO.getList();
        for (API api : apiList) {
            apiInfoDTOs.add(fromAPIToInfoDTO(api));
        }
        apiListDTO.setCount(apiInfoDTOs.size());
        return apiListDTO;
    }

    private static APIInfomationDTO fromAPIToInfoDTO(API api) {
        APIInfomationDTO apiInfoDTO = new APIInfomationDTO();
        APIIdentifier apiId = api.getId();
        apiInfoDTO.setName(apiId.getName());
        apiInfoDTO.setVersion(apiId.getVersion());
        apiInfoDTO.setProvider(apiId.getProviderName());
        return apiInfoDTO;
    }

    public static void setPaginationParams(Object apiListDTO, String query, int offset, int limit, int size) {

        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = EMPTY_STRING;
        String paginatedNext = EMPTY_STRING;

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        PaginationApisDTO paginationDTO = getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        ((APIListDTO) apiListDTO).setPagination(paginationDTO);
    }

    private static PaginationApisDTO getPaginationDTO(int limit, int offset, int total, String next, String previous) {
        PaginationApisDTO paginationDTO = new PaginationApisDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(total);
        paginationDTO.setNext(next);
        paginationDTO.setPrevious(previous);
        return paginationDTO;
    }
}
