/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.factory;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;

import java.util.Map;
import java.util.Set;

public class FakeKeyManagerForTest implements KeyManager {
    @Override public OAuthApplicationInfo createApplication(OAuthAppRequest oAuthAppRequest)
            throws APIManagementException {
        return null;
    }

    @Override public OAuthApplicationInfo updateApplication(OAuthAppRequest oAuthAppRequest)
            throws APIManagementException {
        return null;
    }

    @Override public void deleteApplication(String s) throws APIManagementException {

    }

    @Override public OAuthApplicationInfo retrieveApplication(String s) throws APIManagementException {
        return null;
    }

    @Override public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest accessTokenRequest)
            throws APIManagementException {
        return null;
    }

    @Override public AccessTokenInfo getTokenMetaData(String s) throws APIManagementException {
        return null;
    }

    @Override public KeyManagerConfiguration getKeyManagerConfiguration() throws APIManagementException {
        return null;
    }

    @Override public OAuthApplicationInfo buildFromJSON(String s) throws APIManagementException {
        return null;
    }

    @Override public OAuthApplicationInfo buildFromJSON(OAuthApplicationInfo oAuthApplicationInfo, String s)
            throws APIManagementException {
        return null;
    }

    @Override public AccessTokenRequest buildAccessTokenRequestFromJSON(String s, AccessTokenRequest accessTokenRequest)
            throws APIManagementException {
        return null;
    }

    @Override public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest oAuthAppRequest)
            throws APIManagementException {
        return null;
    }

    @Override public AccessTokenRequest buildAccessTokenRequestFromOAuthApp(OAuthApplicationInfo oAuthApplicationInfo,
            AccessTokenRequest accessTokenRequest) throws APIManagementException {
        return null;
    }

    @Override public void loadConfiguration(KeyManagerConfiguration keyManagerConfiguration)
            throws APIManagementException {

    }

    @Override public boolean registerNewResource(API api, Map map) throws APIManagementException {
        return false;
    }

    @Override public Map getResourceByApiId(String s) throws APIManagementException {
        return null;
    }

    @Override public boolean updateRegisteredResource(API api, Map map) throws APIManagementException {
        return false;
    }

    @Override public void deleteRegisteredResourceByAPIId(String s) throws APIManagementException {

    }

    @Override public void deleteMappedApplication(String s) throws APIManagementException {

    }

    @Override public Set<String> getActiveTokensByConsumerKey(String s) throws APIManagementException {
        return null;
    }

    @Override public AccessTokenInfo getAccessTokenByConsumerKey(String s) throws APIManagementException {
        return null;
    }
}
