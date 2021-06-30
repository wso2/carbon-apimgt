/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.KeyManagerInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.KeyManagerListDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class used to convert KeyManager Models to rest API related Models.
 */
public class KeyManagerMappingUtil {

    public static KeyManagerListDTO toKeyManagerListDto(List<KeyManagerConfigurationDTO> keyManagerConfigurations) {

        KeyManagerListDTO keyManagerListDTO = new KeyManagerListDTO();
        List<KeyManagerInfoDTO> keyManagerInfoDTOList = new ArrayList<>();
        for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurations) {
            keyManagerInfoDTOList.add(fromKeyManagerConfigurationDtoToKeyManagerInfoDto(keyManagerConfigurationDTO));
        }
        keyManagerListDTO.setList(keyManagerInfoDTOList);
        keyManagerListDTO.setCount(keyManagerInfoDTOList.size());
        return keyManagerListDTO;
    }

    private static KeyManagerInfoDTO fromKeyManagerConfigurationDtoToKeyManagerInfoDto(
            KeyManagerConfigurationDTO configurationDto) {

        KeyManagerInfoDTO keyManagerInfoDTO = new KeyManagerInfoDTO();
        keyManagerInfoDTO.setName(configurationDto.getName());
        keyManagerInfoDTO.setDisplayName(configurationDto.getDisplayName());
        keyManagerInfoDTO.setDescription(configurationDto.getDescription());
        keyManagerInfoDTO.setId(configurationDto.getUuid());
        keyManagerInfoDTO.setEnabled(configurationDto.isEnabled());
        keyManagerInfoDTO.setType(configurationDto.getType());
        return keyManagerInfoDTO;
    }
}
