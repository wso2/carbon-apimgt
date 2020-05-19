package org.wso2.carbon.apimgt.impl.dto;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidator;

import java.util.HashMap;
import java.util.Map;

public class TenantKeyManagerDto {

    private Map<String, KeyManagerDto> keyManagerMap = new HashMap<>();
    private Map<String, String> issuerNameMap = new HashMap<>();

    public Map<String, KeyManagerDto> getKeyManagerMap() {

        return keyManagerMap;
    }

    public KeyManagerDto getKeyManagerByName(String name) {

        return keyManagerMap.get(name);
    }

    public void putKeyManagerDto(KeyManagerDto keyManagerDto) {

        keyManagerMap.put(keyManagerDto.getName(), keyManagerDto);
        issuerNameMap.put(keyManagerDto.getIssuer(), keyManagerDto.getName());
    }

    public void removeKeyManagerDtoByName(String name) {

        KeyManagerDto keyManagerDto = keyManagerMap.get(name);
        if (keyManagerDto != null) {
            issuerNameMap.remove(keyManagerDto.getIssuer());
        }
        keyManagerMap.remove(name);
    }

    public JWTValidator getJWTValidatorByIssuer(String issuer) {

        String keyManagerName = issuerNameMap.get(issuer);
        if (StringUtils.isNotEmpty(keyManagerName)) {
            KeyManagerDto keyManagerDto = keyManagerMap.get(keyManagerName);
            if (keyManagerDto != null) {
                return keyManagerDto.getJwtValidator();
            }
        }
        return null;
    }

    public KeyManagerDto getKeyManagerDtoByIssuer(String issuer) {

        String keyManagerName = issuerNameMap.get(issuer);
        if (StringUtils.isNotEmpty(keyManagerName)) {
            return keyManagerMap.get(keyManagerName);
        }
        return null;
    }
}
