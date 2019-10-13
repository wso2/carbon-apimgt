package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.ScopesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ScopesApiServiceImpl;
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
@Path("/scopes")

@Api(description = "the scopes API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ScopesApi  {

  @Context MessageContext securityContext;

ScopesApiService delegate = new ScopesApiServiceImpl();


    @HEAD
    @Path("/{name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Check given scope name is already exist", notes = "Using this operation, user can check a given scope name exists or not. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "scope" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested scope name exists.", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested scope name does not exist.", response = Void.class) })
    public Response validateScope(@ApiParam(value = "Scope name ",required=true) @PathParam("name") String name) throws APIManagementException{
        return delegate.validateScope(name, securityContext);
    }
}
