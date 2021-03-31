/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.util.interceptors.auth;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.RealmUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.uri.template.URITemplateException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class will validate incoming requests with Basic authenticator headers. This will also validate the roles of
 * the user based on the scopes attached to the API resources.
 * You can place this handler name in your web application if you need Basic authentication.
 */
public class BasicAuthenticationInterceptor extends AbstractPhaseInterceptor {

    private static final Log log = LogFactory.getLog(BasicAuthenticationInterceptor.class);

    public BasicAuthenticationInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }

    /**
     * This method handles the incoming message by checking if an anonymous api is being called or invalid
     * authorization headers are present in the request. If not, authenticate the request.
     *
     * @param inMessage cxf Message
     */
    @Override
    public void handleMessage(Message inMessage) {
        //by-passes the interceptor if user calls an anonymous api
        if (RestApiUtil.checkIfAnonymousAPI(inMessage)) {
            return;
        }
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        inMessage.put(RestApiConstants.TENANT_DOMAIN, tenantDomain);
        //Extract and check if "Authorization: Basic" is present in the request. If not, by-passes the interceptor.
        //If yes, set the request_authentication_scheme property in the message as basic_auth and execute the basic 
        //authentication flow.
        AuthorizationPolicy policy = inMessage.get(AuthorizationPolicy.class);
        if (policy != null) {
            inMessage.put(RestApiConstants.REQUEST_AUTHENTICATION_SCHEME, RestApiConstants.BASIC_AUTHENTICATION);
            //Extract user credentials from the auth header and validate.
            String username = StringUtils.trim(policy.getUserName());
            String password = StringUtils.trim(policy.getPassword());
            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
                String errorMessage = StringUtils.isEmpty(username) ?
                        "username cannot be null/empty." : "password cannot be null/empty.";
                log.error("Basic Authentication failed: " + errorMessage);
                throw new AuthenticationException("Unauthenticated request");
            } else if (!authenticate(inMessage, username, password)) {
                throw new AuthenticationException("Unauthenticated request");
            }
            log.debug("User logged into web app using Basic Authentication");
        }
    }

    /**
     * This method authenticates the request using Basic authentication and validate the roles of user based on
     * roles of scope.
     *
     * @param inMessage cxf Message
     * @param username  username in basic auth header
     * @param password  password in basic auth header
     * @return true if user is successfully authenticated and authorized. false otherwise.
     */
    private boolean authenticate(Message inMessage, String username, String password) {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        UserRealm userRealm;
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            userRealm = RealmUtil.getTenantUserRealm(tenantId);

            if (userRealm == null) {
                log.error("Authentication failed: invalid domain or unactivated tenant login");
                return false;
            }
            UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            boolean isAuthenticated = userStoreManager.authenticate(MultitenantUtils.
                    getTenantAwareUsername(username), password);
            if (isAuthenticated) {
                String domain = UserCoreUtil.getDomainFromThreadLocal();
                String domainAwareUserName = UserCoreUtil.addDomainToName(username, domain);
                RestApiCommonUtil
                        .setThreadLocalRequestedTenant(MultitenantUtils.getTenantAwareUsername(username));
                carbonContext.setTenantDomain(tenantDomain);
                carbonContext.setTenantId(tenantId);
                carbonContext.setUsername(domainAwareUserName);
                if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    APIUtil.loadTenantConfigBlockingMode(tenantDomain);
                }
                return validateRoles(inMessage, userRealm, tenantDomain, username);
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while authenticating user: " + username, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return false;
    }

    /**
     * This method validates the roles of the user against the roles associated with the relevant scopes of the invoking
     * API request resource.
     *
     * @param inMessage    cxf Message
     * @param userRealm    UserRealm
     * @param tenantDomain tenant domain name
     * @param username     username
     * @return true if user is authorized, false otherwise.
     */
    private boolean validateRoles(Message inMessage, UserRealm userRealm, String tenantDomain, String username) {

        String basePath = (String) inMessage.get(Message.BASE_PATH);
        String path = (String) inMessage.get(Message.REQUEST_URI);
        String verb = (String) inMessage.get(Message.HTTP_REQUEST_METHOD);
        String resource = path.substring(basePath.length() - 1);
        String version = (String) inMessage.get(RestApiConstants.API_VERSION);
        String[] userRoles;
        Map<String, String> restAPIScopes;
        //get all the URI templates of the REST API from the base path
        Set<URITemplate> uriTemplates = RestApiUtil.getURITemplatesForBasePath(basePath + version);
        if (uriTemplates.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No matching scopes found for request with path: " + basePath + version
                        + ". Skipping role validation.");
            }
            return true;
        }

        //iterate through all the URITemplates to get the relevant URI template and get the scopes attached to validate
        for (Object template : uriTemplates.toArray()) {
            org.wso2.uri.template.URITemplate templateToValidate;
            Map<String, String> var = new HashMap<>();
            String templateString = ((URITemplate) template).getUriTemplate();
            try {
                templateToValidate = new org.wso2.uri.template.URITemplate(templateString);

                //check if the current URITemplate matches with the resource and verb of the API request
                if (templateToValidate.matches(resource, var) && verb != null
                        && verb.equalsIgnoreCase(((URITemplate) template).getHTTPVerb())) {

                    //get the scope list of the matched URITemplate
                    List<Scope> resourceScopeList = ((URITemplate) template).retrieveAllScopes();

                    //Continue the role check only if the invoking resource URI template has roles
                    if (!resourceScopeList.isEmpty()) {
                        //get the configured RESTAPIScopes map for the tenant from cache or registry
                        restAPIScopes = APIUtil.getRESTAPIScopesForTenant(tenantDomain);
                        if (restAPIScopes != null) {
                            //get the current role list of the user from local user store manager 
                            userRoles = userRealm.getUserStoreManager()
                                    .getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));
                            if (userRoles != null) {
                                return validateUserRolesWithRESTAPIScopes(resourceScopeList, restAPIScopes,
                                        userRoles, username, path, verb, inMessage);
                            } else {
                                log.error("Error while validating roles. Invalid user roles found for user: "
                                        + username);
                                return false;
                            }
                        } else {
                            //Error while getting the RESTAPIScopes
                            return false;
                        }
                    } else {
                        //Invoking resource has no scopes attached to it. Consider as anonymous permission.
                        if (log.isDebugEnabled()) {
                            log.debug("Scope not defined in swagger for matching resource " + resource + " and verb "
                                    + verb + ". So consider as anonymous permission and let request to continue.");
                        }
                        return true;
                    }
                }
            } catch (URITemplateException e) {
                log.error("Error while creating URI Template object to validate request. Template pattern: " +
                        templateString, e);
            } catch (UserStoreException e) {
                log.error("Error while getting role list of user: " + username, e);
            }
        }
        //No matching resource or verb found in swagger 
        log.error("Error while validating roles. No matching resource URI template found in swagger for resource "
                + resource + " and verb " + verb);
        return false;
    }

    /**
     * This method validates the user roles against the roles of the REST API scopes defined for the current resource.
     *
     * @param resourceScopeList Scope list of the current resource
     * @param restAPIScopes     RESTAPIScopes mapping for the current tenant
     * @param userRoles         Role list for the user
     * @param username          Username
     * @param path              Path Info
     * @param verb              HTTP Request Method
     * @param inMessage         cxf Message to set the matched user scopes for the resource
     * @return whether user role validation against REST API scope roles is success or not.
     */
    private boolean validateUserRolesWithRESTAPIScopes(List<Scope> resourceScopeList, Map<String, String> restAPIScopes,
                                                       String[] userRoles, String username, String path, String verb,
                                                       Message inMessage) {

        //Holds the REST API scope list which the user will get successfully validated against with
        List<Scope> validatedUserScopes = new ArrayList<>();

        //iterate the non empty scope list of the URITemplate of the invoking resource
        for (Scope scope : resourceScopeList) {
            //get the configured roles list string of the requested resource
            String resourceRolesString = restAPIScopes.get(scope.getKey());
            if (StringUtils.isNotBlank(resourceRolesString)) {
                //split role list string read using comma separator
                List<String> resourceRoleList = Arrays.asList(resourceRolesString.split("\\s*,\\s*"));
                //check if the roles related to the API resource contains any of the role of the user
                for (String role : userRoles) {
                    if (resourceRoleList.contains(role)) {
                        //Role validation is success. Add the current scope to the validated user scope list and
                        //skip role check iteration of current scope and move to next resource scope.  
                        validatedUserScopes.add(scope);
                        if (log.isDebugEnabled()) {
                            log.debug("Basic Authentication: role validation successful for user: "
                                    + username + " with scope: " + scope.getKey()
                                    + " for resource path: " + path + " and verb " + verb);
                            log.debug("Added scope: " + scope.getKey() + " to validated user scope list");
                        }
                        break;
                    }
                }
            } else {
                //No role for the requested resource scope. Add it to the validated user scope list. 
                validatedUserScopes.add(scope);
                if (log.isDebugEnabled()) {
                    log.debug("Role validation skipped. No REST API scope to role mapping defined for resource scope: "
                            + scope.getKey() + " Treated as anonymous scope.");
                }
            }
        }

        List<String> scopes = new ArrayList<>();
        validatedUserScopes.forEach(scope -> scopes.add(scope.getKey()));

        // Add the validated user scope list to the cxf message
        inMessage.getExchange().put(RestApiConstants.USER_REST_API_SCOPES, scopes.toArray(new String[0]));

        if (!validatedUserScopes.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Successfully validated REST API Scopes for the user " + username);
            }
            return true;
        }
        //none of the resource scopes were matched against the user role set
        log.error("Insufficient privileges. Role validation failed for user: "
                + username + " to access resource path: " + path + " and verb " + verb);
        return false;
    }

}
