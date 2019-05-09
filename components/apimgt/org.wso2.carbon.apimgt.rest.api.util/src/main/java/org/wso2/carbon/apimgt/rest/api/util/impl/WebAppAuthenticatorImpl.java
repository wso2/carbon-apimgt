/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.Message;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.authenticators.WebAppAuthenticator;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.uri.template.URITemplateException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This web app authenticator class specifically implemented for API Manager store and publisher rest APIs
 * This will not be able to use as generic authenticator.
 */
public class WebAppAuthenticatorImpl implements WebAppAuthenticator {

    private static final Log log = LogFactory.getLog(WebAppAuthenticatorImpl.class);
    private static final String SUPER_TENANT_SUFFIX =
            APIConstants.EMAIL_DOMAIN_SEPARATOR + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

    /**
     * @param message cxf message to be authenticated
     * @return true if authentication was successful else false
     * @throws APIManagementException when error in authentication process
     */
    public boolean authenticate(Message message) throws APIManagementException {
        String accessToken = RestApiUtil.extractOAuthAccessTokenFromMessage(message,
                RestApiConstants.REGEX_BEARER_PATTERN, RestApiConstants.AUTH_HEADER_NAME);
        AccessTokenInfo tokenInfo = null;
        try {
            tokenInfo = KeyManagerHolder.getKeyManagerInstance().getTokenMetaData(accessToken);
        } catch (APIManagementException e) {
            log.error("Error while retrieving token information for token: " + accessToken, e);
        }
        // if we got valid access token we will proceed with next
        if (tokenInfo != null && tokenInfo.isTokenValid()) {
            // If token is valid then we have to do other validations and set user and tenant to
            //carbon context. Scope validation should come here.
            //If access token is valid then we will perform scope check for given resource.
            if (validateScopes(message, tokenInfo)) {
                //If scope validation successful then set tenant name and user name to current context
                String tenantDomain = MultitenantUtils.getTenantDomain(tokenInfo.getEndUserName());
                int tenantId;
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);
                try {
                    String username = tokenInfo.getEndUserName();
                    if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                        if (username.endsWith(SUPER_TENANT_SUFFIX)) {
                            username = username.substring(0, username.length() - SUPER_TENANT_SUFFIX.length());
                        }
                    }
                    tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                    carbonContext.setTenantDomain(tenantDomain);
                    carbonContext.setTenantId(tenantId);
                    carbonContext.setUsername(username);
                    if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                        APIUtil.loadTenantConfigBlockingMode(tenantDomain);
                    }
                    return true;
                } catch (UserStoreException e) {
                    log.error("Error while retrieving tenant id for tenant domain: " + tenantDomain, e);
                }
            } else {
                log.error("You cannot access API as scope validation failed");
            }
        } else {
            log.error("Authentication failed. Please check your username/password");
        }
        return false;
    }

    /**
     * @param message   CXF message to be validate
     * @param tokenInfo Token information associated with incoming request
     * @return return true if we found matching scope in resource and token information
     * else false(means scope validation failed).
     */
    private boolean validateScopes(Message message, AccessTokenInfo tokenInfo) {
        String basePath = (String) message.get(Message.BASE_PATH);
        String path = (String) message.get(Message.PATH_INFO);
        String verb = (String) message.get(Message.HTTP_REQUEST_METHOD);
        String resource = path.substring(basePath.length() - 1);
        String[] scopes = tokenInfo.getScopes();
        Set<URITemplate> uriTemplates = new HashSet<URITemplate>();
        if (basePath.contains(RestApiConstants.REST_API_PUBLISHER_CONTEXT_FULL_0)) {
            uriTemplates = RestApiUtil.getPublisherAppResourceMapping(RestApiConstants.REST_API_PUBLISHER_VERSION_0);
        } else if (basePath.contains(RestApiConstants.REST_API_PUBLISHER_CONTEXT_FULL_1)) {
            uriTemplates = RestApiUtil.getPublisherAppResourceMapping(RestApiConstants.REST_API_PUBLISHER_VERSION_1);
        } else if (basePath.contains(RestApiConstants.REST_API_STORE_CONTEXT_FULL_0)) {
            uriTemplates = RestApiUtil.getStoreAppResourceMapping(RestApiConstants.REST_API_STORE_VERSION_0);
        } else if (basePath.contains(RestApiConstants.REST_API_STORE_CONTEXT_FULL_1)) {
            uriTemplates = RestApiUtil.getStoreAppResourceMapping(RestApiConstants.REST_API_STORE_VERSION_1);
        } else if (basePath.contains(RestApiConstants.REST_API_ADMIN_CONTEXT)) {
            uriTemplates = RestApiUtil.getAdminAPIAppResourceMapping();
        } else {
            log.error("No matching scope validation logic found for app request with path: " + basePath);
        }
        for (Object template : uriTemplates.toArray()) {
            org.wso2.uri.template.URITemplate templateToValidate = null;
            Map<String, String> var = new HashMap<String, String>();
            //check scopes with what we have
            String templateString = ((URITemplate) template).getUriTemplate();
            try {
                templateToValidate = new org.wso2.uri.template.URITemplate(templateString);
            } catch (URITemplateException e) {
                log.error("Error while creating URI Template object to validate request. Template pattern: " +
                        templateString, e);
            }
            if (templateToValidate != null && templateToValidate.matches(resource, var) && scopes != null
                    && verb != null && verb.equalsIgnoreCase(((URITemplate) template).getHTTPVerb())) {
                for (String scope : scopes) {
                    Scope scp = ((URITemplate) template).getScope();
                    if (scp != null) {
                        if (scope.equalsIgnoreCase(scp.getKey())) {
                            //we found scopes matches
                            if (log.isDebugEnabled()) {
                                log.debug("Scope validation successful for access token: " +
                                        tokenInfo.getAccessToken() + " with scope: " + scp.getKey() +
                                        " for resource path: " + path + " and verb " + verb);
                            }
                            return true;
                        }
                    } else if (((URITemplate) template).getScopes() != null) {
                        //todo: fix template.getScopes() properly to return all scopes
                        List<Scope> scopesList = new ArrayList<Scope>() {{
                            add(((URITemplate) template).getScopes());
                        }};
                        for (Scope scpObj : scopesList) {
                            if (scope.equalsIgnoreCase(scpObj.getKey())) {
                                //we found scopes matches
                                if (log.isDebugEnabled()) {
                                    log.debug("Scope validation successful for access token: " +
                                            tokenInfo.getAccessToken() + " with scope: " + scpObj.getKey() +
                                            " for resource path: " + path + " and verb " + verb);
                                }
                                return true;
                            }
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Scope not defined in swagger for matching resource " + resource + " and verb "
                                    + verb + " . So consider as anonymous permission and let request to continue.");
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
