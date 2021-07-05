/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.keys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.model.entity.Scope;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides a web service interface for the API key data store. This implementation
 * acts as a client stub for the APIKeyValidationService in the API key manager. Using
 * this stub, one may query the key manager to authenticate and authorize API keys.
 * All service invocations are secured using BasicAuth over TLS. Therefore this class
 * may incur a significant overhead on the key validation process.
 */
public class WSAPIKeyDataStore implements APIKeyDataStore {

    public static final Log log = LogFactory.getLog(WSAPIKeyDataStore.class);

    @MethodStats
    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey, String requiredAuthenticationLevel, String clientDomain,
                                                 String matchingResource, String httpVerb,
                                                 String tenantDomain, List<String> keyManagers)
            throws APISecurityException {

        APIKeyValidatorClient client = new APIKeyValidatorClient();
        try {
            return client.getAPIKeyData(context, apiVersion, apiKey, requiredAuthenticationLevel, clientDomain,
                    matchingResource, httpVerb, tenantDomain, keyManagers);
        } catch (APISecurityException ex) {
            throw new APISecurityException(ex.getErrorCode(),
                    "Resource forbidden", ex);
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API key validation", e);
        }
    }

    @MethodStats
    public ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion
                                                    )
            throws APISecurityException {

        APIKeyValidatorClient client = new APIKeyValidatorClient();
        try {
            return client.getAllURITemplates(context, apiVersion);
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API key validation", e);
        }
    }

    @MethodStats
    public ArrayList<URITemplate> getAPIProductURITemplates(String context, String apiVersion)
            throws APISecurityException {

        APIKeyValidatorClient client = new APIKeyValidatorClient();
        try {
            return client.getAPIProductURITemplates(context, apiVersion);
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API key validation", e);
        }
    }

    @Override
    public APIKeyValidationInfoDTO validateSubscription(String context, String version, String consumerKey,
                                                        String tenantDomain, String keyManager)
            throws APISecurityException {

        APIKeyValidatorClient client = new APIKeyValidatorClient();
        try {
            return client.validateSubscription(context, version, consumerKey, tenantDomain, keyManager);
        } catch (APISecurityException ex) {
            throw new APISecurityException(ex.getErrorCode(),
                    "Resource forbidden", ex);
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for subscription validation", e);
        }
    }

    @Override
    public APIKeyValidationInfoDTO validateSubscription(String context, String version, int appId,
                                                        String tenantDomain)
            throws APISecurityException {
        APIKeyValidatorClient client = new APIKeyValidatorClient();
        try {
            return client.validateSubscription(context, version, appId, tenantDomain);
        } catch (APISecurityException ex) {
            throw new APISecurityException(ex.getErrorCode(),
                    "Resource forbidden", ex);
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing in-memory store for subscription validation", e);
        }
    }

    @Override
    public boolean validateScopes(TokenValidationContext tokenValidationContext, String tenantDomain)
            throws APISecurityException {

        APIKeyValidatorClient client = new APIKeyValidatorClient();
        try {
            return client.validateScopes(tokenValidationContext, tenantDomain);
        } catch (APISecurityException ex) {
            throw new APISecurityException(ex.getErrorCode(), "Resource forbidden", ex);
        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for scope validation", e);
        }
    }

    public void cleanup() {

    }

    @Override
    public Map<String, Scope> retrieveScopes(String tenantDomain) {
        APIKeyValidatorClient client = new APIKeyValidatorClient();
        return client.retrieveScopes(tenantDomain);
    }
}
