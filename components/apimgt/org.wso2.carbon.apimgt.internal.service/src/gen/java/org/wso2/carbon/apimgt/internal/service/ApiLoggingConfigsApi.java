package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.APILoggingConfigListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.impl.ApiLoggingConfigsApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

@Path("/api-logging-configs")

@Api(description = "the api-logging-configs API")

@Produces({ "application/json" })


public class ApiLoggingConfigsApi  {

  @Context MessageContext securityContext;

ApiLoggingConfigsApiService delegate = new ApiLoggingConfigsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve persisted per API logging data.", notes = "This retrieve the persisted API logging data for API logging support. ", response = APILoggingConfigListDTO.class, tags={ "Logging" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Event Received success", response = APILoggingConfigListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response apiLoggingConfigsGet() throws APIManagementException{
        return delegate.apiLoggingConfigsGet(securityContext);
    }
}
