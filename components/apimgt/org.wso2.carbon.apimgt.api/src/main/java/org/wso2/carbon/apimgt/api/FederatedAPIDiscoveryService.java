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
import org.wso2.carbon.apimgt.api.model.DiscoveredAPI;
import org.wso2.carbon.apimgt.api.model.FederatedAPIImportRequest;

import java.util.List;
import java.util.Map;

/**
 * This interface provides functionality to schedule or manually trigger the discovery of APIs from a federated environment.
 */
public interface FederatedAPIDiscoveryService {

    /**
     * Schedules the discovery of APIs from the specified federated environment for a given organization.
     *
     * @param environment the federated environment where the API discovery will be scheduled
     * @param organization the organization associated with the specified environment
     */
    void scheduleDiscovery(Environment environment, String organization);

    /**
     * Stops the discovery of APIs from the specified federated environment for a given organization.
     *
     * @param environment the federated environment where the API discovery should be stopped
     * @param organization the organization associated with the specified environment
     */
    void stopDiscovery(Environment environment, String organization);

    /**
     * Discovers APIs from the gateway and classifies them into NEW or UPDATE categories.
     *
     * @param environment the federated environment
     * @param organization the organization
     * @return A map with keys NEW and UPDATE and corresponding lightweight DiscoveredAPI lists
     * @throws APIManagementException if an error occurs
     */
    default Map<String, List<DiscoveredAPI>> discoverExternalAPIs(Environment environment, String organization)
            throws APIManagementException {
        throw new APIManagementException("discoverExternalAPIs is not supported.");
    }

    /**
     * Imports brand-new APIs from the external gateway into WSO2.
     * For each requested API, the full definition is fetched on-demand, then persisted.
     *
     * @param apiRequests  the APIs to import, each carrying the external gateway identifier and any
     *                     optional display name / description overrides
     * @param environment  the federated environment
     * @param organization the organization
     * @return list of API IDs that failed to import; empty if all succeeded
     * @throws APIManagementException if an error occurs
     */
    default List<String> importNewExternalAPIs(List<FederatedAPIImportRequest> apiRequests, Environment environment,
                                               String organization) throws APIManagementException {
        throw new APIManagementException("importNewExternalAPIs is not supported.");
    }

    /**
     * Updates existing WSO2 APIs whose definitions have changed on the external gateway.
     * For each requested API, the full definition is fetched on-demand and the existing entry is updated.
     *
     * @param apiRequests  the APIs to update, each carrying the external gateway identifier and any
     *                     optional display name / description overrides
     * @param environment  the federated environment
     * @param organization the organization
     * @return list of API IDs that failed to update; empty if all succeeded
     * @throws APIManagementException if an error occurs
     */
    default List<String> updateExternalAPIs(List<FederatedAPIImportRequest> apiRequests, Environment environment,
                                            String organization) throws APIManagementException {
        throw new APIManagementException("updateExternalAPIs is not supported.");
    }
}
