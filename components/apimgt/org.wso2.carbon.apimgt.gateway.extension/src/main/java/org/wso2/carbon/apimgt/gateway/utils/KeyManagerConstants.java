/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.utils;

/**
 * This class will hold constants related to key manager de-coupling
 */
public class KeyManagerConstants {

    public static final String OAUTH_CLIENT_ID = "client_id"; //this means consumer key
    public static final String OAUTH_CLIENT_SCOPE = "scope";
    public static final String USERNAME = "username";
    public static final String OAUTH2_TOKEN_EXP_TIME = "exp";
    public static final String OAUTH2_TOKEN_ISSUED_TIME = "iat";

    /**
     * Status codes used in key-validation process
     */
    public static class KeyValidationStatus {
        public static final int API_AUTH_GENERAL_ERROR = 900900;
        public static final int API_AUTH_INVALID_CREDENTIALS = 900901;


        private KeyValidationStatus() {
        }
    }

}
