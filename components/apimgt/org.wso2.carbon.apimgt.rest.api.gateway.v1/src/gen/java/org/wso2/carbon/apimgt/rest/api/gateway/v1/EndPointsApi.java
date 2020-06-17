package org.wso2.carbon.apimgt.rest.api.gateway.v1;

import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.DeployResponseDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.EndPointsApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.impl.EndPointsApiServiceImpl;
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
@Path("/end-points")

@Api(description = "the end-points API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class EndPointsApi  {

  @Context MessageContext securityContext;

EndPointsApiService delegate = new EndPointsApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get end-points from the storage for the API", notes = "This operation is used to get the end-points from the storage ", response = DeployResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API")
        })
    }, tags={ "Get API Artifact" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. End-points successfully retrieved from the storage for the API. ", response = DeployResponseDTO.class),
        @ApiResponse(code = 202, message = "Accepted. The request has been accepted. ", response = DeployResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response endPointsGet( @NotNull @ApiParam(value = "Name of the API ",required=true)  @QueryParam("apiName") String apiName,  @NotNull @ApiParam(value = "Label of the API Gateway ",required=true)  @QueryParam("label") String label,  @NotNull @ApiParam(value = "**API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API I. Should be formatted as **provider-name-version**. ",required=true)  @QueryParam("apiId") String apiId) throws APIManagementException{
        return delegate.endPointsGet(apiName, label, apiId, securityContext);
    }
}
