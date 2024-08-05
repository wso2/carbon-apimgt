package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.NotificationActionRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.NotificationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.NotificationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.NotificationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.NotificationsApiServiceImpl;
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
@Path("/notifications")

@Api(description = "the notifications API")




public class NotificationsApi  {

  @Context MessageContext securityContext;

NotificationsApiService delegate = new NotificationsApiServiceImpl();


    @PATCH
    @Path("/{notificationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Mark a notification as read or unread", notes = "This operation can be used to mark a notification as read or unread. ", response = NotificationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:notifications_manage", description = "Manage notifications"),
            @AuthorizationScope(scope = "apim:notifications_view", description = "View notifications")
        })
    }, tags={ "Notifications",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated Notification. ", response = NotificationDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response changeNotificationStatus(@ApiParam(value = "UUID of the notification. ",required=true) @PathParam("notificationId") String notificationId, @ApiParam(value = "" ,required=true) NotificationActionRequestDTO notificationActionRequestDTO) throws APIManagementException{
        return delegate.changeNotificationStatus(notificationId, notificationActionRequestDTO, securityContext);
    }

    @DELETE
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete all notifications", notes = "This operation can be used to delete all the notifications of a user. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:notifications_manage", description = "Manage notifications"),
            @AuthorizationScope(scope = "apim:notifications_view", description = "View notifications")
        })
    }, tags={ "Notifications",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response deleteAllNotifications() throws APIManagementException{
        return delegate.deleteAllNotifications(securityContext);
    }

    @DELETE
    @Path("/{notificationId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Notification", notes = "This operation can be used to delete a notification. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:notifications_manage", description = "Manage notifications"),
            @AuthorizationScope(scope = "apim:notifications_view", description = "View notifications")
        })
    }, tags={ "Notifications",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response deleteNotification(@ApiParam(value = "UUID of the notification. ",required=true) @PathParam("notificationId") String notificationId) throws APIManagementException{
        return delegate.deleteNotification(notificationId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves all notifications for the user.", notes = "This operation can be used to retrieve all notifications for the user.", response = NotificationListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:notifications_view", description = "View notifications")
        })
    }, tags={ "Notifications",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of Notifications are returned. ", response = NotificationListDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getNotifications( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIManagementException{
        return delegate.getNotifications(limit, offset, securityContext);
    }

    @PATCH
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Mark all notifications as read", notes = "This operation can be used to mark all notifications as read. ", response = NotificationListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:notifications_manage", description = "Manage notifications"),
            @AuthorizationScope(scope = "apim:notifications_view", description = "View notifications")
        })
    }, tags={ "Notifications" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated Notifications. ", response = NotificationListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized. The user is not authorized.", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response markAllNotificationsAsRead( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIManagementException{
        return delegate.markAllNotificationsAsRead(limit, offset, securityContext);
    }
}
