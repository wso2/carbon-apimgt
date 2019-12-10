/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.security.vault.MediationSecurityAdminService;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Mediation Security Admin Service Client to encode the passwords and store
 * into the registry .
 */

public class MediationSecurityAdminServiceProxy {

    private MediationSecurityAdminService mediationSecurityAdminService;
    private String tenantDomain;

    public MediationSecurityAdminServiceProxy(String tenantDomain) {

        mediationSecurityAdminService = ServiceReferenceHolder.getInstance().getMediationSecurityAdminService();
        this.tenantDomain = tenantDomain;
    }

    /**
     * encrypt the plain text password
     *
     * @param cipher        init cipher
     * @param plainTextPass plain text password
     * @return encrypted password
     * @throws APIManagementException
     */
    public String doEncryption(String plainTextPass) throws APIManagementException {

        String encodedValue = null;
        try {
            encodedValue = mediationSecurityAdminService.doEncrypt(plainTextPass);
//			encodedValue = CryptoUtil.getDefaultCryptoUtil()
//			                         .encryptAndBase64Encode(plainTextPass.getBytes()); //why ESB can not use this?
        } catch (Exception e) {
            String msg = "Failed to encrypt the secured endpoint password, " + e.getMessage();
            throw new APIManagementException(msg, e);
        }
        return encodedValue;
    }

    public boolean isAliasExist(String alias) throws APIManagementException {

        UserRegistry registry = GatewayUtils.getRegistry(tenantDomain);
        PrivilegedCarbonContext.startTenantFlow();
        if (tenantDomain != null && StringUtils.isNotEmpty(tenantDomain)) {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
        } else {
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
        }
        try {
            if (registry.resourceExists(APIConstants.API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION)) {
                Resource resource = registry.get(APIConstants.API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION);
                if (resource.getProperty(alias) != null) {
                    return true;
                }
            }
            return false;
        } catch (RegistryException e) {
            throw new APIManagementException("Error while reading registry resource "
                    + APIConstants.API_SYSTEM_CONFIG_SECURE_VAULT_LOCATION + " for tenant " + tenantDomain);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
