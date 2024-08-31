package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.internal.service.*;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.internal.service.dto.APIDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.Response;


public class SubscribeInternalApiServiceImpl implements SubscribeInternalApiService {

    public Response subscribeToAPI(String xWSO2Tenant, Integer appId, String appUuid, APIDTO api,
                                   MessageContext messageContext) {
        SubscriptionValidationDAO subscriptionValidationDAO = new SubscriptionValidationDAO();
        Map<String, Object> subDetails = null;
        String defaultTier = APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS;
        String apiType = api.getApiType();
        int apiId = api.getApiId();
        if ("WS".equals(apiType) || "WEBSUB".equals(apiType) || "SSE".equals(apiType)) {
            defaultTier = APIConstants.DEFAULT_SUB_POLICY_ASYNC_SUBSCRIPTIONLESS;
        }
        try {
            String subscriber = subscriptionValidationDAO.getApplicationSubscriber(appUuid);
            String subscriberTenant = MultitenantUtils.getTenantDomain(subscriber);
            int tenantId = APIUtil.getTenantId(subscriberTenant);
            subDetails = subscriptionValidationDAO
                    .subscribeToAPI(apiId, appId, defaultTier, subscriber);
            int subscriptionId = (int) subDetails.get("id");
            String subscriptionUuid = (String) subDetails.get("uuid");
            SubscriptionEvent subscriptionEvent = new SubscriptionEvent(UUID.randomUUID().toString(),
                    System.currentTimeMillis(), APIConstants.EventType.SUBSCRIPTIONS_CREATE.name(), tenantId,
                    subscriberTenant, subscriptionId, subscriptionUuid, apiId, api.getUuid(),
                    appId, appUuid, defaultTier, APIConstants.SubscriptionStatus.UNBLOCKED,
                    api.getName(), api.getVersion());
            APIUtil.sendNotification(subscriptionEvent, APIConstants.NotifierType.SUBSCRIPTIONS.name());
        } catch (APIManagementException e) {
            Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
        return Response.ok().entity(subDetails).build();
    }
}
