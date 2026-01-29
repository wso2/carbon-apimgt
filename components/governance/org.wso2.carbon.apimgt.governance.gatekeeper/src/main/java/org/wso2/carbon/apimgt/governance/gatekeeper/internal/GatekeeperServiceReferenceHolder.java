/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.gatekeeper.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

/**
 * Service reference holder for the Gatekeeper component.
 */
public class GatekeeperServiceReferenceHolder {

    private static final GatekeeperServiceReferenceHolder instance = new GatekeeperServiceReferenceHolder();

    private APIManagerConfigurationService apiManagerConfigurationService;

    private GatekeeperServiceReferenceHolder() {
        // Private constructor
    }

    public static GatekeeperServiceReferenceHolder getInstance() {
        return instance;
    }

    public APIManagerConfigurationService getAPIManagerConfigurationService() {
        return apiManagerConfigurationService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService apiManagerConfigurationService) {
        this.apiManagerConfigurationService = apiManagerConfigurationService;
    }
}
