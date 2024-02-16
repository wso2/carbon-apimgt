/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.restapi.publisher.SearchApiServiceImplUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.SearchApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SearchResultDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SearchResultListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.SearchResultMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

public class SearchApiServiceImpl implements SearchApiService {

    private static final Log log = LogFactory.getLog(SearchApiServiceImpl.class);

    @Override
    public Response search(Integer limit, Integer offset, String query, String ifNoneMatch,
                           MessageContext messageContext) throws APIManagementException {
        SearchResultListDTO resultListDTO = new SearchResultListDTO();
        if (!query.equals(RestApiConstants.PIZZASHACK_SEARCH_QUERY) && (query.startsWith("name:") |
                query.startsWith("description:"))) {
            query = query.replaceAll(" ", "%20");
        }

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "*" : query;

        if (!query.contains(":")) {
            query = (APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":" + query);
        }

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getOrganization(messageContext);
            Map<String, Object> result;
            if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX)) {
                result = apiProvider.searchPaginatedContent(query, organization, offset, limit);
            } else {
                result = apiProvider.searchPaginatedAPIs(query, organization, offset, limit,
                        RestApiConstants.DEFAULT_SORT_CRITERION, RestApiConstants.DEFAULT_SORT_ORDER);
            }

        /* Above searchPaginatedAPIs method underneath calls searchPaginatedAPIsByContent method,searchPaginatedAPIs
        method and searchAPIDoc method in AbstractApiManager. And those methods respectively returns ArrayList,
        TreeSet and a HashMap.
        Hence the below logic.
        */
            List<Object> apis = SearchApiServiceImplUtil.getAPIListFromAPISearchResult(result);

            List<SearchResultDTO> allMatchedResults = getAllMatchedResults(apis);

            Object totalLength = result.get("length");
            Integer length = 0;
            if (totalLength != null) {
                length = (Integer) totalLength;
            }

            List<Object> allmatchedObjectResults = new ArrayList<>(allMatchedResults);
            resultListDTO.setList(allmatchedObjectResults);
            resultListDTO.setCount(allMatchedResults.size());
            SearchResultMappingUtil.setPaginationParams(resultListDTO, query, offset, limit, length);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(e.getMessage(), e, log);
        }

        return Response.ok().entity(resultListDTO).build();
    }

    private List<SearchResultDTO> getAllMatchedResults(List<Object> apis) throws APIManagementException {
        List<SearchResultDTO> allMatchedResults = new ArrayList<>();
        for (Object searchResult : apis) {
            if (searchResult instanceof API) {
                API api = (API) searchResult;
                SearchResultDTO apiResult = SearchResultMappingUtil.fromAPIToAPIResultDTO(api);
                allMatchedResults.add(apiResult);
            } else if (searchResult instanceof APIProduct) {
                APIProduct apiproduct = (APIProduct) searchResult;
                SearchResultDTO apiResult = SearchResultMappingUtil.fromAPIProductToAPIResultDTO(apiproduct);
                allMatchedResults.add(apiResult);
            } else if (searchResult instanceof Map.Entry) {
                Map.Entry pair = (Map.Entry) searchResult;
                SearchResultDTO docResult;
                if (pair.getValue() instanceof API) {
                    docResult = SearchResultMappingUtil.fromDocumentationToDocumentResultDTO(
                            (Documentation) pair.getKey(), (API) pair.getValue());
                } else {
                    docResult = SearchResultMappingUtil.fromDocumentationToProductDocumentResultDTO(
                            (Documentation) pair.getKey(), (APIProduct) pair.getValue());
                }
                allMatchedResults.add(docResult);
            }
        }
        return allMatchedResults;
    }
}
