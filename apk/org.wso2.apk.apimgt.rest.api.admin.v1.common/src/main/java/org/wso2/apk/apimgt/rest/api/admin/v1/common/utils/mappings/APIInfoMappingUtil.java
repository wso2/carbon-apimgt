/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.mappings;

import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.APIInfoDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.APIInfoListDTO;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class APIInfoMappingUtil {

    /**
     * Converts a APIIdentifier object into APIInfoDTO
     *
     * @param apiId APIIdentifier object
     * @return APIInfoDTO corresponds to APIIdentifier object
     */
    private static APIInfoDTO fromAPIInfoToDTO(APIIdentifier apiId)
            throws UnsupportedEncodingException {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        APIIdentifier apiIdEmailReplacedBack = new APIIdentifier(APIUtil.replaceEmailDomainBack(apiId.getProviderName
                ()).replace(RestApiConstants.API_ID_DELIMITER, RestApiConstants.URL_ENCODED_API_ID_DELIMITER),
                URLEncoder.encode(apiId.getApiName(), RestApiConstants.CHARSET).replace(RestApiConstants
                        .API_ID_DELIMITER, RestApiConstants.URL_ENCODED_API_ID_DELIMITER), apiId.getVersion().
                replace(RestApiConstants.API_ID_DELIMITER, RestApiConstants.URL_ENCODED_API_ID_DELIMITER));
        apiInfoDTO.setName(apiIdEmailReplacedBack.getApiName());
        apiInfoDTO.setVersion(apiIdEmailReplacedBack.getVersion());
        apiInfoDTO.setProvider(apiIdEmailReplacedBack.getProviderName());
        return apiInfoDTO;
    }

    /**
     * Converts a List object of APIIdentifiers into a DTO
     *
     * @param apiIds a list of APIIdentifier objects
     * @return APIInfoListDTO object containing APIInfoDTOs
     */
    public static APIInfoListDTO fromAPIInfoListToDTO(List<APIIdentifier> apiIds) throws
            UnsupportedEncodingException {
        APIInfoListDTO apiInfoListDTO = new APIInfoListDTO();
        List<APIInfoDTO> apiInfoDTOs = apiInfoListDTO.getList();
        if (apiInfoDTOs == null) {
            apiInfoDTOs = new ArrayList<>();
            apiInfoListDTO.setList(apiInfoDTOs);
        }
        for (APIIdentifier apiId : apiIds) {
            apiInfoDTOs.add(fromAPIInfoToDTO(apiId));
        }
        apiInfoListDTO.setCount(apiInfoDTOs.size());
        return apiInfoListDTO;
    }
}
