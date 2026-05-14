/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.multitenant.auth;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.multitenant.auth.internal.MultiTenantAuthDataHolder;
import org.wso2.carbon.apimgt.multitenant.auth.utils.TenantServiceProviderUtil;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authenticator.oidc.OpenIDConnectAuthenticator;
import org.wso2.carbon.identity.application.authenticator.oidc.model.OIDCStateInfo;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.OAuthAdminServiceImpl;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.AMPERSAND_SIGN;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.AUTHENTICATOR_PARAM;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.COMMON_SP_NAME;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.EQUAL_SIGN;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.IDP_PARAMETER;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.SESSION_DATA_KEY_PARAM;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.SUPER_TENANT_DOMAIN;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.TENANT_DOMAIN_PARAM;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.TENANT_IDENTIFIER;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.TENANT_SELECTION_URL_PROP;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.USERINFO_URL;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.USER_SELECTED_TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.authenticator.oidc.OIDCAuthenticatorConstants.CLIENT_ID;
import static org.wso2.carbon.identity.application.authenticator.oidc.OIDCAuthenticatorConstants.CLIENT_SECRET;
import static org.wso2.carbon.identity.application.authenticator.oidc.OIDCAuthenticatorConstants.IdPConfParams.OIDC_LOGOUT_URL;
import static org.wso2.carbon.identity.application.authenticator.oidc.OIDCAuthenticatorConstants.OAUTH2_AUTHZ_URL;
import static org.wso2.carbon.identity.application.authenticator.oidc.OIDCAuthenticatorConstants.OAUTH2_TOKEN_URL;

/**
 * Multi Tenant Authenticator is a federated outbound authenticator that implements
 * tenant-aware SSO for the WSO2 API Manager.
 * <p>
 * The authenticator implements a multi-step flow:
 * <ol>
 *   <li>Step 1 (Initial): Redirect to tenant selection page.</li>
 *   <li>Step 2 (Tenant received): Resolve tenant app client_id and redirect to
 *       IS /t/{tenant}/oauth2/authorize.</li>
 *   <li>Step 3 (Auth code received): Exchange code for token, get user info, build claims.</li>
 * </ol>
 * This extends the {@link OpenIDConnectAuthenticator} implementation.
 */
public class MultiTenantAuthenticator extends OpenIDConnectAuthenticator {

    private static final long serialVersionUID = 6614257960044886319L;
    private static final Log LOG = LogFactory.getLog(MultiTenantAuthenticator.class);
    private static final String SSO_ADDITIONAL_PARAMS = "ssoAdditionalParams";
    private static final String DYNAMIC_PARAMETER_LOOKUP_REGEX = "\\$\\{(\\w+)\\}";
    private static final String DYNAMIC_AUTH_PARAMS_LOOKUP_REGEX = "\\$authparam\\{(\\w+)\\}";

    // =============================================
    // Override public methods
    // =============================================

    @Override
    public boolean canHandle(HttpServletRequest request) {

        // Handle logout requests via the super class.
        if (super.canHandle(request)) {
            return true;
        }
        // Handle the tenant selection response with the tenant identifier parameter.
        return StringUtils.isNotBlank(request.getParameter(TENANT_IDENTIFIER));
    }

    @Override
    public String getFriendlyName() {

        return MultiTenantAuthenticatorConstants.AUTHENTICATOR_FRIENDLY_NAME;
    }

    @Override
    public String getName() {

        return MultiTenantAuthenticatorConstants.AUTHENTICATOR_NAME;
    }

    @Override
    public List<Property> getConfigurationProperties() {

        List<Property> configProperties = new ArrayList<>();

        Property commonSpName = new Property();
        commonSpName.setName(COMMON_SP_NAME);
        commonSpName.setDisplayName("Common Service Provider Name");
        commonSpName.setRequired(true);
        commonSpName.setDescription(
                "Enter common service provider name registered in each tenant (e.g., PublisherCommonSP)");
        configProperties.add(commonSpName);

        Property tenantSelectionUrl = new Property();
        tenantSelectionUrl.setName(TENANT_SELECTION_URL_PROP);
        tenantSelectionUrl.setDisplayName("Tenant Selection Page URL");
        tenantSelectionUrl.setRequired(true);
        tenantSelectionUrl.setDescription(
                "Enter tenant selection page URL (e.g., https://localhost:9443/select-tenant)");
        configProperties.add(tenantSelectionUrl);

        configProperties.addAll(super.getConfigurationProperties());

        Property logoutEndpoint = new Property();
        logoutEndpoint.setName(OIDC_LOGOUT_URL);
        logoutEndpoint.setDisplayName("Logout Endpoint URL");
        logoutEndpoint.setRequired(true);
        logoutEndpoint.setDescription("Enter OpenID Connect logout endpoint URL value");
        logoutEndpoint.setDisplayOrder(11);
        configProperties.add(logoutEndpoint);

        return configProperties;
    }

