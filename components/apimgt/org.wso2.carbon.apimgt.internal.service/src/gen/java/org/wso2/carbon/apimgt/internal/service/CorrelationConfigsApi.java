package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.CorrelationComponentsListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.CorrelationConfigsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.CorrelationConfigsApiServiceImpl;
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
@Path("/correlation-configs")

@Api(description = "the correlation-configs API")

@Produces({ "application/json" })


public class CorrelationConfigsApi  {

  @Context MessageContext securityContext;

CorrelationConfigsApiService delegate = new CorrelationConfigsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve persisted correlation configs data.", notes = "This retrieve the persisted correlation configs data for updating correlation logs. ", response = CorrelationComponentsListDTO.class, tags={ "Logging" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of correlation component configs", response = CorrelationComponentsListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response correlationConfigsGet() throws APIManagementException{
        return delegate.correlationConfigsGet(securityContext);
    }
}
