/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.common.gateway.constants;

/**
 * API Constants used for the common gateway component.
 */
public class APIConstants {
    public static final String CNF = "cnf";
    public static final String DIGEST = "x5t#S256";
    public static final String GATEWAY_PUBLIC_CERTIFICATE_ALIAS = "gateway_certificate_alias";

    /**
     * Constants representing the error codes based on key validation.
     */
    public static class KeyValidationStatus {

        public static final int API_AUTH_GENERAL_ERROR = 900900;
        public static final int API_AUTH_INVALID_CREDENTIALS = 900901;
        public static final int INVALID_SCOPE = 900910;

        private KeyValidationStatus() {

        }
    }
}
