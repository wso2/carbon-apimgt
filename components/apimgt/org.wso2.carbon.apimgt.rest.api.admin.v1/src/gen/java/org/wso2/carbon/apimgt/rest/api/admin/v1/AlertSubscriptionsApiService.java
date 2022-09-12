package org.wso2.carbon.apimgt.rest.api.admin.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertsSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.BotDetectionAlertSubscriptionDTO;

import javax.ws.rs.core.Response;


public interface AlertSubscriptionsApiService {
      public Response getBotDetectionAlertSubscriptions(MessageContext messageContext) throws APIManagementException;
      public Response getSubscribedAlertTypes(MessageContext messageContext) throws APIManagementException;
      public Response subscribeForBotDetectionAlerts(BotDetectionAlertSubscriptionDTO botDetectionAlertSubscriptionDTO, MessageContext messageContext) throws APIManagementException;
      public Response subscribeToAlerts(AlertsSubscriptionDTO alertsSubscriptionDTO, MessageContext messageContext) throws APIManagementException;
      public Response unsubscribeAllAlerts(MessageContext messageContext) throws APIManagementException;
      public Response unsubscribeFromBotDetectionAlerts(String uuid, MessageContext messageContext) throws APIManagementException;
}
