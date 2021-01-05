package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.persistence.APIConstants;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getAvailableTiers;
import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getTiers;
import static org.wso2.carbon.apimgt.persistence.utils.PersistenceUtil.replaceEmailDomainBack;

public class MonetizationLabelData {

    public String getMonetizationLabelData(API api){


        Set<Tier> throttlingPolicies = api.getAvailableTiers();
        List<String> throttlingPolicyNames = new ArrayList<>();
        for (Tier tier : throttlingPolicies) {
            throttlingPolicyNames.add(tier.getName());
        }
        String monetizationLabel = null;
        int free = 0, commercial = 0;
        for (Tier tier : throttlingPolicies) {
            if (tier.getTierPlan().equalsIgnoreCase(RestApiConstants.FREE)) {
                free = free + 1;
            } else if (tier.getTierPlan().equalsIgnoreCase(RestApiConstants.COMMERCIAL)) {
                commercial = commercial + 1;
            }
        }
        if (free > 0 && commercial == 0) {
            monetizationLabel= RestApiConstants.FREE;
        } else if (free == 0 && commercial > 0) {
            monetizationLabel = RestApiConstants.PAID;
        } else if (free > 0 && commercial > 0) {
            monetizationLabel =RestApiConstants.FREEMIUM;
        }
        return monetizationLabel;
    }
    public String getMonetizationLabelData(Map<String, Tier> definedTiers,String tiers,String apiname) {

        Set<Tier> throttlingPolicies = getAvailableTiers(definedTiers, tiers, apiname);

        //Set<Tier> throttlingPolicies = apiTypeWrapper.getApi().getAvailableTiers();
        List<String> throttlingPolicyNames = new ArrayList<>();
        for (Tier tier : throttlingPolicies) {
            throttlingPolicyNames.add(tier.getName());
        }
        String monetizationLabel = null;
        int free = 0, commercial = 0;
        for (Tier tier : throttlingPolicies) {
            if (tier.getTierPlan().equalsIgnoreCase(RestApiConstants.FREE)) {
                free = free + 1;
            } else if (tier.getTierPlan().equalsIgnoreCase(RestApiConstants.COMMERCIAL)) {
                commercial = commercial + 1;
            }
        }
        if (free > 0 && commercial == 0) {
            monetizationLabel= RestApiConstants.FREE;
        } else if (free == 0 && commercial > 0) {
            monetizationLabel = RestApiConstants.PAID;
        } else if (free > 0 && commercial > 0) {
            monetizationLabel =RestApiConstants.FREEMIUM;
        }
        return monetizationLabel;
    }
}
