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

package org.wso2.carbon.apimgt.multitenant.auth;

/**
 * Constants used by the Multi Tenant Authenticator.
 */
public class MultiTenantAuthenticatorConstants {

    // Authenticator identification.
    public static final String AUTHENTICATOR_NAME = "multiTenantAuthenticator";
    public static final String AUTHENTICATOR_FRIENDLY_NAME = "Multi Tenant Authenticator";

    // Tenant constants.
    public static final String SUPER_TENANT_DOMAIN = "carbon.super";
    public static final String TENANT_DOMAIN_PARAM = "tenantDomain";
    public static final String TENANT_IDENTIFIER = "tenantIdentifier";
    public static final String USER_SELECTED_TENANT_DOMAIN = "MT_AUTH_USER_SELECTED_TENANT_DOMAIN";

    // Request parameter names.
    public static final String SESSION_DATA_KEY_PARAM = "sessionDataKey";
    public static final String AUTHENTICATOR_PARAM = "authenticator";
    public static final String IDP_PARAMETER = "idp";

    // Authenticator configuration property keys.
    public static final String COMMON_SP_NAME = "CommonSPName";
    public static final String TENANT_SELECTION_URL_PROP = "TenantSelectionPageUrl";

    // OIDC constants.
    public static final String USERINFO_URL = "UserInfoUrl";

    // Common string constants.
    public static final String EQUAL_SIGN = "=";
    public static final String AMPERSAND_SIGN = "&";

    private MultiTenantAuthenticatorConstants() {

    }

    /**
     * Enum for error messages.
     */
    public enum ErrorMessages {

        NO_REGISTERED_IDP_FOR_ISSUER("ORG-65001", "No registered IdP found for the issuer: %s"),
        JWT_TOKEN_VALIDATION_FAILED("ORG-65002", "Error while validating the ID token."),
        ID_TOKEN_AUD_VALIDATION_FAILED("ORG-65003", "Invalid audience in the ID token."),
        TENANT_DOMAIN_NOT_FOUND("ORG-65004", "Tenant domain not found in the request."),
        SP_NOT_FOUND_FOR_TENANT("ORG-65005", "No service provider found for tenant: %s"),
        CLIENT_ID_RESOLUTION_FAILED("ORG-65006", "Failed to resolve client ID for tenant: %s"),
        TENANT_REDIRECT_FAILED("ORG-65007", "Error while redirecting to tenant login page.");

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
