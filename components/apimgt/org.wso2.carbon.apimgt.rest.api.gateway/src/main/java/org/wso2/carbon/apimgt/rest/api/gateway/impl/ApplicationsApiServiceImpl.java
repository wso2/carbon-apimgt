package org.wso2.carbon.apimgt.rest.api.gateway.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.Application;
import org.wso2.carbon.apimgt.rest.api.gateway.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.ErrorDTO;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

public class ApplicationsApiServiceImpl implements ApplicationsApiService {

    private static final Log log = LogFactory.getLog(ApplicationsApiServiceImpl.class);

    public Response applicationsGet(String name, String uuid, String tenantDomain, MessageContext messageContext) {

        tenantDomain = GatewayUtils.validateTenantDomain(tenantDomain, messageContext);
        SubscriptionDataStore subscriptionDataStore =
                SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        if (subscriptionDataStore == null) {
            log.warn("Subscription data store is not initialized for " + tenantDomain);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        List<Application> applicationList;
        if (StringUtils.isNotEmpty(name)) {
            applicationList = subscriptionDataStore.getApplicationsByName(name);
        } else if (StringUtils.isNotEmpty(uuid)) {
            applicationList = new ArrayList<>();
            if (subscriptionDataStore.getApplicationByUUID(uuid) != null) {
                applicationList.add(subscriptionDataStore.getApplicationByUUID(uuid));
            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDTO().moreInfo("required parameters " +
                    "are missing")).build();
        }
        if (applicationList == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        ApplicationListDTO applicationListDTO = GatewayUtils.generateApplicationList(applicationList,
                subscriptionDataStore);
        return Response.ok().entity(applicationListDTO).build();
    }
}
