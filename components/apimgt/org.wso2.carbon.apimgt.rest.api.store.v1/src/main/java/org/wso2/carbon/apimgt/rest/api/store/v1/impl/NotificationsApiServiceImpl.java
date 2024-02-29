package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Notification;
import org.wso2.carbon.apimgt.api.model.NotificationList;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.NotificationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.NotificationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PatchAllNotificationsRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PatchNotificationRequestDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class NotificationsApiServiceImpl implements NotificationsApiService {

    public Response deleteNotification(String notificationId, MessageContext messageContext)
            throws APIManagementException {

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            boolean result = apiConsumer.deleteNotificationById(username, organization, notificationId);
            if(!result){
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while deleting notifications", e);
        }
        return Response.ok().build();
    }

    public Response deleteNotifications(MessageContext messageContext) throws APIManagementException {

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            boolean result = apiConsumer.deleteAllNotifications(username, organization);
            if(!result){
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while deleting notifications", e);
        }
        return Response.ok().build();
    }

    public Response getNotifications(String sortOrder, Integer limit, Integer offset, MessageContext messageContext)
            throws APIManagementException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        sortOrder = sortOrder != null ? sortOrder : RestApiConstants.DESCENDING_SORT_ORDER;
        NotificationList notificationList;

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            notificationList = apiConsumer.getNotifications(username, organization, sortOrder, limit, offset);
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while getting notifications", e);
        }

        return Response.ok().entity(notificationList).build();
    }

    public Response markAllNotificationsAsRead(PatchAllNotificationsRequestDTO patchAllNotificationsRequestDTO, MessageContext messageContext)
            throws APIManagementException {

        NotificationList notificationList ;

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            notificationList = apiConsumer.markAllNotificationsAsRead(username, organization);
            if(notificationList == null){
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while getting notifications", e);
        }

        return Response.ok().entity(notificationList).build();
    }

    public Response markNotificationAsReadById(String notificationId, PatchNotificationRequestDTO patchNotificationRequestDTO, MessageContext messageContext)
            throws APIManagementException {

        Notification notification ;

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            notification = apiConsumer.markNotificationAsReadById(username, organization, notificationId);
            if(notification == null){
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (APIManagementException e) {
            throw new APIManagementException("Error while getting notification", e);
        }

        return Response.ok().entity(notification).build();
    }
}
