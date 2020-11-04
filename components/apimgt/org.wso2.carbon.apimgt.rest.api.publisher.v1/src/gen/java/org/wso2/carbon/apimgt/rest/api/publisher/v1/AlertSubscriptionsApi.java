package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AlertsInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AlertsInfoResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.AlertSubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.AlertSubscriptionsApiServiceImpl;
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
@Path("/alert-subscriptions")

@Api(description = "the alert-subscriptions API")




public class AlertSubscriptionsApi  {

  @Context MessageContext securityContext;

AlertSubscriptionsApiService delegate = new AlertSubscriptionsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the List of API Publisher Alert Types Subscribed by the User ", notes = "This operation is used to get the list of subscribed alert types by the user. ", response = AlertsInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:pub_alert_manage", description = "Get/ subscribe/ configure publisher alerts")
        })
    }, tags={ "Alert Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The list of subscribed alert types are returned. ", response = AlertsInfoDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getSubscribedAlertTypes() throws APIManagementException{
        return delegate.getSubscribedAlertTypes(securityContext);
    }

    @PUT
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Subscribe to the Selected Tlert types by the User ", notes = "This operation is used to get the list of subscribed alert types by the user. ", response = AlertsInfoResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:pub_alert_manage", description = "Get/ subscribe/ configure publisher alerts")
        })
    }, tags={ "Alert Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Successful response with the newly subscribed alerts. ", response = AlertsInfoResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response subscribeToAlerts(@ApiParam(value = "The alerts list and the email list to subscribe." ,required=true) AlertsInfoDTO body) throws APIManagementException{
        return delegate.subscribeToAlerts(body, securityContext);
    }

    @DELETE
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Unsubscribe User from All the Alert Types ", notes = "This operation is used to unsubscribe the respective user from all the alert types. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:pub_alert_manage", description = "Get/ subscribe/ configure publisher alerts")
        })
    }, tags={ "Alert Subscriptions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The user is unsubscribed from the alerts successfully. ", response = Void.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response unsubscribeAllAlerts() throws APIManagementException{
        return delegate.unsubscribeAllAlerts(securityContext);
    }
}
