package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.SynapseArtifactListDTO;
import org.wso2.carbon.apimgt.internal.service.GatewaySynapseArtifactsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.GatewaySynapseArtifactsApiServiceImpl;
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
@Path("/gateway-synapse-artifacts")

@Api(description = "the gateway-synapse-artifacts API")

@Produces({ "application/json" })


public class GatewaySynapseArtifactsApi  {

  @Context MessageContext securityContext;

GatewaySynapseArtifactsApiService delegate = new GatewaySynapseArtifactsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all synapse artifacts for a given gateway label", notes = "This will provide access to the synapse artifacts in database. ", response = SynapseArtifactListDTO.class, tags={ "Retrieving All Synapse artifacts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of gatewayRuntimeArtifacts", response = SynapseArtifactListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response gatewaySynapseArtifactsGet(     
        @ApiParam(value = "**Search condition**.  label associated with the API ")  @QueryParam("gatewayLabel") String gatewayLabel
) throws APIManagementException{
        return delegate.gatewaySynapseArtifactsGet(gatewayLabel, securityContext);
    }
}
