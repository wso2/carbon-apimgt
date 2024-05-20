package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.NotificationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.NotificationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PatchAllNotificationsRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PatchNotificationRequestDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface NotificationsApiService {
      public Response deleteNotificationById(String notificationId, MessageContext messageContext) throws APIManagementException;
      public Response deleteNotifications(MessageContext messageContext) throws APIManagementException;
      public Response getNotifications(String sortOrder, Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
      public Response markAllNotificationsAsRead(PatchAllNotificationsRequestDTO patchAllNotificationsRequestDTO, MessageContext messageContext) throws APIManagementException;
      public Response markNotificationAsReadById(String notificationId, PatchNotificationRequestDTO patchNotificationRequestDTO, MessageContext messageContext) throws APIManagementException;
}
