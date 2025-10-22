/*
 * Copyright (c) 2025, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import java.util.Map;
import java.util.Set;

/**
 * Utility class for MCP related operations
 */
public class MCPUtils {
    /**
     * Validate whether MCP assigned resources are being removed from the API
     * @param apiUUID UUID of the API
     * @param organization Organization of the API
     * @param uriTemplates Set of URI templates defined in the API
     * @throws APIManagementException If validation fails
     */
    public static void validateMCPResources(String apiUUID, String organization, Set<URITemplate> uriTemplates)
            throws APIManagementException {
        if (uriTemplates == null) {
            return;
        }
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        Map<String, Boolean> apiResourcesAssignedToMCP = apiMgtDAO.getAPIResourcesAssignedToMCP(apiUUID, organization);
        for (Map.Entry<String, Boolean> entry : apiResourcesAssignedToMCP.entrySet()) {
            if (entry.getValue()) {
                String key = entry.getKey();
                if (key.split(":").length != 2) {
                    throw new APIManagementException("Invalid resource key found while validating MCP resources",
                            ExceptionCodes.INTERNAL_ERROR);
                }
                String httpResource = key.split(":")[0];
                String httpMethod = key.split(":")[1];
                boolean resourceFound = false;
                for (URITemplate uriTemplate : uriTemplates) {
                    if (uriTemplate.getUriTemplate().equals(httpResource) &&
                            uriTemplate.getHTTPVerb().equals(httpMethod)) {
                        resourceFound = true;
                        break;
                    }
                }
                if (!resourceFound) {
                    throw new APIManagementException("Cannot delete MCP attached resources from API",
                            ExceptionCodes.API_UPDATE_FORBIDDEN_PER_MCP_USAGE);
                }
            }
        }
    }

}
