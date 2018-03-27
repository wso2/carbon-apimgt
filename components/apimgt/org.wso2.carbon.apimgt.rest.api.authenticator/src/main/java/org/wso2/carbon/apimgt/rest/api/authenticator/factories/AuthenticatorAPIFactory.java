/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.authenticator.factories;

import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.core.dao.SystemApplicationDao;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.authenticator.AuthenticatorService;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.APIMAppConfigurationService;

public class AuthenticatorAPIFactory {
    private static AuthenticatorAPIFactory instance = new AuthenticatorAPIFactory();
    private AuthenticatorService service;

    private AuthenticatorAPIFactory() {
    }

    /**
     * Get an instance of AuthenticatorAPIFactory
     *
     * @return instance of AuthenticatorAPIFactory
     */
    public static AuthenticatorAPIFactory getInstance() {
        return instance;
    }

    /**
     * Get an instance of AuthenticatorService
     *
     * @return AuthenticatorService
     * @throws APIMgtDAOException     if failed to initialize SystemApplicationDao
     * @throws IdentityProviderException if failed to initialize IdentityProvider
     */
    public synchronized AuthenticatorService getService() throws APIMgtDAOException, IdentityProviderException {
        if (service == null) {
            IdentityProvider identityProvider = APIManagerFactory.getInstance().getIdentityProvider();
            SystemApplicationDao systemApplicationDao = DAOFactory.getSystemApplicationDao();
            APIMConfigurationService apimConfigurationService = APIMConfigurationService.getInstance();
            APIMAppConfigurationService apimAppConfigurationService = APIMAppConfigurationService.getInstance();

            service = new AuthenticatorService(identityProvider, systemApplicationDao, apimConfigurationService,
                    apimAppConfigurationService);
        }
        return service;
    }
}
