package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APISecurityAuditInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.AuditapiApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.AuditapiApiServiceImpl;

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
@Path("/auditapi")

@Api(description = "the auditapi API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class AuditapiApi  {

  @Context MessageContext securityContext;

AuditapiApiService delegate = new AuditapiApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve the Security Audit Report of the Audit API", notes = "Retrieve the Security Audit Report of the Audit API ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The Security Audit Report has been returned. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The Security Audit Report was not found. ", response = ErrorDTO.class) })
    public Response auditapiGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) {
        return delegate.auditapiGet(apiId, accept, securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create new Security Audit API", notes = "Create new Security Audit API ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. UUID returned ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Created. The Audit API was not created. ", response = ErrorDTO.class) })
    public Response auditapiPost(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "API object that needs to be added " ,required=true) APISecurityAuditInfoDTO body, @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) {
        return delegate.auditapiPost(apiId, body, accept, securityContext);
    }

    @PUT
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update Swagger Definition in Security Audit API", notes = "Update Swagger Definition in Security Audit API ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Swagger Definition has been updated ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The Audit API was not found. ", response = ErrorDTO.class) })
    public Response auditapiPut(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) {
        return delegate.auditapiPut(apiId, accept, securityContext);
    }
}
