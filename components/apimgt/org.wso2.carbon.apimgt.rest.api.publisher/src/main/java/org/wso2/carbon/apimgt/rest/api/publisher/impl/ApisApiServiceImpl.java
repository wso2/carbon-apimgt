package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.core.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.carbon.lcm.core.impl.LifecycleState;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date =
        "2016-11-01T13:47:43.416+05:30")
public class ApisApiServiceImpl extends ApisApiService {
    private static final Logger log = LoggerFactory.getLogger(ApisApiServiceImpl.class);

    /**
     * Deletes a particular API
     *
     * @param apiId             UUID of API
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return 200 OK if the opration was successful
     * @throws NotFoundException when the particular resource does not exist
     */
    @Override
    public Response apisApiIdDelete(String apiId, String ifMatch, String ifUnmodifiedSince, Request request)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = apisApiIdGetFingerprint(apiId, null, null, null,
                    request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            apiPublisher.deleteAPI(apiId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting  API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves the content of a particular document
     *
     * @param apiId           UUID of API
     * @param documentId      UUID of the document
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return Content of the document
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String accept,
                                                           String ifNoneMatch, String ifModifiedSince, Request
                                                                   request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = apisApiIdDocumentsDocumentIdContentGetFingerprint(apiId, documentId, accept,
                    ifNoneMatch, ifModifiedSince, request);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            DocumentContent documentationContent = apiPublisher.getDocumentationContent(documentId);
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
     * Retrives the fingerprint of a particular document content
     *
     * @param apiId           UUID of API
     * @param documentId      UUID of the document
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return fingerprint of a particular document content
     */
    public String apisApiIdDocumentsDocumentIdContentGetFingerprint(String apiId, String documentId, String accept,
                                                                    String ifNoneMatch, String ifModifiedSince,
                                                                    Request request) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestAPIPublisherUtil.getApiPublisher(username)
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
     * Uploads a document's content and attach to particular document
     *
     * @param apiId             UUID of API
     * @param documentId        UUID of the document
     * @param contentType       Content-Type header value
     * @param fileInputStream   file content stream
     * @param fileDetail        meta infomation about the file
     * @param inlineContent     inline documentation content
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return updated document meta information
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdContentPost(String apiId, String documentId, String contentType,
                                                            InputStream fileInputStream, FileInfo fileDetail, String
                                                                    inlineContent, String ifMatch,
                                                            String ifUnmodifiedSince, Request request) throws
            NotFoundException {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIPublisher apiProvider = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = apisApiIdDocumentsDocumentIdContentGetFingerprint(apiId, documentId, null,
                    null, null, request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            if (fileInputStream != null && inlineContent != null) {
                String msg = "Only one of 'file' and 'inlineContent' should be specified";
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900314L, msg);
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
            }

            //retrieves the document and send 404 if not found
            DocumentInfo documentation = apiProvider.getDocumentationSummary(documentId);
            if (documentation == null) {
                String msg = "Documntation not found " + documentId;
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900314L, msg);
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(errorDTO).build();
            }
            DocumentInfo.Builder docBuilder = new DocumentInfo.Builder(documentation);
            //add content depending on the availability of either input stream or inline content
            if (fileInputStream != null) {
                if (!documentation.getSourceType().equals(DocumentInfo.SourceType.FILE)) {
                    String msg = "Source type of document " + documentId + " is not FILE";
                    log.error(msg);
                    ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900314L, msg);
                    log.error(msg);
                    return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
                }
                docBuilder = docBuilder.fileName(fileDetail.getFileName());
                apiProvider.uploadDocumentationFile(documentId, fileInputStream, fileDetail.getContentType());
            } else if (inlineContent != null) {
                if (!documentation.getSourceType().equals(DocumentInfo.SourceType.INLINE)) {
                    String msg = "Source type of document " + documentId + " is not INLINE";
                    log.error(msg);
                    ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900314L, msg);
                }
                apiProvider.addDocumentationContent(documentId, inlineContent);
            } else {
                String msg = "Either 'file' or 'inlineContent' should be specified";
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900314L, msg);
            }
            apiProvider.updateDocumentation(apiId, docBuilder.build());
            //retrieving the updated doc and the URI
            DocumentInfo updatedDoc = apiProvider.getDocumentationSummary(documentId);
            String newFingerprint = apisApiIdDocumentsDocumentIdContentGetFingerprint(apiId, documentId, null,
                    null, null, request);
            DocumentDTO documentDTO = MappingUtil.toDocumentDTO(updatedDoc);
            return Response.status(Response.Status.CREATED)
                    .header(HttpHeaders.ETAG, "\"" + newFingerprint + "\"").entity(documentDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding content to document" + documentId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            paramList.put(APIMgtConstants.ExceptionsConstants.DOC_ID, documentId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Delete an API's document
     *
     * @param apiId             UUID of API
     * @param documentId        UUID of the document
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return 200 OK response if the deletion was successful
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdDelete(String apiId, String documentId, String ifMatch,
                                                       String ifUnmodifiedSince, Request request) throws
            NotFoundException {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = apisApiIdDocumentsDocumentIdGetFingerprint(apiId, documentId, null, null, null,
                    request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }
            apiPublisher.removeDocumentation(documentId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting document" + documentId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            paramList.put(APIMgtConstants.ExceptionsConstants.DOC_ID, documentId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrives the document identified by the API's ID and the document's ID
     *
     * @param apiId           UUID of API
     * @param documentId      UUID of the document
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return the document qualifying for the provided IDs
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String accept, String ifNoneMatch,
                                                    String ifModifiedSince, Request request) throws NotFoundException {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);

            String existingFingerprint = apisApiIdDocumentsDocumentIdGetFingerprint(apiId, documentId, accept,
                    ifNoneMatch, ifModifiedSince, request);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            DocumentInfo documentInfo = apiPublisher.getDocumentationSummary(documentId);
            if (documentInfo != null) {
                return Response.ok().header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"")
                        .entity(MappingUtil.toDocumentDTO(documentInfo)).build();
            } else {
                String msg = "Documntation not found " + documentId;
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900314L, msg);
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while getting document" + documentId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            paramList.put(APIMgtConstants.ExceptionsConstants.DOC_ID, documentId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves the fingerprint of a document
     *
     * @param apiId           UUID of API
     * @param documentId      UUID of the document
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return fingerprint of the document
     */
    public String apisApiIdDocumentsDocumentIdGetFingerprint(String apiId, String documentId, String accept,
                                                             String ifNoneMatch, String ifModifiedSince, Request
                                                                     request) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestAPIPublisherUtil.getApiPublisher(username)
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
     * Updates an API's document
     *
     * @param apiId             UUID of API
     * @param documentId        UUID of the document
     * @param body              DTO object including the document's meta information
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return updated document meta info DTO as the response
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdPut(String apiId, String documentId, DocumentDTO body,
                                                    String contentType, String ifMatch, String ifUnmodifiedSince,
                                                    Request request)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);

            String existingFingerprint = apisApiIdDocumentsDocumentIdGetFingerprint(apiId, documentId, null, null, null,
                    request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            //DocumentInfo documentInfo = MappingUtil.toDocumentInfo(body);
            DocumentInfo documentInfoOld = apiPublisher.getDocumentationSummary(documentId);
            //validation checks for existence of the document
            if (documentInfoOld == null) {
                String msg = "Error while getting document";
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900314L, msg);
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(errorDTO).build();
            }
            if (body.getType() == DocumentDTO.TypeEnum.OTHER && StringUtils.isBlank(body.getOtherTypeName())) {
                //check otherTypeName for not null if doc type is OTHER
                String msg = "otherTypeName cannot be empty if type is OTHER.";
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900313L, msg);
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
            }
            if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                    (StringUtils.isBlank(body.getSourceUrl()) || !RestApiUtil.isURL(body.getSourceUrl()))) {
                //check otherTypeName for not null if doc type is OTHER
                String msg = "Invalid document sourceUrl Format";
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900313L, msg);
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
            }

            //overriding some properties
            body.setName(documentInfoOld.getName());
            body.setDocumentId(documentInfoOld.getId());

            DocumentInfo documentation = MappingUtil.toDocumentInfo(body);
            //this will fail if user does not have access to the API or the API does not exist
            apiPublisher.updateDocumentation(apiId, documentation);

            //retrieve the updated documentation
            DocumentInfo newDocumentation = apiPublisher.getDocumentationSummary(documentId);
            String newFingerprint = apisApiIdDocumentsDocumentIdGetFingerprint(apiId, documentId, null, null, null,
                    request);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + newFingerprint + "\"")
                    .entity(MappingUtil.toDocumentDTO(newDocumentation)).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating the document " + documentId + " for API : " + apiId;
            log.error(errorMessage, e);
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            paramList.put(APIMgtConstants.ExceptionsConstants.DOC_ID, documentId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves a list of documents of an API
     *
     * @param apiId       UUID of API
     * @param limit       maximum documents to return
     * @param offset      starting position of the pagination
     * @param accept      Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param request     msf4j request object
     * @return a list of document DTOs
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String accept,
                                          String ifNoneMatch, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            List<DocumentInfo> documentInfos = apiPublisher.getAllDocumentation(apiId, offset, limit);
            DocumentListDTO documentListDTO = MappingUtil.toDocumentListDTO(documentInfos);
            return Response.status(Response.Status.OK).entity(documentListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while getting list of documents" + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Adds new document to an API
     *
     * @param apiId             UUID of API
     * @param body              DTO object including the document's meta information
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return newly added document meta info object
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdDocumentsPost(String apiId, DocumentDTO body, String contentType, String ifMatch,
                                           String ifUnmodifiedSince, Request request) throws NotFoundException {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIPublisher apiProvider = RestAPIPublisherUtil.getApiPublisher(username);
            DocumentInfo documentation = MappingUtil.toDocumentInfo(body);
            if (body.getType() == DocumentDTO.TypeEnum.OTHER && StringUtils.isBlank(body.getOtherTypeName())) {
                //check otherTypeName for not null if doc type is OTHER
                RestApiUtil.handleBadRequest("otherTypeName cannot be empty if type is OTHER.", log);
            }
            String sourceUrl = body.getSourceUrl();
            if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                    (StringUtils.isBlank(sourceUrl) || !RestApiUtil.isURL(sourceUrl))) {
                RestApiUtil.handleBadRequest("Invalid document sourceUrl Format", log);
            }
            //this will fail if user does not have access to the API or the API does not exist
            String docid = apiProvider.addDocumentationInfo(apiId, documentation);
            documentation = apiProvider.getDocumentationSummary(docid);
            DocumentDTO newDocumentDTO = MappingUtil.toDocumentDTO(documentation);
            return Response.status(Response.Status.CREATED).entity(newDocumentDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while create  document for api " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieve the gateway configuration of an API
     *
     * @param apiId           UUID of API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return gateway configuration
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdGatewayConfigGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince,
                                              Request request)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = apisApiIdGatewayConfigGetFingerprint(apiId, accept, ifNoneMatch,
                    ifModifiedSince, request);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }
            String gatewayConfig = apiPublisher.getApiGatewayConfig(apiId);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"").entity(gatewayConfig)
                    .build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving gateway config of API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();

        }
    }

    /**
     * Retrieves the fingerprint of a gateway config provided its API's UUID
     *
     * @param apiId           UUID of API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return fingerprint of the gateaway config
     */
    public String apisApiIdGatewayConfigGetFingerprint(String apiId, String accept, String ifNoneMatch,
                                                       String ifModifiedSince, Request request) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestAPIPublisherUtil.getApiPublisher(username).getLastUpdatedTimeOfGatewayConfig(
                    apiId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of gateway config of API " + apiId;
            log.error(errorMessage, e);
            return null;
        }
    }

    /**
     * Update an API's gateway configuration by its UUID
     *
     * @param apiId             UUID of API
     * @param gatewayConfig     gateway configuration
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Updated gateway configuration
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdGatewayConfigPut(String apiId, String gatewayConfig, String contentType, String ifMatch,
                                              String ifUnmodifiedSince, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = apisApiIdGatewayConfigGetFingerprint(apiId, null, null, null, request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            apiPublisher.updateApiGatewayConfig(apiId, gatewayConfig);
            String apiGatewayConfig = apiPublisher.getApiGatewayConfig(apiId);
            String newFingerprint = apisApiIdGatewayConfigGetFingerprint(apiId, null, null, null, request);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + newFingerprint + "\"").entity(apiGatewayConfig)
                    .build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while gateway configuration update for api : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();

        }
    }

    /**
     * Retrives an API by UUID
     *
     * @param apiId           UUID of API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return API which is identified by the given UUID
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince,
                                 Request request
    ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            if (!RestAPIPublisherUtil.getApiPublisher(username).checkIfAPIExists(apiId)) {
                String errorMessage = "API not found : " + apiId;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.API_NOT_FOUND);
                HashMap<String, String> paramList = new HashMap<String, String>();
                paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            }

            String existingFingerprint = apisApiIdGetFingerprint(apiId, accept, ifNoneMatch, ifModifiedSince,
                    request);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            API api = RestAPIPublisherUtil.getApiPublisher(username).getAPIbyUUID(apiId);
            api.setUserSpecificApiPermissions(getAPIPermissionsOfLoggedInUser(username, api));
            APIDTO apidto = MappingUtil.toAPIDto(api);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"").entity(apidto).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();

        } catch (IOException e) {
            String errorMessage = "Error while retrieving API : " + apiId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }
    }

    /**
     * This method retrieves the set of overall permissions for a given api for the logged in user
     *
     * @param loggedInUser - Logged in user
     * @param api - The API whose permissions for the logged in user is retrieved
     * @return The overall list of permissions for the given API for the logged in user
     */
    private List<String> getAPIPermissionsOfLoggedInUser(String loggedInUser, API api) {

        Map<String, Integer> permissionMap = api.getPermissionMap();
        Set<String> loggedInUserRoles = APIUtils.getAllRolesOfUser(loggedInUser);
        Set<String> permissionRoleList = getRolesFromPermissionMap(permissionMap);
        Set<String> rolesOfUserWithAPIPermissions = null;
        Set<String> permissionArrayForUser = new HashSet<>();
        loggedInUserRoles.retainAll(
                permissionRoleList); //get the intersection - retainAll() transforms first set to the intersection
        if (!loggedInUserRoles.isEmpty()) {
            rolesOfUserWithAPIPermissions = loggedInUserRoles;
        }
        if (rolesOfUserWithAPIPermissions != null) {
            for (String role : rolesOfUserWithAPIPermissions) {
                Integer permission = permissionMap.get(role);
                if (permission == APIMgtConstants.Permission.READ_PERMISSION) {
                    permissionArrayForUser.add(APIMgtConstants.Permission.READ);
                } else if (permission == (APIMgtConstants.Permission.READ_PERMISSION
                        + APIMgtConstants.Permission.UPDATE_PERMISSION)) {
                    permissionArrayForUser.add(APIMgtConstants.Permission.READ);
                    permissionArrayForUser.add(APIMgtConstants.Permission.UPDATE);
                } else if (permission == (APIMgtConstants.Permission.READ_PERMISSION
                        + APIMgtConstants.Permission.DELETE_PERMISSION)) {
                    permissionArrayForUser.add(APIMgtConstants.Permission.READ);
                    permissionArrayForUser.add(APIMgtConstants.Permission.DELETE);
                } else if (permission
                        == APIMgtConstants.Permission.READ_PERMISSION + APIMgtConstants.Permission.UPDATE_PERMISSION
                        + APIMgtConstants.Permission.DELETE_PERMISSION) {
                    permissionArrayForUser.add(APIMgtConstants.Permission.READ);
                    permissionArrayForUser.add(APIMgtConstants.Permission.UPDATE);
                    permissionArrayForUser.add(APIMgtConstants.Permission.DELETE);
                }
            }
        }
        List<String> finalAggregatedPermissionList = new ArrayList<>();
        finalAggregatedPermissionList.addAll(permissionArrayForUser);
        return finalAggregatedPermissionList;
    }

    /**
     * This method is used to extract the groupIds or roles from the permissionMap
     * 
     * @param permissionMap - The map containing the group IDs(roles) and their permissions
     * @return - The list of groupIds specified for permissions
     */
    private Set<String> getRolesFromPermissionMap(Map<String, Integer> permissionMap) {
        Set<String> permissionRoleList = new HashSet<>();
        for (String groupId : permissionMap.keySet()) {
            permissionRoleList.add(groupId);
        }
        return permissionRoleList;
    }

    /**
     * Returns the fingerprint of an API
     *
     * @param apiId           UUID of API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return fingerprint of the given API
     */
    public String apisApiIdGetFingerprint(String apiId, String accept, String ifNoneMatch, String ifModifiedSince,
                                          Request request) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestAPIPublisherUtil.getApiPublisher(username).getLastUpdatedTimeOfAPI(apiId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of API " + apiId;
            log.error(errorMessage, e);
            return null;
        }
    }

    /**
     * Retrieves the possible lifecycle states of a given API
     *
     * @param apiId           UUID of API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return possible lifecycle states of a given API
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdLifecycleGet(String apiId, String accept, String ifNoneMatch,
                                          String ifModifiedSince, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            LifecycleState lifecycleState = RestAPIPublisherUtil.getApiPublisher(username).getAPILifeCycleData(apiId);
            return Response.ok().entity(lifecycleState).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Lifecycle state data for API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();

        }
    }

    /**
     * Retrieves the lifecycle history of the API
     *
     * @param apiId           UUID of the API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return lifecycle history of the API
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdLifecycleHistoryGet(String apiId, String accept, String ifNoneMatch,
                                                 String ifModifiedSince, Request request) throws NotFoundException {

        String username = RestApiUtil.getLoggedInUsername();
        try {
            if (RestAPIPublisherUtil.getApiPublisher(username).checkIfAPIExists(apiId)) {
                String lifecycleInstanceId =
                        RestAPIPublisherUtil.getApiPublisher(username).getAPIbyUUID(apiId).getLifecycleInstanceId();
                if (lifecycleInstanceId != null) {
                    List lifecyclestatechangehistory =
                            RestAPIPublisherUtil.getApiPublisher(username)
                                    .getLifeCycleHistoryFromUUID(lifecycleInstanceId);
                    return Response.ok().entity(lifecyclestatechangehistory).build();
                } else {
                    throw new APIManagementException("Could not find lifecycle information for the requested API"
                            + apiId, ExceptionCodes.APIMGT_LIFECYCLE_EXCEPTION);
                }
            } else {
                String errorMessage = "API Not found : " + apiId;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.API_NOT_FOUND);
                HashMap<String, String> paramList = new HashMap<String, String>();
                paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();

            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();

        }
    }

    /**
     * Updates an API by UUID
     *
     * @param apiId             UUID of API
     * @param body              Updated API details
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Updated API
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdPut(String apiId, APIDTO body, String contentType, String ifMatch,
                                 String ifUnmodifiedSince, Request request
    ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = apisApiIdGetFingerprint(apiId, null, null, null, request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            API.APIBuilder api = MappingUtil.toAPI(body).id(apiId);
            apiPublisher.updateAPI(api);

            String newFingerprint = apisApiIdGetFingerprint(apiId, null, null, null, request);
            APIDTO apidto = MappingUtil.toAPIDto(apiPublisher.getAPIbyUUID(apiId));
            return Response.ok().header(HttpHeaders.ETAG, "\"" + newFingerprint + "\"").entity(apidto).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();

        } catch (JsonProcessingException e) {
            String errorMessage = "Error while updating API : " + apiId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        } catch (IOException e) {
            String errorMessage = "Error while updating API : " + apiId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves the swagger definition of an API
     *
     * @param apiId           UUID of API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return swagger definition of an API
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdSwaggerGet(String apiId, String accept,
                                        String ifNoneMatch, String ifModifiedSince, Request request) throws
            NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = apisApiIdSwaggerGetFingerprint(apiId, accept, ifNoneMatch, ifModifiedSince,
                    request);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }
            String swagger = apiPublisher.getApiSwaggerDefinition(apiId);
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
     * Retrieves the fingerprint of a swagger definition of an API
     *
     * @param apiId           UUID of API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return fingerprint of a swagger definition of an API
     */
    public String apisApiIdSwaggerGetFingerprint(String apiId, String accept, String ifNoneMatch,
                                                 String ifModifiedSince, Request request) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestAPIPublisherUtil.getApiPublisher(username).getLastUpdatedTimeOfAPI(apiId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of Swagger definition of API :" + apiId;
            log.error(errorMessage, e);
            return null;
        }
    }

    /**
     * Updates the swagger defnition of an API
     *
     * @param apiId             UUID of API
     * @param apiDefinition     updated swagger defintion
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Updated swagger definition
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdSwaggerPut(String apiId, String apiDefinition, String contentType, String ifMatch,
                                        String ifUnmodifiedSince, Request request
    ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = apisApiIdSwaggerGetFingerprint(apiId, null, null, null, request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }
            apiPublisher.saveSwagger20Definition(apiId, apiDefinition);
            String apiSwagger = apiPublisher.getApiSwaggerDefinition(apiId);
            String newFingerprint = apisApiIdSwaggerGetFingerprint(apiId, null, null, null, request);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + newFingerprint + "\"").entity(apiSwagger).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while put swagger for API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrives the thumbnail of an API
     *
     * @param apiId           UUID of API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return the thumbnail image of an API
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdThumbnailGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince,
                                          Request request
    ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = apisApiIdThumbnailGetFingerprint(apiId, accept, ifNoneMatch, ifModifiedSince,
                    request);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            InputStream imageInputStream = apiPublisher.getThumbnailImage(apiId);
            if (imageInputStream != null) {
                return Response.ok(imageInputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"icon\"")
                        .header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"").build();
            } else {
                return Response.noContent().build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving thumbnail of API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrives the current fingerprint of the thumbnail image of an API
     *
     * @param apiId           UUID of API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return current fingerprint of the thumbnail image of the API
     */
    public String apisApiIdThumbnailGetFingerprint(String apiId, String accept, String ifNoneMatch, String
            ifModifiedSince,
                                                   Request request) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestAPIPublisherUtil.getApiPublisher(username)
                    .getLastUpdatedTimeOfAPIThumbnailImage(apiId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of thumbnail of API " + apiId;
            log.error(errorMessage, e);
            return null;
        }
    }

    /**
     * Updates the thumbnail image of an API
     *
     * @param apiId             UUID of API
     * @param fileInputStream   Image data stream
     * @param fileDetail        meta information of the image
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return meta info about the updated thumbnail image
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdThumbnailPost(String apiId, InputStream fileInputStream, FileInfo fileDetail,
                                           String contentType, String ifMatch, String ifUnmodifiedSince, Request request
    ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = APIManagerFactory.getInstance().getAPIProvider(username);
            String existingFingerprint = apisApiIdThumbnailGetFingerprint(apiId, null, null, null, request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            apiPublisher.saveThumbnailImage(apiId, fileInputStream, fileDetail.getFileName());
            String uriString = RestApiConstants.RESOURCE_PATH_THUMBNAIL
                    .replace(RestApiConstants.APIID_PARAM, apiId);

            FileInfoDTO infoDTO = new FileInfoDTO();
            infoDTO.setRelativePath(uriString);
            infoDTO.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
            String newFingerprint = apisApiIdThumbnailGetFingerprint(apiId, null, null, null, request);
            return Response.status(Response.Status.CREATED).entity(infoDTO)
                    .header(HttpHeaders.ETAG, "\"" + newFingerprint + "\"").build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while uploading image" + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Change the lifecycle state of an API
     *
     * @param action             lifecycle action
     * @param apiId              UUID of API
     * @param lifecycleChecklist lifecycle check list items
     * @param ifMatch            If-Match header value
     * @param ifUnmodifiedSince  If-Unmodified-Since header value
     * @param request            msf4j request object
     * @return 200 OK if the operation is succesful
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisChangeLifecyclePost(String action, String apiId, String lifecycleChecklist, String ifMatch,
                                            String ifUnmodifiedSince, Request request
    ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        Map<String, Boolean> lifecycleChecklistMap = new HashMap<>();
        WorkflowResponseDTO response = null;
        try {
            if (lifecycleChecklist != null) {
                String[] checkList = lifecycleChecklist.split(",");
                for (String checkList1 : checkList) {
                    StringTokenizer attributeTokens = new StringTokenizer(checkList1, ":");
                    String attributeName = attributeTokens.nextToken();
                    Boolean attributeValue = Boolean.valueOf(attributeTokens.nextToken());
                    lifecycleChecklistMap.put(attributeName, attributeValue);
                }
            }
            if (action.trim().equals(APIMgtConstants.CHECK_LIST_ITEM_CHANGE_EVENT)) {
                RestAPIPublisherUtil.getApiPublisher(username).updateCheckListItem(apiId, action,
                        lifecycleChecklistMap);
                WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
                //since workflows are not defined for checklist items
                workflowResponse.setWorkflowStatus(WorkflowStatus.APPROVED);
                response = MappingUtil.toWorkflowResponseDTO(workflowResponse);
                return Response.ok().entity(response).build();
            } else {
                WorkflowResponse workflowResponse = RestAPIPublisherUtil.getApiPublisher(username)
                        .updateAPIStatus(apiId, action, lifecycleChecklistMap);
                response = MappingUtil.toWorkflowResponseDTO(workflowResponse);
                //if workflow is in pending state or if the executor sends any httpworklfowresponse (workflow state can 
                //be in either pending or approved state) send back the workflow response 
                if (WorkflowStatus.CREATED == workflowResponse.getWorkflowStatus()) {
                    URI location = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId);
                    return Response.status(Response.Status.ACCEPTED).header(RestApiConstants.LOCATION_HEADER, location)
                            .entity(response).build();
                } else {                    
                    return Response.ok().entity(response).build();
                }                
            }
            
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating lifecycle of API" + apiId + " to " + action;
            Map<String, String> paramList = new HashMap<>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while adding location header in response for api : " + apiId;
            Map<String, String> paramList = new HashMap<>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorHandler errorHandler = ExceptionCodes.LOCATION_HEADER_INCORRECT;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorHandler, paramList);
            log.error(errorMessage, e);
            return Response.status(errorHandler.getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Creates a new version of an API
     *
     * @param newVersion new version
     * @param apiId      UUID of API
     * @param request    msf4j request object
     * @return created new API
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisCopyApiPost(String newVersion, String apiId, Request request) throws NotFoundException {
        APIDTO newVersionedApi;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String newAPIVersionId = apiPublisher.createNewAPIVersion(apiId, newVersion);
            newVersionedApi = MappingUtil.toAPIDto(apiPublisher.getAPIbyUUID(newAPIVersionId));
            return Response.status(Response.Status.CREATED).entity(newVersionedApi).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while create new API version " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            paramList.put(APIMgtConstants.ExceptionsConstants.API_VERSION, newVersion);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList, e);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (IOException e) {
            String errorMessage = "Error while create new API version " + apiId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }
    }

    /**
     * Retrives all APIs that qualifies for the given fitering attributes
     *
     * @param limit       maximum APIs to return
     * @param offset      starting position of the pagination
     * @param query       search query
     * @param accept      Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param request     msf4j request object
     * @return a list of qualifying APIs
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch,
                            Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        APIListDTO apiListDTO = null;
        try {
            List<API> apiList = RestAPIPublisherUtil.getApiPublisher(username).searchAPIs(limit, offset, query);
            List<API> updatedApiList = setAllApiPermissionsForUser(username, apiList);
            apiListDTO = MappingUtil.toAPIListDTO(updatedApiList);
            return Response.ok().entity(apiListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving APIs";
            HashMap<String, String> paramList = new HashMap<String, String>();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList, e);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Set user specific permissions for each API returned during search
     *
     * @param user - the logged in user name
     * @param originalAPIList - original api list returned by search
     * @return the updated list of APIs
     */
    private List<API> setAllApiPermissionsForUser(String user, List<API> originalAPIList) {
        List<API> updatedAPIList = new ArrayList<API>();
        for (API api : originalAPIList) {
            List<String> aggregatedApiPermissions = getAPIPermissionsOfLoggedInUser(user, api);
            if (!aggregatedApiPermissions.isEmpty()) {
                api.setUserSpecificApiPermissions(aggregatedApiPermissions);
                updatedAPIList.add(api);
            }
        }
        return updatedAPIList;
    }

    /**
     * Check if an API available for the given query
     *
     * @param query       search query
     * @param accept      Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param request     msf4j request object
     * @return 200 if an API is found for the query, 404 otherwise
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisHead(String query, String accept, String ifNoneMatch, Request request)
            throws NotFoundException {
        //TODO improve the query parameters searching options
        String username = RestApiUtil.getLoggedInUsername();
        String context = "context";
        String name = "name";
        boolean status;

        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String[] words = query.split(":");

            if (words.length > 1) {
                if (context.equalsIgnoreCase(words[0])) {
                    status = apiPublisher.checkIfAPIContextExists(words[1]);
                } else if (name.equalsIgnoreCase(words[0])) {
                    status = apiPublisher.checkIfAPINameExists(words[1]);
                } else {
                    status = false;
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (status) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while checking status.";
            HashMap<String, String> paramList = new HashMap<String, String>();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList, e);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Import an API from from a swagger definition
     *
     * @param fileInputStream   file content stream
     * @param fileDetail        meta infomation about the file
     * @param url               swagger url
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Imported API
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisImportDefinitionPost(String contentType, InputStream fileInputStream, FileInfo fileDetail,
                                             String url,
                                             String ifMatch, String ifUnmodifiedSince, Request request)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {

            if (fileInputStream != null && url != null) {
                String msg = "Only one of 'file' and 'url' should be specified";
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900314L, msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
            }

            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String uuid = "";
            if (fileInputStream != null) {
                uuid = apiPublisher.addApiFromDefinition(fileInputStream);
            } else if (url != null) {
                URL swaggerUrl = new URL(url);
                HttpURLConnection urlConn = (HttpURLConnection) swaggerUrl.openConnection();
                uuid = apiPublisher.addApiFromDefinition(urlConn);
            } else {
                String msg = "Either 'file' or 'inlineContent' should be specified";
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900314L, msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
            }
            API returnAPI = apiPublisher.getAPIbyUUID(uuid);
            return Response.status(Response.Status.CREATED).entity(MappingUtil.toAPIDto(returnAPI)).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API";
            HashMap<String, String> paramList = new HashMap<String, String>();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (MalformedURLException e) {
            String errorMessage = "Error while constructing swagger url";
            ErrorHandler errorHandler = ExceptionCodes.SWAGGER_URL_MALFORMED;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorHandler);
            log.error(errorMessage, e);
            return Response.status(errorHandler.getHttpStatusCode()).entity(errorDTO).build();
        } catch (IOException e) {
            String errorMessage = "Error while adding new API";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }
    }

    /**
     * Creates a new API
     *
     * @param body        DTO model including the API details
     * @param contentType Content-Type header value
     * @param request     msf4j request object
     * @return Newly created API
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisPost(APIDTO body, String contentType, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            API.APIBuilder apiBuilder = MappingUtil.toAPI(body);
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.addAPI(apiBuilder);
            API returnAPI = apiPublisher.getAPIbyUUID(apiBuilder.getId());
            return Response.status(Response.Status.CREATED).entity(MappingUtil.toAPIDto(returnAPI)).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion();
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_NAME, body.getName());
            paramList.put(APIMgtConstants.ExceptionsConstants.API_VERSION, body.getVersion());
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (JsonProcessingException e) {
            String errorMessage = "Error while adding new API";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        } catch (IOException e) {
            String errorMessage = "Error while adding new API";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }
    }

    /**
     * Remove pending lifecycle state change workflow tasks.
     * 
     * @param apiId api id
     * @param request     msf4j request object
     * @return Empty payload
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response apisApiIdLifecycleLifecyclePendingTaskDelete(String apiId, Request request)
            throws NotFoundException {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.removePendingLifecycleWorkflowTaskForAPI(apiId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while removing pending task for API state change for api " + apiId;
            Map<String, String> paramList = new HashMap<>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
