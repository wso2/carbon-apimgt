package org.wso2.carbon.throttle.service;

import io.swagger.annotations.ApiParam;
import org.wso2.carbon.throttle.service.dto.RevokedJWTListDTO;
import org.wso2.carbon.throttle.service.factories.RevokedjwtApiServiceFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/revokedjwt")

@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/revokedjwt", description = "the revokedjwt API")
public class RevokedjwtApi  {

   private final RevokedjwtApiService delegate = RevokedjwtApiServiceFactory.getRevokedjwtApi();

    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "JTIs of revoked jwt tokens", notes = "This will provide access to JTIs of revoked JWT tokens in database.\n", response = RevokedJWTListDTO.class, responseContainer = "List")
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "An array of shops around you"),
        
        @io.swagger.annotations.ApiResponse(code = 200, message = "Unexpected error") })

    public Response revokedjwtGet(@ApiParam(value = "**Search condition**.\n\nYou can search for an application by specifying the name as \"query\" attribute.\n\nEg.\n\"app1\" will match an application if the name is exactly \"app1\".\n\nCurrently this does not support wildcards. Given name must exactly match the application name.\n") @QueryParam("query")  String query)
    {
    return delegate.revokedjwtGet(query);
    }
}

