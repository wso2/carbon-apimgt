package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.NotificationActionRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.NotificationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.NotificationListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface NotificationsApiService {
      public Response changeNotificationStatus(String notificationId, NotificationActionRequestDTO notificationActionRequestDTO, MessageContext messageContext) throws APIManagementException;
      public Response deleteAllNotifications(MessageContext messageContext) throws APIManagementException;
      public Response deleteNotification(String notificationId, MessageContext messageContext) throws APIManagementException;
      public Response getNotifications(Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
      public Response markAllNotificationsAsRead(Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
}
