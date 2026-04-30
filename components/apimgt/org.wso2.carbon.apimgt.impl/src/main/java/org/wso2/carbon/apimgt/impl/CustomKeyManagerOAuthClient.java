/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Scope;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Custom Key Manager implementation for Out-of-Band (OOB) OAuth application provisioning.
 *
 * This connector supports scenarios where OAuth applications are pre-created in an external
 * Key Manager. Users can then map these applications to WSO2 API
 * Manager by providing the Client ID and Client Secret in the Developer Portal.
 */
public class CustomKeyManagerOAuthClient extends AbstractKeyManager {

    private static final Log log = LogFactory.getLog(CustomKeyManagerOAuthClient.class);

    /**
     * Not supported in OOB mode. Applications must be created directly in the external
     * Key Manager.
     *
     * Throws APIManagementException always, as DCR is not supported.
     */
    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest oAuthAppRequest) throws APIManagementException {

        log.warn("Application creation is not supported in OOB mode. " +
                "Please create the application directly in the external Key Manager.");
        throw new APIManagementException("Application creation is not supported in OOB mode. ",
                ExceptionCodes.OPERATION_NOT_IMPLEMENTED_FOR_CUSTOM_KM);
    }

    /**
     * Not supported in OOB mode. Applications must be updated directly in the external
     * Key Manager.
     *
     * Throws APIManagementException always, as DCR is not supported.
     */
    @Override
    public OAuthApplicationInfo updateApplication(OAuthAppRequest oAuthAppRequest) throws APIManagementException {

        log.warn("Application updating is not supported in OOB mode. " +
                "Please update the application directly in the external Key Manager.");
        throw new APIManagementException("Application updating is not supported in OOB mode. ",
                ExceptionCodes.OPERATION_NOT_IMPLEMENTED_FOR_CUSTOM_KM);
    }

    /**
     * In OOB mode, this only removes the mapping from WSO2. The application in the
     * external Key Manager remains unchanged.
     *
     * The clientID parameter is the client ID of the application to unmap.
     */
    @Override
    public void deleteApplication(String clientID) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Removing application mapping for client ID: " + clientID);
        }
    }

    /**
     * Returns application information for the given client ID including the consumer secret
     * if available. In OOB mode, this retrieves the stored mapping without validation against
     * the external Key Manager.
     *
     * The clientID parameter is the client ID to retrieve.
     * Returns OAuthApplicationInfo containing the client ID.
     */
    @Override
    public OAuthApplicationInfo retrieveApplication(String clientID) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving application info for client ID: " + clientID);
        }

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setClientId(clientID);
        oAuthApplicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_ID, clientID);

        return oAuthApplicationInfo;
    }

    /**
     * Not supported in OOB mode. Tokens must be obtained directly from the external
     * Key Manager's token endpoint.
     *
     * Throws APIManagementException always, as token generation is not supported.
     */
    @Override
    public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest accessTokenRequest)
            throws APIManagementException {

        log.warn("Application AccessToken generation is not supported in OOB mode. " +
                "Please obtain tokens directly from the external Key Manager.");
        throw new APIManagementException("Application AccessToken generation is not supported in OOB mode. ",
                ExceptionCodes.OPERATION_NOT_IMPLEMENTED_FOR_CUSTOM_KM);
    }

    /**
     * Not supported in OOB mode. Client secrets must be regenerated directly in the
     * external Key Manager.
     *
     * Throws APIManagementException always, as secret regeneration is not supported.
     */
    @Override
    public String getNewApplicationConsumerSecret(AccessTokenRequest accessTokenRequest) throws APIManagementException {

        log.warn("ApplicationConsumerSecret generation is not supported in OOB mode. " +
                "Please regenerate the secret directly in the external Key Manager.");
        throw new APIManagementException("ApplicationConsumerSecret generation is not supported in OOB mode. ",
                ExceptionCodes.OPERATION_NOT_IMPLEMENTED_FOR_CUSTOM_KM);
    }

    /**
     * Token introspection is not implemented. Returns null.
     */
    @Override
    public AccessTokenInfo getTokenMetaData(String accessToken) throws APIManagementException {

        return new AccessTokenInfo();
    }

    @Override
    public KeyManagerConfiguration getKeyManagerConfiguration() throws APIManagementException {

        return configuration;
    }

    /**
     * Maps an externally created OAuth application to WSO2 API Manager. This is the
     * primary method for OOB provisioning where users provide pre-existing credentials.
     *
     * The oAuthAppRequest parameter contains the OAuth application info with client credentials.
     * Returns the mapped OAuthApplicationInfo.
     */
    @Override
    public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest oAuthAppRequest) throws APIManagementException {

        OAuthApplicationInfo appInfo = oAuthAppRequest.getOAuthApplicationInfo();

        String clientId = appInfo.getClientId();
        if (clientId == null || clientId.isEmpty()) {
            throw new APIManagementException("Client ID must be provided for mapping an OAuth application in OOB mode.",
                    ExceptionCodes.INVALID_CLIENT_ID_FOR_OOB_MODE);
        }

        if (log.isDebugEnabled()) {
            log.debug("Mapping OAuth application with client ID: " + clientId);
        }

        OAuthApplicationInfo mappedApp = new OAuthApplicationInfo();
        mappedApp.setClientId(clientId);
        mappedApp.addParameter(ApplicationConstants.OAUTH_CLIENT_ID, clientId);

        return mappedApp;
    }

    /**
     * Stores the Key Manager configuration for later use.
     *
     * The keyManagerConfiguration parameter is the configuration from the Admin Portal.
     */
    @Override
    public void loadConfiguration(KeyManagerConfiguration keyManagerConfiguration) throws APIManagementException {

        this.configuration = keyManagerConfiguration;
    }

    // ==================== Resource and Scope Methods (No-op in OOB Mode) ====================

    @Override
    public boolean registerNewResource(API api, Map map) throws APIManagementException {

        return true;
    }

    @Override
    public Map getResourceByApiId(String s) throws APIManagementException {

        return null;
    }

    @Override
    public boolean updateRegisteredResource(API api, Map map) throws APIManagementException {

        return false;
    }

    @Override
    public void deleteRegisteredResourceByAPIId(String s) throws APIManagementException {

    }

    @Override
    public void deleteMappedApplication(String s) throws APIManagementException {

    }

    @Override
    public Set<String> getActiveTokensByConsumerKey(String s) throws APIManagementException {

        return new HashSet<>();
    }

    @Override
    public AccessTokenInfo getAccessTokenByConsumerKey(String s) throws APIManagementException {

        return new AccessTokenInfo();
    }

    @Override
    public Map<String, Set<Scope>> getScopesForAPIS(String s) throws APIManagementException {

        return null;
    }

    @Override
    public void registerScope(Scope scope) throws APIManagementException {

    }

    @Override
    public Scope getScopeByName(String s) throws APIManagementException {

        return null;
    }

    @Override
    public Map<String, Scope> getAllScopes() throws APIManagementException {

        return new HashMap<>();
    }

    @Override
    public void deleteScope(String s) throws APIManagementException {

    }

    @Override
    public void updateScope(Scope scope) throws APIManagementException {

    }

    @Override
    public boolean isScopeExists(String s) throws APIManagementException {

        return false;
    }

    @Override
    public String getType() {

        return APIConstants.KeyManager.CUSTOM_KM_TYPE;
    }
}
