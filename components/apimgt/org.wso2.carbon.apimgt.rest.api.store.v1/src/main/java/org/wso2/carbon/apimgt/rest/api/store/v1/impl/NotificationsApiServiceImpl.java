/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Notification;
import org.wso2.carbon.apimgt.api.model.NotificationList;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.NotificationsApiService;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PatchAllNotificationsRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PatchNotificationRequestDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class NotificationsApiServiceImpl implements NotificationsApiService {

    private static final Log log = LogFactory.getLog(NotificationsApiServiceImpl.class);
    private final String portalToDisplay = RestApiConstants.DEV_PORTAL;

    public Response deleteNotificationById(String notificationId, MessageContext messageContext)
            throws APIManagementException {
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            boolean result = apiConsumer.deleteNotificationById(username, organization, notificationId,
                    portalToDisplay);
            if (!result) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_NOTIFICATION, notificationId, e, log);
            } else {
                String errorMessage = "Error while deleting the notification";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return Response.ok().build();
    }

    public Response deleteNotifications(MessageContext messageContext) throws APIManagementException {
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            boolean result = apiConsumer.deleteAllNotifications(username, organization, portalToDisplay);
            if (!result) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError("No any notification found", e, log);
            } else {
                String errorMessage = "Error while deleting notifications";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
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
            notificationList = apiConsumer.getNotifications(username, organization, portalToDisplay, sortOrder, limit,
                    offset);
            return Response.ok().entity(notificationList).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError("No any notification found", e, log);
            } else {
                String errorMessage = "Error while getting notifications";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    public Response markAllNotificationsAsRead(PatchAllNotificationsRequestDTO patchAllNotificationsRequestDTO,
            MessageContext messageContext) throws APIManagementException {
        NotificationList notificationList;
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            notificationList = apiConsumer.markAllNotificationsAsRead(username, organization, portalToDisplay);
            if (notificationList == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok().entity(notificationList).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError("No any notification found", e, log);
            } else {
                String errorMessage = "Error while marking notifications as read";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    public Response markNotificationAsReadById(String notificationId,
            PatchNotificationRequestDTO patchNotificationRequestDTO, MessageContext messageContext)
            throws APIManagementException {
        Notification notification;
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            notification = apiConsumer.markNotificationAsReadById(username, organization, notificationId,
                    portalToDisplay);
            if (notification == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok().entity(notification).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError("No any notification found", e, log);
            } else {
                String errorMessage = "Error while marking the notification " + notificationId + " as read";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }
}
