package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.factories.ApplicationsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/applications")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/applications", description = "the applications API")
public class ApplicationsApi  {

   private final ApplicationsApiService delegate = ApplicationsApiServiceFactory.getApplicationsApi();

    @POST
    @Path("/{applicationId}/change-owner")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Change Application Owner", notes = "This operation is used to change the owner of an Application.\nIn order to change the owner of an application, we need to pass the new application owner as a query parameter\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nApplication owner changed successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Application does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response applicationsApplicationIdChangeOwnerPost(@ApiParam(value = "",required=true) @QueryParam("owner") String owner,
    @ApiParam(value = "Application UUID\n",required=true ) @PathParam("applicationId") String applicationId)
    {
    return delegate.applicationsApplicationIdChangeOwnerPost(owner,applicationId);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve/Search applications\n", notes = "This operation can be used to retrieve list of applications that is belonged to the given user, If no user\nis provided then the application for the user associated with the provided access token will be returned.\n", response = ApplicationListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nApplication list returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported.\n") })

    public Response applicationsGet(@ApiParam(value = "username of the application creator\n") @QueryParam("user") String user,
    @ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource (Will be supported in future).\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Whether to get the applications of all the users of all tenants\n") @QueryParam("tenantDomain")  String tenantDomain)
    {
    return delegate.applicationsGet(user,limit,offset,accept,ifNoneMatch,tenantDomain);
    }
}

