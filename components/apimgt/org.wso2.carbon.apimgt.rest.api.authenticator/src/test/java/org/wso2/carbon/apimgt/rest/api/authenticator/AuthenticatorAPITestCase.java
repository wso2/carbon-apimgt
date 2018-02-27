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

import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public class AuthenticatorAPITestCase {
    @Test
    public void testCallback(){
        AuthenticatorAPI authenticatorAPI = new AuthenticatorAPI();
        Request request = Mockito.mock(Request.class);
        Mockito.when(request.getUri()).thenReturn("/login/callback/publisher");

        Response response = authenticatorAPI.callback(request, "publisher", "xxx-auth-code-xxx");
    }
}
