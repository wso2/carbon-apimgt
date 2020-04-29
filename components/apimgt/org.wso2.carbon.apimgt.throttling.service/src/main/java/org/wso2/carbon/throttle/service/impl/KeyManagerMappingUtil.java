package org.wso2.carbon.throttle.service.impl;

import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.throttle.service.dto.KeyManagerDTO;

public class KeyManagerMappingUtil {
    public static KeyManagerDTO toKeyManagerDTO(String tenantDomain,KeyManagerConfigurationDTO keyManagerConfigurationDTO){
        KeyManagerDTO keyManagerDTO = new KeyManagerDTO();
        keyManagerDTO.setEnabled(keyManagerConfigurationDTO.isEnabled());
        keyManagerDTO.setName(keyManagerConfigurationDTO.getName());
        keyManagerDTO.setTenantDomain(tenantDomain);
        keyManagerDTO.setType(keyManagerConfigurationDTO.getType());
        keyManagerDTO.setConfiguration(keyManagerConfigurationDTO.getAdditionalProperties());
        return keyManagerDTO;
    }
}
