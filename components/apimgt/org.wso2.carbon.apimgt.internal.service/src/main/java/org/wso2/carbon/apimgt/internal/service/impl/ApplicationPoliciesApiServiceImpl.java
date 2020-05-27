package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.subscription.ApplicationPolicy;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
import org.wso2.carbon.apimgt.internal.service.ApplicationPoliciesApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.core.Response;

public class ApplicationPoliciesApiServiceImpl implements ApplicationPoliciesApiService {

    @Override
    public Response applicationPoliciesGet(String xWSO2Tenant, MessageContext messageContext)
            throws APIManagementException {

        if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            return Response.ok().entity(SubscriptionValidationDAO.
                    getAllApplicationPolicies(Integer.parseInt(xWSO2Tenant))).build();

        }
        return Response.ok().entity(SubscriptionValidationDAO.getAllApplicationPolicies()).build();

    }

    @Override
    public Response applicationPoliciesPolicyIdGet(Integer policyId, MessageContext messageContext)
            throws APIManagementException {

        ApplicationPolicy applicationPolicy = SubscriptionValidationDAO.getApplicationPolicyById(policyId);
        if (applicationPolicy != null) {
            return Response.ok().entity(applicationPolicy).build();
        }
        return null;
    }
}
