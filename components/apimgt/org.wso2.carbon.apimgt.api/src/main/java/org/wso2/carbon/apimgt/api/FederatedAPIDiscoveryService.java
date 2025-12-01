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

/**
 * This interface provides functionality to schedule the discovery of APIs from a federated environment.
 */
public interface FederatedAPIDiscoveryService {
    /**
     * Schedules the discovery of APIs from a federated environment.
     * @param environment The federated environment for which to schedule API discovery.
     * @param organization The organization context for the discovery operation.
     */
    void scheduleDiscovery(Environment environment, String organization);

    /**
     * Stops the discovery of APIs from a federated environment.
     * @param environment The federated environment for which to stop API discovery.
     * @param organization The organization context for the discovery operation.
     */
    void stopDiscovery(Environment environment, String organization);
}
