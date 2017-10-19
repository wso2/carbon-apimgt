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
import org.wso2.carbon.apimgt.impl.clients.OAuth2TokenValidationServiceClient;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;

public class OAuth2TokenValidationServiceClientWrapper extends OAuth2TokenValidationServiceClient {
    private static OAuth2TokenValidationServiceStub serviceStub;

    public OAuth2TokenValidationServiceClientWrapper() throws APIManagementException {}

    @Override
    protected OAuth2TokenValidationServiceStub getOAuth2TokenValidationServiceStub(String serviceURL, ConfigurationContext ctx)
            throws AxisFault {
        return this.serviceStub;
    }

    public static void setServiceStub(OAuth2TokenValidationServiceStub stub) {
        serviceStub = stub;
    }
}
