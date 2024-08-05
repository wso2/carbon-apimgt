package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Notification;
import org.wso2.carbon.apimgt.api.model.NotificationList;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.NotificationsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.NotificationActionRequestDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;


public class NotificationsApiServiceImpl implements NotificationsApiService {

    private final String portalToDisplay = RestApiConstants.PUBLISHER_PORTAL;

    /**
     * Changes the status of a notification.
     *
     * @param notificationId The ID of the notification to change.
     * @param notificationActionRequestDTO The request containing the action to perform on the notification
     *                                     eg: Set markAsRead to true to mark the notification as read or false to
     *                                     mark it as unread.
     * @param messageContext The message context for the request.
     * @return A Response object containing the updated notification.
     * @throws APIManagementException If an error occurs while changing the notification status.
     */
    @Override
    public Response changeNotificationStatus(String notificationId, NotificationActionRequestDTO notificationActionRequestDTO,
                                             MessageContext messageContext) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Notification notification = apiProvider.changeNotificationStatus(username, organization, notificationId,
                notificationActionRequestDTO.isMarkAsRead(), portalToDisplay);
        return Response.ok().entity(notification).build();
    }

    /**
     * Deletes a notification.
     *
     * @param notificationId The ID of the notification to delete.
     * @param messageContext The message context for the request.
     * @return A Response object indicating the result of the delete operation.
     * @throws APIManagementException If an error occurs while deleting the notification.
     */
    @Override
    public Response deleteNotification(String notificationId, MessageContext messageContext) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        apiProvider.deleteNotification(username, organization, notificationId, portalToDisplay);
        return Response.ok().build();
    }

    /**
     * Retrieves a list of notifications.
     *
     * @param limit The maximum number of notifications to return. If null, the default limit is used.
     * @param offset The starting point within the list of notifications. If null, the default offset is used.
     * @param messageContext The message context for the request.
     * @return A Response object containing the list of notifications.
     * @throws APIManagementException If an error occurs while retrieving the notifications.
     */
    @Override
    public Response getNotifications(Integer limit, Integer offset, MessageContext messageContext)
            throws APIManagementException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        String username = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        NotificationList notificationList = apiProvider.getNotifications(username, organization, portalToDisplay, limit,
                offset);
        return Response.ok().entity(notificationList).build();
    }

    /**
     * Marks all notifications as read.
     *
     * @param limit The maximum number of notifications to return after marking as read. If null, the default limit is used.
     * @param offset The starting point within the list of notifications. If null, the default offset is used.
     * @param messageContext The message context for the request.
     * @return A Response object containing the updated list of notifications.
     * @throws APIManagementException If an error occurs while marking the notifications as read.
     */
    @Override
    public Response markAllNotificationsAsRead(Integer limit, Integer offset, MessageContext messageContext)
            throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        NotificationList notificationList = apiProvider.markAllNotificationsAsRead(username, organization,
                portalToDisplay, limit, offset);
        return Response.ok().entity(notificationList).build();
    }

    /**
     * Deletes all notifications.
     *
     * @param messageContext The message context for the request.
     * @return A Response object indicating the result of the delete operation.
     * @throws APIManagementException If an error occurs while deleting the notifications.
     */
    @Override
    public Response deleteAllNotifications(MessageContext messageContext) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        apiProvider.deleteAllNotifications(username, organization, portalToDisplay);
        return Response.ok().build();
    }
}
