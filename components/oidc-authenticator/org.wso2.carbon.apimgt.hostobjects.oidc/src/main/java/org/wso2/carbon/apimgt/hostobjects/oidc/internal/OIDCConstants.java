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
package org.wso2.carbon.apimgt.hostobjects.oidc.internal;

public class OIDCConstants {
    public static final String ERROR_CODE = "errorCode";

    public static final String IDP_URL = "identityProviderUri";
    public static final String ISSUER_ID = "issuerId";
    public static final String AUTHORIZATION_ENDPOINT_URI = "authorizationEndpointUri";
    public static final String TOKEN_ENDPOINT_URI = "tokenEndpointURI";
    public static final String USER_INFO_URI = "userInfoURI";
    public static final String JWKS_URI = "jwksURI";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String AUTHORIZATION_TYPE = "authorization_type";
    public static final String SCOPE = "scope";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String CLIENT_ALGORITHM = "client_algorithm";
    public static final String NONCE = "nonce";
    public static final String STATE = "state";

    // Allow for time sync issues by having a window of X seconds.
    public static final int  TIME_SKEW_ALLOWANCE= 300;




    public OIDCConstants() {
    }
}


