package org.wso2.carbon.apimgt.rest.api.devops;

import org.wso2.carbon.apimgt.rest.api.devops.dto.CorrelationComponentsListDTO;
import org.wso2.carbon.apimgt.rest.api.devops.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.devops.ConfigApiService;
import org.wso2.carbon.apimgt.rest.api.devops.impl.ConfigApiServiceImpl;
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
@Path("/config")

@Api(description = "the config API")




public class ConfigApi  {

  @Context MessageContext securityContext;

ConfigApiService delegate = new ConfigApiServiceImpl();


    @GET
    @Path("/correlation/")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "GET status of correlation log components ", notes = "", response = CorrelationComponentsListDTO.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Status of Correlation Log Components", response = CorrelationComponentsListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Request component not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while retrieving info", response = ErrorDTO.class) })
    public Response configCorrelationGet() throws APIManagementException{
        return delegate.configCorrelationGet(securityContext);
    }

    @PUT
    @Path("/correlation/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Enable / Disable correlation logs ", notes = "This operation enables you to enable / disable correlation logs  of the product. This operation can be done at the product level  or at each component level by providing the componentName query parameter. ", response = CorrelationComponentsListDTO.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successfully Set the status of the correlation logs.", response = CorrelationComponentsListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Request components not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while configuring the correlation log status", response = ErrorDTO.class) })
    public Response configCorrelationPut(@ApiParam(value = "The enable / disable correlation log status is provided as a payload. " ) CorrelationComponentsListDTO correlationComponentsListDTO) throws APIManagementException{
        return delegate.configCorrelationPut(correlationComponentsListDTO, securityContext);
    }
}
