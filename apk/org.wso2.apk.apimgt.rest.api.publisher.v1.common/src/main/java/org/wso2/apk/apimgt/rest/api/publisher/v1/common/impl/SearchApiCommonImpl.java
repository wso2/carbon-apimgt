/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.apk.apimgt.rest.api.publisher.v1.common.impl;

import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIProvider;
import org.wso2.apk.apimgt.api.model.API;
import org.wso2.apk.apimgt.api.model.APIProduct;
import org.wso2.apk.apimgt.api.model.Documentation;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;
import org.wso2.apk.apimgt.rest.api.publisher.v1.common.mappings.SearchResultMappingUtil;
import org.wso2.apk.apimgt.rest.api.publisher.v1.dto.SearchResultDTO;
import org.wso2.apk.apimgt.rest.api.publisher.v1.dto.SearchResultListDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Util class for SearchApiService related operations
 */
public class SearchApiCommonImpl {

    private SearchApiCommonImpl() {
        //to hide default constructor
    }

    public static SearchResultListDTO search(Integer limit, Integer offset, String query, String organization)
            throws APIManagementException {

        SearchResultListDTO resultListDTO = new SearchResultListDTO();

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "*" : query;

        if (!query.contains(":")) {
            query = (APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":" + query);
        }

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
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
        List<Object> apis = getAPIListFromAPISearchResult(result);

        List<SearchResultDTO> allMatchedResults = getAllMatchedResults(apis);

        Object totalLength = result.get("length");
        int length = 0;
        if (totalLength != null) {
            length = (Integer) totalLength;
        }

        List<Object> allMatchedObjectResults = new ArrayList<>(allMatchedResults);
        resultListDTO.setList(allMatchedObjectResults);
        resultListDTO.setCount(allMatchedResults.size());
        SearchResultMappingUtil.setPaginationParams(resultListDTO, query, offset, limit, length);
        return resultListDTO;
    }

    /**
     * @param resultsMap API search result map
     * @return API List
     */
    private static List<Object> getAPIListFromAPISearchResult(Map<String, Object> resultsMap) {

        List<Object> apis;
        Object apiSearchResults = resultsMap.get("apis");
        if (apiSearchResults instanceof List<?>) {
            apis = (List<Object>) apiSearchResults;
        } else if (apiSearchResults instanceof HashMap) {
            Collection<String> values = ((HashMap) apiSearchResults).values();
            apis = new ArrayList<>(values);
        } else {
            apis = new ArrayList<>((Collection<?>) apiSearchResults);
        }
        return apis;
    }

    private static List<SearchResultDTO> getAllMatchedResults(List<Object> apis) throws APIManagementException {

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
                Map.Entry<Object, Object> pair = (Map.Entry) searchResult;
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
