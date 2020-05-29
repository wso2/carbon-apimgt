/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.admin.v1.SettingsApiService;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.SettingsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class SettingsApiServiceImpl implements SettingsApiService {

    private static final Log log = LogFactory.getLog(SettingsApiServiceImpl.class);

    @Override
    public Response settingsGet(MessageContext messageContext) {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            boolean isUserAvailable = false;
            if (!APIConstants.WSO2_ANONYMOUS_USER.equalsIgnoreCase(username)) {
                isUserAvailable = true;
            }
            SettingsMappingUtil settingsMappingUtil = new SettingsMappingUtil();
            SettingsDTO settingsDTO = settingsMappingUtil.fromSettingstoDTO(isUserAvailable);
            return Response.ok().entity(settingsDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Admin Settings";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get all scopes of a user
     *
     * @param username Search query
     * @return Scope list
     */
    @Override
    public Response settingsScopesGet(String username, String scopeName, MessageContext messageContext) {
        String[] userRoles ;
        List<String> scopeList = new ArrayList<>();
        SettingsDTO settingsDTO = new SettingsDTO();
        org.wso2.carbon.user.api.UserStoreManager userStoreManager;
        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(MultitenantUtils.getTenantDomain(username));
            userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            userRoles = userStoreManager.getRoleListOfUser(username);

            SettingsMappingUtil settingsMappingUtil = new SettingsMappingUtil();
            if (settingsMappingUtil.GetRoleScopeList(userRoles, username).contains(scopeName)) {
                scopeList.add(scopeName);
                settingsDTO.setScopes(scopeList);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException  e) {
            String errorMessage = "Error when getting the list of scopes";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(settingsDTO).build();
    }
}
