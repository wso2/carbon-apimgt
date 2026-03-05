/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

public class APIKeyUtils {

    private static final Log log = LogFactory.getLog(JWTUtil.class);

    /**
     * Check whether Lightweight API Key Generation is enabled.
     *
     * @return true if Lightweight API Key Generation is enabled, false otherwise
     */
    public static boolean isLightweightAPIKeyGenerationEnabled() {
        try {
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                    .getAPIManagerConfiguration();
            if (config != null) {
                String lightweightAPIKeyGenerationEnabled = config.getFirstProperty(APIConstants.LIGHTWEIGHT_API_KEY_GENERATION_ENABLED);
                return Boolean.parseBoolean(lightweightAPIKeyGenerationEnabled);
            }
        } catch (Exception e) {
            log.error("Error while reading Lightweight API Key Generation configuration", e);
        }
        return true;
    }
}
