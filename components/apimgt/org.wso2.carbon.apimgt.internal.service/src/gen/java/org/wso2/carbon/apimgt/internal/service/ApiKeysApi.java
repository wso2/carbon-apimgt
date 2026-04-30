package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.APIKeyListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.ApiKeysApiService;
import org.wso2.carbon.apimgt.internal.service.impl.ApiKeysApiServiceImpl;
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
@Path("/api-keys")

@Api(description = "the api-keys API")




public class ApiKeysApi  {

  @Context MessageContext securityContext;

ApiKeysApiService delegate = new ApiKeysApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "An Array of API keys generated", notes = "This will provide API keys generated ", response = APIKeyListDTO.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of API keys", response = APIKeyListDTO.class, responseContainer = "List"),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response apiKeysGet( @NotNull  @ApiParam(value = "This is used to specify the tenant domain, where the resource need to be retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.apiKeysGet(xWSO2Tenant, securityContext);
    }
}
