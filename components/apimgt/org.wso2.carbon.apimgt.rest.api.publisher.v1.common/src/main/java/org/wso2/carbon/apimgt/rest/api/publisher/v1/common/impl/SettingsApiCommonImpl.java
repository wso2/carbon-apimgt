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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl;

import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.SettingsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SettingsDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Util class for SettingsApiService related operations
 */
public class SettingsApiCommonImpl {

    private SettingsApiCommonImpl() {
        //To hide default constructor
    }

    public static SettingsDTO getSettings(String organization) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        boolean isUserAvailable = !APIConstants.WSO2_ANONYMOUS_USER.equalsIgnoreCase(username);
        SettingsMappingUtil settingsMappingUtil = new SettingsMappingUtil();
        return settingsMappingUtil.fromSettingstoDTO(isUserAvailable, organization);
    }

    /**
     * @param definition Swagger Definition
     * @return List of scopes
     * @throws APIManagementException If error in parsing the scopes from the definition
     */
    public static List<String> getScopeListForSwaggerDefinition(String definition) throws APIManagementException {

        APIDefinition parser = OASParserUtil.getOASParser(definition);
        Set<Scope> scopeSet = parser.getScopes(definition);
        List<String> scopeList = new ArrayList<>();
        for (Scope entry : scopeSet) {
            scopeList.add(entry.getKey());
        }
        return scopeList;
    }

}
