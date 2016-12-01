package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.publisher.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:47:43.416+05:30")
public class ApisApiServiceImpl extends ApisApiService {
    private static final Logger log = LoggerFactory.getLogger(ApisApiServiceImpl.class);

    @Override
    public Response apisApiIdDelete(String apiId
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            RestAPIPublisherUtil.getApiPublisher(username).deleteAPI(apiId);
            return Response.ok().build();
        } catch (APIManagementException e) {
//Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the
// resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while deleting API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

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
    public Response apisApiIdDocumentsDocumentIdContentPost(String apiId
, String documentId
, String contentType
, InputStream fileInputStream, FileInfo fileDetail
, String inlineContent
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdDelete(String apiId
, String documentId
, String ifMatch
, String ifUnmodifiedSince
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
    public Response apisApiIdDocumentsDocumentIdPut(String apiId
, String documentId
, DocumentDTO body
, String contentType
, String ifMatch
, String ifUnmodifiedSince
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
    @Override
    public Response apisApiIdDocumentsPost(String apiId
, DocumentDTO body
, String contentType
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response apisApiIdGatewayConfigGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String gatewayConfig = apiPublisher.getApiGatewayConfig(apiId);
            return Response.ok().entity(gatewayConfig).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving gateway config of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response apisApiIdGatewayConfigPut(String apiId, String gatewayConfig, String contentType, String ifMatch,
            String ifUnmodifiedSince) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.updateApiGatewayConfig(apiId, gatewayConfig);
            String apiGatewayConfig = apiPublisher.getApiGatewayConfig(apiId);
            return Response.ok().entity(apiGatewayConfig).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response apisApiIdGet(String apiId
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            if (RestAPIPublisherUtil.getApiPublisher(username).checkIfAPIExists(apiId)){
                APIDTO apidto = MappingUtil.toAPIDto(RestAPIPublisherUtil.getApiPublisher(username).getAPIbyUUID(apiId));
                return Response.ok().entity(apidto).build();
            }else{
                RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }
    @Override
    public Response apisApiIdPut(String apiId
, APIDTO body
, String contentType
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.updateAPI(MappingUtil.toAPI(body));
            APIDTO apidto = MappingUtil.toAPIDto(apiPublisher.getAPIbyUUID(apiId));
            return Response.ok().entity(apidto).build();
        } catch (APIManagementException e) {

            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while updating API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }
    @Override
    public Response apisApiIdSwaggerGet(String apiId
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String swagger = apiPublisher.getSwagger20Definition(apiId);
            return Response.ok().entity(swagger).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving swagger of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;

    }
    @Override
    public Response apisApiIdSwaggerPut(String apiId
, String apiDefinition
, String contentType
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.saveSwagger20Definition(apiId, apiDefinition);
            String apiSwagger = apiPublisher.getSwagger20Definition(apiId);
            return Response.ok().entity(apiSwagger).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }
    @Override
    public Response apisApiIdThumbnailGet(String apiId
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            InputStream imageInputStream = apiPublisher.getThumbnailImage(apiId);
            if (imageInputStream != null) {
                return Response.ok(imageInputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE).header
                        ("Content-Disposition", "attachment; filename=\"thename.jpg\"").build();
            } else {
                return Response.noContent().build();
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving thumbnail of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }
    @Override
    public Response apisApiIdThumbnailPost(String apiId
, InputStream fileInputStream, FileInfo fileDetail
, String contentType
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = APIManagerFactory.getInstance().getAPIProvider(username);
            apiPublisher.saveThumbnailImage(apiId, fileInputStream, contentType);
            String uriString = RestApiConstants.RESOURCE_PATH_THUMBNAIL
                    .replace(RestApiConstants.APIID_PARAM, apiId);
//            URI uri = new URI(uriString);
            FileInfoDTO infoDTO = new FileInfoDTO();
            infoDTO.setRelativePath(uriString);
            infoDTO.setMediaType(MediaType.APPLICATION_OCTET_STREAM);
/*
            return Response.created(uri).entity(infoDTO).build();
*/
            return Response.status(Response.Status.CREATED).entity(infoDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving thumbnail of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
//        catch (URISyntaxException e) {
//            String errorMessage = "Error while retrieving thumbnail location of API: " + apiId;
//            RestApiUtil.handleInternalServerError(errorMessage, e, log);
//        }
        return null;
    }

    @Override
    public Response apisChangeLifecyclePost(String action
            , String apiId
            , String lifecycleChecklist
            , String ifMatch
            , String ifUnmodifiedSince
    ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        Map<String, Boolean> lifecycleChecklistMap = new HashMap<>();
        try {
            if (lifecycleChecklist != null) {
                String[] checkList = lifecycleChecklist.split(",");
                for (String checkList1 : checkList) {
                    String attributeName = new StringTokenizer(checkList1, ":").nextToken();
                    Boolean attributeValue = Boolean.valueOf(new StringTokenizer(checkList1, ":").nextToken());
                    lifecycleChecklistMap.put(attributeName, attributeValue);
                }
            }
            RestAPIPublisherUtil.getApiPublisher(username).updateAPIStatus(apiId, action, lifecycleChecklistMap);
            return Response.ok().build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while updating lifecycle of API " + apiId, e, log);
            }
        }
        return null;
    }
    @Override
    public Response apisCopyApiPost(String newVersion
, String apiId
 ) throws NotFoundException {
      //  URI newVersionedApiUri;
        APIDTO newVersionedApi;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String newAPIVersionId = apiPublisher.createNewAPIVersion(apiId, newVersion);
            newVersionedApi = MappingUtil.toAPIDto(apiPublisher.getAPIbyUUID(newAPIVersionId));
            //This URI used to set the location header of the POST response
//            newVersionedApiUri =
//                    new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + newVersionedApi.getId());
//            // return Response.created(newVersionedApiUri).entity(newVersionedApi).build();
            return Response.status(Response.Status.CREATED).entity(newVersionedApi).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceAlreadyExists(e)) {
                String errorMessage = "Requested new version " + newVersion + " of API " + apiId + " already exists";
                RestApiUtil.handleResourceAlreadyExistsError(errorMessage, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
                // existence of the resource
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while copying API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
//        catch (URISyntaxException e) {
//            String errorMessage = "Error while retrieving API location of " + apiId;
//            RestApiUtil.handleInternalServerError(errorMessage, e, log);
//        }
        return null;
    }

    @Override
    public Response apisGet(Integer limit
, Integer offset
, String query
, String accept
, String ifNoneMatch
 ) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        APIListDTO apiListDTO = null;
        try {
            apiListDTO = MappingUtil.toAPIListDTO(RestAPIPublisherUtil.getApiPublisher(username).searchAPIs
                    (limit,offset,query));
            return Response.ok().entity(apiListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving APIs";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response apisHead(String query, String accept, String ifNoneMatch) throws NotFoundException {
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
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response apisPost(APIDTO body
, String contentType
 ) throws NotFoundException {
     String username = RestApiUtil.getLoggedInUsername();
        API.APIBuilder apiBuilder = MappingUtil.toAPI(body);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.addAPI(apiBuilder);
            API returnAPI = apiPublisher.getAPIbyUUID(apiBuilder.getId());
//          URI  createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + returnAPI.getId());
//            return Response.created(createdApiUri).entity(MappingUtil.toAPIDto(returnAPI)).build();
            return Response.status(Response.Status.CREATED).entity(MappingUtil.toAPIDto(returnAPI)).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion();
            HashMap<String, String> paramList = new HashMap<String, String>();

            paramList.put(APIMgtConstants.ExceptionsConstants.API_NAME, body.getName());
            paramList.put(APIMgtConstants.ExceptionsConstants.API_VERSION, body.getVersion());

            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler().getErrorMessage(),
                    e.getErrorHandler().getErrorCode(), e.getErrorHandler().getErrorDescription(),paramList);
            log.error(errorMessage,e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
//        catch (URISyntaxException e) {
//            String errorMessage = "Error while retrieving API location : " + body.getProvider() + "-" +
//                    body.getName() + "-" + body.getVersion();
//            RestApiUtil.handleInternalServerError(errorMessage, e, log);
//        }

    }
}
