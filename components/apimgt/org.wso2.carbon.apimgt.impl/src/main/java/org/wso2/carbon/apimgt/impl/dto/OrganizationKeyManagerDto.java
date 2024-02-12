/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.dto;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class OrganizationKeyManagerDto {

    private Map<String, KeyManagerDto> keyManagerMap = new LinkedHashMap<>();
    private Map<String, String> issuerNameMap = new HashMap<>();

    public Map<String, KeyManagerDto> getKeyManagerMap() {

        return keyManagerMap;
    }

    public KeyManagerDto getKeyManagerByName(String name) {

        return keyManagerMap.get(name);
    }

    public void putKeyManagerDto(KeyManagerDto keyManagerDto) {

        if (APIConstants.KeyManager.DEFAULT_KEY_MANAGER.equals(keyManagerDto.getName())) {
            Map<String, KeyManagerDto> newKeyManagerMap = new LinkedHashMap<>();
            newKeyManagerMap.put(keyManagerDto.getName(), keyManagerDto);
            keyManagerMap.remove(keyManagerDto.getName());
            newKeyManagerMap.putAll(keyManagerMap);
            keyManagerMap = newKeyManagerMap;
        } else {
            keyManagerMap.put(keyManagerDto.getName(), keyManagerDto);
        }
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
