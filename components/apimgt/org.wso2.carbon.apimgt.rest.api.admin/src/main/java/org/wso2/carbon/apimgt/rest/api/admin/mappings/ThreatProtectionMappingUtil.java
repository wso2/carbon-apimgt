package org.wso2.carbon.apimgt.rest.api.admin.mappings;

import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThreatProtectionPolicyDTO;

public class ThreatProtectionMappingUtil {
    /**
     * Converts ThreatProtectionPolicy core model ThreatProtectionPolicyDTO rest api core model
     * @param policy apimgt core model of ThreatProtectionPolicy
     * @return ThreatProtectionPolicyDTO rest api core model
     */
    public static ThreatProtectionPolicyDTO toThreatProtectionPolicyDTO(ThreatProtectionPolicy policy) {
        if (policy == null) return null;

        ThreatProtectionPolicyDTO dto = new ThreatProtectionPolicyDTO();
        dto.setUuid(policy.getUuid());
        dto.setName(policy.getName());
        dto.setType(policy.getType());
        dto.setPolicy(policy.getPolicy());
        return dto;
    }


    /**
     * Converts rest api core ThreatProtectionJsonPolicy into apimgt core ThreatProtectionJsonPolicy
     * @param dto rest api core ThreatProtectionJsonPolicyDTO
     * @return apimgt core ThreatProtectionJsonPolicy
     */
    public static ThreatProtectionPolicy toThreatProtectionPolicy(ThreatProtectionPolicyDTO dto) {
        if (dto == null) return null;
        ThreatProtectionPolicy policy = new ThreatProtectionPolicy();
        policy.setUuid(dto.getUuid());
        policy.setType(dto.getType());
        policy.setName(dto.getName());
        policy.setPolicy(dto.getPolicy());
        return policy;
    }
}
