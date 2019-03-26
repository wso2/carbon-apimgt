/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.rest.api.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.hybrid.gateway.rest.api.exceptions.AuthenticationException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.StringTokenizer;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * This class provides authentication facility for to micro gateway REST API
 * Basic authentication is used for this purpose
 * Users with admin roles are only eligible for accessing those JAX-RS services
 */
public class AuthenticatorUtil {
    private static final String MISSING_CREDENTIALS_MESSAGE = "Missing Credentials";
    private static final String MISSING_CREDENTIALS_DESCRIPTION = "Required credentials not provided. " +
            "Make sure your API invocation call has a header: " +
            "'Authorization: Bearer Base64Encoded(username:password)'";
    private static final String AUTHORIZATION_PROPERTY = "Authorization";
    private static final String AUTHENTICATION_SCHEME = "Basic";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final Log log = LogFactory.getLog(AuthenticatorUtil.class);

    private AuthenticatorUtil() {
    }

    /**
     * Checks whether received credentials for calling to micro gateway REST API.
     *
     * @param headers HTTP headers of the received request
     * @return Response indicating whether authentication and authorization got succeeded
     */
    public static AuthDTO authorizeUser(HttpHeaders headers) throws AuthenticationException {
        //Fetch authorization header
        List<String> authorization = headers.getRequestHeader(AUTHORIZATION_PROPERTY);

        //If no authorization information present; block access
        if (authorization == null || authorization.isEmpty()) {
            log.warn("Received a request to micro gateway REST API without Authorization header.");
            return new AuthDTO(null, null, false, Response.Status.UNAUTHORIZED,
                    MISSING_CREDENTIALS_MESSAGE, MISSING_CREDENTIALS_DESCRIPTION);
        }
        //Get encoded username and password (Replacing authentication scheme with "" to isolate username and password)
        String encodedCredentials = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");

        //Decode username and password
        String usernameAndPassword;
        String username;
        char[] password;
        try {
            usernameAndPassword = new String(Base64.decodeBase64(encodedCredentials.getBytes(DEFAULT_ENCODING)),
                    Charset.forName(DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            log.warn("Received a request to micro gateway REST API with Unsupported Authorization " +
                    "header.");
            return new AuthDTO(null, null, false, Response.Status.UNAUTHORIZED,
                    "Unsupported Encoding of Credentials",
                    "Authorization header is in an Unsupported encoding type. Should be in UTF-8");
        }

        if (!usernameAndPassword.contains(":")) {
            log.warn("Received a request to micro gateway REST API without credentials.");
            return new AuthDTO(null, null, false, Response.Status.UNAUTHORIZED,
                    MISSING_CREDENTIALS_MESSAGE, MISSING_CREDENTIALS_DESCRIPTION);
        }

        //Split username and password tokens
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
        username = tokenizer.nextToken();
        password = tokenizer.nextToken().toCharArray();

        if (username.isEmpty() || password.length == 0) {
            log.warn("Received a micro gateway REST API authentication request with empty username or password.");

            return new AuthDTO(username, null, false, Response.Status.UNAUTHORIZED,
                    MISSING_CREDENTIALS_MESSAGE, MISSING_CREDENTIALS_DESCRIPTION);
        }

        String description = "User : " + username + " is unauthorized to invoke micro gateway REST API.";
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(username);
            if (log.isDebugEnabled()) {
                log.debug("Authenticating user : " + username + " for accessing micro gateway REST API.");
            }
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(tenantAwareUsername);
            UserStoreManager userstoremanager =
                    CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();

            //authenticate user provided credentials
            if (userstoremanager.authenticate(tenantAwareUsername, password)) {
                // Overwriting the char array to clean up password
                for (int i = 0; i < password.length; i++) {
                    password[i] = 0;
                }
                if (log.isDebugEnabled()) {
                    log.debug("User : " + username + " authenticated successfully for micro gateway REST API.");
                }
                //Get admin role name of the current domain
                String adminRoleName = CarbonContext
                        .getThreadLocalCarbonContext()
                        .getUserRealm()
                        .getRealmConfiguration()
                        .getAdminRoleName();
                String[] userRoles = userstoremanager.getRoleListOfUser(tenantAwareUsername);

                //user is only authorized for retrieving identifiers of updated APIs if he is an admin of the tenant
                for (String userRole : userRoles) {
                    if (adminRoleName.equalsIgnoreCase(userRole)) {
                        if (log.isDebugEnabled()) {
                            log.debug(username + " is authorized to access micro gateway REST API.");
                        }
                        return new AuthDTO(username, tenantDomain, true, Response.Status.OK,
                                null, null);
                    }
                }
                description = "User : " + username + " does not have permission to access micro gateway REST API";
            }
        } catch (UserStoreException e) {
            String errorMessage = "Error while authenticating user : " + username;
            log.error(errorMessage, e);
            throw new AuthenticationException(errorMessage, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        if (log.isDebugEnabled()) {
            log.debug(description);
        }
        return new AuthDTO(null, null, false, Response.Status.UNAUTHORIZED,
                "Unauthorized", description);
    }
}
