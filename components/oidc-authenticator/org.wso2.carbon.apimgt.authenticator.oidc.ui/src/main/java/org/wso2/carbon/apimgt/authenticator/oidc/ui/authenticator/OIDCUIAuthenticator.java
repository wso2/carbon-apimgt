/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.authenticator.oidc.ui.authenticator;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.apimgt.authenticator.oidc.ui.common.OIDCAuthenticationClient;
import org.wso2.carbon.apimgt.authenticator.oidc.ui.common.OIDCConstants;
import org.wso2.carbon.apimgt.authenticator.oidc.ui.common.Util;
import org.wso2.carbon.apimgt.authenticator.oidc.ui.internal.OIDCAuthFEDataHolder;
import org.wso2.carbon.ui.AbstractCarbonUIAuthenticator;
import org.wso2.carbon.ui.CarbonSSOSessionManager;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * OIDC UI Authenticator class
 * Handles OIDC login response if valid response code and nonce exists
 */
public class OIDCUIAuthenticator extends AbstractCarbonUIAuthenticator {

    private static final int DEFAULT_PRIORITY_LEVEL = 50;

    public static final Log log = LogFactory.getLog(OIDCUIAuthenticator.class);


    @Override
    public boolean canHandle(HttpServletRequest request) {

        return request.getRequestURI().contains(
                "/carbon/admin/logout_action.jsp");
    }

    @Override
    public void authenticate(HttpServletRequest request) throws AuthenticationException {

        String username = "";

        HttpSession session = request.getSession();
        String responseCode = request.getParameter(OIDCConstants.PARAM_CODE);
        String sessionNonce = (String) request.getSession().getAttribute(
                OIDCConstants.PARAM_NONCE);

        ServletContext servletContext = request.getSession().getServletContext();
        ConfigurationContext configContext = (ConfigurationContext) servletContext.getAttribute(
                CarbonConstants.CONFIGURATION_CONTEXT);

        String backEndServerURL = request.getParameter("backendURL");
        if (backEndServerURL == null) {
            backEndServerURL = CarbonUIUtil.getServerURL(servletContext, session);
        }
        session.setAttribute(CarbonConstants.SERVER_URL, backEndServerURL);
        String cookie = (String) session.getAttribute(ServerConstants.
                ADMIN_SERVICE_AUTH_TOKEN);

        // authorize the user with the back-end
        OIDCAuthenticationClient authenticationClient;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Invoking the OIDC Authenticator BE for the Response Code : " +
                        responseCode);
            }
            authenticationClient = new OIDCAuthenticationClient(
                    configContext, backEndServerURL, cookie, session);

            username = authenticationClient.login(responseCode, sessionNonce);

            // add an entry to CarbonSSOSessionManager : IdpSessionIndex --> localSessionId
            if (username != null && !username.equals("")) {

                CarbonSSOSessionManager oidcSessionManager = OIDCAuthFEDataHolder.
                        getInstance().getCarbonSSOSessionManager();

                if (responseCode != null) {
                    // Session id is provided only when Single Logout enabled at the IdP.
                    oidcSessionManager.addSessionMapping(responseCode,
                            session.getId());
                    request.getSession().setAttribute(OIDCConstants.IDP_SESSION_INDEX, responseCode);
                }
                onSuccessAdminLogin(request, username);
            } else {
                log.error("Authentication failed due to empty user name");
                throw new AuthenticationException("Authentication failed due to empty user name");
            }
        } catch (Exception e) {
            log.error("Error when login to OIDC server", e);
            throw new AuthenticationException("Error when login to OIDC server.", e);
        }

        if (username == null || username.equals("")) {
            throw new AuthenticationException("Authentication failure " + username);
        }

    }

    @Override
    public void unauthenticate(Object o) throws Exception {

        HttpServletRequest request = (HttpServletRequest) o;
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute(CarbonConstants.LOGGED_USER);
        ServletContext servletContext = session.getServletContext();
        ConfigurationContext configContext = (ConfigurationContext) servletContext
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

        String backendServerURL = CarbonUIUtil.getServerURL(servletContext, session);
        try {
            String cookie = (String) session.getAttribute(ServerConstants.
                    ADMIN_SERVICE_AUTH_TOKEN);

            OIDCAuthenticationClient authClient = new
                    OIDCAuthenticationClient(configContext, backendServerURL, cookie, session);

            authClient.logout(session);
            log.info(username + "@" + PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    getTenantDomain() +" successfully logged out");

        } catch (Exception ignored) {
            String msg = "OIDC logout failed";
            log.error(msg, ignored);
            throw new Exception(msg, ignored);
        }

        String logoutUrl = Util.getIdentityProviderURI() + "logout";

        request.setAttribute(OIDCConstants.HTTP_ATTR_IS_LOGOUT_REQ, true);
        request.setAttribute(OIDCConstants.EXTERNAL_LOGOUT_PAGE, logoutUrl);
    }

    public int getPriority() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(OIDCConstants.AUTHENTICATOR_NAME);
        if (authenticatorConfig != null && authenticatorConfig.getPriority() > 0) {
            return authenticatorConfig.getPriority();
        }
        return DEFAULT_PRIORITY_LEVEL;
    }

    @Override
    public String getAuthenticatorName() {
        return OIDCConstants.AUTHENTICATOR_NAME;
    }

    public boolean isDisabled() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(OIDCConstants.AUTHENTICATOR_NAME);

        return authenticatorConfig != null && authenticatorConfig.isDisabled();
    }

	@Override
	public void authenticateWithCookie(HttpServletRequest request)
			throws AuthenticationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	 public String doAuthentication(Object credentials, boolean isRememberMe, ServiceClient client,
	            HttpServletRequest request) throws AuthenticationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleRememberMe(Map transportHeaders, HttpServletRequest httpServletRequest)
			throws AuthenticationException {
		// TODO Auto-generated method stub
		
	}
}
