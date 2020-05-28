/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.AMDefaultKeyManagerImpl;
import org.wso2.carbon.apimgt.impl.dao.KeyMgtDAO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * This class is responsible for calling the KM management services in WSO2 API-M KeyManager Profile server.
 */
public final class KeyMgtRegistrationService {

    private static final Log log = LogFactory.getLog(KeyMgtRegistrationService.class);

    private KeyMgtRegistrationService() {

        throw new IllegalStateException("Service class for key manager registration");
    }

    /**
     * This method will call the KM server and register an oauth app to manage KM operations.
     *
     * @param tenantDomain TenantDomain to call the services on
     */
    public static void registerKeyMgtApplication(String tenantDomain) {

        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        try {
            //check whether an application is already registered for the tenant
            OAuthApplicationInfo oAuthApplicationInfo = KeyMgtDAO.getInstance().getApplicationForTenant(tenantId);
            if (oAuthApplicationInfo == null) { // if not registered
                log.info("Registering OAuth application for " + tenantDomain);
                oAuthApplicationInfo = ((AMDefaultKeyManagerImpl) KeyManagerHolder.getKeyManagerInstance())
                        .registerKeyMgtApplication(tenantDomain);
                // add the application info to the AM database
                KeyMgtDAO.getInstance().addApplication(oAuthApplicationInfo.getClientId(),
                        oAuthApplicationInfo.getClientSecret(), tenantId);
            } else {
                log.debug("OAuth application already registered for " + tenantDomain + ". Skip registering.");
            }
        } catch (APIManagementException e) {
            log.error("Error registering OAuth Application for tenant: " + tenantDomain + e.getMessage());
        }
    }
}
