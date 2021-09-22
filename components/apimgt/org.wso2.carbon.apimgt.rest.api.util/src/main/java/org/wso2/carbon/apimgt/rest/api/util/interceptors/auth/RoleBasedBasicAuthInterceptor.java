package org.wso2.carbon.apimgt.rest.api.util.interceptors.auth;

/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * This class is responsible to validate if the user who is invoking the API has a specific role.
 * (At the moment, it supports only the admin role validation)
 *
 */
public class RoleBasedBasicAuthInterceptor extends AbstractPhaseInterceptor {

    private static final Log log = LogFactory.getLog(RoleBasedBasicAuthInterceptor.class);

    private static final String REQUEST_AUTHENTICATION_SCHEME = "request_authentication_scheme";
    public static final String BASIC_AUTHENTICATION = "basic_auth";


    public RoleBasedBasicAuthInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }

    /**
     * This method handles the incoming message by checking if an anonymous api is being called or invalid
     * authorization headers are present in the request. If not, authenticate the request.
     *
     * @param inMessage cxf Message
     */
    @Override public void handleMessage(Message inMessage) {
        //by-passes the interceptor if user calls an anonymous api
        if (RestApiUtil.checkIfAnonymousAPI(inMessage)) {
            return;
        }

        //Extract and check if "Authorization: Basic" is present in the request. If not, by-passes the interceptor.
        //If yes, set the request_authentication_scheme property in the message as basic_auth and execute the basic
        //authentication flow.
        AuthorizationPolicy policy = inMessage.get(AuthorizationPolicy.class);
        if (policy != null) {
            inMessage.put(REQUEST_AUTHENTICATION_SCHEME, BASIC_AUTHENTICATION);
            //Extract user credentials from the auth header and validate.
            String username = StringUtils.trim(policy.getUserName());
            String password = StringUtils.trim(policy.getPassword());
            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
                String errorMessage = StringUtils.isEmpty(username) ?
                        "username cannot be null/empty." :
                        "password cannot be null/empty.";
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
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);
        RegistryService registryService = (RegistryService) carbonContext.getOSGiService(RegistryService.class, null);
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        int tenantId;
        UserRealm userRealm;
        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            userRealm = AnonymousSessionUtil.getRealmByTenantDomain(registryService, realmService, tenantDomain);
            if (userRealm == null) {
                log.error("Authentication failed: invalid domain or unactivated tenant login");
                return false;
            }
            //if authenticated
            if (userRealm.getUserStoreManager()
                    .authenticate(MultitenantUtils.getTenantAwareUsername(username), password)) {
                //set the correct tenant info for downstream code.
                carbonContext.setTenantDomain(tenantDomain);
                carbonContext.setTenantId(tenantId);
                carbonContext.setUsername(username);
                return validateRoles(userRealm, username);
            } else {
                log.error("Authentication failed: Invalid credentials");
            }
        } catch (UserStoreException | CarbonException e) {
            log.error("Error occurred while authenticating user: " + username, e);
        }
        return false;
    }

    /**
     * This method validates the roles of the user against the roles associated with the relevant scopes of the invoking
     * API request resource.
     *
     * @param userRealm UserRealm
     * @param username  username
     * @return true if user is authorized, false otherwise.
     */
    private boolean validateRoles(UserRealm userRealm, String username) {
        String[] userRoles;
        try {
            userRoles = userRealm.getUserStoreManager()
                    .getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(username));
            String adminRole = userRealm.getRealmConfiguration().getAdminRoleName();
            for (String role : userRoles) {
                if (adminRole.equals(role)) {
                    return true;
                }
            }
            return false;
        } catch (UserStoreException e) {
            log.error("Error while getting role list of user: " + username, e);
        }

        //No matching role found
        log.error("Error while validating roles. Required role not available");
        return false;
    }
}
