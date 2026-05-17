package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.FederatedApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.FederatedApisApiServiceImpl;
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
@Path("/federated-apis")

@Api(description = "the federated-apis API")




public class FederatedApisApi  {

  @Context MessageContext securityContext;

FederatedApisApiService delegate = new FederatedApisApiServiceImpl();


    @POST
    @Path("/discover")
    
    
    @ApiOperation(value = "Discover federated APIs", notes = "Discover federated APIs from a specified federated gateway and return NEW and UPDATE", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "Federated APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class) })
    public Response discoverFederatedAPIs( @NotNull @ApiParam(value = "Name of the environment/gateway",required=true)  @QueryParam("environment") String environment) throws APIManagementException{
        return delegate.discoverFederatedAPIs(environment, securityContext);
    }

    @POST
    @Path("/import")
    @Consumes({ "application/json" })
    
    @ApiOperation(value = "Import discovered APIs", notes = "Import explicitly selected new APIs", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "Federated APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class) })
    public Response importFederatedAPIs( @NotNull @ApiParam(value = "Name of the environment/gateway",required=true)  @QueryParam("environment") String environment, @ApiParam(value = "List of API IDs to import" ,required=true) List<String> requestBody) throws APIManagementException{
        return delegate.importFederatedAPIs(environment, requestBody, securityContext);
    }

    @POST
    @Path("/update")
    @Consumes({ "application/json" })
    
    @ApiOperation(value = "Update existing federated APIs", notes = "Update explicitly selected APIs from a specified federated gateway", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "Federated APIs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Void.class) })
    public Response updateFederatedAPIs( @NotNull @ApiParam(value = "Name of the environment/gateway",required=true)  @QueryParam("environment") String environment, @ApiParam(value = "List of API IDs to update" ,required=true) List<String> requestBody) throws APIManagementException{
        return delegate.updateFederatedAPIs(environment, requestBody, securityContext);
    }
}
