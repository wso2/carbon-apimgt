package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.RatingListDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.mappings.CommentMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.mappings.RatingMappingUtil;

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
    public Response apisApiIdCommentsCommentIdDelete(String commentId, String apiId, String ifMatch,
                                            String ifUnmodifiedSince, String minorVersion) throws NotFoundException {
        return null;
    }

    @Override
    public Response apisApiIdCommentsCommentIdGet(String commentId, String apiId, String accept, String ifNoneMatch,
                                                 String ifModifiedSince, String minorVersion) throws NotFoundException {
        CommentDTO commentDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            Comment comment = apiStore.getCommentByUUID(commentId, apiId);
            commentDTO = CommentMappingUtil.fromCommentToDTO(comment);
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while retrieving Comment for given API " + apiId
                            + "with commentId " + commentId, e, log);
        }
        return Response.ok().entity(commentDTO).build();
    }

    @Override
    public Response apisApiIdCommentsPost(String apiId, CommentDTO body, String contentType, String minorVersion)
            throws NotFoundException {
        return null;
    }

    @Override
    public Response apisApiIdCommentsPut(String commentId, String apiId, CommentDTO body, String contentType, String
            ifMatch, String ifUnmodifiedSince, String minorVersion) throws NotFoundException {
        return null;
    }

    /**
     * Retrieves the content of the document
     *
     * @param apiId API ID
     * @param documentId Document ID
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param minorVersion Minor-Version header value
     * @return content of the document
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String accept,
                                                           String ifNoneMatch, String ifModifiedSince,
                                                           String minorVersion) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            String existingFingerprint = apisApiIdDocumentsDocumentIdContentGetFingerprint(apiId, documentId, accept,
                    ifNoneMatch, ifModifiedSince, minorVersion);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            DocumentContent documentationContent = apiStore.getDocumentationContent(documentId);
            DocumentInfo documentInfo = documentationContent.getDocumentInfo();
            if (DocumentInfo.SourceType.FILE.equals(documentInfo.getSourceType())) {
                String filename = documentInfo.getFileName();
                return Response.ok(documentationContent.getFileContent())
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_TYPE)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"")
                        .build();
            } else if (DocumentInfo.SourceType.INLINE.equals(documentInfo.getSourceType())) {
                String content = documentationContent.getInlineContent();
                return Response.ok(content)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, MediaType.TEXT_PLAIN)
                        .header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"")
                        .build();
            } else if (DocumentInfo.SourceType.URL.equals(documentInfo.getSourceType())) {
                String sourceUrl = documentInfo.getSourceURL();
                return Response.seeOther(new URI(sourceUrl))
                        .header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"")
                        .build();
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

    /**
     * Retrieves the fingerprint of a document content given its UUID
     *
     * @param apiId API ID
     * @param documentId Document ID
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param minorVersion Minor-Version header value
     * @return Fingerprint of the document content
     */
    public String apisApiIdDocumentsDocumentIdContentGetFingerprint(String apiId, String documentId, String accept,
            String ifNoneMatch, String ifModifiedSince, String minorVersion) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestApiUtil.getConsumer(username)
                    .getLastUpdatedTimeOfDocumentContent(apiId, documentId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage =
                    "Error while retrieving last updated time of content of document " + documentId + " of API "
                            + apiId;
            log.error(errorMessage, e);
            return null;
        }
    }

    /**
     * Retrives the document identified by the API's ID and the document's ID
     *
     * @param apiId UUID of API
     * @param documentId UUID of the document
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param minorVersion minor version header
     * @return the document qualifying for the provided IDs
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String accept,
                                                    String ifNoneMatch, String ifModifiedSince, String minorVersion)
            throws NotFoundException {

        DocumentDTO documentDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            String existingFingerprint = apisApiIdDocumentsDocumentIdGetFingerprint(apiId, documentId, accept,
                    ifNoneMatch, ifModifiedSince, minorVersion);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            DocumentInfo documentInfo = apiStore.getDocumentationSummary(documentId);
            documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentInfo);
            return Response.ok().entity(documentDTO)
                    .header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"").build();
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError(
                            "Error while retrieving documentation for given apiId " + apiId + "with docId "
                                    + documentId, e, log);
        }
        return null;
    }

    /**
     * Retrieves the fingerprint of the document given its UUID
     *
     * @param apiId API ID
     * @param documentId Document ID
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param minorVersion Minor-Version header value
     * @return Fingerprint of the document
     */

    public String apisApiIdDocumentsDocumentIdGetFingerprint(String apiId, String documentId, String accept,
            String ifNoneMatch, String ifModifiedSince, String minorVersion) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestApiUtil.getConsumer(username)
                    .getLastUpdatedTimeOfDocument(documentId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage =
                    "Error while retrieving last updated time of document " + documentId + " of API " + apiId;
            log.error(errorMessage, e);
            return null;
        }
    }

    /**
     * Retrieves a list of documents of an API
     *
     * @param apiId UUID of API
     * @param limit maximum documents to return
     * @param offset starting position of the pagination
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param minorVersion minor version header
     * @return a list of document DTOs
     * @throws NotFoundException When the particular resource does not exist in the system
     */
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
    public Response apisApiIdGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince,
            String minorVersion) throws NotFoundException {

        APIDTO apiToReturn = null;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIStore apiStore = RestApiUtil.getConsumer(username);
            String existingFingerprint = apisApiIdGetFingerprint(apiId, accept, ifNoneMatch, ifModifiedSince,
                    minorVersion);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            API api = apiStore.getAPIbyUUID(apiId);
            apiToReturn = APIMappingUtil.toAPIDTO(api);
            return Response.ok().entity(apiToReturn)
                    .header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"")
                    .build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response apisApiIdRatingGet(String apiId, Integer limit, Integer offset, String accept, String minorVersion)
            throws NotFoundException {

        RatingListDTO ratingListDTO = null;
        double avgRating, userRating;
        List<RatingDTO> ratingDTOList = null;


        String username = RestApiUtil.getLoggedInUsername();
        Response response;
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            userRating = apiStore.getUserRating(apiId, username);
            avgRating = apiStore.getAvgRating(apiId);
            ratingDTOList = RatingMappingUtil.fromRatingListToDTOList(apiStore.getUserRatingDTOList(apiId));

            ratingListDTO = RatingMappingUtil.fromRatingListToDTO(avgRating, userRating, offset, limit, ratingDTOList);

            return Response.ok().entity(ratingListDTO).build();

        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while retrieving Rating for given API " + apiId, e, log);
            return null;
        }

    }

    @Override
    public Response apisApiIdRatingPost(String apiId, RatingDTO body, String contentType, String minorVersion)
            throws NotFoundException {
        return null;
    }

    /**
     * Retrieves the fingerprint of the API given its ID
     * 
     * @param apiId API ID
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param minorVersion Minor-Version header value
     * @return Fingerprint of the API
     */
    public String apisApiIdGetFingerprint(String apiId, String accept, String ifNoneMatch, String ifModifiedSince,
            String minorVersion) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestApiUtil.getConsumer(username).getLastUpdatedTimeOfAPI(apiId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of API " + apiId;
            log.error(errorMessage, e);
            return null;
        }
    }

    /**
     * Retrieves the swagger definition of an API
     *
     * @param apiId UUID of API
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param minorVersion minor version header
     * @return swagger definition of an API
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdSwaggerGet(String apiId, String accept, String ifNoneMatch,
            String ifModifiedSince, String minorVersion) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            String existingFingerprint = apisApiIdSwaggerGetFingerprint(apiId, accept, ifNoneMatch, ifModifiedSince,
                    minorVersion);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            String swagger = apiStore.getSwagger20Definition(apiId);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"").entity(swagger).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving swagger definition of API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves the fingerprint of the swagger given its API's ID
     * 
     * @param apiId API ID
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param minorVersion Minor-Version header value
     * @return Retrieves the fingerprint String of the swagger
     */
    public String apisApiIdSwaggerGetFingerprint(String apiId, String accept, String ifNoneMatch,
            String ifModifiedSince, String minorVersion) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestApiUtil.getConsumer(username).getLastUpdatedTimeOfAPI(apiId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of Swagger definition of API :" + apiId;
            log.error(errorMessage, e);
            return null;
        }
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

}
