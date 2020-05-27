package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.ApplicationsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.ApplicationsApiServiceImpl;
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

@Produces({ "application/json" })


public class ApplicationsApi  {

  @Context MessageContext securityContext;

ApplicationsApiService delegate = new ApplicationsApiServiceImpl();


    @GET
    @Path("/{applicationId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get an application", notes = "This will provide access to application in database. ", response = Object.class, tags={ "Subscription Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Application in the database", response = Object.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response applicationsApplicationIdGet(@ApiParam(value = "Application Identifier consisting of the UUID of the Application. ",required=true) @PathParam("applicationId") Integer applicationId) throws APIManagementException{
        return delegate.applicationsApplicationIdGet(applicationId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all applications", notes = "This will provide access to applications in database. ", response = ApplicationDTO.class, responseContainer = "List", tags={ "Subscription Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of applications in the database", response = ApplicationDTO.class, responseContainer = "List"),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response applicationsGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.applicationsGet(xWSO2Tenant, securityContext);
    }
}
