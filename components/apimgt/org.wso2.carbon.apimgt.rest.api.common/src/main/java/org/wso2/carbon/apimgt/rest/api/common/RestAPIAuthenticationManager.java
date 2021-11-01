/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.rest.api.common.internal.ServiceReferenceHolder;

public class RestAPIAuthenticationManager {

    private static final Log log = LogFactory.getLog(RestAPIAuthenticationManager.class);
    private static RestAPIAuthenticator authenticator = null;

    public static RestAPIAuthenticator getAuthenticator() {
        if (authenticator == null) {
            ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();
            if (serviceReferenceHolder.getAuthenticator() != null) {
                log.info("Authenticating in Back-end JWT");
                authenticator = serviceReferenceHolder.getAuthenticator();
            }
        }

        return authenticator;
    }
}
