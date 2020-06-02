package org.wso2.carbon.apimgt.rest.api.endpoint.registry;

import org.wso2.carbon.apimgt.rest.api.endpoint.registry.SettingsApiService;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.impl.SettingsApiServiceImpl;
import org.wso2.carbon.apimgt.api.endpoint.registry.api.EndpointRegistryException;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.inject.Inject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.io.InputStream;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
@Path("/settings")




@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJAXRSCXFCDIServerCodegen", date = "2020-06-02T13:36:35.317+05:30[Asia/Colombo]")
public class SettingsApi  {

@Context MessageContext securityContext;

SettingsApiService delegate = new SettingsApiServiceImpl();

    @GET
    
    
    
    @Operation(summary = "Retrieve endpoint registry settings", description = "Retrieve Endpoint Registry settings ", tags={ "Settings" })

    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200",
            description = "OK. Settings returned "),
    
        @ApiResponse(responseCode = "404",
            description = "Not Found. Requested Settings does not exist. ")
     })
    public Response getSettings() throws EndpointRegistryException {
        return delegate.getSettings(securityContext);
        }
}
