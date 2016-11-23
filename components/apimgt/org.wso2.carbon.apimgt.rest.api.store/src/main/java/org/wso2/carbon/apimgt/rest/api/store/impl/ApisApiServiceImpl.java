package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.store.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.mappings.DocumentationMappingUtil;

import javax.ws.rs.core.Response;
import java.util.List;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:48:55.078+05:30")
public class ApisApiServiceImpl extends ApisApiService {
    
    private static final Logger log = LoggerFactory.getLogger(ApisApiServiceImpl.class);

    @Override public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String accept,
            String ifNoneMatch, String ifModifiedSince) throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }


    @Override public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String accept,
            String ifNoneMatch, String ifModifiedSince) throws NotFoundException {

        DocumentDTO documentDTO = null;
        String apiConsumer = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(apiConsumer);
            DocumentInfo documentInfo = apiStore.getDocumentationSummary(documentId);
            documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentInfo);
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while retrieving documentation for given apiId " + apiId + "with docId " + documentId, e, log);
        }
        return Response.ok().entity(documentDTO).build();
    }


    @Override public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String accept,
            String ifNoneMatch) throws NotFoundException {

        DocumentListDTO documentListDTO = null;
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        String apiConsumer = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(apiConsumer);
            List<DocumentInfo> documentInfoResults = apiStore.getAllDocumentation(apiId, offset, limit);
            documentListDTO = DocumentationMappingUtil
                    .fromDocumentationListToDTO(documentInfoResults, offset, limit);
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while retrieving documentation for given apiId " + apiId, e, log);
        }

        return Response.ok().entity(documentListDTO).build();
    }
    
    /**
     * Get API of given ID
     *
     * @param apiId  API ID
     * @param accept accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return API of the given ID
     */
    @Override
    public Response apisApiIdGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince)
            throws NotFoundException {

        APIDTO apiToReturn = null;
        try {
            String apiConsumer = RestApiUtil.getLoggedInUsername();
            APIStore apiStore = RestApiUtil.getConsumer(apiConsumer);
            API api = apiStore.getAPIbyUUID(apiId);
            apiToReturn = APIMappingUtil.toAPIDTO(api);
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while retrieving API : " + apiId, e, log);
            }
        }
        return Response.ok().entity(apiToReturn).build();
    }
    
    
    @Override
    public Response apisApiIdSwaggerGet(String apiId, String accept, String ifNoneMatch,
            String ifModifiedSince) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    
    /**
     * Retrieves APIs qualifying under given search condition 
     * @param limit maximum number of APIs returns
     * @param offset starting index
     * @param query search condition
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return matched APIs for the given search condition
     * 
     */
    @Override
    public Response apisGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch)
            throws NotFoundException {
        List<API> apisResult = null;
        APIListDTO apiListDTO = null;
        try {
            String apiConsumer = RestApiUtil.getLoggedInUsername();
            APIStore apiStore = RestApiUtil.getConsumer(apiConsumer);
            apisResult = apiStore.searchAPIs(query, offset, limit);
            // convert API
            apiListDTO = APIMappingUtil.toAPIListDTO(apisResult);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(" Error while retrieving APIs ", e, log);
        }
        return Response.ok().entity(apiListDTO).build();
    }
}
