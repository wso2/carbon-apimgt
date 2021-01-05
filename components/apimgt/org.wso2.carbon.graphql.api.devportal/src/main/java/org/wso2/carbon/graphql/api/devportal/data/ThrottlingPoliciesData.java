package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Tier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getAvailableTiers;

public class ThrottlingPoliciesData {



    public String getThrottlingPoliciesData(API api){

        Set<Tier> throttlingPolicies = api.getAvailableTiers();

        List<String> throttlingPolicyNames = new ArrayList<>();
        String throttlingPolicy = null;
        for (Tier tier : throttlingPolicies) {
            throttlingPolicyNames.add(tier.getName());
        }

        if (throttlingPolicyNames!=null){
            throttlingPolicy = String.join(",",throttlingPolicyNames);
        }

        return throttlingPolicy;
    }
    public String getThrottlingPoliciesData(Map<String, Tier> definedTiers, String tiers, String apiname){


        Set<Tier> throttlingPolicies =  getAvailableTiers(definedTiers, tiers, apiname);
        List<String> throttlingPolicyNames = new ArrayList<>();
        String throttlingPolicy = null;
        for (Tier tier : throttlingPolicies) {
            throttlingPolicyNames.add(tier.getName());
        }

        if (throttlingPolicyNames!=null){
            throttlingPolicy = String.join(",",throttlingPolicyNames);
        }

        return throttlingPolicy;
    }
}
