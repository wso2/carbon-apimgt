package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;

import org.wso2.carbon.graphql.api.devportal.ArtifactData;

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class SubscribeAvailableData {


    public boolean getSubscriptionAvailable(String Id) throws  APIPersistenceException {
        ArtifactData artifactData = new ArtifactData();

        DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);
        String apiTenant = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(devPortalAPI.getProviderName()));
        String subscriptionAvailability = devPortalAPI.getSubscriptionAvailability();
        String subscriptionAllowedTenants =devPortalAPI.getSubscriptionAvailableOrgs();
        boolean IsSubscriptionAvailability = isSubscriptionAvailable(apiTenant,subscriptionAvailability,subscriptionAllowedTenants);
        return IsSubscriptionAvailability;
    }
    private static boolean isSubscriptionAvailable(String apiTenant, String subscriptionAvailability,
                                                   String subscriptionAllowedTenants) {

        String userTenant = RestApiUtil.getRequestedTenantDomain("");
        boolean subscriptionAllowed = false;
        if (!userTenant.equals(apiTenant)) {
            if (APIConstants.SUBSCRIPTION_TO_ALL_TENANTS.equals(subscriptionAvailability)) {
                subscriptionAllowed = true;
            } else if (APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS.equals(subscriptionAvailability)) {
                String allowedTenants[] = null;
                if (subscriptionAllowedTenants != null) {
                    allowedTenants = subscriptionAllowedTenants.split(",");
                    if (allowedTenants != null) {
                        for (String tenant : allowedTenants) {
                            if (tenant != null && tenant.trim().equals(userTenant)) {
                                subscriptionAllowed = true;
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            subscriptionAllowed = true;
        }
        return subscriptionAllowed;
    }
}
