package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DeploymentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.DeploymentsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.DeploymentsApiServiceImpl;
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
@Path("/deployments")

@Api(description = "the deployments API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class DeploymentsApi  {

  @Context MessageContext securityContext;

DeploymentsApiService delegate = new DeploymentsApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve Deployment Environments Details", notes = "This operation can be used to retrieve cloud clusters information defines in tenant-conf.json file.  With that you can deploy an API to selected cloud environments. ", response = DeploymentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Deployments" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the list of deployment environments information in the body. ", response = DeploymentListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response deploymentsGet() throws APIManagementException{
        return delegate.deploymentsGet(securityContext);
    }
}
