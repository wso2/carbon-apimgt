package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.internal.service.*;
import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.core.Response;

public class SubscriptionsApiServiceImpl implements SubscriptionsApiService {

    @Override
    public Response subscriptionsGet(String xWSO2Tenant, MessageContext messageContext) throws APIManagementException {

        if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            return Response.ok().entity(SubscriptionValidationDAO.getAllSubscriptions(Integer.parseInt(xWSO2Tenant))).build();
        } else {
            return Response.ok().entity(SubscriptionValidationDAO.getAllSubscriptions()).build();
        }
    }

    @Override
    public Response subscriptionsSubscriptionIdGet(Integer subscriptionId, MessageContext messageContext) throws APIManagementException {

        return Response.ok().entity(SubscriptionValidationDAO.getSubscriptionById(subscriptionId)).build();

    }
}
