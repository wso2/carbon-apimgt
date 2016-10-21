package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.constants.APIConstants;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;


import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

/**
 *  This is the service implementation class for Store API related operations*
 */
public class ApisApiServiceImpl extends ApisApiService {
    
    private static final Log log = LogFactory.getLog(ApplicationsApiServiceImpl.class);

    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId,String documentId,String xWSO2Tenant,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId,String documentId,String xWSO2Tenant,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsGet(String apiId,Integer limit,Integer offset,String xWSO2Tenant,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince,String xWSO2Tenant){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdSwaggerGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince,String xWSO2Tenant){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    
    /** 
    * Retrieves APIs qualifying under given search condition 
    *
    * @param limit maximum number of APIs returns
    * @param offset starting index
    * @param xWSO2Tenant requested tenant domain for cross tenant invocations
    * @param query search condition
    * @param accept Accept header value
    * @param ifNoneMatch If-None-Match header value
    * @return matched APIs for the given search condition
    */
    @Override
    public Response apisGet(Integer limit,Integer offset,String xWSO2Tenant,String query,String accept,String ifNoneMatch){
       
        Map<String, Object> apisMap;
        int size = 0;
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        APIListDTO apiListDTO = new APIListDTO();
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);

            // TODO - check tenant availability
            /*if (!RestApiUtil.isTenantAvailable(requestedTenantDomain)) {
                RestApiUtil.handleBadRequest("Provided tenant domain '" + xWSO2Tenant + "' is invalid", log);
            }*/

            //if query parameter is not specified, This will search by name
            // FIXME- move constants to a different component.
            String searchType = APIConstants.API_NAME;
            String searchContent = "*";
            if (!StringUtils.isBlank(query)) {
                String[] querySplit = query.split(":");
                if (querySplit.length == 2 && StringUtils.isNotBlank(querySplit[0]) && StringUtils
                        .isNotBlank(querySplit[1])) {
                    searchType = querySplit[0];
                    searchContent = querySplit[1];
                } else if (querySplit.length == 1) {
                    searchContent = query;
                } else {
                    RestApiUtil.handleBadRequest("Provided query parameter '" + query + "' is invalid", log);
                }
            }

            if (searchType.equalsIgnoreCase(APIConstants.API_STATUS) && 
                    searchContent.equalsIgnoreCase(APIConstants.PROTOTYPED)) {
                apisMap = apiConsumer.getAllPaginatedAPIsByStatus(requestedTenantDomain, offset, limit,
                        APIConstants.PROTOTYPED, false);
            } else {
                apisMap = apiConsumer
                        .searchPaginatedAPIs(searchContent, searchType, requestedTenantDomain, offset, limit, true);
            }

            Object apisResult = apisMap.get(APIConstants.API_DATA_APIS);
            //APIConstants.API_DATA_LENGTH is returned by executing searchPaginatedAPIs()
            if (apisMap.containsKey(APIConstants.API_DATA_LENGTH)) {
                size = (int) apisMap.get(APIConstants.API_DATA_LENGTH);
            //APIConstants.API_DATA_TOT_LENGTH is returned by executing getAllPaginatedAPIsByStatus()
            } else if (apisMap.containsKey(APIConstants.API_DATA_TOT_LENGTH)) {
                size = (int) apisMap.get(APIConstants.API_DATA_TOT_LENGTH);
            } else {
                log.warn("Size could not be determined from apis GET result for query " + query);
            }

            if (apisResult != null) {
                Set<API> apiSet = (Set)apisResult;
                apiListDTO = APIMappingUtil.fromAPISetToDTO(apiSet);
                APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, size);
            }

            return Response.ok().entity(apiListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.rootCauseMessageMatches(e, "start index seems to be greater than the limit count")) {
                //this is not an error of the user as he does not know the total number of apis available. Thus sends 
                //  an empty response
                apiListDTO.setCount(0);
                apiListDTO.setNext("");
                apiListDTO.setPrevious("");
                return Response.ok().entity(apiListDTO).build();
            } else {
                String errorMessage = "Error while retrieving APIs";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while checking availability of tenant " + requestedTenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
