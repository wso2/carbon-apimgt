/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.impl.dao.ApiKeyMgtDAO;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.internal.service.ApiKeysApiService;
import org.wso2.carbon.apimgt.internal.service.dto.APIKeyDTO;

import java.util.List;

import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

public class ApiKeysApiServiceImpl implements ApiKeysApiService {

    public Response apiKeysGet(String xWSO2Tenant, MessageContext messageContext) throws APIManagementException {

        List<APIKeyInfo> apiKeyInfoList = ApiKeyMgtDAO.getInstance().getAllAPIKeys(xWSO2Tenant);
        return Response.ok().entity(getAPIKeyInfoDTOList(apiKeyInfoList)).build();
    }

    private List<APIKeyDTO> getAPIKeyInfoDTOList(List<APIKeyInfo> apiKeyInfoList) {

        List<APIKeyDTO> apiKeyDTOList = apiKeyInfoList.stream()
                .map(src -> {
                    APIKeyDTO dto = new APIKeyDTO();
                    dto.setApiKeyHash(src.getApiKeyHash());
                    dto.setKeyName(src.getKeyName());
                    dto.setKeyType(src.getKeyType());
                    dto.setStatus(src.getStatus());
                    dto.setExpiresAt((int) Math.min(src.getExpiresAt(), Integer.MAX_VALUE));
                    dto.setAppId(src.getAppId());
                    dto.setAdditionalProperties(src.getProperties());
                    return dto;
                })
                .collect(Collectors.toList());
        return apiKeyDTOList;
    }
}
