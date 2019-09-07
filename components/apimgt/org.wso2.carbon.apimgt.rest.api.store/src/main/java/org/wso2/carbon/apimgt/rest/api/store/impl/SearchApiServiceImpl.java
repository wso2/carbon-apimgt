package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;


import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SearchResultListDTO;

import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;
import java.util.Map;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.SearchResultMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import javax.ws.rs.core.Response;

public class SearchApiServiceImpl extends SearchApiService {
    private static final Log log = LogFactory.getLog(SearchApiServiceImpl.class);

    @Override
    public Response searchGet(Integer limit,Integer offset,String xWSO2Tenant,String query,String accept,String ifNoneMatch){
        SearchResultListDTO resultListDTO = new SearchResultListDTO();
        List<SearchResultDTO> allmatchedResults = new ArrayList<>();


        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);

        try {

            if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }
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
        } catch (UserStoreException e) {
            String errorMessage = "Error while retrieving search results";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }

        return Response.ok().entity(resultListDTO).build();
    }

    @Override public String searchGetGetLastUpdatedTime(Integer limit, Integer offset, String xWSO2Tenant, String query,
            String accept, String ifNoneMatch) {
        return null;
    }
}
