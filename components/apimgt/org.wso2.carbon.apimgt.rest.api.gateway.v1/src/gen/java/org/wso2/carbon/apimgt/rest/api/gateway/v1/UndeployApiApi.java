package org.wso2.carbon.apimgt.rest.api.gateway.v1;

import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.DeployResponseDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.UndeployApiApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.impl.UndeployApiApiServiceImpl;
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
@Path("/undeploy-api")

@Api(description = "the undeploy-api API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class UndeployApiApi  {

  @Context MessageContext securityContext;

UndeployApiApiService delegate = new UndeployApiApiServiceImpl();


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Uneploy the API in the gateway", notes = "This operation is used to undeploy an API in the gateway. If the Tenant domain is not provided carbon.super will be picked as the Tenant domain. ", response = DeployResponseDTO.class, tags={ "Undeploy API" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API successfully undeployed from the Gateway. ", response = DeployResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response undeployApiPost( @NotNull @ApiParam(value = "Name of the API ",required=true)  @QueryParam("apiName") String apiName,  @NotNull @ApiParam(value = "version of the API ",required=true)  @QueryParam("version") String version,  @ApiParam(value = "Tenant Domain of the API ")  @QueryParam("tenantDomain") String tenantDomain) throws APIManagementException{
        return delegate.undeployApiPost(apiName, version, tenantDomain, securityContext);
    }
}
