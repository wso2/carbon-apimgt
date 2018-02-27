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

package org.wso2.carbon.apimgt.rest.api.authenticator;

import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.core.dao.SystemApplicationDao;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public class AuthenticatorAPIService {
    private KeyManager keyManager;
    private SystemApplicationDao systemApplicationDao;
    private APIMConfigurationService apimConfigurationService;
    private AuthenticatorServiceUtils authenticatorServiceUtils;



    public Response authenticate(Request request, String appName, String userName, String password, String assertion,
                                 String grantType, String validityPeriod, boolean isRememberMe, String scopesList){
        return null;
    }
}
