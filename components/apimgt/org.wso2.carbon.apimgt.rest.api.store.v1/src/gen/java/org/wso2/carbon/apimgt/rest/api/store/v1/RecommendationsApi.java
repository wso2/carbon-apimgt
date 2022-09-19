package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RecommendationsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.RecommendationsApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
@Path("/recommendations")

@Api(description = "the recommendations API")




public class RecommendationsApi  {

  @Context MessageContext securityContext;

RecommendationsApiService delegate = new RecommendationsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Give API Recommendations for a User", notes = "This API can be used to get recommended APIs for a user who logs into the API Developer Portal", response = RecommendationsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Recommendations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested recommendations are returned ", response = RecommendationsDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response recommendationsGet() throws APIManagementException{
        return delegate.recommendationsGet(securityContext);
    }
}
