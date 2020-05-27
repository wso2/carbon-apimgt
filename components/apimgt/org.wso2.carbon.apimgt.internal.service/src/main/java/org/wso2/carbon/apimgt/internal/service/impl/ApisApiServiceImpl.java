package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.subscription.API;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.internal.service.ApisApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.core.Response;

public class ApisApiServiceImpl implements ApisApiService {

    @Override
    public Response apisApiIdGet(Integer apiId, MessageContext messageContext) throws APIManagementException {

        API api = SubscriptionValidationDAO.getApi(apiId);
        if (api != null) {
            return Response.ok().entity(api).build();
        }
        return null;
    }

    @Override
    public Response apisGet(String xWSO2Tenant, MessageContext messageContext) throws APIManagementException {

        if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            return Response.ok().entity(SubscriptionValidationDAO.getAllApis(Integer.parseInt(xWSO2Tenant))).build();
        }
        return Response.ok().entity(SubscriptionValidationDAO.getAllApis()).build();

    }
}
