/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.keymgt.handlers;


public final class ResourceConstants {

    public static final String RESOURCE_CONTEXT = "context";
    public static final String RESOURCE_VERSION = "apiVersion";
    public static final String RESOURCE_AUTH_LEVEL = "authLevel";
    public static final String RESOURCE_PATH = "resource";
    public static final String RESOURCE_VERB = "httpVerb";
    public static final String KEY_DOMAIN = "clientDomain";
    public static final String CHECK_ROLES_FROM_SAML_ASSERTION = "checkRolesFromSamlAssertion";
    public static final String SAML2_ASSERTION = "SAML2Assertion";
    public static final String SAML2_SSO_AUTHENTICATOR_NAME = "SAML2SSOAuthenticator";
    public static final String ROLE_CLAIM_ATTRIBUTE = "RoleClaimAttribute";
    public static final String ATTRIBUTE_VALUE_SEPARATOR = "AttributeValueSeparator";
    public static final String ROLE_ATTRIBUTE_NAME = "http://wso2.org/claims/role";
    public static final String ATTRIBUTE_VALUE_SEPERATER = ",";
    public static final String RETRIEVE_ROLES_FROM_USERSTORE_FOR_SCOPE_VALIDATION = "retrieveRolesFromUserStoreForScopeValidation";
    public static final String ROLE_CLAIM = "ROLE_CLAIM";

    public static final String RESOURCE_PARAMS = "keymgt_resource_params";
    public static final String INTROSPECTURI = "http://localhost:8080/openid-connect-server-webapp/introspect";
    public static final String AUTH_TOKEN_PARAM_NAME = "token";
    public static final String AUTHORIZATION_PARAM_NAME = "Authorization";
    public static final String BASIC_TOKEN_NAME = "Basic";
    public static final String INTROSPECTION_TOKEN = "Y2xpZW50OnNlY3JldA=="; //base64_encode(clientid:secret)
    public static final String UTF8_PARAM_NAME = "UTF-8";
    public static final String ACTIVE_PARAM_NAME = "active";
    public static final String IAT_PARAM_NAME = "iat";
    public static final String EXP_PARAM_NAME = "exp";
    public static final String CLIENT_ID_PARAM_NAME = "client_id";
    
    private ResourceConstants(){
        
    }


}
