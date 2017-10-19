/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.clients.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.clients.RegistryCacheInvalidationClient;
import org.wso2.carbon.apimgt.registry.cache.stub.RegistryCacheInvalidationServiceStub;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;

public class RegistryCacheInvalidationClientWrapper extends RegistryCacheInvalidationClient {
    private AuthenticationAdminStub authStub;
    private RegistryCacheInvalidationServiceStub cacheStub;

    public RegistryCacheInvalidationClientWrapper(AuthenticationAdminStub authStub, RegistryCacheInvalidationServiceStub cacheStub)
            throws APIManagementException {
        this.authStub = authStub;
        this.cacheStub = cacheStub;
    }

    @Override
    protected AuthenticationAdminStub getAuthenticationAdminStub(String serverURL) throws AxisFault {
        return authStub;
    }

    @Override
    protected RegistryCacheInvalidationServiceStub getRegistryCacheInvalidationServiceStub(String serverURL,
            ConfigurationContext ctx) throws AxisFault {
        return cacheStub;
    }
}
