package org.wso2.carbon.apimgt.rest.api.admin.mappings;


import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Limit;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierDTO;

import java.util.ArrayList;
import java.util.List;

public class PolicyMappingUtil {

    public static TierDTO fromPolicyToDTO(Policy policy)    {
        TierDTO tierDTO = new TierDTO();
        if (policy != null) {
            tierDTO.setName(policy.getPolicyName());
            tierDTO.setDescription(policy.getDescription());
            tierDTO.setTimeUnit(policy.getDefaultQuotaPolicy().getLimit().getTimeUnit());
            tierDTO.setUnitTime(policy.getDefaultQuotaPolicy().getLimit().getUnitTime());
        } else {
            tierDTO.setName("DummyPolicy");
            tierDTO.setDescription("Testing Policy");
        }

        return tierDTO;
    }

    public static List<TierDTO> fromPoliciesToDTOs(List<Policy> policies)   {
        List<TierDTO> tiers = new ArrayList();

        for (Policy policy : policies)  {
            tiers.add(fromPolicyToDTO(policy));
        }
        return tiers;
    }

    public static Policy toPolicy(String tierLevel, TierDTO tierDTO) {

        Policy policy = null;
        if (APIMgtConstants.ThrottlePolicyConstants.API_LEVEL.equals(tierLevel))    {
            policy = new APIPolicy(tierDTO.getName());
            policy.setDescription(tierDTO.getDescription());
            QuotaPolicy quotaPolicy = new QuotaPolicy();
            Limit limit = new Limit();
            limit.setTimeUnit(tierDTO.getTimeUnit());
            limit.setUnitTime(tierDTO.getUnitTime());
            quotaPolicy.setLimit(limit);
            quotaPolicy.setType("request");
            policy.setDefaultQuotaPolicy(quotaPolicy);
        } else if (APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL.equals(tierLevel)) {
            policy = new ApplicationPolicy(tierDTO.getName());
        } else if (APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL.equals(tierLevel)){
            policy = new SubscriptionPolicy(tierDTO.getName());
        }

        return policy;
    }
}
