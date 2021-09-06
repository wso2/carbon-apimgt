/*
 *
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.util.authenticators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.Message;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.OAuthTokenInfo;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.MethodStats;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.uri.template.URITemplateException;

import javax.cache.Cache;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class implemented for common methods of JWT and Opaque Authentications
 */
public abstract class AbstractOAuthAuthenticator {
    Log log = LogFactory.getLog(AbstractOAuthAuthenticator.class);

    /**
     * @param message cxf message to be authenticated
     * @return true if authentication was successful else false
     * @throws APIManagementException when error in authentication process
     */
    public abstract boolean authenticate(Message message) throws APIManagementException;

    /**
     * @return rest API token cache
     */
    public Cache getRESTAPITokenCache() {
        return CacheProvider.getRESTAPITokenCache();
    }

    /**
     * @return rest API invalid token cache
     */
     public Cache getRESTAPIInvalidTokenCache() {
        return CacheProvider.getRESTAPIInvalidTokenCache();
    }

    /**
     * @param message   CXF message to be validate
     * @param tokenInfo Token information associated with incoming request
     * @return return true if we found matching scope in resource and token information
     * else false(means scope validation failed).
     */
    @MethodStats
    public boolean validateScopes(Message message, OAuthTokenInfo tokenInfo) {
        String basePath = (String) message.get(Message.BASE_PATH);
        // path is obtained from Message.REQUEST_URI instead of Message.PATH_INFO, as Message.PATH_INFO contains
        // decoded values of request parameters
        String path = (String) message.get(Message.REQUEST_URI);
        String verb = (String) message.get(Message.HTTP_REQUEST_METHOD);
        String resource = path.substring(basePath.length() - 1);
        String[] scopes = tokenInfo.getScopes();

        String version = (String) message.get(RestApiConstants.API_VERSION);

        //get all the URI templates of the REST API from the base path
        Set<URITemplate> uriTemplates = RestApiUtil.getURITemplatesForBasePath(basePath + version);
        if (uriTemplates.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No matching scopes found for request with path: " + basePath
                        + ". Skipping scope validation.");
            }
            return true;
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
                                        message.get(RestApiConstants.MASKED_TOKEN) + " with scope: " + scp.getKey() +
                                        " for resource path: " + path + " and verb " + verb);
                            }
                            return true;
                        }
                    } else if (!((URITemplate) template).retrieveAllScopes().isEmpty()) {
                        List<Scope> scopesList = ((URITemplate) template).retrieveAllScopes();
                        for (Scope scpObj : scopesList) {
                            if (scope.equalsIgnoreCase(scpObj.getKey())) {
                                //we found scopes matches
                                if (log.isDebugEnabled()) {
                                    log.debug("Scope validation successful for access token: " +
                                            message.get(RestApiConstants.MASKED_TOKEN) + " with scope: " + scpObj.getKey() +
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
