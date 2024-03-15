package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Notification;
import org.wso2.carbon.apimgt.api.model.NotificationList;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PatchAllNotificationsRequestDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PatchNotificationRequestDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class NotificationsApiServiceImpl implements NotificationsApiService {

    private static final Log log = LogFactory.getLog(NotificationsApiServiceImpl.class);

    @Override
    public Response getNotifications(String sortOrder, Integer limit, Integer offset, MessageContext messageContext)
            throws APIManagementException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        sortOrder = sortOrder != null ? sortOrder : RestApiConstants.DESCENDING_SORT_ORDER;
        NotificationList notificationList;
        String portalToDisplay = "publisher";

        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            notificationList = apiProvider.getNotifications(username, organization, portalToDisplay, sortOrder, limit,
                    offset);
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while getting notifications", e);
        }

        return Response.ok().entity(notificationList).build();
    }

    @Override
    public Response markAllNotificationsAsRead(PatchAllNotificationsRequestDTO patchAllNotificationsRequestDTO,
            MessageContext messageContext) throws APIManagementException {
        NotificationList notificationList;
        String username = RestApiCommonUtil.getLoggedInUsername();
        String portalToDisplay = "publisher";
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            notificationList = apiProvider.markAllNotificationsAsRead(username, organization, portalToDisplay);
            if (notificationList == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while getting notifications", e);
        }

        return Response.ok().entity(notificationList).build();
    }

    @Override
    public Response markNotificationAsReadById(String notificationId,
            PatchNotificationRequestDTO patchNotificationRequestDTO, MessageContext messageContext)
            throws APIManagementException {
        Notification notification;
        String username = RestApiCommonUtil.getLoggedInUsername();
        String portalToDisplay = "publisher";
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            notification = apiProvider.markNotificationAsReadById(username, organization, notificationId,
                    portalToDisplay);
            if (notification == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while getting notification", e);
        }

        return Response.ok().entity(notification).build();
    }

    @Override
    public Response deleteNotifications(MessageContext messageContext) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        String portalToDisplay = "publisher";
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            boolean result = apiProvider.deleteAllNotifications(username, organization, portalToDisplay);
            if (!result) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while deleting notifications", e);
        }
        return Response.ok().build();
    }

    @Override
    public Response deleteNotification(String notificationId, MessageContext messageContext)
            throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        String portalToDisplay = "publisher";
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            boolean result = apiProvider.deleteNotificationById(username, organization, notificationId,
                    portalToDisplay);
            if (!result) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while deleting notifications", e);
        }
        return Response.ok().build();
    }

}
