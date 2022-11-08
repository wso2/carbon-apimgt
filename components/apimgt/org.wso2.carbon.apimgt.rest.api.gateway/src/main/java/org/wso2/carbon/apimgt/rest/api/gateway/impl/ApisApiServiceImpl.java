package org.wso2.carbon.apimgt.rest.api.gateway.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.entity.Subscription;
import org.wso2.carbon.apimgt.rest.api.gateway.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.ErrorDTO;

import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    public Response apisApiIdGet(String apiId, String tenantDomain, MessageContext messageContext) {

        tenantDomain = GatewayUtils.validateTenantDomain(tenantDomain, messageContext);
        SubscriptionDataStore subscriptionDataStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        if (subscriptionDataStore == null) {
            log.warn("Subscription data store is not initialized for " + tenantDomain);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        API api;
        if (StringUtils.isNotEmpty(apiId)) {
            api = subscriptionDataStore.getAPIByUUID(apiId);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDTO().moreInfo("required parameters " +
                    "are missing")).build();

        }
        if (api == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<Subscription> subscriptionsByAPIId = subscriptionDataStore.getSubscriptionsByAPIId(api.getApiId());
        APIInfoDTO apiInfoDTO = GatewayUtils.generateAPIInfo(api, subscriptionsByAPIId, subscriptionDataStore);
        return Response.ok().entity(apiInfoDTO).build();
    }

    public Response apisGet(String context, String version, String tenantDomain, MessageContext messageContext) {

        tenantDomain = GatewayUtils.validateTenantDomain(tenantDomain, messageContext);
        SubscriptionDataStore subscriptionDataStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        if (subscriptionDataStore == null) {
            log.warn("Subscription data store is not initialized for " + tenantDomain);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (StringUtils.isNotEmpty(context) && StringUtils.isNotEmpty(version)) {
            API api = subscriptionDataStore.getApiByContextAndVersion(context, version);
            if (api == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            APIListDTO apiListDTO = GatewayUtils.generateAPIListDTO(Collections.singletonList(api));
            return Response.ok().entity(apiListDTO).build();
        } else if ((StringUtils.isEmpty(context) && StringUtils.isNotEmpty(version)) ||
                (StringUtils.isNotEmpty(context) && StringUtils.isEmpty(version))) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDTO().moreInfo("required parameters " +
                    "are missing")).build();
        } else {
            List<API> apiList = subscriptionDataStore.getAPIs();
            APIListDTO apiListDTO = GatewayUtils.generateAPIListDTO(apiList);
            return Response.status(Response.Status.OK).entity(apiListDTO).build();
        }
    }
}
