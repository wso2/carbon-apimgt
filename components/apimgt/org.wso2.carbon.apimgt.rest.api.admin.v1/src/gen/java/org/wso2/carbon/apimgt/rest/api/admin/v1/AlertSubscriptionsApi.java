package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertsSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.AlertSubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.AlertSubscriptionsApiServiceImpl;
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
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class AlertSubscriptionsApi  {

  @Context MessageContext securityContext;

AlertSubscriptionsApiService delegate = new AlertSubscriptionsApiServiceImpl();


    @GET
    @Path("/bot-detection")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Subscriptions for Bot Detection ", notes = "Get the list of subscriptions which are subscribed to receive email alerts for bot detection ", response = BotDetectionAlertSubscriptionListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin_alert_manage", description = "Manage admin alerts"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Bot Detection Alert Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The list of bot detection alert subscriptions are returned. ", response = BotDetectionAlertSubscriptionListDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getBotDetectionAlertSubscriptions() throws APIManagementException{
        return delegate.getBotDetectionAlertSubscriptions(securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Subscribed Alert Types ", notes = "This operation is used to get the list of subscribed alert types by the user. ", response = AlertsSubscriptionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin_alert_manage", description = "Manage admin alerts"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Alert Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The list of subscribed alert types are returned. ", response = AlertsSubscriptionDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getSubscribedAlertTypes() throws APIManagementException{
        return delegate.getSubscribedAlertTypes(securityContext);
    }

    @POST
    @Path("/bot-detection")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Subscribe for Bot Detection Alerts", notes = "Register a subscription for bot detection alerts ", response = BotDetectionAlertSubscriptionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin_alert_manage", description = "Manage admin alerts"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Bot Detection Alert Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Bot detection alert subscription is registered successfully. ", response = BotDetectionAlertSubscriptionDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response subscribeForBotDetectionAlerts(@ApiParam(value = "The email to register to receive bot detection alerts " ,required=true) BotDetectionAlertSubscriptionDTO body) throws APIManagementException{
        return delegate.subscribeForBotDetectionAlerts(body, securityContext);
    }

    @PUT
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Subscribe to an Admin Alert ", notes = "This operation is used to subscribe to admin alerts ", response = AlertsSubscriptionDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin_alert_manage", description = "Manage admin alerts"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Alert Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the newly subscribed alerts. ", response = AlertsSubscriptionDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response subscribeToAlerts(@ApiParam(value = "The alerts list and the email list to subscribe." ,required=true) AlertsSubscriptionDTO body) throws APIManagementException{
        return delegate.subscribeToAlerts(body, securityContext);
    }

    @DELETE
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Unsubscribe User from all Admin Alerts ", notes = "This operation is used to unsubscribe the respective user from all the admin alert types. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin_alert_manage", description = "Manage admin alerts"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Alert Subscriptions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The user is unsubscribed from the alerts successfully. ", response = Void.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response unsubscribeAllAlerts() throws APIManagementException{
        return delegate.unsubscribeAllAlerts(securityContext);
    }

    @DELETE
    @Path("/bot-detection/{uuid}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Unsubscribe from bot detection alerts.", notes = "Delete a Bot Detection Alert Subscription ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin_alert_manage", description = "Manage admin alerts"),
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Bot Detection Alert Subscriptions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Bot detection alert subscription is deleted successfully. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response unsubscribeFromBotDetectionAlerts(@ApiParam(value = "uuid of the subscription",required=true) @PathParam("uuid") String uuid) throws APIManagementException{
        return delegate.unsubscribeFromBotDetectionAlerts(uuid, securityContext);
    }
}
