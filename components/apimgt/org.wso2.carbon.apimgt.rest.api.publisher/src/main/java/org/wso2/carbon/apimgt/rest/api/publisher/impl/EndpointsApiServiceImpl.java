package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.EndpointsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.Request;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date =
        "2017-01-03T15:53:15.692+05:30")
public class EndpointsApiServiceImpl extends EndpointsApiService {
    private static final Logger log = LoggerFactory.getLogger(EndpointsApiServiceImpl.class);

    /**
     * Delete an endpoint by providing its ID
     *
     * @param endpointId        ID of the endpoint
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return 200 OK response if the deletion was successful
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response endpointsEndpointIdDelete(String endpointId, String ifMatch, String ifUnmodifiedSince,
            Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            String existingFingerprint = endpointsEndpointIdGetFingerprint(endpointId, null, null,
                    request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            apiPublisher.deleteEndpoint(endpointId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting  Endpoint : " + endpointId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.ENDPOINT_ID, endpointId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler
                    (), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves an endpoint by providing its ID
     *
     * @param endpointId      ID of the endpoint
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header
     * @param request         msf4j request object
     * @return Endpoint DTO represented by the ID
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response endpointsEndpointIdGet(String endpointId, String ifNoneMatch, String ifModifiedSince,
            Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            Endpoint endPoint = apiPublisher.getEndpoint(endpointId);
            if (endPoint == null) {
                String msg = "Endpoint not found " + endpointId;
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(ExceptionCodes.ENDPOINT_NOT_FOUND);
                return Response.status(ExceptionCodes.ENDPOINT_NOT_FOUND.getHttpStatusCode()).entity(errorDTO).build();
            }

            String existingFingerprint = endpointsEndpointIdGetFingerprint(endpointId, ifNoneMatch,
                    ifModifiedSince, request);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            return Response.ok().entity(MappingUtil.toEndPointDTO(endPoint))
                    .header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"").build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while get  Endpoint : " + endpointId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.ENDPOINT_ID, endpointId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler
                    (), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (IOException e) {
            String errorMessage = "Error while Converting Endpoint Security Details in Endpoint :" + endpointId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves the fingerprint of the endpoint identified by the UUID
     *
     * @param endpointId        ID of the endpoint
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return fingerprint of the endpoint
     */
    public String endpointsEndpointIdGetFingerprint(String endpointId, String ifMatch, String ifUnmodifiedSince,
            Request request) {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            String lastUpdatedTime = RestAPIPublisherUtil.getApiPublisher(username).getLastUpdatedTimeOfEndpoint(
                    endpointId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage =
                    "Error while retrieving last updated time of endpoint " + endpointId;
            log.error(errorMessage, e);
            return null;
        }
    }

    /**
     * Updates an existing endpoint
     *
     * @param endpointId        ID of the endpoint
     * @param body              Updated endpoint details
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return updated endpoint
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response endpointsEndpointIdPut(String endpointId, EndPointDTO body, String ifMatch,
            String ifUnmodifiedSince, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            Endpoint endpoint = MappingUtil.toEndpoint(body);
            Endpoint retrievedEndpoint = apiPublisher.getEndpoint(endpointId);
            if (retrievedEndpoint == null) {
                String msg = "Endpoint not found " + endpointId;
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(ExceptionCodes.ENDPOINT_NOT_FOUND);
                return Response.status(ExceptionCodes.ENDPOINT_NOT_FOUND.getHttpStatusCode()).entity(errorDTO).build();
            }

            String existingFingerprint = endpointsEndpointIdGetFingerprint(endpointId, null, null, request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            Endpoint updatedEndpint = new Endpoint.Builder(endpoint).id(endpointId).build();
            apiPublisher.updateEndpoint(updatedEndpint);
            Endpoint updatedEndpoint = apiPublisher.getEndpoint(endpointId);
            String newFingerprint = endpointsEndpointIdGetFingerprint(endpointId, null, null, request);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + newFingerprint + "\"")
                    .entity(MappingUtil.toEndPointDTO(updatedEndpoint)).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while getting the endpoint :" + endpointId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (JsonProcessingException e) {
            String errorMessage = "Error while Converting Endpoint Security Details in Endpoint :" + endpointId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        } catch (IOException e) {
            String errorMessage = "Error while Converting Endpoint Security Details in Endpoint :" + endpointId;
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }
    }

    /**
     * Retrieve all endpoints available
     *
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header
     * @param request         msf4j request object
     * @return A list of endpoints avaliable
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response endpointsGet(String ifNoneMatch, String ifModifiedSince, Request request)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            List<Endpoint> endpointList = apiPublisher.getAllEndpoints();
            EndPointListDTO endPointListDTO = new EndPointListDTO();
            for (Endpoint endpoint : endpointList) {
                endPointListDTO.addListItem(MappingUtil.toEndPointDTO(endpoint));
            }
            endPointListDTO.setCount(endPointListDTO.getList().size());
            return Response.ok().entity(endPointListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while get All Endpoint";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (IOException e) {
            String errorMessage = "Error while Converting Endpoint Security Details in Endpoint";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }
    }

    @Override
    public Response endpointsHead(String name, String ifNoneMatch, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        boolean status;
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            if (apiPublisher.isEndpointExist(name)) {
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
     * Adds a new Endpoint
     *
     * @param body            Endpoint details to be added
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header
     * @param request         msf4j request object
     * @return Newly created endpoint details as the payload
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response endpointsPost(EndPointDTO body, String ifNoneMatch, String ifModifiedSince,
            Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            Endpoint endpoint = MappingUtil.toEndpoint(body);
            String endpointId = apiPublisher.addEndpoint(endpoint);
            Endpoint retrievedEndpoint = apiPublisher.getEndpoint(endpointId);
            return Response.status(Response.Status.CREATED).entity(MappingUtil.toEndPointDTO(retrievedEndpoint))
                    .build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while get All Endpoint";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (JsonProcessingException e) {
            String errorMessage = "Error while Converting Endpoint Security Details in Endpoint";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        } catch (IOException e) {
            String errorMessage = "Error while Converting Endpoint Security Details in Endpoint ";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        }
    }
}
