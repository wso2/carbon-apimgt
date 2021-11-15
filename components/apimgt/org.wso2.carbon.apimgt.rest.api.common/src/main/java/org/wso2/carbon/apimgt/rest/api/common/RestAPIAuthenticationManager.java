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

import java.util.HashMap;

/**
 * RestAPIAuthenticationManager class handling authenticators for each request. Requests may receive via different
 * authentication options like access_token, JWT under auth header , backend JWT and so on. This class will select the
 * appropriate authenticator for each request.
 */

public class RestAPIAuthenticationManager {

    private static final Log log = LogFactory.getLog(RestAPIAuthenticationManager.class);

    public static RestAPIAuthenticator getAuthenticator(HashMap<String, Object> authContext) {
        ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();
        if (serviceReferenceHolder.getAuthenticators() != null) {
            for (RestAPIAuthenticator restAPIAuthenticator : serviceReferenceHolder.getAuthenticators()) {
                if (restAPIAuthenticator.canHandle(authContext)) {
                    log.debug("Detected an appropriate authenticator to handle the request");
                    return restAPIAuthenticator;
                };
            }
        }
        return null;
    }
}
