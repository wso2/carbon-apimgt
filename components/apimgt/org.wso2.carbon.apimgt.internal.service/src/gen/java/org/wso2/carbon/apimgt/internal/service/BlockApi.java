package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.BlockConditionsDTO;
import org.wso2.carbon.apimgt.internal.service.impl.BlockApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

@Path("/block")

@Api(description = "the block API")

@Produces({ "application/json" })


public class BlockApi  {

  @Context MessageContext securityContext;

BlockApiService delegate = new BlockApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "blocking events available", notes = "This will provide access to throttled events in database. ", response = BlockConditionsDTO.class, responseContainer = "Map", tags={ "Throttling" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Block Conditions", response = BlockConditionsDTO.class, responseContainer = "Map") })
    public Response blockGet() throws APIManagementException{
        return delegate.blockGet(securityContext);
    }
}
