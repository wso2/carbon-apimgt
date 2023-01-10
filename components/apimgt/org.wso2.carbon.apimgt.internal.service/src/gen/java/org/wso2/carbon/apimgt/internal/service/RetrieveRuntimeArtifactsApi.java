package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.RetrieveRuntimeArtifactsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.RetrieveRuntimeArtifactsApiServiceImpl;
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
@Path("/retrieve-runtime-artifacts")

@Api(description = "the retrieve-runtime-artifacts API")

@Produces({ "application/json" })


public class RetrieveRuntimeArtifactsApi  {

  @Context MessageContext securityContext;

RetrieveRuntimeArtifactsApiService delegate = new RetrieveRuntimeArtifactsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all the runtime artifacts for a given data-planeId", notes = "This will provide access to runtime artifacts in database. ", response = Void.class, tags={ "Retrieving Runtime artifacts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of runtime Artifacts", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response retrieveRuntimeArtifactsGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.  type of gateway ")  @QueryParam("type") String type,  @ApiParam(value = "**Search condition**.  Data-plane ID ")  @QueryParam("dataplaneId") String dataplaneId) throws APIManagementException{
        return delegate.retrieveRuntimeArtifactsGet(xWSO2Tenant, type, dataplaneId, securityContext);
    }
}
