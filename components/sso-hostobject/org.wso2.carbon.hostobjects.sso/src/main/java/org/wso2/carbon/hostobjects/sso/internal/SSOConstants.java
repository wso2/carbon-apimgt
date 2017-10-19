/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.hostobjects.sso.internal;

public class SSOConstants {
    public static final String ERROR_CODE = "errorCode";

    public static final String IDP_URL = "identityProviderURL";
    public static final String KEY_STORE_NAME = "keyStoreName";
    public static final String KEY_STORE_PASSWORD = "keyStorePassword";
    public static final String IDP_ALIAS = "identityAlias";
    public static final String ISSUER_ID = "issuerId";
    public static final String SIGN_REQUESTS = "signRequests";

    public static final String SAML_ENCODED = "isEncoded";
    public static final String SAML_DEFLATE = "deflate";

    public static final String DEFAULT_DEFLATE_VALUE = "false";
    public static final String DEFAULT_ENCODED_VALUE = "true";

    public static final String IS_AUTHENTICATED = "authenticated";
    public static final String USERNAME = "username";
    public static final String ASSERTIONENCRYPTIONENABLED = "assertionEncryptionEnabled";

    public static final String NAME_ID_POLICY = "nameIdPolicy";
    public static final String NAME_ID_POLICY_DEFAULT = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
    public static final String LOGOUT_USER = "urn:oasis:names:tc:SAML:2.0:logout:user";
    public static final String TIMESTAMP_SKEW_IN_SECONDS = "timestampSkewInSeconds";

    public SSOConstants() {
    }
}


