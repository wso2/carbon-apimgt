/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import java.util.Map;
import java.util.Set;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;

/**
 * This class holds the key manager implementation considering WSO2 as the identity provider
 * This is the default key manager supported by API Manager
 */
public class AMDefaultKeyManagerImpl extends AbstractKeyManager {

    @Override
    public OAuthApplicationInfo buildFromJSON(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteApplication(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteMappedApplication(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteRegisteredResourceByAPIId(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public AccessTokenInfo getAccessTokenByConsumerKey(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getActiveTokensByConsumerKey(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KeyManagerConfiguration getKeyManagerConfiguration() throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map getResourceByApiId(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AccessTokenInfo getTokenMetaData(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void loadConfiguration(KeyManagerConfiguration arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean registerNewResource(API arg0, Map arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public OAuthApplicationInfo retrieveApplication(String arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OAuthApplicationInfo updateApplication(OAuthAppRequest arg0) throws APIManagementException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean updateRegisteredResource(API arg0, Map arg1) throws APIManagementException {
        // TODO Auto-generated method stub
        return false;
    }

}
