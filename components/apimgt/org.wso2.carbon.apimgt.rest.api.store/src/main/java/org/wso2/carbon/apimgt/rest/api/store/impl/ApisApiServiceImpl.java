package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.mappings.DocumentationMappingUtil;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:48:55.078+05:30")
public class ApisApiServiceImpl extends ApisApiService {

    private static final Logger log = LoggerFactory.getLogger(ApisApiServiceImpl.class);

    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String accept,
                                                           String ifNoneMatch, String ifModifiedSince,
                                                           String minorVersion) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            DocumentContent documentationContent = apiStore.getDocumentationContent(documentId);
            DocumentInfo documentInfo = documentationContent.getDocumentInfo();
            if (DocumentInfo.SourceType.FILE.equals(documentInfo.getSourceType())) {
                String filename = documentInfo.getFileName();
                return Response.ok(documentationContent.getFileContent())
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_TYPE)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .build();
            } else if (DocumentInfo.SourceType.INLINE.equals(documentInfo.getSourceType())) {
                String content = documentationContent.getInlineContent();
                return Response.ok(content)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, MediaType.TEXT_PLAIN).build();
            } else if (DocumentInfo.SourceType.URL.equals(documentInfo.getSourceType())) {
                String sourceUrl = documentInfo.getSourceURL();
                return Response.seeOther(new URI(sourceUrl)).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving document " + documentId + " of the API " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving source URI location of " + documentId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }
        return null;
    }

    public String apisApiIdDocumentsDocumentIdContentGetLastUpdatedTime(String apiId, String documentId,
            String accept, String ifNoneMatch, String ifModifiedSince, String minorVersion) {
        return null;
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String accept,
                                                    String ifNoneMatch, String ifModifiedSince, String minorVersion)
            throws NotFoundException {

        DocumentDTO documentDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            DocumentInfo documentInfo = apiStore.getDocumentationSummary(documentId);
            documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentInfo);
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while retrieving documentation for given apiId " + apiId + "with docId " + documentId, e, log);
        }
        return Response.ok().entity(documentDTO).build();
    }

    public String apisApiIdDocumentsDocumentIdGetLastUpdatedTime(String apiId, String documentId,
            String accept, String ifNoneMatch, String ifModifiedSince, String minorVersion) {
        return null;
    }

    @Override
    public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String accept,
                                          String ifNoneMatch, String minorVersion) throws NotFoundException {

        DocumentListDTO documentListDTO = null;
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            List<DocumentInfo> documentInfoResults = apiStore.getAllDocumentation(apiId, offset, limit);
            documentListDTO = DocumentationMappingUtil
                    .fromDocumentationListToDTO(documentInfoResults, offset, limit);
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while retrieving documentation for given apiId " + apiId, e, log);
        }

        return Response.ok().entity(documentListDTO).build();
    }
    
    public String apisApiIdDocumentsGetLastUpdatedTime(String apiId, Integer limit, Integer offset,
            String accept, String ifNoneMatch, String minorVersion) {
        return null;
    }

    /**
     * Get API of given ID
     *
     * @param apiId           API ID
     * @param accept          accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param minorVersion    Minor-Version header value
     * @return API of the given ID
     * @throws NotFoundException If failed to get the API
     */
    @Override
    public Response apisApiIdGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince, String minorVersion)
            throws NotFoundException {

        APIDTO apiToReturn = null;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIStore apiStore = RestApiUtil.getConsumer(username);
            API api = apiStore.getAPIbyUUID(apiId);
            apiToReturn = APIMappingUtil.toAPIDTO(api);
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.ok().entity(apiToReturn).build();
    }

    public String apisApiIdGetLastUpdatedTime(String apiId, String accept, String ifNoneMatch,
            String ifModifiedSince, String minorVersion) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            return apiStore.getLastUpdatedTimeOfAPI(apiId);
        } catch (APIManagementException e) {
            throw new InternalServerErrorException("Error while getting last update time", e);
        }
    }

    @Override
    public Response apisApiIdSwaggerGet(String apiId, String accept, String ifNoneMatch,
                                        String ifModifiedSince, String minorVersion) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            String swagger = apiStore.getSwagger20Definition(apiId);
            return Response.ok().entity(swagger).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving swagger definition of API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
    
    public String apisApiIdSwaggerGetLastUpdatedTime(String apiId, String accept, String ifNoneMatch,
            String ifModifiedSince, String minorVersion) {
        return "apisApiIdSwaggerGetLastUpdatedTime";
    }

    /**
     * Retrieves APIs qualifying under given search condition
     *
     * @param limit        maximum number of APIs returns
     * @param offset       starting index
     * @param query        search condition
     * @param accept       Accept header value
     * @param ifNoneMatch  If-None-Match header value
     * @param minorVersion Minor-Version header value
     * @return matched APIs for the given search condition
     */
    @Override
    public Response apisGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch,
                            String minorVersion) throws NotFoundException {
        List<API> apisResult = null;
        APIListDTO apiListDTO = null;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIStore apiStore = RestApiUtil.getConsumer(username);
            apisResult = apiStore.searchAPIs(query, offset, limit);
            // convert API
            apiListDTO = APIMappingUtil.toAPIListDTO(apisResult);
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving APIs ";
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_NAME, query);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.ok().entity(apiListDTO).build();
    }

    public String apisGetLastUpdatedTime(Integer limit, Integer offset, String query, String accept,
            String ifNoneMatch, String minorVersion) {
        return null;
    }
}
