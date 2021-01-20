package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.persistence.dto.DevPortalAPI;
import org.wso2.carbon.apimgt.persistence.exceptions.APIPersistenceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.graphql.api.devportal.modules.TierNameDTO;

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
    public String getThrottlingPoliciesData(String Id) throws APIPersistenceException {


//        Set<Tier> throttlingPolicies =  getAvailableTiers(definedTiers, tiers, apiname);
//        List<String> throttlingPolicyNames = new ArrayList<>();
        String throttlingPolicy = "";
//        for (Tier tier : throttlingPolicies) {
//            throttlingPolicyNames.add(tier.getName());
//        }
//
//        if (throttlingPolicyNames!=null){
//            throttlingPolicy = String.join(",",throttlingPolicyNames);
//        }
        ArtifactData artifactData = new ArtifactData();




//       String  tiers = artifactData.getDevportalApis(Id).getAttribute(APIConstants.API_OVERVIEW_TIER);
//        String[] tierNames = tiers.split("\\|\\|");
//
        DevPortalAPI devPortalAPI = artifactData.getApiFromUUID(Id);
//
        Set<String> tierNames = devPortalAPI.getAvailableTierNames();
        for (String tierName : tierNames) {
            throttlingPolicy += tierName;
        }

        return throttlingPolicy;
    }
}
