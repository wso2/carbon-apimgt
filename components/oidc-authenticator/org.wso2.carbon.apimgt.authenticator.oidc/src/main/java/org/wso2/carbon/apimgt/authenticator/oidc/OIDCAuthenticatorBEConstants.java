/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.apimgt.authenticator.oidc;

public class OIDCAuthenticatorBEConstants {

    public static final String OIDC_AUTHENTICATOR_NAME = "OIDCAuthenticator";

    // SSO Configuration Params
    public static final String SERVICE_PROVIDER_ID = "ServiceProviderID";
    public static final String IDENTITY_PROVIDER_URI = "identityProviderURI";
    public static final String AUTHORIZATION_ENDPOINT_URI = "authorizationEndpointURI";
    public static final String TOKEN_ENDPOINT_URI = "tokenEndpointURI";
    public static final String USER_INFO_URI = "userInfoURI";
    public static final String JWKS_URL = "jwksURI";

    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String CLIENT_RESPONSE_TYPE = "responseType";
    public static final String CLIENT_AUTHORIZATION_TYPE = "authorizationType";
    public static final String CLIENT_SCOPE = "scope";
    public static final String CLIENT_REDIRECT_URI = "redirectURI";
    public static final String CLIENT_ALGORITHM = "clientAlgorithm";


    // Allow for time sync issues by having a window of X seconds.
    public static final int  TIME_SKEW_ALLOWANCE= 300;





    public static final String ROLE_ATTRIBUTE_NAME = "http://wso2.org/claims/role";
    public static final String ATTRIBUTE_VALUE_SEPERATER = ",";



    

    public class PropertyConfig {
        public static final String AUTH_CONFIG_PARAM_IDP_CERT_ALIAS = "IdPCertAlias";
    	public static final String RESPONSE_SIGNATURE_VALIDATION_ENABLED = "ResponseSignatureValidationEnabled";
    	public static final String VALIDATE_SIGNATURE_WITH_USER_DOMAIN = "VerifySignatureWithUserDomain";
        public static final String ROLE_CLAIM_ATTRIBUTE = "RoleClaimAttribute";
        public static final String ATTRIBUTE_VALUE_SEPARATOR = "AttributeValueSeparator";
        
    	public static final String JIT_USER_PROVISIONING_ENABLED = "JITUserProvisioningEnabled";
    	public static final String PROVISIONING_DEFAULT_USERSTORE = "ProvisioningDefaultUserstore";
    	public static final String PROVISIONING_DEFAULT_ROLE = "ProvisioningDefaultRole";
    	public static final String IS_SUPER_ADMIN_ROLE_REQUIRED = "IsSuperAdminRoleRequired";
    }

}