    /**
     * Main entry point — implements the multi-step authentication flow.
     * <p>
     * If no tenant identifier is present, redirects to the tenant selection page.
     * Otherwise stores the tenant domain in the context and delegates to the super class.
     */
    @Override
    public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
                                           AuthenticationContext context) throws AuthenticationFailedException,
            LogoutFailedException {

        LOG.info("Processing authentication request for authenticator: " + getName());

        if (context.isLogoutRequest()) {
            String idTokenHint = this.getIdTokenHint(context);
            String tenantDomain = extractTenantDomainFromIdTokenHintSub(idTokenHint);
            // Fallback to super tenant
            if (StringUtils.isBlank(tenantDomain)) {
                tenantDomain = SUPER_TENANT_DOMAIN;
            }
            String serverBaseURL = getServerBaseURL();
            context.setProperty(USER_SELECTED_TENANT_DOMAIN, tenantDomain);
            if (!SUPER_TENANT_DOMAIN.equals(context.getProperty(USER_SELECTED_TENANT_DOMAIN))) {
                context.getAuthenticatorProperties().put(IdentityApplicationConstants.OAuth2.CALLBACK_URL, serverBaseURL
                        + "/commonauth");
                context.getAuthenticatorProperties().put(OIDC_LOGOUT_URL, serverBaseURL + "/t/"
                        + tenantDomain + "/oidc/logout");
            }
            return super.process(request, response, context);
        }

        try {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (parameterMap != null && (parameterMap.containsKey("code")
                    || parameterMap.containsKey("error"))) {
                // Callback from IdP with auth code or error. 
                // Delegate to OIDC parent for token exchange and user info.
                return super.process(request, response, context);
            }
            String tenantIdentifier = request.getParameter(TENANT_IDENTIFIER);
            if (StringUtils.isBlank(tenantIdentifier)) {
                redirectToTenantSelectionPage(response, context);
                return AuthenticatorFlowStatus.INCOMPLETE;
            }
            // Store the user-selected tenant domain in a unique property to prevent it from being overridden
            context.setProperty(USER_SELECTED_TENANT_DOMAIN, tenantIdentifier);
            context.setProperty(TENANT_DOMAIN_PARAM, tenantIdentifier);
            return super.process(request, response, context);
        } catch (IOException e) {
            throw new AuthenticationFailedException(
                    MultiTenantAuthenticatorConstants.ErrorMessages.TENANT_REDIRECT_FAILED.getMessage(), e);
        }
    }

    // =============================================
    // Override protected methods
    // =============================================

    /**
     * {@inheritDoc}
     * <p>
     * Resolves the tenant-specific OAuth2 credentials and endpoints before delegating
     * to the super class to build and send the authorize redirect.
     */
    @Override
    protected void initiateAuthenticationRequest(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {
        try {
            if (!SUPER_TENANT_DOMAIN.equals(context.getProperty(USER_SELECTED_TENANT_DOMAIN))) {
                overrideTenantAuthenticatorProperties(context);
            }
            super.initiateAuthenticationRequest(request, response, context);
        } catch (AuthenticationFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationFailedException("Error while initiating authentication request.", e);
        }
    }

    @Override
    protected String getScope(String scope, Map<String, String> authenticatorProperties) {

        if (StringUtils.isBlank(scope)) {
            scope = "openid groups";
        }
        return scope;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resolves tenant-specific OAuth2 credentials and endpoints, then delegates to the
     * super class to exchange the authorization code for tokens and retrieve user info.
     */
    @Override
    protected void processAuthenticationResponse(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationContext context) throws AuthenticationFailedException {

        try {
            if (SUPER_TENANT_DOMAIN.equals(context.getProperty(USER_SELECTED_TENANT_DOMAIN))) {
                super.processAuthenticationResponse(request, response, context);
                return;
            } else {
                overrideTenantAuthenticatorProperties(context);
                super.processAuthenticationResponse(request, response, context);
            }
            
            // Fix tenant domain and user details in the authenticated user object
            AuthenticatedUser user = context.getSubject();
            if (user != null) {
                // Get the tenant domain that was selected by the user during authentication
                String userSelectedTenantDomain = (String) context.getProperty(USER_SELECTED_TENANT_DOMAIN);
                String userName = user.getAuthenticatedSubjectIdentifier();
                
                if (StringUtils.isNotBlank(userSelectedTenantDomain)) {
                    String userStoreDomain = "PRIMARY";
                    
                    // Extract user store domain if present (format: DOMAIN/username)
                    if (userName != null && userName.contains("/")) {
                        userStoreDomain = IdentityUtil.extractDomainFromName(userName);
                        userName = MultitenantUtils.getTenantAwareUsername(userName);
                    }
                    
                    // Set all required fields on the AuthenticatedUser object
                    user.setUserName(userName);
                    user.setTenantDomain(userSelectedTenantDomain);
                    user.setUserStoreDomain(userStoreDomain);
                    user.setAuthenticatedSubjectIdentifier(userName);
                    
                    // Set the updated user back into the context
                    context.setSubject(user);
                } else {
                    LOG.warn("User selected tenant domain not found in context. "
                            + "User may be authenticated in wrong tenant.");
                }
            }
        } catch (AuthenticationFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationFailedException("Error while resolving service provider credentials.", e);
        }
    }

    @Override
    protected void initiateLogoutRequest(HttpServletRequest request, HttpServletResponse response,
                                         AuthenticationContext context) throws LogoutFailedException {

        if (SUPER_TENANT_DOMAIN.equals(context.getProperty(USER_SELECTED_TENANT_DOMAIN))) {
            super.initiateLogoutRequest(request, response, context);
            return;
        }
        if (this.isLogoutEnabled(context)) {
            String logoutUrl = this.getLogoutUrl(context.getAuthenticatorProperties());
            Map<String, String> paramMap = new HashMap<>();
            String idTokenHint = this.getIdTokenHint(context);
            if (StringUtils.isNotBlank(idTokenHint)) {
                paramMap.put("id_token_hint", idTokenHint);
            }

            String callback = this.getCallbackUrl(context.getAuthenticatorProperties());
            paramMap.put("post_logout_redirect_uri", callback);
            String sessionID = this.getStateParameter(context, context.getAuthenticatorProperties());
            paramMap.put("state", sessionID);

            String userSelectedTenantDomain = null;
            if (context.getProperty(USER_SELECTED_TENANT_DOMAIN) != null) {
                userSelectedTenantDomain = (String) context.getProperty(USER_SELECTED_TENANT_DOMAIN);
            } else {
                AuthenticatedUser authenticatedUser = context.getSubject();
                if (authenticatedUser != null
                        && StringUtils.isNotBlank(authenticatedUser.getAuthenticatedSubjectIdentifier())) {
                    String subjectIdentifier = authenticatedUser.getAuthenticatedSubjectIdentifier();
                    String[] parts = subjectIdentifier.split("@");
                    if (parts.length > 1) {
                        userSelectedTenantDomain = parts[parts.length - 1];
                    }
                }
            }

            if (StringUtils.isNotBlank(userSelectedTenantDomain)) {
                paramMap.put("tenantDomain", userSelectedTenantDomain);
            } else {
                LOG.warn("Unable to extract tenant domain from authenticated user." +
                        " post_logout_redirect_uri will be used without tenantDomain parameter.");
            }

            try {
                logoutUrl = FrameworkUtils.buildURLWithQueryParams(logoutUrl, paramMap);
                response.sendRedirect(logoutUrl);
            } catch (IOException e) {
                String idpName = context.getExternalIdP().getName();
                String tenantDomain = context.getTenantDomain();
                throw new LogoutFailedException("Error occurred while initiating the logout request to IdP: "
                        + idpName + " of tenantDomain: " + tenantDomain, e);
            }
        } else {
            super.initiateLogoutRequest(request, response, context);
        }
    }

    // =============================================
    // Private helper methods - Tenant and Authentication
    // =============================================

    /**
     * Redirects the user to the tenant selection page, passing the session data key,
     * authenticator name, and IdP name as query parameters.
     *
     * @param response The HTTP response used for the redirect.
     * @param context  The current authentication context.
     * @throws IOException If the redirect fails.
     */
    private void redirectToTenantSelectionPage(HttpServletResponse response, AuthenticationContext context)
            throws IOException {

        Map<String, String> authenticatorProperties = context.getAuthenticatorProperties();
        String tenantSelectionUrl = authenticatorProperties.get(TENANT_SELECTION_URL_PROP);
        String sessionDataKey = context.getContextIdentifier();

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(SESSION_DATA_KEY_PARAM, sessionDataKey);
        paramMap.put(AUTHENTICATOR_PARAM, getName());
        paramMap.put(IDP_PARAMETER, context.getExternalIdP().getIdPName());

        String redirectUrl = FrameworkUtils.buildURLWithQueryParams(tenantSelectionUrl, paramMap);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Redirecting to tenant selection page: " + redirectUrl);
        }
        response.sendRedirect(redirectUrl);
    }

    /**
     * Resolves the tenant-specific OAuth2 client credentials and endpoint URLs, then
     * overrides the authenticator properties so that the super class operates against
     * the correct tenant.
     * <p>
     * This is invoked by both {@link #initiateAuthenticationRequest} and
     * {@link #processAuthenticationResponse} to ensure consistent configuration.
     *
     * @param context The current authentication context.
     * @throws Exception If the service provider or OAuth app cannot be resolved.
     */
    private void overrideTenantAuthenticatorProperties(AuthenticationContext context) throws Exception {

        Map<String, String> authenticatorProperties = context.getAuthenticatorProperties();

        // Get the user-selected tenant domain from the unique property
        String tenantDomain = (String) context.getProperty(USER_SELECTED_TENANT_DOMAIN);
        if (StringUtils.isBlank(tenantDomain)) {
            // Fallback to TENANT_DOMAIN_PARAM if USER_SELECTED_TENANT_DOMAIN is not set
            tenantDomain = (String) context.getProperty(TENANT_DOMAIN_PARAM);
        }
        String spName = authenticatorProperties.get(COMMON_SP_NAME);

        String resolvedClientId = TenantServiceProviderUtil.resolveClientId(tenantDomain, spName);
        if (StringUtils.isBlank(resolvedClientId)) {
            throw new Exception(String.format(
                    MultiTenantAuthenticatorConstants.ErrorMessages.CLIENT_ID_RESOLUTION_FAILED
                            .getMessage(), tenantDomain));
        }
        OAuthConsumerAppDTO oauthApp = getOAuthAdminService().getOAuthApplicationData(resolvedClientId);
        String resolvedClientSecret = oauthApp.getOauthConsumerSecret();

        String serverBaseURL = getServerBaseURL();

        authenticatorProperties.put(CLIENT_ID, resolvedClientId);
        authenticatorProperties.put(CLIENT_SECRET, resolvedClientSecret);
        authenticatorProperties.put(OAUTH2_AUTHZ_URL, serverBaseURL + "/t/" + tenantDomain + "/oauth2/authorize");
        authenticatorProperties.put(OAUTH2_TOKEN_URL, serverBaseURL + "/t/" + tenantDomain + "/oauth2/token");
        authenticatorProperties.put(USERINFO_URL, serverBaseURL + "/t/" + tenantDomain + "/oauth2/userinfo");
        authenticatorProperties.put(FrameworkConstants.QUERY_PARAMS, getQueryParams(context));
        authenticatorProperties.put("Scopes", getScopes(context));
        // Dynamically resolve the callback URL based on the original redirect_uri (supports publisher/devportal/admin)
        authenticatorProperties.put("callbackUrl", resolveCallbackUrl(context, tenantDomain));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolved client ID '" + resolvedClientId + "' for SP '" + spName
                    + "' in tenant: " + tenantDomain);
        }
    }

    // =============================================
    // Private helper methods - URL and Query Params
    // =============================================

    /**
     * Dynamically resolves the server base URL (e.g., https://localhost:9443) from
     * the Identity Server configuration instead of using hardcoded values.
     *
     * @return The server base URL.
     */
    private String getServerBaseURL() {

        String serverURL = IdentityUtil.getServerURL("", true, true);
        // Remove any trailing slash
        if (serverURL.endsWith("/")) {
            serverURL = serverURL.substring(0, serverURL.length() - 1);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolved server base URL: " + serverURL);
        }
        return serverURL;
    }

    /**
     * Dynamically determines the callback URL based on the original redirect_uri from the
     * authorization request. This supports both APIM Publisher and DevPortal authentication flows.
     * <p>
     * The method extracts the original redirect_uri parameter and determines the appropriate
     * callback URL path based on whether it's a publisher or devportal request.
     *
     * @param context The authentication context containing the original query parameters.
     * @return The appropriate callback URL (e.g., /publisher/services/auth/callback/login
     *         or /devportal/services/auth/callback/login).
     */
    private String resolveCallbackUrl(AuthenticationContext context, String tenantDomain) {

        String serverBaseURL = getServerBaseURL();
        String defaultCallbackUrl = serverBaseURL + "/t/" + tenantDomain + "/commonauth";

        try {
            String queryParams = context.getQueryParams();
            if (StringUtils.isBlank(queryParams)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No query parameters found, using default callback URL: " + defaultCallbackUrl);
                }
                return defaultCallbackUrl;
            }

            // Extract the original redirect_uri from the query parameters
            String redirectUriParam = Arrays.stream(queryParams.split("&"))
                    .filter(param -> param.startsWith("redirect_uri="))
                    .findFirst()
                    .orElse(null);

            if (StringUtils.isBlank(redirectUriParam)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("No redirect_uri parameter found, using default callback URL: " + defaultCallbackUrl);
                }
                return defaultCallbackUrl;
            }

            // Extract the redirect URI value and decode it
            String redirectUri = redirectUriParam.substring("redirect_uri=".length());
            String callbackUrl = java.net.URLDecoder.decode(redirectUri, "UTF-8");

            if (LOG.isDebugEnabled()) {
                LOG.debug("Original redirect_uri: " + callbackUrl);
            }
            return callbackUrl;

        } catch (Exception e) {
            LOG.error("Error resolving callback URL, using default: " + defaultCallbackUrl, e);
            return defaultCallbackUrl;
        }
    }

    private String getScopes(AuthenticationContext context) {

        String queryParams = context.getQueryParams();
        if (StringUtils.isBlank(queryParams)) {
            return null;
        }
        String scopeParams = Arrays.stream(queryParams.split("&"))
                .filter(param -> param.startsWith("scope="))
                .findFirst()
                .orElse(null);
        if (StringUtils.isNotBlank(scopeParams)) {
            try {
                // Extract the scope value (remove "scope=" prefix)
                String scopeValue = scopeParams.substring("scope=".length());
                // URL decode the scope value (converts %3A to : and + to space)
                return java.net.URLDecoder.decode(scopeValue, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.error("Error decoding scope parameter, returning original value", e);
                return scopeParams.substring("scope=".length());
            }
        }
        return null;
    }

    /**
     * Constructs the query parameters string to be included in an authorization request.
     *
     * @param context       The authentication context.
     * @return Query parameters string.
     */
    private String getQueryParams(AuthenticationContext context) {

        StringBuilder paramBuilder = new StringBuilder();

        String additionalQueryParams = resolveAdditionalQueryParams(context);
        if (StringUtils.isNotBlank(additionalQueryParams)) {
            paramBuilder.append(AMPERSAND_SIGN).append(additionalQueryParams);
        }

        String queryParams = context.getQueryParams();
        String redirectUrl = null;
        if (StringUtils.isNotBlank(queryParams)) {
            redirectUrl = Arrays.stream(queryParams.split("&"))
                    .filter(params -> params.startsWith("redirect_uri="))
                    .findFirst()
                    .orElse(null);
        }

        if (StringUtils.isNotBlank(redirectUrl)) {
            String serverBaseURL = getServerBaseURL();
            paramBuilder.append(AMPERSAND_SIGN).append("redirect_uri").append(EQUAL_SIGN)
                    .append(serverBaseURL + "/commonauth");
        }

        return paramBuilder.toString();
    }

    private String resolveAdditionalQueryParams(AuthenticationContext context) {

        Map<String, String> runtimeParams = getRuntimeParams(context);
        String additionalQueryParams = runtimeParams.get(SSO_ADDITIONAL_PARAMS);
        if (StringUtils.isBlank(additionalQueryParams)) {
            return StringUtils.EMPTY;
        }
        additionalQueryParams = handleAuthParams(runtimeParams, additionalQueryParams);
        additionalQueryParams = handleRequestParams(context, additionalQueryParams);
        return additionalQueryParams;
    }

    private String handleAuthParams(Map<String, String> runtimeParams, String queryString) {

        Matcher matcher = Pattern.compile(DYNAMIC_AUTH_PARAMS_LOOKUP_REGEX)
                .matcher(queryString);
        while (matcher.find()) {
            String value = StringUtils.EMPTY;
            String paramName = matcher.group(1);
            if (StringUtils.isNotEmpty(runtimeParams.get(paramName))) {
                value = runtimeParams.get(paramName);
            }
            queryString = queryString.replaceAll("\\$authparam\\{" + paramName + "}", Matcher.quoteReplacement(value));
        }
        return queryString;
    }

    private String handleRequestParams(AuthenticationContext context, String queryString) {

        String requestParamsString = context.getQueryParams();
        Map<String, String> requestParams = new HashMap<>();
        if (StringUtils.isNotBlank(requestParamsString)) {
            String[] params = requestParamsString.split(AMPERSAND_SIGN);
            for (String param : params) {
                String[] keyValue = param.split(EQUAL_SIGN, 2);
                if (keyValue.length == 2) {
                    requestParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        Matcher matcher = Pattern.compile(DYNAMIC_PARAMETER_LOOKUP_REGEX)
                .matcher(queryString);
        while (matcher.find()) {
            String paramName = matcher.group(1);
            String value = StringUtils.EMPTY;
            if (requestParams.containsKey(paramName)) {
                value = requestParams.get(paramName);
            }
            queryString = queryString.replaceAll("\\$\\{" + paramName + "}", Matcher.quoteReplacement(value));
        }
        return queryString;
    }

    // =============================================
    // Private helper methods - Service and Token
    // =============================================

    /**
     * Retrieves the {@link OAuthAdminServiceImpl} from the data holder.
     *
     * @return The OAuthAdminServiceImpl instance.
     */
    private OAuthAdminServiceImpl getOAuthAdminService() {

        return MultiTenantAuthDataHolder.getInstance().getOAuthAdminService();
    }

    // This method is repeating in OpenIDConnectAuthenticator, consider refactoring to a common utility if needed.
    private boolean isLogoutEnabled(AuthenticationContext context) {

        String logoutUrl = this.getLogoutUrl(context.getAuthenticatorProperties());
        return StringUtils.isNotBlank(logoutUrl);
    }

    // This method is repeating in OpenIDConnectAuthenticator, consider refactoring to a common utility if needed.
    private String getIdTokenHint(AuthenticationContext context) {

        return context.getStateInfo() instanceof OIDCStateInfo ? ((OIDCStateInfo) context.getStateInfo())
                .getIdTokenHint() : null;
    }

    // This method is repeating in OpenIDConnectAuthenticator, consider refactoring to a common utility if needed.
    private String getStateParameter(AuthenticationContext context, Map<String, String> authenticatorProperties) {

        String state = context.getContextIdentifier() + "," + "OIDC";
        return this.getState(state, authenticatorProperties);
    }

    private String extractTenantDomainFromIdTokenHintSub(String idTokenHint) {

        if (StringUtils.isBlank(idTokenHint)) {
            return null;
        }
        try {
            String[] tokenParts = idTokenHint.split("\\.");
            if (tokenParts.length < 2) {
                return null;
            }
            String payload = new String(Base64.getUrlDecoder().decode(tokenParts[1]),
                    StandardCharsets.UTF_8);
            JSONObject payloadJson = new JSONObject(payload);
            String tenantedQualifiedUsername = payloadJson.optString("sub", null);
            if (StringUtils.isBlank(tenantedQualifiedUsername) || !tenantedQualifiedUsername.contains("@")) {
                return null;
            }
            String[] parts = tenantedQualifiedUsername.split("@");
            return parts[parts.length - 1];
        } catch (Exception e) {
            LOG.error("Error extracting tenant domain from ID token hint.", e);
            return null;
        }
    }
}
