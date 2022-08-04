/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.common.gateway.dto;

/**
 * AuthenticationResponse object contains the authentication status of the request when it passed
 * through an authenticator.
 */
public class AuthenticationResponse {

    private boolean authenticated;
    private boolean mandatoryAuthentication;
    private boolean continueToNextAuthenticator;
    private int errorCode;
    private String errorMessage;

    public AuthenticationResponse(boolean authenticated, boolean mandatoryAuthentication,
                                  boolean continueToNextAuthenticator,
                                  int errorCode, String errorMessage) {
        this.authenticated = authenticated;
        this.mandatoryAuthentication = mandatoryAuthentication;
        this.continueToNextAuthenticator = continueToNextAuthenticator;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isMandatoryAuthentication() {
        return mandatoryAuthentication;
    }

    public boolean isContinueToNextAuthenticator() {
        return continueToNextAuthenticator;
    }
}
