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

    public Response getAllAPIs(Integer limit, Integer offset, String query, String ifNoneMatch,
                               MessageContext messageContext) {
        SearchResultListDTO resultListDTO = new SearchResultListDTO();
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? APIConstants.CHAR_ASTERIX : query;
        try {
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
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(e.getMessage(), e, log);
        }
        return Response.ok().entity(resultListDTO).build();
    }

    public Response providerNamePost(String provider, String apiId, MessageContext messageContext)
            throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        try {
            if (!APIUtil.isUserExist(provider)) {
                throw new APIManagementException("User " + provider + " not found.",
                        ExceptionCodes.USER_NOT_FOUND);
            }
            APIAdmin apiAdmin = new APIAdminImpl();
            apiAdmin.updateApiProvider(apiId, provider, tenantDomain);
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while changing the API provider", e);
        }
        return Response.ok().build();
    }

    private List<ApiResultDTO> getAllMatchedResults(List<Object> apis) throws APIManagementException {
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
