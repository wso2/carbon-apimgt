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
import org.wso2.carbon.apimgt.impl.clients.ApplicationManagementServiceClient;
import org.wso2.carbon.apimgt.impl.clients.OAuthAdminClient;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;

public class ApplicationManagementServiceClientWrapper extends ApplicationManagementServiceClient {
    private static IdentityApplicationManagementServiceStub serviceStub;

    public ApplicationManagementServiceClientWrapper() throws APIManagementException {}

    @Override
    protected IdentityApplicationManagementServiceStub getIdentityApplicationManagementServiceStub(String serviceURL,
            ConfigurationContext ctx) throws AxisFault {
        return this.serviceStub;
    }

    public static void setServiceStub(IdentityApplicationManagementServiceStub stub) {
        serviceStub = stub;
    }
}
