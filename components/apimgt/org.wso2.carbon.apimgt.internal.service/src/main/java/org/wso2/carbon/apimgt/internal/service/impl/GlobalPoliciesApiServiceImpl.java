package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.model.subscription.GlobalPolicy;
import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.GlobalPoliciesApiService;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;

import java.util.ArrayList;
import java.util.List;


import javax.ws.rs.core.Response;


public class GlobalPoliciesApiServiceImpl implements GlobalPoliciesApiService {

    public Response globalPoliciesGet(String xWSO2Tenant, String policyName, Boolean allTenants, MessageContext messageContext) {
        allTenants = allTenants != null && allTenants;
        SubscriptionValidationDAO subscriptionValidationDAO = new SubscriptionValidationDAO();
        if (allTenants) {
            return Response.ok().entity(SubscriptionValidationDataUtil.
                    fromGlobalPolicyToGlobalPolicyListDTO(subscriptionValidationDAO.
                            getAllGlobalPolicies())).build();
        }

        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            if (StringUtils.isNotEmpty(policyName)) {
                List<GlobalPolicy> model = new ArrayList<>();
                GlobalPolicy globalPolicy = subscriptionValidationDAO.
                        getGlobalPolicyByNameForTenant(policyName, xWSO2Tenant);
                if (globalPolicy != null) {
                    model.add(globalPolicy);
                }
                return Response.ok().entity(SubscriptionValidationDataUtil.
                        fromGlobalPolicyToGlobalPolicyListDTO(model)).build();

            } else {
                return Response.ok().entity(SubscriptionValidationDataUtil.
                        fromGlobalPolicyToGlobalPolicyListDTO(subscriptionValidationDAO.
                                getAllGlobalPolicies(xWSO2Tenant))).build();
            }
        } else {
            if (StringUtils.isNotEmpty(policyName)) {
                return Response.status(Response.Status.BAD_REQUEST.getStatusCode(),
                        "X-WSo2-Tenant header is missing.").build();
            }
        }
        return Response.ok().entity(SubscriptionValidationDataUtil.
                fromGlobalPolicyToGlobalPolicyListDTO(subscriptionValidationDAO.
                        getAllGlobalPolicies())).build();
    }
}
