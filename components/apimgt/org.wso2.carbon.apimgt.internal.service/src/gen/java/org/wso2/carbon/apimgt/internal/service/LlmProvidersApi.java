package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.LLMProviderListDTO;
import org.wso2.carbon.apimgt.internal.service.LlmProvidersApiService;
import org.wso2.carbon.apimgt.internal.service.impl.LlmProvidersApiServiceImpl;
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
@Path("/llm-providers")

@Api(description = "the llm-providers API")

@Produces({ "application/json" })


public class LlmProvidersApi  {

  @Context MessageContext securityContext;

LlmProvidersApiService delegate = new LlmProvidersApiServiceImpl();


    @GET
    @Path("/{llmProviderId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve persisted LLM Providers", notes = "This retrieve the persisted LLM Providers. ", response = LLMProviderListDTO.class, tags={ "LLMProviders",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Event Received success", response = LLMProviderListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response getLLMProviderById(@ApiParam(value = "",required=true) @PathParam("llmProviderId") String llmProviderId, @ApiParam(value = "This is used to specify the tenant domain, where the resource need to be             retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.getLLMProviderById(llmProviderId, xWSO2Tenant, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve persisted LLM Providers", notes = "This retrieve the persisted LLM Providers. ", response = LLMProviderListDTO.class, tags={ "LLMProviders" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Event Received success", response = LLMProviderListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response getLLMProviders(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be             retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "")  @QueryParam("name") String name,  @ApiParam(value = "")  @QueryParam("apiVersion") String apiVersion) throws APIManagementException{
        return delegate.getLLMProviders(xWSO2Tenant, name, apiVersion, securityContext);
    }
}
