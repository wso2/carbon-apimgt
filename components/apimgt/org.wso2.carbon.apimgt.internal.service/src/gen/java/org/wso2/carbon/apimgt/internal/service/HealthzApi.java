package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.HealthStatusDTO;
import org.wso2.carbon.apimgt.internal.service.HealthzApiService;
import org.wso2.carbon.apimgt.internal.service.impl.HealthzApiServiceImpl;
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
@Path("/healthz")

@Api(description = "the healthz API")

@Produces({ "application/json" })


public class HealthzApi  {

  @Context MessageContext securityContext;

HealthzApiService delegate = new HealthzApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve health status of the server", notes = "This retrieves the health status of the server ", response = HealthStatusDTO.class, tags={ "Health" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Health status success", response = HealthStatusDTO.class),
        @ApiResponse(code = 200, message = "The server is unhealthy", response = ErrorDTO.class) })
    public Response healthzGet() throws APIManagementException{
        return delegate.healthzGet(securityContext);
    }
}
