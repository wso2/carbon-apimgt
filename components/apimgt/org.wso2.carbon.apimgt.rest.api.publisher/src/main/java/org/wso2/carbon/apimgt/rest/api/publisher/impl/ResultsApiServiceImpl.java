/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.publisher.ResultsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListPaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ResultDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ResultListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.SearchResultMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.*;

public class ResultsApiServiceImpl extends ResultsApiService {
    private static final Log log = LogFactory.getLog(ResultsApiServiceImpl.class);

    @Override
    public Response resultsGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch){
        ResultListDTO resultListDTO = new ResultListDTO();
        List<ResultDTO> allmatchedResults = new ArrayList<>();


        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "*" : query;

        try {
            //rest api results search by content is independent of config in api-manager.xml
            query = query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":") ?
                    query :
                    (APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":" + query);
            query = APIUtil.getSingleSearchCriteria(query);

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
            Map<String, Object> result = apiProvider
                    .searchPaginatedAPIs(query, tenantDomain, offset, limit, false);
            Set<API> apis = (Set<API>) result.get("apis");
            Map<Documentation, API> docs = (Map<Documentation, API>) result.get("docs");

            for (API api : apis) {
                ResultDTO apiResult = SearchResultMappingUtil.fromAPIToAPIResultDTO(api);
                allmatchedResults.add(apiResult);
            }

            Iterator it = docs.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry pair = (Map.Entry) it.next();
                ResultDTO docResult = SearchResultMappingUtil.fromDocumentationToDocumentResultDTO((Documentation) pair.getKey(), (API) pair.getValue());
                allmatchedResults.add(docResult);
            }

            resultListDTO.setList(allmatchedResults);
            resultListDTO.setCount(allmatchedResults.size());
            SearchResultMappingUtil.setPaginationParams(resultListDTO, query, offset, limit, resultListDTO.getCount());

            //Add pagination section in the response
            int totalLength = (Integer) result.get("length");

            APIListPaginationDTO paginationDTO = new APIListPaginationDTO();
            paginationDTO.setOffset(offset);
            paginationDTO.setLimit(limit);
            paginationDTO.setTotal(totalLength);
            resultListDTO.setPagination(paginationDTO);

        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving search results";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }

        return Response.ok().entity(resultListDTO).build();
    }
}
