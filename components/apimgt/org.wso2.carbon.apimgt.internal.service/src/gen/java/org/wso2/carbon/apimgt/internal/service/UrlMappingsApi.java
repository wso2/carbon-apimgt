package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.UrlMappingsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.UrlMappingsApiServiceImpl;
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
@Path("/url-mappings")

@Api(description = "the url-mappings API")

@Produces({ "application/json" })


public class UrlMappingsApi  {

  @Context MessageContext securityContext;

UrlMappingsApiService delegate = new UrlMappingsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all url mappings", notes = "This will provide access to url mappings in database. ", response = Object.class, tags={ "Subscription Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "The url mappings in the database", response = Object.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response urlMappingsGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.urlMappingsGet(xWSO2Tenant, securityContext);
    }

    @GET
    @Path("/{mappingId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all url mappings", notes = "This will provide access to url mappings in database. ", response = Object.class, tags={ "Subscription Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "The url mappings in the database", response = Object.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response urlMappingsMappingIdGet(@ApiParam(value = "**ID** of the ***URL Mapping * of an api. ",required=true) @PathParam("mappingId") Integer mappingId) throws APIManagementException{
        return delegate.urlMappingsMappingIdGet(mappingId, securityContext);
    }
}
