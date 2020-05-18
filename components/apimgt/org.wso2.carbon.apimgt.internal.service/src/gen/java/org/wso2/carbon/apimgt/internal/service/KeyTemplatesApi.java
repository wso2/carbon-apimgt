package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.KeyTemplatesApiService;
import org.wso2.carbon.apimgt.internal.service.impl.KeyTemplatesApiServiceImpl;
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
@Path("/keyTemplates")

@Api(description = "the keyTemplates API")

@Produces({ "application/json" })


public class KeyTemplatesApi  {

  @Context MessageContext securityContext;

KeyTemplatesApiService delegate = new KeyTemplatesApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "An Array of key templates according to custom policies", notes = "This will provide access to key templates define in custom policies ", response = String.class, responseContainer = "List", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of shops around you", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response keyTemplatesGet() throws APIManagementException{
        return delegate.keyTemplatesGet(securityContext);
    }
}
