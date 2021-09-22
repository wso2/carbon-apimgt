package org.wso2.carbon.apimgt.rest.api.gateway;

import org.wso2.carbon.apimgt.rest.api.gateway.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.ApiLoggingApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.impl.ApiLoggingApiServiceImpl;
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
@Path("/api-logging")

@Api(description = "the api-logging API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ApiLoggingApi  {

  @Context MessageContext securityContext;

ApiLoggingApiService delegate = new ApiLoggingApiServiceImpl();


    @DELETE
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete log enabled API data ", notes = "", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Successfully deleted", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Request API context is not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while deleting API details", response = ErrorDTO.class) })
    public Response apiLoggingDelete( @ApiParam(value = "API context with the version (eg :pizzashack/1.0.0). If this is not provided all API details will be deleted. ")  @QueryParam("context") String context) throws APIManagementException{
        return delegate.apiLoggingDelete(context, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "GET log enabled API data ", notes = "", response = APIListDTO.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Logs enabled API details", response = APIListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Request API resource or external store Ids not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while retrieving API data to be logged", response = ErrorDTO.class) })
    public Response apiLoggingGet( @ApiParam(value = "API context with the version (eg: pizzashack/1.0.0). If this is not provided, then it will retrieve all logs enabled APIs with log levels. ")  @QueryParam("context") String context) throws APIManagementException{
        return delegate.apiLoggingGet(context, securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "POST logging data of the API with its context template and version ", notes = "This operation enables you to provide the API context template(context/version) with the log level (headers, body or all). You should either provide a payload or context and logLevel as query parameters with this API request. ", response = APIListDTO.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully Enabled the logs for the API", response = APIListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Request API resource or external store Ids not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while configuring API to be logged", response = ErrorDTO.class) })
    public Response apiLoggingPost(@ApiParam(value = "context and the logLevel can be provided as a payload. This is useful if you want to enable logs for multiple APIs at once. " ) APIListDTO payload,  @ApiParam(value = "API context ( context/version eg: pizzashack/1.0.0) ")  @QueryParam("context") String context,  @ApiParam(value = "logLevel of the API. \"all\",\"headers\" or \"body\" ", allowableValues="all, headers, body")  @QueryParam("logLevel") String logLevel) throws APIManagementException{
        return delegate.apiLoggingPost(payload, context, logLevel, securityContext);
    }
}
