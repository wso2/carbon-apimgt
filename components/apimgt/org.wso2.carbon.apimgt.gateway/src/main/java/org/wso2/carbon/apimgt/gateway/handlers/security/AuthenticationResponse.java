/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.security;

/**
 * AuthenticationResponse object contains the authentication status of the request when it passed
 * through an authenticator.
 */
public class AuthenticationResponse extends org.wso2.carbon.apimgt.common.gateway.dto.AuthenticationResponse {

    public AuthenticationResponse(boolean authenticated, boolean mandatoryAuthentication, boolean continueToNextAuthenticator,
                                  int errorCode, String errorMessage) {
        super(authenticated, mandatoryAuthentication, continueToNextAuthenticator, errorCode, errorMessage);
    }

}
