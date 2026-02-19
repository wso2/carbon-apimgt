/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APIKeyDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Util class to map alerts to DTOs
 */
public class APIKeyMappingUtil {

    /**
     * Map APIKeyInfo list to APIKeyDTO list
     *
     * @param apiKeyInfoList
     * @return List<APIKeyDTO>
     */
    public static List<APIKeyDTO> fromAPIKeyInfoListToAPIKeyListDTO(
            List<APIKeyInfo> apiKeyInfoList) {

        List<APIKeyDTO> apiKeyDTOList = apiKeyInfoList.stream()
                .map(src -> {
                    APIKeyDTO dto = new APIKeyDTO();
                    dto.setKeyDisplayName(src.getKeyDisplayName());
                    dto.setApiName(src.getApiName());
                    dto.setApplicationName(src.getApplicationName());
                    dto.setKeyType(APIKeyDTO.KeyTypeEnum.valueOf(src.getKeyType()));
                    dto.setUser(src.getAuthUser());
                    dto.setIssuedOn(src.getCreatedTime());
                    dto.setValidityPeriod(Math.toIntExact(src.getValidityPeriod()));
                    dto.setLastUsed(src.getLastUsedTime());
                    return dto;
                })
                .collect(Collectors.toList());
        return apiKeyDTOList;
    }
}
