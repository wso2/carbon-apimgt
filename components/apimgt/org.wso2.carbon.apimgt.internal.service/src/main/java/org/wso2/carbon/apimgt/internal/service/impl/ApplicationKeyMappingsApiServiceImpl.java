package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.internal.service.ApplicationKeyMappingsApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.core.Response;

public class ApplicationKeyMappingsApiServiceImpl implements ApplicationKeyMappingsApiService {

    @Override
    public Response applicationKeyMappingsConsumerKeyGet(String consumerKey, MessageContext messageContext) throws APIManagementException {

        return Response.ok().entity(SubscriptionValidationDAO.getApplicationKeyMapping(consumerKey)).build();
    }

    @Override
    public Response applicationKeyMappingsGet(String xWSO2Tenant, MessageContext messageContext) throws APIManagementException {

        if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            return Response.ok().entity(SubscriptionValidationDAO.
                    getAllApplicationKeyMappings(Integer.parseInt(xWSO2Tenant))).build();

        }
        return Response.ok().entity(SubscriptionValidationDAO.getAllApplicationKeyMappings()).build();

    }
}
