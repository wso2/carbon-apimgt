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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.validators.OAuth2ScopeValidator;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultKeyValidationHandler extends AbstractKeyValidationHandler {

    private static final Log log = LogFactory.getLog(DefaultKeyValidationHandler.class);
    private static final String GRAPHQL_QUERY = "QUERY";
    private static final String GRAPHQL_MUTATION = "MUTATION";
    private static final String GRAPHQL_SUBSCRIPTION = "SUBSCRIPTION";


    public DefaultKeyValidationHandler(){
        log.info(this.getClass().getName() + " Initialised");
    }

    @Override
    public boolean validateToken(TokenValidationContext validationContext) throws APIKeyMgtException {
        // If validationInfoDTO is taken from cache, validity of the cached infoDTO is checked with each request.
        if (validationContext.isCacheHit()) {
            APIKeyValidationInfoDTO infoDTO = validationContext.getValidationInfoDTO();

            // TODO: This should only happen in GW
            boolean tokenExpired = APIUtil.isAccessTokenExpired(infoDTO);
            if (tokenExpired) {
                infoDTO.setAuthorized(false);
                infoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                log.debug("Token " + validationContext.getAccessToken() + " expired.");
                return false;
            } else {
                return true;
            }
        }

        AccessTokenInfo tokenInfo;

        try {

            // Obtaining details about the token.
            tokenInfo = KeyManagerHolder.getKeyManagerInstance().getTokenMetaData(validationContext.getAccessToken());

            if (tokenInfo == null) {
                return false;
            }

            // Setting TokenInfo in validationContext. Methods down in the chain can use TokenInfo.
            validationContext.setTokenInfo(tokenInfo);
            //TODO: Eliminate use of APIKeyValidationInfoDTO if possible

            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
            validationContext.setValidationInfoDTO(apiKeyValidationInfoDTO);

            if (!tokenInfo.isTokenValid()) {
                apiKeyValidationInfoDTO.setAuthorized(false);
                if (tokenInfo.getErrorcode() > 0) {
                    apiKeyValidationInfoDTO.setValidationStatus(tokenInfo.getErrorcode());
                }else {
                    apiKeyValidationInfoDTO.setValidationStatus(APIConstants
                                                                        .KeyValidationStatus.API_AUTH_GENERAL_ERROR);
                }
                return false;
            }

            apiKeyValidationInfoDTO.setAuthorized(tokenInfo.isTokenValid());
            apiKeyValidationInfoDTO.setEndUserName(tokenInfo.getEndUserName());
            apiKeyValidationInfoDTO.setConsumerKey(tokenInfo.getConsumerKey());
            apiKeyValidationInfoDTO.setIssuedTime(tokenInfo.getIssuedTime());
            apiKeyValidationInfoDTO.setValidityPeriod(tokenInfo.getValidityPeriod());

            if (tokenInfo.getScopes() != null) {
                Set<String> scopeSet = new HashSet<String>(Arrays.asList(tokenInfo.getScopes()));
                apiKeyValidationInfoDTO.setScopes(scopeSet);
            }

        } catch (APIManagementException e) {
            log.error("Error while obtaining Token Metadata from Authorization Server", e);
            throw new APIKeyMgtException("Error while obtaining Token Metadata from Authorization Server");
        }

        return tokenInfo.isTokenValid();
    }

    @Override
    public boolean validateScopes(TokenValidationContext validationContext) throws APIKeyMgtException {

        if (validationContext.isCacheHit()) {
            return true;
        }

        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = validationContext.getValidationInfoDTO();

        if (apiKeyValidationInfoDTO == null) {
            throw new APIKeyMgtException("Key Validation information not set");
        }

        String[] scopes = null;
        Set<String> scopesSet = apiKeyValidationInfoDTO.getScopes();

        if (scopesSet != null && !scopesSet.isEmpty()) {
            scopes = scopesSet.toArray(new String[scopesSet.size()]);
            if (log.isDebugEnabled() && scopes != null) {
                StringBuilder scopeList = new StringBuilder();
                for (String scope : scopes) {
                    scopeList.append(scope);
                    scopeList.append(",");
                }
                scopeList.deleteCharAt(scopeList.length() - 1);
                log.debug("Scopes allowed for token : " + validationContext.getAccessToken() + " : "
                        + scopeList.toString());
            }
        }

        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserName(MultitenantUtils.getTenantAwareUsername(apiKeyValidationInfoDTO.getEndUserName()));
        user.setTenantDomain(apiKeyValidationInfoDTO.getSubscriberTenantDomain());

        if (user.getUserName() != null && APIConstants.FEDERATED_USER
                .equalsIgnoreCase(IdentityUtil.extractDomainFromName(user.getUserName()))) {
            user.setFederatedUser(true);
        }

        String clientId = apiKeyValidationInfoDTO.getConsumerKey();
        AccessTokenDO accessTokenDO = new AccessTokenDO(clientId, user, scopes, null,
                null, apiKeyValidationInfoDTO.getValidityPeriod(), apiKeyValidationInfoDTO.getValidityPeriod(),
                apiKeyValidationInfoDTO.getType());

        if (apiKeyValidationInfoDTO.getProductName() == null && apiKeyValidationInfoDTO.getProductProvider() == null) {
            accessTokenDO.setTokenType(accessTokenDO.getTokenType() + ":" + APIConstants.API_SUBSCRIPTION_TYPE);
        } else {
            String productName = apiKeyValidationInfoDTO.getProductName();
            String productProvider = apiKeyValidationInfoDTO.getProductProvider();
            accessTokenDO.setTokenType(
                    accessTokenDO.getTokenType() + ":" + APIConstants.API_PRODUCT_SUBSCRIPTION_TYPE + ":" + productName
                            + ":" + productProvider);
        }

        accessTokenDO.setAccessToken(validationContext.getAccessToken());

        String actualVersion = validationContext.getVersion();
        //Check if the api version has been prefixed with _default_
        if (actualVersion != null && actualVersion.startsWith(APIConstants.DEFAULT_VERSION_PREFIX)) {
            //Remove the prefix from the version.
            actualVersion = actualVersion.split(APIConstants.DEFAULT_VERSION_PREFIX)[1];
        }
        String resource = validationContext.getContext() + "/" + actualVersion + validationContext
                .getMatchingResource()
                + ":" +
                validationContext.getHttpVerb();

        Set<OAuth2ScopeValidator> oAuth2ScopeValidators = new HashSet<> (OAuthServerConfiguration.getInstance().
                getOAuth2ScopeValidators());

        boolean isOperationValid = true;

        if((validationContext.getHttpVerb().equals(GRAPHQL_QUERY))|| (validationContext.getHttpVerb().
                equals(GRAPHQL_MUTATION)) || (validationContext.getHttpVerb().equals(GRAPHQL_SUBSCRIPTION))){
            String operationList = validationContext.getMatchingResource();
            List<String> operationResourceArray = new ArrayList<String>(Arrays.asList(operationList.split(",")));

            for (String operationResource : operationResourceArray){
                for (OAuth2ScopeValidator validator : oAuth2ScopeValidators) {
                    try {
                        resource = validationContext.getContext() + "/" + actualVersion + "/" + operationResource + ":" +
                                validationContext.getHttpVerb();
                        isOperationValid = validator.validateScope(accessTokenDO, resource);
                        if (!isOperationValid) {
                            log.debug(String.format("Scope validation failed for %s", validator.getValidatorName()));
                            apiKeyValidationInfoDTO.setAuthorized(false);
                            apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.INVALID_SCOPE);
                            return false;
                        }
                    } catch(IdentityOAuth2Exception e) {
                        log.error("ERROR while validating token scope " + e.getMessage(), e);
                        apiKeyValidationInfoDTO.setAuthorized(false);
                        apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.INVALID_SCOPE);
                        return false;
                    }
                }
            }
            return true;
        }

        //validate scope for filtered validators from db
        String[] scopeValidators;
        OAuthAppDO appInfo;
        try {
            OAuthAppDAO oAuthAppDAO = new OAuthAppDAO();
            appInfo = oAuthAppDAO.getAppInformation(clientId);
            scopeValidators = appInfo.getScopeValidators();     //get scope validators from the DB
            boolean isValid = true;
            List<String> appScopeValidators=  new ArrayList<>(Arrays.asList(scopeValidators));

            for (OAuth2ScopeValidator validator : oAuth2ScopeValidators) {
                try {
                    if (validator != null && ArrayUtils.isEmpty(scopeValidators)) {
                        // validate scopes for old created applications
                        isValid = validator.validateScope(accessTokenDO, resource);
                        oAuth2ScopeValidators.clear();
                    } else if (validator != null && scopeValidators != null &&
                            appScopeValidators.contains(validator.getValidatorName())) {
                        //take the intersection of defined scope validators and scope validators registered for the apps
                        log.debug(String.format("Validating scope of token %s using %s", accessTokenDO.getTokenId(),
                                validator.getValidatorName()));

                        isValid = validator.validateScope(accessTokenDO, resource);
                        appScopeValidators.remove(validator.getValidatorName());
                    }
                    if (!isValid) {
                        log.debug(String.format("Scope validation failed for %s", validator.getValidatorName()));
                        apiKeyValidationInfoDTO.setAuthorized(false);
                        apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.INVALID_SCOPE);
                        return false;
                    }
                } catch (IdentityOAuth2Exception e) {
                    log.error("ERROR while validating token scope " + e.getMessage(), e);
                    apiKeyValidationInfoDTO.setAuthorized(false);
                    apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.INVALID_SCOPE);
                    return false;
                }
            }
            if (!appScopeValidators.isEmpty()) {   //if scope validators are not defined in identity.xml but there are
                // scope validators assigned to an application, throws exception.
                throw new IdentityOAuth2Exception(String.format("The scope validator(s) %s registered for application %s@%s" +
                                " is/are not found in the server configuration ", StringUtils.join(appScopeValidators, ", "),
                        appInfo.getApplicationName(), OAuth2Util.getTenantDomainOfOauthApp(appInfo)));
            }

        } catch (InvalidOAuthClientException e) {
            log.error("Could not Fetch Application data for client with clientId = " + clientId);
            apiKeyValidationInfoDTO.setAuthorized(false);
            apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.INVALID_SCOPE);
            return false;
        } catch (IdentityOAuth2Exception e) {
            log.error("Error while retrieving the app information");
            apiKeyValidationInfoDTO.setAuthorized(false);
            apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.INVALID_SCOPE);
            return false;
        }
        return true;
    }
}
