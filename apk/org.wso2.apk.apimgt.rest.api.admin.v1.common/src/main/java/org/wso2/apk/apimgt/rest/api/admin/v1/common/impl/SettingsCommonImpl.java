/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.apk.apimgt.rest.api.admin.v1.common.impl;

import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.mappings.SettingsMappingUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.SettingsDTO;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;

public class SettingsCommonImpl {

    private SettingsCommonImpl() {
    }

    /**
     * Retrieves admin portal related server settings
     *
     * @return List of settings
     * @throws APIManagementException When an internal error occurs
     */
    public static SettingsDTO getAdminSettings() throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        boolean isUserAvailable = !APIConstants.WSO2_ANONYMOUS_USER.equalsIgnoreCase(username);
        SettingsMappingUtil settingsMappingUtil = new SettingsMappingUtil();
        return settingsMappingUtil.fromSettingsToDTO(isUserAvailable);
    }
}
