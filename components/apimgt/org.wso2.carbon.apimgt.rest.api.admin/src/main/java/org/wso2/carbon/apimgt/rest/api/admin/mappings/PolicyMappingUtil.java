package org.wso2.carbon.apimgt.rest.api.admin.mappings;


import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierDTO;

public class PolicyMappingUtil {

    public static TierDTO fromPolicyToDTO(Policy policy)    {
        TierDTO tierDTO = new TierDTO();
        tierDTO.setName(policy.getPolicyName());
        tierDTO.setDescription(policy.getDescription());


        return tierDTO;
    }

    public static Policy toPolicy(TierDTO tierDTO) {

        Policy policy = new Policy(tierDTO.getName());
        return policy;


    }
}
