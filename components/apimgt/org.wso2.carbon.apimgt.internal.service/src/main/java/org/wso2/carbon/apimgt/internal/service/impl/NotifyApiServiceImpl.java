package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.NotifyApiService;
import org.wso2.carbon.apimgt.notification.NotificationEventService;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

public class NotifyApiServiceImpl implements NotifyApiService {

    private static final Log log = LogFactory.getLog(NotifyApiServiceImpl.class);
    @Override
    public Response notifyPost(String xWSO2KEYManager, String body, MessageContext messageContext) {

        try {
            NotificationEventService notificationEventService =
                    (NotificationEventService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .getOSGiService(NotificationEventService.class, new Hashtable<>());
            Map<String, List<String>> requestHeaders = messageContext.getHttpHeaders().getRequestHeaders();
            notificationEventService.processEvent(xWSO2KEYManager, body, requestHeaders);
            return Response.ok().build();
        } catch (APIManagementException e) {
            log.error("Error while processing notification", e);
            JSONObject responseObj = new JSONObject();
            responseObj.put("Message", e.getMessage());
            String responseStringObj = String.valueOf(responseObj);
            return Response.serverError().entity(responseStringObj).build();
        }
    }
}
