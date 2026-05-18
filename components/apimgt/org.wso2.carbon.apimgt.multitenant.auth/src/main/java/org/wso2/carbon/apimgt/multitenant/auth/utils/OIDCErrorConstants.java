/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.multitenant.auth.utils;

/**
 * Error constants for OIDC token validation operations.
 */
public final class OIDCErrorConstants {

    private OIDCErrorConstants() {
    }
    /**
     * Relevant error messages and error codes.
     */
    public enum ErrorMessages {

        JWT_TOKEN_AUD_CLAIM_VALIDATION_FAILED("OID-60018",
                "None of the audience values matched the token endpoint alias: %s."),
        JWT_TOKEN_ISS_CLAIM_VALIDATION_FAILED(
                "OID-65016", "Error while validating the iss claim in the jwt token"),
        JWT_TOKEN_SIGNATURE_VALIDATION_FAILED("OID-65017",
                "Error while validating the JWT token signature");

        private final String code;
        private final String message;

        /**
         * Create an Error Message.
         *
         * @param code    Relevant error code.
         * @param message Relevant error message.
         */
        ErrorMessages(String code, String message) {

            this.code = code;
            this.message = message;
        }

        /**
         * To get the code of specific error.
         *
         * @return Error code.
         */
        public String getCode() {

            return code;
        }

        /**
         * To get the message of specific error.
         *
         * @return Error message.
         */
        public String getMessage() {

            return message;
        }

        @Override
        public String toString() {

            return String.format("%s  - %s", code, message);
        }
    }
}
