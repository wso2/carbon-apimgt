package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ApplicationKeyMappingListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.ApplicationKeyMappingsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.ApplicationKeyMappingsApiServiceImpl;
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
@Path("/application-key-mappings")

@Api(description = "the application-key-mappings API")

@Produces({ "application/json" })


public class ApplicationKeyMappingsApi  {

  @Context MessageContext securityContext;

ApplicationKeyMappingsApiService delegate = new ApplicationKeyMappingsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all application key mappings", notes = "This will provide access to application vs key mappings in database. ", response = ApplicationKeyMappingListDTO.class, tags={ "Subscription Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of application key mappings in the database", response = ApplicationKeyMappingListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response applicationKeyMappingsGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.  Consumer Key of the application ")  @QueryParam("consumerKey") String consumerKey,  @ApiParam(value = "**Search condition**.  KeyManager asscciated to consumer_key of the application ")  @QueryParam("keymanager") String keymanager) throws APIManagementException{
        return delegate.applicationKeyMappingsGet(xWSO2Tenant, consumerKey, keymanager, securityContext);
    }
}
