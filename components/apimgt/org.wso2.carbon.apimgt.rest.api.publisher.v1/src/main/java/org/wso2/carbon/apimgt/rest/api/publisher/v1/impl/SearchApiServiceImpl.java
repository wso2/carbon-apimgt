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
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.SearchApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SearchResultDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SearchResultListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.SearchResultMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

public class SearchApiServiceImpl implements SearchApiService {

    private static final Log log = LogFactory.getLog(SearchApiServiceImpl.class);

    public Response search(Integer limit, Integer offset, String query, String ifNoneMatch,
                              MessageContext messageContext) throws APIManagementException {
        SearchResultListDTO resultListDTO = new SearchResultListDTO();
        List<SearchResultDTO> allmatchedResults = new ArrayList<>();

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "*" : query;

        if (!query.contains(":")) {
            query = (APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":" + query);
        }

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getOrganization(messageContext);
        Map<String, Object> result = null;
        if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX)) {
            result = apiProvider.searchPaginatedContent(query, organization, offset, limit);
        } else {
            result = apiProvider.searchPaginatedAPIs(query, organization, offset, limit, null, null);
        }
        ArrayList<Object> apis;
        /* Above searchPaginatedAPIs method underneath calls searchPaginatedAPIsByContent method,searchPaginatedAPIs
        method and searchAPIDoc method in AbstractApiManager. And those methods respectively returns ArrayList,
        TreeSet and a HashMap.
        Hence the below logic.
        */
        Object apiSearchResults = result.get("apis");
        if (apiSearchResults instanceof List<?>) {
            apis = (ArrayList<Object>) apiSearchResults;
        } else if (apiSearchResults instanceof HashMap) {
            Collection<String> values = ((HashMap) apiSearchResults).values();
            apis = new ArrayList<Object>(values);
        } else {
            apis = new ArrayList<Object>();
            apis.addAll((Collection<?>) apiSearchResults);
        }

        for (Object searchResult : apis) {
            if (searchResult instanceof API) {
                API api = (API) searchResult;
                SearchResultDTO apiResult = SearchResultMappingUtil.fromAPIToAPIResultDTO(api);
                allmatchedResults.add(apiResult);
            } else if (searchResult instanceof APIProduct) {
                APIProduct apiproduct = (APIProduct) searchResult;
                SearchResultDTO apiResult = SearchResultMappingUtil.fromAPIProductToAPIResultDTO(apiproduct);
                allmatchedResults.add(apiResult);
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
                allmatchedResults.add(docResult);
            }
        }

        Object totalLength = result.get("length");
        Integer length = 0;
        if (totalLength != null) {
            length = (Integer) totalLength;
        }

        List<Object> allmatchedObjectResults = new ArrayList<>(allmatchedResults);
        resultListDTO.setList(allmatchedObjectResults);
        resultListDTO.setCount(allmatchedResults.size());
        SearchResultMappingUtil.setPaginationParams(resultListDTO, query, offset, limit, length);

        return Response.ok().entity(resultListDTO).build();
    }
}
