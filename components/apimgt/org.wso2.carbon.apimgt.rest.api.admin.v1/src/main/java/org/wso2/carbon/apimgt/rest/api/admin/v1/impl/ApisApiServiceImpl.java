/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.restapi.publisher.SearchApiServiceImplUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApiResultDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SearchResultListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.APIInfoMappingUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.ApplicationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    /**
     * This method gets all the APIs in the admin portal.
     *
     * @param limit pagination limit/ end value
     * @param offset pagination start value
     * @param query search query if the user has given any.
     * @param ifNoneMatch
     * @param messageContext
     * @return api results
     * @throws APIManagementException If there is any when getting the all apis
     */
    public Response getAllAPIs(Integer limit, Integer offset, String query, String ifNoneMatch,
                               MessageContext messageContext) throws APIManagementException {
        SearchResultListDTO resultListDTO = new SearchResultListDTO();
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? APIConstants.CHAR_ASTERIX : query;
        APIAdmin apiAdmin = new APIAdminImpl();
        String organization = RestApiUtil.getOrganization(messageContext);
        Map<String, Object> result = apiAdmin
                .searchPaginatedApis(query, organization, offset, limit);
        List<Object> apis = SearchApiServiceImplUtil.getAPIListFromAPISearchResult(result);
        List<ApiResultDTO> allMatchedResults = getAllMatchedResults(apis);
        resultListDTO.setApis(allMatchedResults);
        resultListDTO.setCount(allMatchedResults.size());
        APIInfoMappingUtil.setPaginationParams(resultListDTO, limit, offset, (Integer) result
                .get(APIConstants.ADMIN_API_LIST_RESPONSE_PARAMS_TOTAL));
        return Response.ok().entity(resultListDTO).build();
    }

    /**
     * change API provider
     *
     * @param provider new provider name
     * @param apiId API Id of the given API
     * @param messageContext
     * @return whether the api provider change successful or not
     * @throws APIManagementException if there is any error when changing the API provider.
     */
    public Response providerNamePost(String provider, String apiId, MessageContext messageContext)
            throws APIManagementException {
        String organisation = RestApiCommonUtil.getLoggedInUserTenantDomain();
        try {
            if (!APIUtil.isUserExist(provider)) {
                throw new APIManagementException("User " + provider + " not found.",
                        ExceptionCodes.USER_NOT_FOUND);
            }
            APIAdmin apiAdmin = new APIAdminImpl();
            apiAdmin.updateApiProvider(apiId, provider, organisation);
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while changing the API provider",
                    ExceptionCodes.CHANGE_API_PROVIDER_FAILED);
        }
        return Response.ok().build();
    }

    /**
     * Return the apis object as ApiResultDTO object.
     *
     * @param apis apis object
     * @return ApiResultDTO object
     */
    private List<ApiResultDTO> getAllMatchedResults(List<Object> apis) {
        List<ApiResultDTO> allMatchedResults = new ArrayList<>();
        for (Object searchResult : apis) {
            if (searchResult instanceof API) {
                API api = (API) searchResult;
                ApiResultDTO apiResult = APIInfoMappingUtil.fromAPIToAPIResultDTO(api);
                allMatchedResults.add(apiResult);
            }
        }
        return allMatchedResults;
    }
}
