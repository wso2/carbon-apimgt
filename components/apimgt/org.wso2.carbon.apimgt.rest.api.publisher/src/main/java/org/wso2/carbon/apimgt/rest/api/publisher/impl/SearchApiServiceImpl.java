package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.SearchResultListDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.SearchResultMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;

public class SearchApiServiceImpl extends SearchApiService {
    private static final Log log = LogFactory.getLog(SearchApiServiceImpl.class);
    @Override
    public Response searchGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch){
        SearchResultListDTO resultListDTO = new SearchResultListDTO();
        List<SearchResultDTO> allmatchedResults = new ArrayList<>();


        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "*" : query;

        try {
            query = query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":") ?
                    query :
                    (APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":" + query);
            query = APIUtil.getSingleSearchCriteria(query);

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
            Map<String, Object> result = apiProvider
                    .searchPaginatedAPIs(query, tenantDomain, offset, limit, false);
            ArrayList<Object> apis = (ArrayList<Object>) result.get("apis");

            for (Object searchResult : apis) {
                if (searchResult instanceof API) {
                    API api = (API) searchResult;
                    SearchResultDTO apiResult = SearchResultMappingUtil.fromAPIToAPIResultDTO(api);
                    allmatchedResults.add(apiResult);
                } else if (searchResult instanceof Map.Entry) {
                    Map.Entry pair = (Map.Entry) searchResult;
                    SearchResultDTO docResult = SearchResultMappingUtil.fromDocumentationToDocumentResultDTO((Documentation) pair.getKey(), (API) pair.getValue());
                    allmatchedResults.add(docResult);
                }
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
