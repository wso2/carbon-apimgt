package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertTypesListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.AlertTypesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.AlertTypesApiServiceImpl;
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
@Path("/alert-types")

@Api(description = "the alert-types API")




public class AlertTypesApi  {

  @Context MessageContext securityContext;

AlertTypesApiService delegate = new AlertTypesApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Admin Alert Types ", notes = "This operation is used to get the list of supportd alert types for the apim admin dashboard ", response = AlertTypesListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:admin_alert_manage", description = "Manage admin alerts")
        })
    }, tags={ "Alerts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The list of admin alert types are returned. ", response = AlertTypesListDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getAdminAlertTypes() throws APIManagementException{
        return delegate.getAdminAlertTypes(securityContext);
    }
}
