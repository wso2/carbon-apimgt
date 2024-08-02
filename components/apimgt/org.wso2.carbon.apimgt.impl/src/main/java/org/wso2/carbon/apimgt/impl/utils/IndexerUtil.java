/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utility class for Indexers.
 */
public class IndexerUtil {
    private static final Log log = LogFactory.getLog(IndexerUtil.class);

    /**
     * Method to fetch API details for a given resource.
     *
     * @param registry Registry
     * @param resource Resource
     * @param fields   Fields list
     * @throws RegistryException      on failure
     * @throws APIManagementException on failure
     */
    public static void fetchRequiredDetailsFromAssociatedAPI(Registry registry, Resource resource,
                                                             Map<String, List<String>> fields)
            throws RegistryException, APIManagementException {
        String resourceFilePath = resource.getPath();
        String apiPath = resourceFilePath.substring(0, resourceFilePath.lastIndexOf('/') + 1)
                + APIConstants.API_KEY;
        if (registry.resourceExists(apiPath)) {
            Resource apiResource = registry.get(apiPath);
            GenericArtifactManager apiArtifactManager = APIUtil.getArtifactManager(registry, APIConstants.API_KEY);
            GenericArtifact apiArtifact = apiArtifactManager.getGenericArtifact(apiResource.getUUID());
            String apiStatus = apiArtifact.getAttribute(APIConstants.API_OVERVIEW_STATUS).toLowerCase();
            String publisherRoles = apiResource.getProperty(APIConstants.PUBLISHER_ROLES);
            fields.put(APIConstants.API_OVERVIEW_STATUS, Arrays.asList(apiStatus));
            fields.put(APIConstants.PUBLISHER_ROLES, Arrays.asList(publisherRoles));
        } else {
            log.warn("API does not exist at " + apiPath);
        }
    }
}
