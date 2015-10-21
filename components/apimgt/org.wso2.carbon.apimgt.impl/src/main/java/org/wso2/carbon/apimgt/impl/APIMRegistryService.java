/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.UnsupportedEncodingException;

/**
 * Service interface for accessing the governance registry
 */
public interface APIMRegistryService {
    /**
    * Get resource belonging to a given user from the provided path in config registry
    */
    String getConfigRegistryResourceContent(String tenantDomain, final String registryLocation)
            throws UserStoreException, RegistryException;

    /**
     * Get resource belonging to a given user from the provided path in governance registry
     */
    String getGovernanceRegistryResourceContent(String tenantDomain, final String registryLocation)
            throws UserStoreException, RegistryException;

}
