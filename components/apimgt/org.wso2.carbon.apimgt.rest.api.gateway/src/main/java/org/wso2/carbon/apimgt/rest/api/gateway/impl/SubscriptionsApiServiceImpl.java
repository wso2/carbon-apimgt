package org.wso2.carbon.apimgt.rest.api.gateway.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.rest.api.gateway.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.SubscriptionDTO;

import javax.ws.rs.core.Response;

public class SubscriptionsApiServiceImpl implements SubscriptionsApiService {

    private static final Log log = LogFactory.getLog(SubscriptionsApiServiceImpl.class);
    private boolean debugEnabled = log.isDebugEnabled();

    public Response subscriptionsGet(String apiUUID, String appUUID, String tenantDomain,
                                     MessageContext messageContext) {

        tenantDomain = GatewayUtils.validateTenantDomain(tenantDomain, messageContext);
        if (debugEnabled) {
            log.debug("Retrieving subscription - apiUUID: " + apiUUID + ", appUUID: " + appUUID + 
                    ", tenant: " + tenantDomain);
        }
        SubscriptionDataStore subscriptionDataStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        if (subscriptionDataStore == null) {
            log.warn("Subscription data store is not initialized for " + tenantDomain);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (StringUtils.isNotEmpty(apiUUID) && StringUtils.isNotEmpty(appUUID)) {
            Subscription subscription = subscriptionDataStore.getSubscriptionByUUID(apiUUID, appUUID);
            if (subscription == null) {
                if (debugEnabled) {
                    log.debug("No subscription found for apiUUID: " + apiUUID + ", appUUID: " + appUUID);
                }
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            SubscriptionDTO subscriptionDTO = GatewayUtils.convertToSubscriptionDto(subscription);
            if (debugEnabled) {
                log.debug("Successfully retrieved subscription for apiUUID: " + apiUUID + 
                        ", appUUID: " + appUUID);
            }
            return Response.ok().entity(subscriptionDTO).build();

        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDTO().moreInfo("required parameters " +
                    "are missing")).build();
        }
    }
}
