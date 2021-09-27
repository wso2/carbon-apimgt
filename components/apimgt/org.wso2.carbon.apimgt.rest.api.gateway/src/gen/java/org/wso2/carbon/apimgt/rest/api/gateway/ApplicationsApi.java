package org.wso2.carbon.apimgt.rest.api.gateway;

import org.wso2.carbon.apimgt.rest.api.gateway.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.impl.ApplicationsApiServiceImpl;
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
@Path("/applications")

@Api(description = "the applications API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ApplicationsApi  {

  @Context MessageContext securityContext;

ApplicationsApiService delegate = new ApplicationsApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the applications information by providing the application name or uuid.", notes = "This operation is used to get the application information from storage. If the Tenant domain is not provided carbon.super will be picked as the Tenant domain. ", response = ApplicationListDTO.class, tags={ "Get Application Info" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Applications successfully retrieved from the storage. ", response = ApplicationListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response applicationsGet( @ApiParam(value = "Name of the Application ")  @QueryParam("name") String name,  @ApiParam(value = "UUID of the Application ")  @QueryParam("uuid") String uuid,  @ApiParam(value = "Tenant Domain of the Application ")  @QueryParam("tenantDomain") String tenantDomain) throws APIManagementException{
        return delegate.applicationsGet(name, uuid, tenantDomain, securityContext);
    }
}
