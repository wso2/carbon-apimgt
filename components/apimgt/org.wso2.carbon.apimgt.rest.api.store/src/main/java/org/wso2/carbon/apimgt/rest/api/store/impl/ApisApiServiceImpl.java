package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIPublisherImpl;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummaryResults;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.store.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.APIMappingUtil;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:48:55.078+05:30")
public class ApisApiServiceImpl extends ApisApiService {
    
    private static final Logger log = LoggerFactory.getLogger(APIPublisherImpl.class);

    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId
, String documentId
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId
, String documentId
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsGet(String apiId
, Integer limit
, Integer offset
, String accept
, String ifNoneMatch
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    
    /**
     * Get API of given ID
     *
     * @param apiId  API ID
     * @param accept accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param xWSO2Tenant requested tenant domain for cross tenant invocations
     * @return API of the given ID
     */
    @Override
    public Response apisApiIdGet(String apiId
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {

        APIDTO apiToReturn = null;
        
        try {
            APIStore apiStore = RestApiUtil.getConsumer("subscriber-name"); // TODO -- get logged in user's name
            API api = apiStore.getAPIbyUUID(apiId);
            apiToReturn = APIMappingUtil.toAPIDTO(api);
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } 
        return Response.ok().entity(apiToReturn).build();
    }
    
    
    @Override
    public Response apisApiIdSwaggerGet(String apiId
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    
    /**
     * Retrieves APIs qualifying under given search condition 
     * @param limit maximum number of APIs returns
     * @param offset starting index
     * @param xWSO2Tenant requested tenant domain for cross tenant invocations
     * @param query search condition
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return matched APIs for the given search condition
     * 
     */
    @Override
    public Response apisGet(Integer limit
, Integer offset
, String query
, String accept
, String ifNoneMatch
 ) throws NotFoundException {
        
        APISummaryResults apisResult = null;
        APIListDTO apiListDTO = null;
        
        try {
            APIStore apiStore = RestApiUtil.getConsumer("subscriber-name"); // TODO -- get logged in user's name

            apisResult = apiStore.searchAPIs(query, offset, limit);
            
            // convert API
            apiListDTO = APIMappingUtil.toAPIListDTO(apisResult);           
            
        } catch (APIManagementException e) {
           RestApiUtil.handleInternalServerError(" Error while retrieving APIs ", e, log);
        }       
       
        return Response.ok().entity(apiListDTO).build();
    }
}
