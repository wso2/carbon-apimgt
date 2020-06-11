package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.ApplicationsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;

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

    @GET
    @Path("/{applicationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of an application", notes = "This operation can be used to retrieve details of an individual application specifying the application id in the URI.\n", response = ApplicationDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nApplication returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested application does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response applicationsApplicationIdGet(@ApiParam(value = "**Application Identifier** consisting of the UUID of the Application.\n",required=true ) @PathParam("applicationId")  String applicationId,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resource (Will be supported in future).\n"  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "Validator for conditional requests; based on Last Modified header of the\nformerly retrieved variant of the resource (Will be supported in future).\n"  )@HeaderParam("If-Modified-Since") String ifModifiedSince)
    {
    return delegate.applicationsApplicationIdGet(applicationId,accept,ifNoneMatch,ifModifiedSince);
    }
}

