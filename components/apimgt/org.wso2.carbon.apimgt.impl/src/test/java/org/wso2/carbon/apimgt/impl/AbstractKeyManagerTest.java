/*
*  Copyright (c) 2005-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;

public class AbstractKeyManagerTest {

    @Test
    public void buildAccessTokenRequestFromJSONTest() throws APIManagementException {
        String jsonPayload = "{ \"callbackUrl\": \"www.google.lk\", \"clientName\": \"rest_api_publisher\", " +
                "\"tokenScope\": \"Production\", \"owner\": \"admin\", \"grantType\": \"password refresh_token\", \"saasApp\": true }";

        AbstractKeyManager keyManager = new AMDefaultKeyManagerImpl();
        keyManager.buildAccessTokenRequestFromJSON(jsonPayload, new AccessTokenRequest());


    }

}
