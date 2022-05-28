package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.GaConfigApiService;
import org.wso2.carbon.apimgt.internal.service.impl.GaConfigApiServiceImpl;
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
@Path("/ga-config")

@Api(description = "the ga-config API")

@Produces({ "application/json" })


public class GaConfigApi  {

  @Context MessageContext securityContext;

GaConfigApiService delegate = new GaConfigApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Google analytics config related to tenant.", notes = "This will provide access to runtime artifacts in database. ", response = Void.class, tags={ "Retrieving Runtime artifacts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "google analytics configuration", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response gaConfigGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.   Api ID ")  @QueryParam("apiId") String apiId) throws APIManagementException{
        return delegate.gaConfigGet(xWSO2Tenant, apiId, securityContext);
    }
}
