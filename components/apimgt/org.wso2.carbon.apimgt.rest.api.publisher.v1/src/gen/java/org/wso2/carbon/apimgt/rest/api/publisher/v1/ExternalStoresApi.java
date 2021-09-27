package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ExternalStoreDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ExternalStoresApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ExternalStoresApiServiceImpl;
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
