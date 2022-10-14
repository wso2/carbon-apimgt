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
package org.wso2.apk.apimgt.impl.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.APIConstants.ConfigType;
import org.wso2.apk.apimgt.impl.dao.SystemConfigurationsDAO;
import org.wso2.apk.apimgt.impl.dto.UserRegistrationConfigDTO;

import java.util.Iterator;

/**
 * Config Service Implementation for retrieve configurations.
 */
public class APIMConfigServiceImpl implements APIMConfigService {

    private static final Log log = LogFactory.getLog(APIMConfigServiceImpl.class);
    public static final String ERROR_WHILE_RETRIEVING_EXTERNAL_STORES_CONFIGURATION = "Error while retrieving External Stores Configuration from registry";
    protected SystemConfigurationsDAO systemConfigurationsDAO;

    public APIMConfigServiceImpl() {
        systemConfigurationsDAO = SystemConfigurationsDAO.getInstance();
    }

    @Override
    public void addExternalStoreConfig(String organization, String externalStoreConfig) {
    }

    @Override
    public void updateExternalStoreConfig(String organization, String externalStoreConfig)
    {
    }

    @Override
    public String getExternalStoreConfig(String organization) throws APIManagementException {

        //TODO registry implementation
//        if (organization == null) {
//            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
//        }
//        try {
//            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
//            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
//                APIUtil.loadTenantRegistry(tenantId);
//            }
//            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
//                    .getGovernanceSystemRegistry(tenantId);
//            if (registry.resourceExists(APIConstants.EXTERNAL_API_STORES_LOCATION)) {
//                Resource resource = registry.get(APIConstants.EXTERNAL_API_STORES_LOCATION);
//                return new String((byte[]) resource.getContent(), Charset.defaultCharset());
//            } else {
//                return null;
//            }
//
//        } catch (RegistryException e) {
//            throw new APIManagementException(ERROR_WHILE_RETRIEVING_EXTERNAL_STORES_CONFIGURATION, e,
//                    ExceptionCodes.ERROR_RETRIEVE_EXTERNAL_STORE_CONFIG);
//        }
        return ""; //Dummy return
    }

    @Override
    public void addTenantConfig(String organization, String tenantConfig) throws APIManagementException {

        if (organization == null) {
            organization = APIConstants.SUPER_TENANT_DOMAIN;
        }
        systemConfigurationsDAO.addSystemConfig(organization, ConfigType.TENANT.toString(), tenantConfig);
    }

    @Override
    public String getTenantConfig(String organization) throws APIManagementException {

        if (organization == null) {
            organization = APIConstants.SUPER_TENANT_DOMAIN;
        }
        return systemConfigurationsDAO.getSystemConfig(organization, ConfigType.TENANT.toString());
    }

    @Override
    public void updateTenantConfig(String organization, String tenantConfig) throws APIManagementException {

    }

    @Override
    public String getWorkFlowConfig(String organization) throws APIManagementException {

        return "";
    }

    @Override
    public void updateWorkflowConfig(String organization, String workflowConfig) throws APIManagementException {

    }

    @Override
    public void addWorkflowConfig(String organization, String workflowConfig) throws APIManagementException {

    }

    @Override
    public String getGAConfig(String organization) throws APIManagementException {

        return "";
    }

    @Override
    public void updateGAConfig(String organization, String gaConfig) throws APIManagementException {

    }

    @Override
    public void addGAConfig(String organization, String gaConfig) throws APIManagementException {

    }

    @Override
    public UserRegistrationConfigDTO getSelfSighupConfig(String organization) throws APIManagementException {

        if (organization == null) {
            organization = APIConstants.SUPER_TENANT_DOMAIN;
        }
        JsonObject tenantConfig = (JsonObject) new JsonParser().parse(getTenantConfig(organization));
        if (tenantConfig.has(APIConstants.SELF_SIGN_UP_NAME)) {
            return getSignupUserRegistrationConfigDTO((JsonObject) tenantConfig.get(APIConstants.SELF_SIGN_UP_NAME));
        } else {
            return null;
        }
    }

    private static UserRegistrationConfigDTO getSignupUserRegistrationConfigDTO(JsonObject selfSighupConfig) {

        UserRegistrationConfigDTO config = new UserRegistrationConfigDTO();
        JsonArray roles = (JsonArray) selfSighupConfig.get(APIConstants.SELF_SIGN_UP_REG_ROLES_ELEM);
        Iterator<JsonElement> rolesIterator = roles.iterator();
        while (rolesIterator.hasNext()) {
            config.getRoles().add(rolesIterator.next().getAsString());
        }
        return config;
    }

    @Override
    public void updateSelfSighupConfig(String organization, String selfSignUpConfig) {
    }

    @Override
    public void addSelfSighupConfig(String organization, String selfSignUpConfig) {
    }
}
