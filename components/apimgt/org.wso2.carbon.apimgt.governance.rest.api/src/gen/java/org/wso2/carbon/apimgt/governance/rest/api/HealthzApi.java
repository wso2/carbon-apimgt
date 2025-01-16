package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.HealthzResponseDTO;
import org.wso2.carbon.apimgt.governance.rest.api.HealthzApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.HealthzApiServiceImpl;
import org.wso2.carbon.apimgt.governance.impl.error.GovernanceException;

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




public class HealthzApi  {

  @Context MessageContext securityContext;

HealthzApiService delegate = new HealthzApiServiceImpl();


    @GET
    @Path("/liveness")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Liveness Check", notes = "Checks if the application is alive and able to respond to requests.", response = HealthzResponseDTO.class, tags={ "Health Check", "Internal",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Application is alive", response = HealthzResponseDTO.class) })
    public Response getHealthzLiveness() throws GovernanceException{
        return delegate.getHealthzLiveness(securityContext);
    }

    @GET
    @Path("/readiness")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Readiness Check", notes = "Checks if the application is ready to handle requests.", response = HealthzResponseDTO.class, tags={ "Health Check", "Internal" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Application is ready", response = HealthzResponseDTO.class) })
    public Response getHealthzReadiness() throws GovernanceException{
        return delegate.getHealthzReadiness(securityContext);
    }
}
