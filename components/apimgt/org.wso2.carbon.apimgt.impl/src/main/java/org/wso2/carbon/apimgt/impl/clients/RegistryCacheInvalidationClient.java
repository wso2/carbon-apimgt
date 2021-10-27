/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl.clients;

import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.registry.cache.stub.RegistryCacheInvalidationServiceStub;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;

/**
 * This is the client implementation of RegistryCacheInvalidationService
 *
 */
public class RegistryCacheInvalidationClient {

    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    Map<String, Environment> environments;
    

    public RegistryCacheInvalidationClient() throws APIManagementException {
        environments = APIUtil.getEnvironments();
    }

}
