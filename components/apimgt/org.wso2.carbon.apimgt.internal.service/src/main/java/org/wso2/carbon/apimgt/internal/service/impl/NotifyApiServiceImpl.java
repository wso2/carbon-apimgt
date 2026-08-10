package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.NotifyApiService;
import org.wso2.carbon.apimgt.notification.NotificationEventService;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

public class NotifyApiServiceImpl implements NotifyApiService {

    private static final Log log = LogFactory.getLog(NotifyApiServiceImpl.class);
    @Override
    public Response notifyPost(String xWSO2KEYManager, String body, MessageContext messageContext) {

        String authenticatedOrganization = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (!isNotificationDispatchAllowed(authenticatedOrganization)) {
            log.warn("Notification event dispatch is not permitted for organization: "
                    + authenticatedOrganization);
            return Response.status(Response.Status.FORBIDDEN).build();
        }
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

    /**
     * Determines whether the authenticated caller may dispatch notification events.
     * <p>
     * Notification events are published on behalf of the whole deployment by the key manager, and
     * the organization an event applies to is carried in the event payload rather than derived
     * from the caller. Dispatch is therefore restricted to the super tenant.
     *
     * @param authenticatedOrganization organization of the authenticated caller
     * @return true if the caller may dispatch notification events
     */
    static boolean isNotificationDispatchAllowed(String authenticatedOrganization) {

        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(authenticatedOrganization);
    }
}
