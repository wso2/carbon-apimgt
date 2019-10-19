package org.wso2.carbon.throttle.service;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.throttle.service.dto.ErrorDTO;
import org.wso2.carbon.throttle.service.dto.RevokeAPIKeyDTO;
import org.wso2.carbon.throttle.service.factories.ApikeyApiServiceFactory;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
@Path("/apikey")


@Produces({ "application/json" })


public class ApikeyApi  {


  ApikeyApiService delegate = ApikeyApiServiceFactory.getApikeyApi();


    @POST
    @Path("/revoke")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Revoke given API Key", notes = "Revoke and notify the provided API Key", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API key revoked successfully. ", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response revokeAPIKey(@ApiParam(value = "API Key revoke request object" ) RevokeAPIKeyDTO body) {
        return delegate.revokeAPIKey(body);
    }
}
