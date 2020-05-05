package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.BlockConditionsDTO;
import org.wso2.carbon.apimgt.internal.service.BlockApiService;
import org.wso2.carbon.apimgt.internal.service.impl.BlockApiServiceImpl;
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
@Path("/block")

@Api(description = "the block API")

@Produces({ "application/json" })


public class BlockApi  {

  @Context MessageContext securityContext;

BlockApiService delegate = new BlockApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "throttled events available", notes = "This will provide access to throttled events in database. ", response = BlockConditionsDTO.class, responseContainer = "Map", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of shops around you", response = BlockConditionsDTO.class, responseContainer = "Map") })
    public Response blockGet() throws APIManagementException{
        return delegate.blockGet(securityContext);
    }
}
