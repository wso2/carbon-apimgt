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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.model.entity.Scope;
import org.wso2.carbon.apimgt.keymgt.service.APIKeyValidationService;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class APIKeyValidatorClient {

    private static final Log log = LogFactory.getLog(APIKeyValidatorClient.class);
    private APIKeyValidationService apiKeyValidationService;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS",
            justification = "It is required to set two options on the Options object")
    public APIKeyValidatorClient() {

        apiKeyValidationService = new APIKeyValidationService();
    }

    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion, String apiKey,
                                                 String requiredAuthenticationLevel, String clientDomain,
                                                 String matchingResource, String httpVerb, String tenantDomain,
                                                 List<String> keyManagers) throws APISecurityException {

        try {
            return apiKeyValidationService
                    .validateKey(context, apiVersion, apiKey, requiredAuthenticationLevel, clientDomain
                            , matchingResource, httpVerb, tenantDomain, keyManagers);
        } catch (APIKeyMgtException | APIManagementException e) {
            log.error("Error while retrieving data from datastore", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while retrieving data from datastore", e);
        }

    }

    public APIKeyValidationInfoDTO validateSubscription(String context, String version, String consumerKey,
                                                        String tenantDomain, String keyManager)
            throws APISecurityException {

        try {
            return apiKeyValidationService
                    .validateSubscription(context, version, consumerKey, tenantDomain, keyManager);
        } catch (APIKeyMgtException | APIManagementException e) {
            log.error("Error while  validate subscriptions", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API subscription validation", e);
        }
    }

    public APIKeyValidationInfoDTO validateSubscription(String context, String version, int appId,
                                                        String tenantDomain)
            throws APISecurityException {

        try {
            return apiKeyValidationService
                    .validateSubscription(context, version, appId, tenantDomain);
        } catch (APIKeyMgtException | APIManagementException e) {
            log.error("Error while  validate subscriptions", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API subscription validation", e);
        }
    }

    public boolean validateScopes(TokenValidationContext tokenValidationContext, String tenantDomain)
            throws APISecurityException {

        try {
            return apiKeyValidationService.validateScopes(tokenValidationContext, tenantDomain);
        } catch (APIKeyMgtException e) {
            String message = "Error while accessing backend services for token scope validation";
            log.error(message, e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, message, e);
        }
    }

    public ArrayList<URITemplate> getAllURITemplates(String context, String apiVersion
                                                    ) throws APISecurityException {

        try {
            return apiKeyValidationService.getAllURITemplates(context, apiVersion);
        } catch (APIManagementException e) {
            log.error("Error while retrieving data from datastore", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while retrieving data from datastore", e);
        }
    }

    public ArrayList<URITemplate> getAPIProductURITemplates(String context, String apiVersion
                                                           ) throws APISecurityException {

        try {
            return apiKeyValidationService.getAPIProductURITemplates(context, apiVersion);
        } catch (APIManagementException e) {
            log.error("Error while retrieving data from datastore", e);
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while retrieving data from datastore", e);
        }
    }

    public Map<String, Scope> retrieveScopes(String tenantDomain) {
        return apiKeyValidationService.retrieveScopes(tenantDomain);

    }
}
