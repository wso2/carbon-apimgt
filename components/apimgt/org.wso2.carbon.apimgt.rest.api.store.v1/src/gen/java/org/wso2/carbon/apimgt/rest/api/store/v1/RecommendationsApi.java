package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RecommendationsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.RecommendationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.RecommendationsApiServiceImpl;
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
@Path("/recommendations")

@Api(description = "the recommendations API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class RecommendationsApi  {

  @Context MessageContext securityContext;

RecommendationsApiService delegate = new RecommendationsApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Give API recommendations for a user", notes = "This API can be used to get recommended APIs for a user who logs into the API store", response = RecommendationsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Recommendations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested recommendations are returned ", response = RecommendationsDTO.class),
        @ApiResponse(code = 404, message = "Recommendations not Found. ", response = ErrorDTO.class) })
    public Response recommendationsGet() throws APIManagementException{
        return delegate.recommendationsGet(securityContext);
    }
}
