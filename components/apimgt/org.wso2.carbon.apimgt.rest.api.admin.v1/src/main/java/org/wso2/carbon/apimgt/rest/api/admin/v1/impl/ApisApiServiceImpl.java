package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.factory.PersistenceFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.APIInfoMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    public Response getAllAPIs(Integer limit, Integer offset, String sortBy, String sortOrder, String xWSO2Tenant,
                               String query, String ifNoneMatch, String accept, MessageContext messageContext) {
        List<API> allMatchedApis = new ArrayList<>();
        Object apiListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        sortBy = sortBy != null ? sortBy : RestApiConstants.DEFAULT_SORT_CRITERION;
        sortOrder = sortOrder != null ? sortOrder : RestApiConstants.DESCENDING_SORT_ORDER;
        try {

            //revert content search back to normal search by name to avoid doc result complexity and
            // to comply with REST api practices
            if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":")) {
                query = query
                        .replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":",
                                APIConstants.NAME_TYPE_PREFIX + ":");
            }

            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            Map<String, Object> result;

            result = apiProvider.searchPaginatedAPIs(query, organization, offset, limit, sortBy, sortOrder);

            Set<API> apis = (Set<API>) result.get("apis");
            allMatchedApis.addAll(apis);

            apiListDTO = APIInfoMappingUtil.fromAPIListToInfoDTO(allMatchedApis);

            //Add pagination section in the response
            Object totalLength = result.get("length");
            Integer length = 0;
            if (totalLength != null) {
                length = (Integer) totalLength;
            }
            APIInfoMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, length);
            if (APIConstants.APPLICATION_GZIP.equals(accept)) {
                try {
                    File zippedResponse = GZIPUtils.constructZippedResponse(apiListDTO);
                    return Response.ok().entity(zippedResponse)
                            .header("Content-Disposition", "attachment").
                            header("Content-Encoding", "gzip").build();
                } catch (APIManagementException e) {
                    RestApiUtil.handleInternalServerError(e.getMessage(), e, log);
                }
            } else {
                return Response.ok().entity(apiListDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving APIs";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response providerProviderNameApisApiIdPut(String providerName, String apiId, MessageContext messageContext)
            throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        String organization = null;
        if (tenantDomain != null) {
            organization = tenantDomain;
        }
        try {
            if (!APIUtil.isUserExist(providerName)) {
                throw new APIManagementException("User Not Found. Username :" + providerName + ",",
                        ExceptionCodes.USER_NOT_FOUND);
            }
            APIPersistence apiPersistenceInstance;
            apiPersistenceInstance = PersistenceFactory.getAPIPersistenceInstance();
            ApiMgtDAO.getInstance().updateApiProvider(apiId, providerName);
            apiPersistenceInstance.changeApiProvider(providerName, apiId,  organization);
        }catch (APIManagementException | APIPersistenceException e) {
            throw new APIManagementException("Error While Changing the Api Provider", e);
        }
        return Response.ok().build();
    }
}
