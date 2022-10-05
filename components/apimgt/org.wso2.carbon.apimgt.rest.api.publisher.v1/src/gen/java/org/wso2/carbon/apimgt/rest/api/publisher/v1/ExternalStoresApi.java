package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ExternalStoreDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ExternalStoresApiServiceImpl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/external-stores")

@Api(description = "the external-stores API")




public class ExternalStoresApi  {

  @Context MessageContext securityContext;

ExternalStoresApiService delegate = new ExternalStoresApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve External Stores List to Publish an API", notes = "Retrieve external stores list configured to publish an API ", response = ExternalStoreDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations")
        })
    }, tags={ "External Stores" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. External Stores list returned ", response = ExternalStoreDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getAllExternalStores() throws APIManagementException{
        return delegate.getAllExternalStores(securityContext);
    }
}
