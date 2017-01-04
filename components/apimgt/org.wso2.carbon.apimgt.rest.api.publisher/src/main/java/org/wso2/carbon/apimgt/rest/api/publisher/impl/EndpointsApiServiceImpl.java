package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.EndpointsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date =
        "2017-01-03T15:53:15.692+05:30")
public class EndpointsApiServiceImpl extends EndpointsApiService {
    private static final Logger log = LoggerFactory.getLogger(EndpointsApiServiceImpl.class);

    @Override
    public Response endpointsEndpointIdDelete(String endpointId
            , String ifMatch
            , String ifUnmodifiedSince
    ) throws NotFoundException {
        String username = "";
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.deleteEndpoint(endpointId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting  Endpoint : " + endpointId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.ENDPOINT_ID, endpointId);
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler
                    (), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response endpointsEndpointIdGet(String endpointId
    ) throws NotFoundException {
        String username = "";
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            Endpoint endPoint = apiPublisher.getEndpoint(endpointId);
            if (endPoint != null) {
                return Response.ok().entity(MappingUtil.toEndPointDTO(endPoint)).build();
            } else {
                String msg = "Endpoint not found " + endpointId;
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(ExceptionCodes.ENDPOINT_NOT_FOUND);
                log.error(msg);
                return Response.status(ExceptionCodes.ENDPOINT_NOT_FOUND.getHttpStatusCode()).entity(errorDTO).build();
            }

        } catch (APIManagementException e) {
            String errorMessage = "Error while get  Endpoint : " + endpointId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.ENDPOINT_ID, endpointId);
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler
                    (), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response endpointsEndpointIdPut(String endpointId
            , EndPointDTO body
            , String contentType
            , String ifMatch
            , String ifUnmodifiedSince
    ) throws NotFoundException {
        String username = "";
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            Endpoint endpoint = MappingUtil.toEndpoint(body);
            Endpoint retrievedEndpoint = apiPublisher.getEndpoint(endpointId);
            if (retrievedEndpoint != null) {
                apiPublisher.updateEndpoint(endpoint);
                Endpoint updatedEndpoint = apiPublisher.getEndpoint(endpointId);
                return Response.ok().entity(MappingUtil.toEndPointDTO(updatedEndpoint)).build();
            } else {
                String msg = "Endpoint not found " + endpointId;
                log.error(msg);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(ExceptionCodes.ENDPOINT_NOT_FOUND);
                log.error(msg);
                return Response.status(ExceptionCodes.ENDPOINT_NOT_FOUND.getHttpStatusCode()).entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while get All Endpoint";
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response endpointsGet() throws NotFoundException {
        String username = "";
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
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response endpointsPost(EndPointDTO body
            , String contentType
    ) throws NotFoundException {
        String username = "";
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            Endpoint endpoint = MappingUtil.toEndpoint(body);
            String endpointId = apiPublisher.addEndpoint(endpoint);
            Endpoint retrievedEndpoint = apiPublisher.getEndpoint(endpointId);
            return Response.status(Response.Status.CREATED).entity(MappingUtil.toEndPointDTO(retrievedEndpoint))
                    .build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while get All Endpoint";
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
