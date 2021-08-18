package org.wso2.carbon.apimgt.rest.api.gateway.v1;

import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.ApiApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.impl.ApiApiServiceImpl;
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
@Path("/api")

@Api(description = "the api API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ApiApi  {

  @Context MessageContext securityContext;

ApiApiService delegate = new ApiApiServiceImpl();


    @GET
    @Path("/info")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the subscription information of an api by providing the api name, version and context.", notes = "This operation is used to get the subscription information of an API from storage. If the Tenant domain is not provided carbon.super will be picked as the Tenant domain. ", response = APIInfoDTO.class, tags={ "Get API Info" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. End-points successfully retrieved from the storage for the API. ", response = APIInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response apiInfoGet( @NotNull @ApiParam(value = "Context of the API ",required=true)  @QueryParam("context") String context,  @NotNull @ApiParam(value = "version of the API ",required=true)  @QueryParam("version") String version,  @ApiParam(value = "Tenant Domain of the API ")  @QueryParam("tenantDomain") String tenantDomain) throws APIManagementException{
        return delegate.apiInfoGet(context, version, tenantDomain, securityContext);
    }
}
