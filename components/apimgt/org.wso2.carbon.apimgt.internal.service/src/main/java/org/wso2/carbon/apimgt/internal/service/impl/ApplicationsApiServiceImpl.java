package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.internal.service.ApplicationsApiService;

import javax.ws.rs.core.Response;

public class ApplicationsApiServiceImpl implements ApplicationsApiService {

    @Override
    public Response applicationsApplicationIdGet(Integer applicationId, MessageContext messageContext) throws APIManagementException {

        return Response.ok().entity(SubscriptionValidationDAO.getApplicationById(applicationId)).build();
    }

    @Override
    public Response applicationsGet(String xWSO2Tenant, MessageContext messageContext) throws APIManagementException {

        if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            return Response.ok().entity(SubscriptionValidationDAO.getAllApplications(Integer.parseInt(xWSO2Tenant))).build();
        }
        return Response.ok().entity(SubscriptionValidationDAO.getAllApplications()).build();

    }
}
