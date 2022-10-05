package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.UUIDListDTO;
import org.wso2.carbon.apimgt.internal.service.RetrieveApiArtifactsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.RetrieveApiArtifactsApiServiceImpl;
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
@Path("/retrieve-api-artifacts")

@Api(description = "the retrieve-api-artifacts API")

@Produces({ "application/json" })


public class RetrieveApiArtifactsApi  {

  @Context MessageContext securityContext;

RetrieveApiArtifactsApiService delegate = new RetrieveApiArtifactsApiServiceImpl();


    @POST
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get API runtime artifacts from ID list", notes = "This will provide access to API runtime artifacts in database for a given API UUID list. ", response = Void.class, tags={ "Retrieve API artifacts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of runtime Artifacts", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response retrieveApiArtifactsPost(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.  label associated with the APIs ")  @QueryParam("gatewayLabel") String gatewayLabel,  @ApiParam(value = "type of gateway ")  @QueryParam("type") String type, @ApiParam(value = "API UUID list payload" ) UUIDListDTO uuidList) throws APIManagementException{
        return delegate.retrieveApiArtifactsPost(xWSO2Tenant, gatewayLabel, type, uuidList, securityContext);
    }
}
