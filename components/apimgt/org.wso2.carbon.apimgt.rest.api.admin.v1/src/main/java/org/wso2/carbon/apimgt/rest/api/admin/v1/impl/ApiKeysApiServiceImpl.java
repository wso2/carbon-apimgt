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

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.ApiKeysApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APIKeyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.APIKeyMappingUtil;

import javax.ws.rs.core.Response;
import java.util.List;

public class ApiKeysApiServiceImpl implements ApiKeysApiService {

    /**
     * Delete an api key provided by the display name
     * @param apiId API ID
     * @param applicationId Application ID
     * @param keyType Application key type
     * @param keyDisplayName API key display name
     * @param messageContext Message context
     * @return API key deletion response
     */
    public Response apiKeysApiIdApplicationIdKeyTypeKeyDisplayNameDelete(String apiId, String applicationId,
                                                                         String keyType, String keyDisplayName,
                                                                         MessageContext messageContext)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        apiAdmin.revokeAPIKey(apiId, applicationId, keyType, keyDisplayName);
        return Response.ok().build();
    }

    /**
     * Returns a list of all available API keys in Active state
     * @param messageContext Message context
     * @return A list of all api keys response
     */
    public Response apiKeysGet(MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        List<APIKeyInfo> apiKeyInfoList = apiAdmin.getAllApiKeys();
        List<APIKeyDTO> apiKeyDTOList =
                APIKeyMappingUtil.fromAPIKeyInfoListToAPIKeyListDTO(apiKeyInfoList);
        return Response.ok().entity(apiKeyDTOList).build();
    }
}
