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

import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.core.dao.SystemApplicationDao;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.authenticator.AuthenticatorAPIService;
import org.wso2.carbon.apimgt.rest.api.authenticator.AuthenticatorServiceUtils;

public class AuthenticatorAPIFactory {
    private static AuthenticatorAPIService service;

    private AuthenticatorAPIFactory() {

    }

    public static AuthenticatorAPIService getService() throws APIMgtDAOException, KeyManagementException {
        if (service == null) {
            KeyManager keyManager = APIManagerFactory.getInstance().getKeyManager();
            SystemApplicationDao systemApplicationDao = DAOFactory.getSystemApplicationDao();
            APIMConfigurationService apimConfigurationService = APIMConfigurationService.getInstance();

            AuthenticatorServiceUtils authenticatorServiceUtils = new AuthenticatorServiceUtils(keyManager,
                    systemApplicationDao, apimConfigurationService);
            service = new AuthenticatorAPIService();
        }
        return service;
    }
}
