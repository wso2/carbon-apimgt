package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.SynapseArtifactDTO;
import org.wso2.carbon.apimgt.internal.service.SynapseArtifactsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.SynapseArtifactsApiServiceImpl;
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
@Path("/synapse-artifacts")

@Api(description = "the synapse-artifacts API")

@Produces({ "application/json" })


public class SynapseArtifactsApi  {

  @Context MessageContext securityContext;

SynapseArtifactsApiService delegate = new SynapseArtifactsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all the synapse artifacts for a given API UUID and gateway label", notes = "This will provide access to synapse artifacts in database. ", response = String.class, tags={ "Retrieving Synapse artifacts",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "String of gatewayRuntimeArtifacts", response = String.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response synapseArtifactsGet( @ApiParam(value = "**Search condition**.   Api ID ")  @QueryParam("apiId") String apiId,  @ApiParam(value = "**Search condition**.  label associated with the API ")  @QueryParam("gatewayLabel") String gatewayLabel,  @ApiParam(value = "**Search condition**.  Publish/Remove ")  @QueryParam("gatewayInstruction") String gatewayInstruction) throws APIManagementException{
        return delegate.synapseArtifactsGet(apiId, gatewayLabel, gatewayInstruction, securityContext);
    }

    @POST
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Save the given API Artifacts in database", notes = "This will provide access to the synapse artifacts in database.", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Synapse Artifacts saved in database Successfully ", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response synapseArtifactsPost(@ApiParam(value = "gateway Artifacts which need to be saved " ,required=true) SynapseArtifactDTO body) throws APIManagementException{
        return delegate.synapseArtifactsPost(body, securityContext);
    }
}
