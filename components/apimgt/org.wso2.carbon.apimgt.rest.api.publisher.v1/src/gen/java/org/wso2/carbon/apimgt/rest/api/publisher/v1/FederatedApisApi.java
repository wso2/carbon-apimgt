package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.InlineResponse2001DTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.InlineResponse200DTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.InlineResponse202DTO;
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
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Trigger async discovery of federated APIs", notes = "Submits a discovery task for the specified gateway environment and immediately returns a task ID. If a discovery task for the same environment is already running, the existing task ID is returned (de-duplication). Poll GET /federated-apis/status/{taskId} to retrieve the result. ", response = InlineResponse202DTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "Federated APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 202, message = "Accepted. Discovery task submitted (or already in progress).", response = InlineResponse202DTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response discoverFederatedAPIs( @NotNull @ApiParam(value = "Name of the environment/gateway",required=true)  @QueryParam("environment") String environment) throws APIManagementException{
        return delegate.discoverFederatedAPIs(environment, securityContext);
    }

    @GET
    @Path("/cached")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve cached federated APIs discovery results", notes = "Returns previously cached discovery results from the DB for the given environment, WITHOUT triggering a new gateway call. Includes lastDiscoveredAt timestamp.", response = InlineResponse2001DTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "Federated APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Cached API discovery results and last discovered timestamp.", response = InlineResponse2001DTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getCachedDiscoveryResults( @NotNull @ApiParam(value = "Name of the environment/gateway",required=true)  @QueryParam("environment") String environment) throws APIManagementException{
        return delegate.getCachedDiscoveryResults(environment, securityContext);
    }

    @GET
    @Path("/status/{taskId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Poll the status of a discovery task", notes = "Returns the current status (PENDING, COMPLETED, or FAILED) and the discovered API list when completed.", response = InlineResponse200DTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "Federated APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Task status and optional result payload.", response = InlineResponse200DTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getDiscoveryTaskStatus(@ApiParam(value = "The task ID returned by POST /federated-apis/discover",required=true) @PathParam("taskId") String taskId) throws APIManagementException{
        return delegate.getDiscoveryTaskStatus(taskId, securityContext);
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
