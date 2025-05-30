package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantAPIPayloadResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantChatQueryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantChatResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantGenAPIPayloadDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantRegenerateSpecDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.DesignAssistantApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.DesignAssistantApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.inject.Inject;

import io.swagger.annotations.*;
import java.io.InputStream;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
@Path("/design-assistant")

@Api(description = "the design-assistant API")




public class DesignAssistantApi  {

  @Context MessageContext securityContext;

DesignAssistantApiService delegate = new DesignAssistantApiServiceImpl();


    @POST
    @Path("/generate-api-payload")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create API Payload with API Design Assistant", notes = "Creates an API payload using the stored specification. ", response = DesignAssistantAPIPayloadResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Design Assistant",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "API payload successfully created.", response = DesignAssistantAPIPayloadResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response designAssistantApiPayloadGen(@ApiParam(value = "Input for API payload creation" ,required=true) DesignAssistantGenAPIPayloadDTO designAssistantGenAPIPayloadDTO) throws APIManagementException{
        return delegate.designAssistantApiPayloadGen(designAssistantGenAPIPayloadDTO, securityContext);
    }

    @POST
    @Path("/chat")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Generate API Specifications with API Design Assistant", notes = "Generates API specifications based on natural language input.  Key features: - Converts text descriptions into structured API specifications - Provides QoS suggestions and other improvements - Supports session-based API generation ", response = DesignAssistantChatResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Design Assistant",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful API specification generation", response = DesignAssistantChatResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response designAssistantChat(@ApiParam(value = "Input for API specification generation" ,required=true) DesignAssistantChatQueryDTO designAssistantChatQueryDTO) throws APIManagementException{
        return delegate.designAssistantChat(designAssistantChatQueryDTO, securityContext);
    }

    @POST
    @Path("/regenerate-spec")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Regenerate API Specifications without governance rule violations with API Design Assistant", notes = "Regenerates API specifications without governance rule violations. ", response = DesignAssistantRegenerateSpecDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "API Design Assistant" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful API specification generation", response = DesignAssistantRegenerateSpecDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class) })
    public Response designAssistantRegenerateSpec(@ApiParam(value = "Input for API specification regeneration" ,required=true) DesignAssistantChatQueryDTO designAssistantChatQueryDTO) throws APIManagementException{
        return delegate.designAssistantRegenerateSpec(designAssistantChatQueryDTO, securityContext);
    }
}
