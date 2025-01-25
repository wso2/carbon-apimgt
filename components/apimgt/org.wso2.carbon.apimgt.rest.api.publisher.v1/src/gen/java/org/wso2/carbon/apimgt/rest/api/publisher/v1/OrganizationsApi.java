package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OrganizationListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.OrganizationsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.OrganizationsApiServiceImpl;
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
@Path("/organizations")

@Api(description = "the organizations API")




public class OrganizationsApi  {

  @Context MessageContext securityContext;

OrganizationsApiService delegate = new OrganizationsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all registered Organizations", notes = "Get all Registered Organizations ", response = OrganizationListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_manage", description = "Manage all API related operations"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations"),
            @AuthorizationScope(scope = "apim:api_product_import_export", description = "Import and export API Products related operations"),
            @AuthorizationScope(scope = "apim:publisher_organization_read", description = "Read organization")
        })
    }, tags={ "Organizations" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Organizations returned ", response = OrganizationListDTO.class) })
    public Response organizationsGet() throws APIManagementException{
        return delegate.organizationsGet(securityContext);
    }
}
