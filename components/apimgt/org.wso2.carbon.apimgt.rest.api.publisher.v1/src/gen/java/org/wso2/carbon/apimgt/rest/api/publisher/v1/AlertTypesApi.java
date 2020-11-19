package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AlertTypesListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.AlertTypesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.AlertTypesApiServiceImpl;
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
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class AlertTypesApi  {

  @Context MessageContext securityContext;

AlertTypesApiService delegate = new AlertTypesApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the list of API Publisher alert types. ", notes = "This operation is used to get the list of supportd alert types for the 'publisher' agent. ", response = AlertTypesListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:pub_alert_manage", description = "Get/ subscribe/ configure publisher alerts")
        })
    }, tags={ "Alerts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The list of publisher alert types are returned. ", response = AlertTypesListDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getPublisherAlertTypes() throws APIManagementException{
        return delegate.getPublisherAlertTypes(securityContext);
    }
}
