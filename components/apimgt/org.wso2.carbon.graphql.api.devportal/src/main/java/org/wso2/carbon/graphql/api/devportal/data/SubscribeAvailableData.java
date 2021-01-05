package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class SubscribeAvailableData {


    public boolean getSubscriptionAvailable(String Id) throws GovernanceException {
        ArtifactData artifactData = new ArtifactData();
        String apiTenant = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(artifactData.getDevportalApis(Id).getAttribute(org.wso2.carbon.apimgt.persistence.APIConstants.API_OVERVIEW_PROVIDER)));
        String subscriptionAvailability = artifactData.getDevportalApis(Id).getAttribute(org.wso2.carbon.apimgt.persistence.APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABILITY);
        String subscriptionAllowedTenants = artifactData.getDevportalApis(Id).getAttribute(org.wso2.carbon.apimgt.persistence.APIConstants.API_OVERVIEW_SUBSCRIPTION_AVAILABLE_TENANTS);
        boolean IsSubscriptionAvailability = isSubscriptionAvailable(apiTenant,subscriptionAvailability,subscriptionAllowedTenants);
        return IsSubscriptionAvailability;
    }
    private static boolean isSubscriptionAvailable(String apiTenant, String subscriptionAvailability,
                                                   String subscriptionAllowedTenants) {

        String userTenant = RestApiUtil.getLoggedInUserTenantDomain();
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
