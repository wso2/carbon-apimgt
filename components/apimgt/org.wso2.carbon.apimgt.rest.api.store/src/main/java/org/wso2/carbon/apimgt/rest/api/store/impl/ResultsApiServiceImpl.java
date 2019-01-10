/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.ResultsApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListPaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ResultDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ResultListDTO;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.SearchResultMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;


import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ResultsApiServiceImpl extends ResultsApiService {
    private static final Log log = LogFactory.getLog(ResultsApiServiceImpl.class);
    @Override
    public Response resultsGet(Integer limit, Integer offset, String xWSO2Tenant, String query, String accept,
            String ifNoneMatch) {
        ResultListDTO resultListDTO = new ResultListDTO();
        List<ResultDTO> allmatchedResults = new ArrayList<>();


        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);

        try {

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }
            //rest api results search by content is independent of config in api-manager.xml
            query = query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":") ?
                    query :
                    (APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":" + query);
            query = APIUtil.getSingleSearchCriteria(query);

            //set lcstate to apis
            boolean displayAPIsWithMultipleStatus = APIUtil.isAllowDisplayAPIsWithMultipleStatus();

            String [] statusList = {APIConstants.PUBLISHED.toLowerCase(), APIConstants.PROTOTYPED.toLowerCase(), "null"};
            if (displayAPIsWithMultipleStatus) {
                statusList = new String[]{APIConstants.PUBLISHED.toLowerCase(), APIConstants.PROTOTYPED.toLowerCase(), APIConstants.DEPRECATED.toLowerCase(), "null"};
            }

            String lcCriteria = APIConstants.LCSTATE_SEARCH_TYPE_KEY;
            lcCriteria = lcCriteria + APIUtil.getORBasedSearchCriteria(statusList);

            query = query + APIConstants.SEARCH_AND_TAG + lcCriteria;

            String username = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);

            Map<String, Object> result = apiConsumer
                    .searchPaginatedAPIs(query, requestedTenantDomain, offset, limit, false);
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
        } catch (UserStoreException e) {
            String errorMessage = "Error while retrieving search results";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }

        return Response.ok().entity(resultListDTO).build();
    }

    @Override public String resultsGetGetLastUpdatedTime(Integer limit, Integer offset, String xWSO2Tenant,
            String query, String accept, String ifNoneMatch) {
        return null;
    }
}
