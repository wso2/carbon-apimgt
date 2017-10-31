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
package org.wso2.carbon.apimgt.rest.api.common.api;

import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

/**
 * This is the interface that need to implement when a custom REAT API security mechanism is needed.
 */
public interface RESTAPIAuthenticator {
/**
* Authentication logic is executed here
* @param request    MSF4J Request
* @param responder  MSF4J Response
* @param serviceMethodInfo  Contains details about the HTTP method
* @return authentication status. true if authentication is successful; else false.
* @throws APIMgtSecurityException   If failed to authenticate the request.
* */
    boolean authenticate(Request request, Response responder, ServiceMethodInfo serviceMethodInfo)
            throws APIMgtSecurityException;
}
