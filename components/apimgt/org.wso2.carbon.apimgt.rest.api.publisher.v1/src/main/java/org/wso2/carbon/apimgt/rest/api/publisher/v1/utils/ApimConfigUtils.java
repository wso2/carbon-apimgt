/*
 * Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils;

import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

import java.util.Map;

/**
 * Contains util methods relevant to the APIM related configurations
 */
public class ApimConfigUtils {

    public static boolean isBackendUrlValidationForOrgEnabled() {
        boolean isOrgValidationEnabled = false;
        Map<String, String> apiCreatorValidationConfigs = APIManagerConfiguration.getApiCreatorValidationProperties();
        if (apiCreatorValidationConfigs != null &&
                apiCreatorValidationConfigs.containsKey(APIConstants.ENABLE_CHOREO_API_BACKEND_URL_ORG_VALIDATION)) {
            isOrgValidationEnabled = Boolean.valueOf(apiCreatorValidationConfigs.get(
                    APIConstants.ENABLE_CHOREO_API_BACKEND_URL_ORG_VALIDATION));
        }
        return isOrgValidationEnabled;
    }
}
