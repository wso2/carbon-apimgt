/*
 *
 *  * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.common.interceptors;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.security.basic.AbstractBasicAuthSecurityInterceptor;

/**
 * Security Interceptor that does basic authentication for REST ApI requests.
 */
@Component(
        name = "org.wso2.carbon.apimgt.rest.api.common.interceptors.BasicAuthSecurityInterceptor",
        service = Interceptor.class,
        immediate = true
)
public class BasicAuthSecurityInterceptor extends AbstractBasicAuthSecurityInterceptor {
    private static final Logger log = LoggerFactory.getLogger(BasicAuthSecurityInterceptor.class);

    /*
    * basic auth athentication logic is executed here
    * @pqram username
    * @param password
    * @return authentication status. true if authentication is successful; else false
    * */
    @Override
    protected boolean authenticate(String username, String password) {
        // Authentication logic is added in here. For simplicity, we just check that username == password
        //todo improve
        if (username.equals(password)) {
            return true;
        }
        return false;

    }
}
