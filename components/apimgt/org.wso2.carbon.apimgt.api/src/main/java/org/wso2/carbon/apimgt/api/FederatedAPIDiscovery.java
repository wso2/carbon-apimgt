/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 *
 */

package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.Environment;

import java.util.List;

/**
 * This interface provides functionality to discover APIs from a federated environment.
 */
public interface FederatedAPIDiscovery {


    /**
     * Initializes the FederatedAPIDiscovery with the given environment and organization.
     *
     * @param environment   The environment from which APIs will be discovered.
     * @param organization  The organization for which the discovery is being performed.
     * @throws APIManagementException if an error occurs during initialization.
     */
    void init(Environment environment, String organization) throws APIManagementException;

    /**
     * Discovers APIs from the federated environment.
     * This method should be called to initiate the discovery process.
     */
    List<DiscoveredAPI> discoverAPI();

    default boolean isAPIUpdated(String existingReferenceArtifact, String newReferenceArtifact) {
        return true;
    }
}
