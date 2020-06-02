package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.KeyManagerListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.KeyManagersApiServiceImpl;
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
@Path("/key-managers")

@Api(description = "the key-managers API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class KeyManagersApi  {

  @Context MessageContext securityContext;

KeyManagersApiService delegate = new KeyManagersApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Key Managers", notes = "Get all Key managers ", response = KeyManagerListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Key Managers (Collection)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Categories returned ", response = KeyManagerListDTO.class) })
    public Response keyManagersGet(@ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.keyManagersGet(xWSO2Tenant, securityContext);
    }
}
