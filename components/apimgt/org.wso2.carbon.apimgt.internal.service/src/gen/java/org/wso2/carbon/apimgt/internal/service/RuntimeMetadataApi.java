package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.RuntimeMetadataApiService;
import org.wso2.carbon.apimgt.internal.service.impl.RuntimeMetadataApiServiceImpl;
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
@Path("/runtime-metadata")

@Api(description = "the runtime-metadata API")

@Produces({ "application/json" })


public class RuntimeMetadataApi  {

  @Context MessageContext securityContext;

RuntimeMetadataApiService delegate = new RuntimeMetadataApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Metadata information for API runtimes", notes = "This will provide access to the deployment metadata in json format ", response = Void.class, tags={ "Retrieving Runtime artifacts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Json file of runtime metadata", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response runtimeMetadataGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.   Api ID ")  @QueryParam("apiId") String apiId,  @ApiParam(value = "**Search condition**.  label associated with the APIs ")  @QueryParam("gatewayLabel") String gatewayLabel) throws APIManagementException{
        return delegate.runtimeMetadataGet(xWSO2Tenant, apiId, gatewayLabel, securityContext);
    }
}
