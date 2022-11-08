package org.wso2.carbon.apimgt.rest.api.devops;

import org.wso2.carbon.apimgt.rest.api.devops.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.LoggingApiInputDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.LoggingApiOutputListDTO;
import org.wso2.carbon.apimgt.rest.api.devops.TenantLogsApiService;
import org.wso2.carbon.apimgt.rest.api.devops.impl.TenantLogsApiServiceImpl;
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
@Path("/tenant-logs")

@Api(description = "the tenant-logs API")




public class TenantLogsApi  {

  @Context MessageContext securityContext;

TenantLogsApiService delegate = new TenantLogsApiServiceImpl();


    @GET
    @Path("/{tenantId}/apis/{apiId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "GET log enabled API data ", notes = "", response = LoggingApiOutputListDTO.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Logs enabled API details", response = LoggingApiOutputListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Request API resource or external store Ids not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while retrieving API data to be logged", response = ErrorDTO.class) })
    public Response tenantLogsTenantIdApisApiIdGet(@ApiParam(value = "Tenant ID ",required=true) @PathParam("tenantId") String tenantId, @ApiParam(value = "The API ID for the logging operation ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.tenantLogsTenantIdApisApiIdGet(tenantId, apiId, securityContext);
    }

    @PUT
    @Path("/{tenantId}/apis/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Set logging levels of the API with its api ID ", notes = "This operation enables you to provide the API context template(context/version) with the log level (OFF|BASIC|STANDARD|FULL). You should either provide the api ID and the api log level. ", response = LoggingApiOutputListDTO.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully Enabled the logs for the API", response = LoggingApiOutputListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Request API resource or external store Ids not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while configuring API to be logged", response = ErrorDTO.class) })
    public Response tenantLogsTenantIdApisApiIdPut(@ApiParam(value = "Tenant ID ",required=true) @PathParam("tenantId") String tenantId, @ApiParam(value = "The API ID for the logging operation ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "The logLeve is provided as a payload. " ) LoggingApiInputDTO loggingApiInputDTO) throws APIManagementException{
        return delegate.tenantLogsTenantIdApisApiIdPut(tenantId, apiId, loggingApiInputDTO, securityContext);
    }

    @GET
    @Path("/{tenantId}/apis/")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "GET log level of APIs ", notes = "", response = LoggingApiOutputListDTO.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Log level of APIs", response = LoggingApiOutputListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Request API resource or external store Ids not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while retrieving API data to be logged", response = ErrorDTO.class) })
    public Response tenantLogsTenantIdApisGet(@ApiParam(value = "Tenant ID ",required=true) @PathParam("tenantId") String tenantId,  @ApiParam(value = "Log level of the APIs ")  @QueryParam("log-level") String logLevel) throws APIManagementException{
        return delegate.tenantLogsTenantIdApisGet(tenantId, logLevel, securityContext);
    }
}
