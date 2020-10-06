package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.SynapseAttributesDTO;
import org.wso2.carbon.apimgt.internal.service.SynapseAttributesApiService;
import org.wso2.carbon.apimgt.internal.service.impl.SynapseAttributesApiServiceImpl;
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
@Path("/synapse-attributes")

@Api(description = "the synapse-attributes API")

@Produces({ "application/json" })


public class SynapseAttributesApi  {

  @Context MessageContext securityContext;

SynapseAttributesApiService delegate = new SynapseAttributesApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get API UUID and label associated with it", notes = "This will provide access to synapse artifacts in database. ", response = SynapseAttributesDTO.class, tags={ "Retrieving Synapse artifacts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of gatewayRuntimeArtifacts", response = SynapseAttributesDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response synapseAttributesGet(     
        @ApiParam(value = "**Search condition**.   API Name ")  @QueryParam("apiName") String apiName
,      
        @ApiParam(value = "**Search condition**.  tenantDomain associated with the API ")  @QueryParam("tenantDomain") String tenantDomain
,      
        @ApiParam(value = "**Search condition**. version of the API ")  @QueryParam("version") String version
) throws APIManagementException{
        return delegate.synapseAttributesGet(apiName, tenantDomain, version, securityContext);
    }
}
