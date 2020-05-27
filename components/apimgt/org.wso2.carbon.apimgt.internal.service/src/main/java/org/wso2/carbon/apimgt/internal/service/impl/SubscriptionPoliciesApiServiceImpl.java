package org.wso2.carbon.apimgt.internal.service.impl;

        import org.apache.commons.lang3.StringUtils;
        import org.wso2.carbon.apimgt.api.APIManagementException;
        import org.wso2.carbon.apimgt.api.model.subscription.SubscriptionPolicy;
        import org.wso2.carbon.apimgt.impl.dao.SubscriptionValidationDAO;
        import org.wso2.carbon.apimgt.internal.service.SubscriptionPoliciesApiService;
        import org.apache.cxf.jaxrs.ext.MessageContext;

        import javax.ws.rs.core.Response;

public class SubscriptionPoliciesApiServiceImpl implements SubscriptionPoliciesApiService {

    @Override
    public Response subscriptionPoliciesGet(String xWSO2Tenant, MessageContext messageContext) throws APIManagementException {

        if (StringUtils.isNotEmpty(xWSO2Tenant)) {
            return Response.ok().entity(SubscriptionValidationDAO.getAllSubscriptions(Integer.parseInt(xWSO2Tenant))).build();

        } else {
            return Response.ok().entity(SubscriptionValidationDAO.getAllSubscriptionPolicies()).build();
        }
    }

    @Override
    public Response subscriptionPoliciesPolicyIdGet(Integer policyId, MessageContext messageContext) throws APIManagementException {

        SubscriptionPolicy subscriptionPolicy = SubscriptionValidationDAO.getSubscriptionPolicy(policyId);

        if (subscriptionPolicy != null) {
            return Response.ok().entity(subscriptionPolicy).build();
        }
        return null;
    }
}
