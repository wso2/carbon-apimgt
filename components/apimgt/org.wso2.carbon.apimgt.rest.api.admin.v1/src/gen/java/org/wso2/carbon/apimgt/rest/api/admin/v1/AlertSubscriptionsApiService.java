package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertsSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface AlertSubscriptionsApiService {
      public Response getBotDetectionAlertSubscriptions(MessageContext messageContext) throws APIManagementException;
      public Response getSubscribedAlertTypes(MessageContext messageContext) throws APIManagementException;
      public Response subscribeForBotDetectionAlerts(BotDetectionAlertSubscriptionDTO body, MessageContext messageContext) throws APIManagementException;
      public Response subscribeToAlerts(AlertsSubscriptionDTO body, MessageContext messageContext) throws APIManagementException;
      public Response unsubscribeAllAlerts(MessageContext messageContext) throws APIManagementException;
      public Response unsubscribeFromBotDetectionAlerts(String uuid, MessageContext messageContext) throws APIManagementException;
}
