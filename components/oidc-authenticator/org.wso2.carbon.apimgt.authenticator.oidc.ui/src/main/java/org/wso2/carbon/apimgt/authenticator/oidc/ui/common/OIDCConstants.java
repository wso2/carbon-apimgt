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
package org.wso2.carbon.apimgt.authenticator.oidc.ui.common;


public final class OIDCConstants {
    public static final String AUTHENTICATOR_NAME = "OIDCAuthenticator";


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


    public static final String PARAM_RESPONSE_TYPE = "response_type";
    public static final String PARAM_CLIENT_ID = "client_id";
    public static final String PARAM_CLIENT_SECRET = "client_secret";
    public static final String PARAM_AUTHORIZATION_TYPE = "authorization_type";
    public static final String PARAM_SCOPE = "scope";
    public static final String PARAM_REDIRECT_URI = "redirect_uri";
    public static final String PARAM_NONCE = "nonce";
    public static final String PARAM_STATE = "state";
    public static final String PARAM_CODE = "code";
    //public static final String STATE_SESSION_VARIABLE = "STATE";


    public static final java.lang.String NOTIFICATIONS_ERROR_MSG = "ErrorMessage";

    public static final java.lang.String IDP_SESSION_INDEX = "idpSessionIndex";
    public static final java.lang.String HTTP_ATTR_IS_LOGOUT_REQ = "logoutRequest";
    public static final java.lang.String LOGGED_IN_USER = "loggedInUser";
    public static final java.lang.String EXTERNAL_LOGOUT_PAGE = "ExternalLogoutPage";



    public static final String LOG_OUT_REQ = "logout";





    public static final String LOGIN_PAGE = "LoginPage";
    public static final String LANDING_PAGE = "LandingPage";
    public static final String FEDERATION_CONFIG = "FederationConfig";
    public static final String FEDERATION_CONFIG_USER = "FederationConfigUser";
    public static final String FEDERATION_CONFIG_PASSWORD = "FederationConfigPassword";
    public static final String LOGIN_ATTRIBUTE_NAME = "LoginAttributeName";



    public static final class ErrorMessageConstants{
        public static final String RESPONSE_NOT_PRESENT = "response.not.present";
        public static final String RESPONSE_INVALID = "response.invalid";
        public static final String RESPONSE_MALFORMED = "response.malformed";
        public static final String SUCCESSFUL_SIGN_OUT = "successful.signed.out";
    }
}
