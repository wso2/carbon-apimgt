/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.AccessTokenRequest;
import org.wso2.carbon.apimgt.api.model.ApplicationConstants;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ScopeDTO;
import org.wso2.carbon.apimgt.impl.kmclient.FormEncoder;
import org.wso2.carbon.apimgt.impl.kmclient.KMClientErrorDecoder;
import org.wso2.carbon.apimgt.impl.kmclient.KeyManagerClientException;
import org.wso2.carbon.apimgt.impl.kmclient.model.ClientInfo;
import org.wso2.carbon.apimgt.impl.kmclient.model.DCRClient;
import org.wso2.carbon.apimgt.impl.kmclient.model.AuthClient;
import org.wso2.carbon.apimgt.impl.kmclient.model.IntrospectInfo;
import org.wso2.carbon.apimgt.impl.kmclient.model.IntrospectionClient;
import org.wso2.carbon.apimgt.impl.kmclient.model.TokenInfo;
import org.wso2.carbon.apimgt.impl.recommendationmgt.AccessTokenGenerator;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class holds the key manager implementation considering WSO2 as the identity provider
 * This is the default key manager supported by API Manager.
 */
public class AMDefaultKeyManagerImpl extends AbstractKeyManager {

    private static final Log log = LogFactory.getLog(AMDefaultKeyManagerImpl.class);
    private static final String GRANT_TYPE_VALUE = "client_credentials";

    private DCRClient dcrClient;
    private IntrospectionClient introspectionClient;
    private AuthClient authClient;
    private CloseableHttpClient kmHttpClient;
    private AccessTokenGenerator accessTokenGenerator;

    @Override
    public OAuthApplicationInfo createApplication(OAuthAppRequest oauthAppRequest) throws APIManagementException {

        // OAuthApplications are created by calling to APIKeyMgtSubscriber Service
        OAuthApplicationInfo oAuthApplicationInfo = oauthAppRequest.getOAuthApplicationInfo();

        // Subscriber's name should be passed as a parameter, since it's under the subscriber the OAuth App is created.
        String userId = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.
                OAUTH_CLIENT_USERNAME);

        if (StringUtils.isEmpty(userId)) {
            throw new APIManagementException("Missing user ID for OAuth application creation.");
        }

        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        if (StringUtils.isEmpty(tenantDomain)) {
            tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
        String accessToken = getAccessTokenForKeyMgt();
        String authorizationHeader = getBearerAuthorizationHeader(accessToken);

        String applicationName = oAuthApplicationInfo.getClientName();
        String keyType = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.APP_KEY_TYPE);

        if (StringUtils.isNotEmpty(applicationName) && StringUtils.isNotEmpty(keyType)) {
            String domain = UserCoreUtil.extractDomainFromName(userId);
            if (domain != null && !domain.isEmpty() && !UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(domain)) {
                userId = userId.replace(UserCoreConstants.DOMAIN_SEPARATOR, "_");
            }
            applicationName = String.format("%s_%s_%s", APIUtil.replaceEmailDomain(userId), applicationName, keyType);
        } else {
            throw new APIManagementException("Missing required information for OAuth application creation.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Trying to create OAuth application :" + applicationName);
        }

        String tokenScope = (String) oAuthApplicationInfo.getParameter("tokenScope");
        String[] tokenScopes = new String[1];
        tokenScopes[0] = tokenScope;

        ClientInfo request = createClientInfo(oAuthApplicationInfo, applicationName, false);
        ClientInfo createdClient;

        try {
            if (APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
                createdClient = dcrClient.createApplication(authorizationHeader, request);
            } else {
                createdClient = dcrClient.createApplicationForTenant(tenantDomain, authorizationHeader, request);
            }
            buildDTOFromClientInfo(createdClient, oAuthApplicationInfo);

            oAuthApplicationInfo.addParameter("tokenScope", tokenScopes);
            oAuthApplicationInfo.setIsSaasApplication(false);

            return oAuthApplicationInfo;

        } catch (KeyManagerClientException e) {
            handleException("Cannot create OAuth application  : " + applicationName, e);
            return null;
        }
    }

    /**
     * Construct ClientInfo object for application create request
     *
     * @param info            The OAuthApplicationInfo object
     * @param applicationName The name of the application to be created. We specifically request for this value as this
     *                        should be formatted properly prior to calling this method
     * @return constructed ClientInfo object
     * @throws JSONException for errors in parsing the OAuthApplicationInfo json string
     */
    private ClientInfo createClientInfo(OAuthApplicationInfo info, String applicationName, boolean isUpdate)
            throws JSONException {

        ClientInfo clientInfo = new ClientInfo();
        JSONObject infoJson = new JSONObject(info.getJsonString());
        if (infoJson.has(ApplicationConstants.OAUTH_CLIENT_GRANT)) {
            // this is done as there are instances where the grant string begins with a comma character.
            String grantString = infoJson.getString(ApplicationConstants.OAUTH_CLIENT_GRANT);
            if(grantString.startsWith(",")) {
                grantString = grantString.substring(1);
            }
            String[] grantTypes = grantString.split(",");
            clientInfo.setGrantTypes(Arrays.asList(grantTypes));
        }
        if (StringUtils.isNotEmpty(info.getCallBackURL())) {
            String callBackURL = info.getCallBackURL();
            String[] callbackURLs = callBackURL.trim().split("\\s*,\\s*");
            clientInfo.setRedirectUris(Arrays.asList(callbackURLs));
        }
        clientInfo.setClientName(applicationName);
        //todo: run tests by commenting the type
        clientInfo.setTokenType(info.getTokenType());
        if (StringUtils.isNotEmpty(info.getClientId())) {
            if (isUpdate) {
                clientInfo.setClientId(info.getClientId());
            } else {
                clientInfo.setPresetClientId(info.getClientId());
            }
        }
        if (StringUtils.isNotEmpty(info.getClientSecret())) {
            if (isUpdate) {
                clientInfo.setClientId(info.getClientSecret());
            } else {
                clientInfo.setPresetClientSecret(info.getClientSecret());
            }
        }
        return clientInfo;
    }

    @Override
    public OAuthApplicationInfo updateApplication(OAuthAppRequest appInfoDTO) throws APIManagementException {

        OAuthApplicationInfo oAuthApplicationInfo = appInfoDTO.getOAuthApplicationInfo();

        String userId = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME);
        String applicationName = oAuthApplicationInfo.getClientName();
        String keyType = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.APP_KEY_TYPE);

        // First we attempt to get the tenant domain from the userID and if it is not possible, we fetch it
        // from the ThreadLocalCarbonContext
        String tenantDomain = MultitenantUtils.getTenantDomain(userId);
        if (StringUtils.isEmpty(tenantDomain)) {
            tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }

        String accessToken = getAccessTokenForKeyMgt();
        String authorizationHeader = getBearerAuthorizationHeader(accessToken);

        if (StringUtils.isNotEmpty(applicationName) && StringUtils.isNotEmpty(keyType)) {
            // Replace the domain name separator with an underscore for secondary user stores
            String domain = UserCoreUtil.extractDomainFromName(userId);
            if (domain != null && !domain.isEmpty() && !UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(domain)) {
                userId = userId.replace(UserCoreConstants.DOMAIN_SEPARATOR, "_");
            }
            // Construct the application name subsequent to replacing email domain separator
            applicationName = String.format("%s_%s_%s", APIUtil.replaceEmailDomain(userId), applicationName, keyType);
        } else {
            throw new APIManagementException("Missing required information for OAuth application update.");
        }

        log.debug("Updating OAuth Client with ID : " + oAuthApplicationInfo.getClientId());
        if (log.isDebugEnabled() && oAuthApplicationInfo.getCallBackURL() != null) {
            log.debug("CallBackURL : " + oAuthApplicationInfo.getCallBackURL());
        }
        if (log.isDebugEnabled() && applicationName != null) {
            log.debug("Client Name : " + applicationName);
        }

        ClientInfo request = createClientInfo(oAuthApplicationInfo, applicationName, true);
        ClientInfo createdClient;
        try {
            if (APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
                createdClient = dcrClient.updateApplication(authorizationHeader,
                        oAuthApplicationInfo.getClientId(), request);
            } else {
                createdClient = dcrClient.updateApplicationForTenant(tenantDomain, authorizationHeader,
                        oAuthApplicationInfo.getClientId(), request);
            }
            return buildDTOFromClientInfo(createdClient, new OAuthApplicationInfo());
        } catch (KeyManagerClientException e) {
            handleException("Error occurred while updating OAuth Client : ", e);
            return null;
        }
    }

    @Override
    public OAuthApplicationInfo updateApplicationOwner(OAuthAppRequest appInfoDTO, String owner)
            throws APIManagementException {

        log.debug("Owner not updated in the Identity Server as all apps are created under ");
        return appInfoDTO.getOAuthApplicationInfo();
    }

    @Override
    public void deleteApplication(String consumerKey) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Trying to delete OAuth application for consumer key :" + consumerKey);
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String accessToken = getAccessTokenForKeyMgt();
        String authorizationHeader = getBearerAuthorizationHeader(accessToken);

        try {
            if (APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
                dcrClient.deleteApplication(authorizationHeader, consumerKey);
            } else {
                dcrClient.deleteApplicationForTenant(tenantDomain, authorizationHeader, consumerKey);
            }
        } catch (KeyManagerClientException e) {
            handleException("Cannot remove service provider for the given consumer key : " + consumerKey, e);
        }
    }

    @Override
    public OAuthApplicationInfo retrieveApplication(String consumerKey) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Trying to retrieve OAuth application for consumer key :" + consumerKey);
        }

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String accessToken = getAccessTokenForKeyMgt();
        String authorizationHeader = getBearerAuthorizationHeader(accessToken);

        ClientInfo clientInfo;
        try {
            if (APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
                clientInfo = dcrClient.getApplication(authorizationHeader, consumerKey);
            } else {
                clientInfo = dcrClient.getApplicationForTenant(tenantDomain, authorizationHeader, consumerKey);
            }
            return buildDTOFromClientInfo(clientInfo, new OAuthApplicationInfo());
        } catch (KeyManagerClientException e) {
            handleException("Cannot retrieve service provider for the given consumer key : " + consumerKey, e);
            return null;
        }
    }

    @Override
    public AccessTokenInfo getNewApplicationAccessToken(AccessTokenRequest tokenRequest) throws APIManagementException {

        AccessTokenInfo tokenInfo;

        if (tokenRequest == null) {
            log.warn("No information available to generate Token.");
            return null;
        }

        //We do not revoke the previously obtained token anymore since we do not possess the access token.

        // When validity time set to a negative value, a token is considered never to expire.
        if (tokenRequest.getValidityPeriod() == OAuthConstants.UNASSIGNED_VALIDITY_PERIOD) {
            // Setting a different -ve value if the set value is -1 (-1 will be ignored by TokenValidator)
            tokenRequest.setValidityPeriod(-2L);
        }

        //Generate New Access Token
        String scopes = String.join(" ", tokenRequest.getScope());
        TokenInfo tokenResponse;

        try {
            if (tokenRequest.getValidityPeriod() != 0) {
                String definedValidityPeriod = Long.toString(tokenRequest.getValidityPeriod());
                tokenResponse = authClient.generateWithValidityPeriod(tokenRequest.getClientId(),
                        tokenRequest.getClientSecret(), GRANT_TYPE_VALUE, scopes, definedValidityPeriod);
            } else {
                tokenResponse = authClient.generate(tokenRequest.getClientId(),
                        tokenRequest.getClientSecret(), GRANT_TYPE_VALUE, scopes);
            }
        } catch (KeyManagerClientException e) {
            throw new APIManagementException("Error occurred while calling token endpoint!", e);
        }

        tokenInfo = new AccessTokenInfo();
        if (StringUtils.isNotEmpty(tokenResponse.getScope())) {
            tokenInfo.setScope(tokenResponse.getScope().split(" "));
        } else {
            tokenInfo.setScope(new String[0]);
        }
        tokenInfo.setAccessToken(tokenResponse.getToken());
        tokenInfo.setValidityPeriod(tokenResponse.getExpiry());

        return tokenInfo;
    }

    @Override
    public String getNewApplicationConsumerSecret(AccessTokenRequest tokenRequest) throws APIManagementException {

        throw new APIManagementException("Regenerating consumer secret is not supported at the moment.");
    }

    @Override
    public AccessTokenInfo getTokenMetaData(String accessToken) throws APIManagementException {

        AccessTokenInfo tokenInfo = new AccessTokenInfo();

        String clientAccessToken = getAccessTokenForKeyMgt();
        String authorizationHeader = getBearerAuthorizationHeader(clientAccessToken);

        try {
            IntrospectInfo introspectInfo = introspectionClient.introspect(authorizationHeader, accessToken);
            tokenInfo.setAccessToken(accessToken);
            boolean isActive = introspectInfo.isActive();
            if (!isActive) {
                tokenInfo.setTokenValid(false);
                tokenInfo.setErrorcode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                return tokenInfo;
            }
            tokenInfo.setTokenValid(true);
            tokenInfo.setValidityPeriod(introspectInfo.getExpiry() * 1000L);
            if (StringUtils.isNotEmpty(introspectInfo.getScope())) {
                String[] scopes = introspectInfo.getScope().split(" ");
                tokenInfo.setScope(scopes);
            }
            tokenInfo.setConsumerKey(introspectInfo.getClientId());
            tokenInfo.setIssuedTime(System.currentTimeMillis());
            String username = introspectInfo.getUsername();
            if (!StringUtils.isEmpty(username)) {
                tokenInfo.setEndUserName(username);
            }

            return tokenInfo;
        } catch (KeyManagerClientException e) {
            throw new APIManagementException("Error occurred in token introspection!", e);
        }
    }

    @Override
    public KeyManagerConfiguration getKeyManagerConfiguration() throws APIManagementException {

        return configuration;
    }

    /**
     * This method will create a new record at CLIENT_INFO table by given OauthAppRequest.
     *
     * @param appInfoRequest oAuth application properties will contain in this object
     * @return OAuthApplicationInfo with created oAuth application details.
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    @Override
    public OAuthApplicationInfo mapOAuthApplication(OAuthAppRequest appInfoRequest)
            throws APIManagementException {

        //initiate OAuthApplicationInfo
        OAuthApplicationInfo oAuthApplicationInfo = appInfoRequest.getOAuthApplicationInfo();

        String consumerKey = oAuthApplicationInfo.getClientId();
        String tokenScope = (String) oAuthApplicationInfo.getParameter("tokenScope");
        String[] tokenScopes = new String[1];
        tokenScopes[0] = tokenScope;
        String clientSecret = (String) oAuthApplicationInfo.getParameter("client_secret");
        //for the first time we set default time period.
        oAuthApplicationInfo.addParameter(ApplicationConstants.VALIDITY_PERIOD,
                getConfigurationParamValue(APIConstants.IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD));

        String userId = (String) oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_USERNAME);
        String tenantDomain;
        if (StringUtils.isNotEmpty(userId)) {
            tenantDomain = MultitenantUtils.getTenantDomain(userId);
        } else {
            tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        }
        String accessToken = getAccessTokenForKeyMgt();
        String authorizationHeader = getBearerAuthorizationHeader(accessToken);

        //check whether given consumer key and secret match or not. If it does not match throw an exception.
        ClientInfo clientInfo;
        try {
            if (APIConstants.SUPER_TENANT_DOMAIN.equals(tenantDomain)) {
                clientInfo = dcrClient.getApplication(authorizationHeader, consumerKey);
            } else {
                clientInfo = dcrClient.getApplicationForTenant(tenantDomain, authorizationHeader, consumerKey);
            }
            buildDTOFromClientInfo(clientInfo, oAuthApplicationInfo);
        } catch (KeyManagerClientException e) {
            handleException("Some thing went wrong while getting OAuth application for given consumer key " +
                    oAuthApplicationInfo.getClientId(), e);
        }

        if (!clientSecret.equals(oAuthApplicationInfo.getClientSecret())) {
            throw new APIManagementException("The secret key is wrong for the given consumer key " + consumerKey);
        }
        oAuthApplicationInfo.addParameter("tokenScope", tokenScopes);
        oAuthApplicationInfo.setIsSaasApplication(false);

        if (log.isDebugEnabled()) {
            log.debug("Creating semi-manual application for consumer id  :  " + oAuthApplicationInfo.getClientId());
        }

        return oAuthApplicationInfo;
    }

    /**
     * Builds an OAuthApplicationInfo object using the ClientInfo response
     *
     * @param appResponse ClientInfo response object
     * @param oAuthApplicationInfo original OAuthApplicationInfo object
     * @return OAuthApplicationInfo object with response information added
     */
    private OAuthApplicationInfo buildDTOFromClientInfo(ClientInfo appResponse,
                                                        OAuthApplicationInfo oAuthApplicationInfo) {

        oAuthApplicationInfo.setClientName(appResponse.getClientName());
        oAuthApplicationInfo.setClientId(appResponse.getClientId());
        if (appResponse.getRedirectUris() != null) {
            oAuthApplicationInfo.setCallBackURL(String.join(",", appResponse.getRedirectUris()));
            oAuthApplicationInfo.addParameter(ApplicationConstants.OAUTH_REDIRECT_URIS,
                    String.join(",", appResponse.getRedirectUris()));
        }
        oAuthApplicationInfo.setClientSecret(appResponse.getClientSecret());
        if (appResponse.getGrantTypes() != null) {
            oAuthApplicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_GRANT,
                    String.join(" ", appResponse.getGrantTypes()));
        } else if (oAuthApplicationInfo.getParameter(ApplicationConstants.OAUTH_CLIENT_GRANT) instanceof String) {
            oAuthApplicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_GRANT, ((String) oAuthApplicationInfo.
                    getParameter(ApplicationConstants.OAUTH_CLIENT_GRANT)).replace(",", " "));
        }
        oAuthApplicationInfo.addParameter(ApplicationConstants.OAUTH_CLIENT_NAME, appResponse.getClientName());
        return oAuthApplicationInfo;
    }

    /**
     * This method initialize the HTTP Client and Connection Manager to call services in KeyManager.
     *
     * @throws APIManagementException if an error occurs while initializing HttpClient
     */
    protected void initializeHttpClient() throws APIManagementException {

        try {
            String authServerURL = (String) configuration.getParameter(APIConstants.AUTHSERVER_URL);
            java.net.URL keyManagerURL = new java.net.URL(authServerURL);
            int keyManagerPort = keyManagerURL.getPort();
            String keyManagerProtocol = keyManagerURL.getProtocol();
            this.kmHttpClient = (CloseableHttpClient) APIUtil.getHttpClient(keyManagerPort, keyManagerProtocol);
        } catch (MalformedURLException e) {
            throw new APIManagementException("Error while initializing HttpClient due to malformed URL", e);
        }
    }

    @Override
    public void loadConfiguration(KeyManagerConfiguration configuration) throws APIManagementException {
        this.configuration = configuration;
        //Initialize a Http Client and Connection Manager using the ServerURL of KM
        initializeHttpClient();

        String consumerKey = (String) configuration.getParameter(APIConstants.KEY_MANAGER_CONSUMER_KEY);
        String consumerSecret = (String) configuration.getParameter(APIConstants.KEY_MANAGER_CONSUMER_SECRET);
        String keyManagerServiceUrl = (String) configuration.getParameter(APIConstants.AUTHSERVER_URL);
        String tokenEndpoint = keyManagerServiceUrl.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0].concat(
                "/oauth2/token");
        String revokeEndpoint = keyManagerServiceUrl.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0].concat(
                "/oauth2/revoke");
        accessTokenGenerator = new AccessTokenGenerator(tokenEndpoint,revokeEndpoint,consumerKey,consumerSecret);

        try {
            java.net.URL keyManagerServicesURL = new java.net.URL(keyManagerServiceUrl);
            int keyManagerPort = keyManagerServicesURL.getPort();
            String keyManagerProtocol = keyManagerServicesURL.getProtocol();
            String keyManagerHost = keyManagerServicesURL.getHost();
            String keyManagerURL = keyManagerProtocol.concat("://").concat(keyManagerHost);
            if (keyManagerPort != 0) {
                keyManagerURL = keyManagerURL.concat(":").concat(Integer.toString(keyManagerPort));
            }
            String authEndpoint = tokenEndpoint.replace("/token", "");
            revokeEndpoint = revokeEndpoint.replace("/revoke", "");

            dcrClient = Feign.builder()
                    .client(new OkHttpClient())
                    .encoder(new GsonEncoder())
                    .decoder(new GsonDecoder())
                    .logger(new Slf4jLogger())
                    .errorDecoder(new KMClientErrorDecoder())
                    .target(DCRClient.class, keyManagerURL);
            authClient = Feign.builder()
                    .client(new OkHttpClient())
                    .encoder(new GsonEncoder())
                    .decoder(new GsonDecoder())
                    .logger(new Slf4jLogger())
                    .errorDecoder(new KMClientErrorDecoder())
                    .encoder(new FormEncoder())
                    .target(AuthClient.class, authEndpoint);
            introspectionClient = Feign.builder()
                    .client(new OkHttpClient())
                    .encoder(new GsonEncoder())
                    .decoder(new GsonDecoder())
                    .logger(new Slf4jLogger())
                    .errorDecoder(new KMClientErrorDecoder())
                    .encoder(new FormEncoder())
                    .target(IntrospectionClient.class, keyManagerURL.concat("/oauth2/introspect"));

        } catch (MalformedURLException e) {
            log.error("Error in parsing the Key Validator URL provided: " + keyManagerServiceUrl, e);
        }
    }

    @Override
    public boolean registerNewResource(API api, Map resourceAttributes) throws APIManagementException {
//        //Register new resource means create new API with given Scopes.
        //todo commented below code because of blocker due to API publish fail. need to find a better way of doing this
//        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
//        apiMgtDAO.addAPI(api, CarbonContext.getThreadLocalCarbonContext().getTenantId());

        return true;
    }

    @Override
    public Map getResourceByApiId(String apiId) throws APIManagementException {

        return null;
    }

    @Override
    public boolean updateRegisteredResource(API api, Map resourceAttributes) throws APIManagementException {

        return false;
    }

    @Override
    public void deleteRegisteredResourceByAPIId(String apiID) throws APIManagementException {

    }

    @Override
    public void deleteMappedApplication(String consumerKey) throws APIManagementException {

    }

    @Override
    public Set<String> getActiveTokensByConsumerKey(String consumerKey) throws APIManagementException {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        return apiMgtDAO.getActiveTokensOfConsumerKey(consumerKey);
    }

    /**
     * Returns the access token information of the provided consumer key.
     *
     * @param consumerKey The consumer key.
     * @return AccessTokenInfo The access token information.
     * @throws APIManagementException
     */
    @Override
    public AccessTokenInfo getAccessTokenByConsumerKey(String consumerKey) throws APIManagementException {

        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

        APIKey apiKey;
        try {
            apiKey = apiMgtDAO.getAccessTokenInfoByConsumerKey(consumerKey);
            if (apiKey != null) {
                tokenInfo.setAccessToken(apiKey.getAccessToken());
                tokenInfo.setConsumerSecret(apiKey.getConsumerSecret());
                tokenInfo.setValidityPeriod(apiKey.getValidityPeriod());
                tokenInfo.setScope(apiKey.getTokenScope().split("\\s"));
            } else {
                tokenInfo.setAccessToken("");
                //set default validity period
                tokenInfo.setValidityPeriod(3600);
            }
            tokenInfo.setConsumerKey(consumerKey);

        } catch (SQLException e) {
            handleException("Cannot retrieve information for the given consumer key : "
                    + consumerKey, e);
        } catch (CryptoException e) {
            handleException("Token decryption failed of an access token for the given consumer key : "
                    + consumerKey, e);
        }
        return tokenInfo;
    }

    @Override
    public Map<String, Set<Scope>> getScopesForAPIS(String apiIdsString)
            throws APIManagementException {

        Map<String, Set<Scope>> apiToScopeMapping = new HashMap<>();
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        Map<String, Set<String>> apiToScopeKeyMapping = apiMgtDAO.getScopesForAPIS(apiIdsString);
        for (String apiId : apiToScopeKeyMapping.keySet()) {
            Set<Scope> apiScopes = new LinkedHashSet<>();
            Set<String> scopeKeys = apiToScopeKeyMapping.get(apiId);
            for (String scopeKey : scopeKeys) {
                Scope scope = getScopeByName(scopeKey);
                apiScopes.add(scope);
            }
            apiToScopeMapping.put(apiId, apiScopes);
        }
        return apiToScopeMapping;
    }

    /**
     * Get scope management service tenant URL for given KM endpoint.
     *
     * @return Scope Management Service host URL (Eg:https://localhost:9444/api/identity/oauth2/v1.0/scopes)
     * @throws APIManagementException If a malformed km endpoint is provided
     */
    private String getScopeManagementServiceEndpoint() throws APIManagementException {

        String authServerURL = (String) configuration.getParameter(APIConstants.AUTHSERVER_URL);
        if (StringUtils.isEmpty(authServerURL)) {
            throw new APIManagementException("API Key Validator Server URL cannot be empty or null");
        }
        String scopeMgtTenantEndpoint = authServerURL.split("/" + APIConstants.SERVICES_URL_RELATIVE_PATH)[0];
        if (StringUtils.isNoneEmpty(tenantDomain) && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            scopeMgtTenantEndpoint += "/t/" + tenantDomain;
        }
        scopeMgtTenantEndpoint += APIConstants.KEY_MANAGER_OAUTH2_SCOPES_REST_API_BASE_PATH;
        return scopeMgtTenantEndpoint;
    }

    /**
     * This method will be used to register a Scope in the authorization server.
     *
     * @param scope        Scope to register
     * @throws APIManagementException if there is an error while registering a new scope.
     */
    @Override
    public void registerScope(Scope scope) throws APIManagementException {

        String accessToken = getAccessTokenForKeyMgt();
        String scopeEndpoint = getScopeManagementServiceEndpoint();
        String scopeKey = scope.getKey();
        try {
            HttpPost httpPost = new HttpPost(scopeEndpoint);
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, getBearerAuthorizationHeader(accessToken));
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            ScopeDTO scopeDTO = new ScopeDTO();
            scopeDTO.setName(scopeKey);
            scopeDTO.setDisplayName(scope.getName());
            scopeDTO.setDescription(scope.getDescription());
            if (scope.getRoles() != null) {
                scopeDTO.setBindings(Arrays.asList(scope.getRoles().split(",")));
            }
            StringEntity payload = new StringEntity(new Gson().toJson(scopeDTO));
            httpPost.setEntity(payload);
            if (log.isDebugEnabled()) {
                log.debug("Invoking Scope Management REST API of KM: " + scopeEndpoint + " to register scope "
                        + scopeKey);
            }
            try (CloseableHttpResponse httpResponse = kmHttpClient.execute(httpPost)) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_CREATED) {
                    String responseString = readHttpResponseAsString(httpResponse);
                    throw new APIManagementException("Error occurred while registering scope: " + scopeKey + " via "
                            + scopeEndpoint + ". Error Status: " + statusCode + " . Error Response: " + responseString);
                }
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while registering scope: " + scopeKey + " via " + scopeEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
    }

    /**
     * Read response body for HTTPResponse as a string.
     *
     * @param httpResponse HTTPResponse
     * @return Response Body String
     * @throws APIManagementException If an error occurs while reading the response
     */
    protected String readHttpResponseAsString(CloseableHttpResponse httpResponse) throws APIManagementException {

        try {
            HttpEntity entity = httpResponse.getEntity();
            String responseString = entity != null ? EntityUtils.toString(entity, StandardCharsets.UTF_8) : null;
            //release all resources held by the responseHttpEntity
            EntityUtils.consume(entity);
            return responseString;
        } catch (IOException e) {
            String errorMessage = "Error occurred while reading response body as string";
            throw new APIManagementException(errorMessage, e);
        }
    }


    /**
     * Get access token with key management scopes for the tenant using the KM Mgt OAuth Application.
     *
     * @return Access Token
     */
    protected String getAccessTokenForKeyMgt() throws APIManagementException {

        return accessTokenGenerator.getAccessToken(APIConstants.KEY_MANAGER_OAUTH2_REST_API_MGT_SCOPES);
    }

    /**
     * Construct Bearer Authorization header for AccessTokenInfo.
     *
     * @param accessToken Access Token
     * @return Bearer Authorization header
     */
    private String getBearerAuthorizationHeader(String accessToken) {

        return APIConstants.AUTHORIZATION_BEARER + accessToken;
    }

    /**
     * This method will be used to retrieve details of a Scope in the authorization server.
     *
     * @param name    Scope Name to retrieve
     * @return Scope object
     * @throws APIManagementException if an error while retrieving scope
     */
    @Override
    public Scope getScopeByName(String name) throws APIManagementException {

        ScopeDTO scopeDTO;
        String accessToken = getAccessTokenForKeyMgt();
        String scopeEndpoint = getScopeManagementServiceEndpoint()
                + (APIConstants.KEY_MANAGER_OAUTH2_SCOPES_REST_API_SCOPE_NAME
                .replace(APIConstants.KEY_MANAGER_OAUTH2_SCOPES_SCOPE_NAME_PARAM, name));
        HttpGet httpGet = new HttpGet(scopeEndpoint);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, getBearerAuthorizationHeader(accessToken));
        if (log.isDebugEnabled()) {
            log.debug("Invoking Scope Management REST API of KM: " + scopeEndpoint + " to get scope "
                    + name);
        }
        try (CloseableHttpResponse httpResponse = kmHttpClient.execute(httpGet)) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            String responseString = readHttpResponseAsString(httpResponse);
            if (statusCode == HttpStatus.SC_OK && StringUtils.isNoneEmpty(responseString)) {
                scopeDTO = new Gson().fromJson(responseString, ScopeDTO.class);
            } else {
                throw new APIManagementException("Error occurred while retrieving scope: " + name + " via "
                        + scopeEndpoint + ". Error Status: " + statusCode + ". Error Response: " + responseString);
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while retrieving scope: " + name + " via " + scopeEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
        return fromDTOToScope(scopeDTO);
    }

    /**
     * Get Scope object from ScopeDTO response received from authorization server.
     *
     * @param scopeDTO ScopeDTO response
     * @return Scope model object
     */
    private Scope fromDTOToScope(ScopeDTO scopeDTO) {

        Scope scope = new Scope();
        scope.setName(scopeDTO.getDisplayName());
        scope.setKey(scopeDTO.getName());
        scope.setDescription(scopeDTO.getDescription());
        scope.setRoles(String.join(",", scopeDTO.getBindings()));
        return scope;
    }

    /**
     * Get Scope object list from ScopeDTO List response received from authorization server.
     *
     * @param scopeDTOList Scope DTO List
     * @return Scope Object to Scope Name Mappings
     */
    private Map<String, Scope> fromDTOListToScopeListMapping(List<ScopeDTO> scopeDTOList) {

        Map<String, Scope> scopeListMapping = new HashMap<>();
        for (ScopeDTO scopeDTO : scopeDTOList) {
            scopeListMapping.put(scopeDTO.getName(), fromDTOToScope(scopeDTO));
        }
        return scopeListMapping;
    }

    /**
     * This method will be used to retrieve all the scopes available in the authorization server for the given tenant
     * domain.
     *
     * @return Mapping of Scope object to scope key
     * @throws APIManagementException if an error occurs while getting scopes list
     */
    @Override
    public Map<String, Scope> getAllScopes() throws APIManagementException {

        List<ScopeDTO> allScopeDTOS;
        // Get access token
        String accessToken = getAccessTokenForKeyMgt();
        String scopeEndpoint = getScopeManagementServiceEndpoint();
        HttpGet httpGet = new HttpGet(scopeEndpoint);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, getBearerAuthorizationHeader(accessToken));
        if (log.isDebugEnabled()) {
            log.debug("Invoking Scope Management REST API of KM: " + scopeEndpoint + " to get scopes");
        }
        try (CloseableHttpResponse httpResponse = kmHttpClient.execute(httpGet)) {
            String responseString = readHttpResponseAsString(httpResponse);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK && StringUtils.isNoneEmpty(responseString)) {
                Type scopeListType =
                        new TypeToken<ArrayList<ScopeDTO>>() {
                        }.getType();
                allScopeDTOS = new Gson().fromJson(responseString, scopeListType);
            } else {
                throw new APIManagementException("Error occurred while retrieving scopes via: " + scopeEndpoint
                        + ". Error Status: " + statusCode + " . Error Response: " + responseString);
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while getting retrieving via " + scopeEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
        return fromDTOListToScopeListMapping(allScopeDTOS);
    }

    /**
     * This method will be used to attach a Scope in the authorization server to a API resource.
     *
     * @param api          API
     * @param uriTemplates URITemplate set with attached scopes
     * @throws APIManagementException if an error occurs while attaching scope to resource
     */
    @Override
    public void attachResourceScopes(API api, Set<URITemplate> uriTemplates)
            throws APIManagementException {

        //TODO: remove after scope validation from swagger completes
        ApiMgtDAO.getInstance().addResourceScopes(api, uriTemplates, tenantDomain);
    }

    /**
     * This method will be used to update the local scopes and resource to scope attachments of an API in the
     * authorization server.
     *
     * @param api               API
     * @param oldLocalScopeKeys Old local scopes of the API before update (excluding the versioned local scopes
     * @param newLocalScopes    New local scopes of the API after update
     * @param oldURITemplates   Old URI templates of the API before update
     * @param newURITemplates   New URI templates of the API after update
     * @throws APIManagementException if fails to update resources scopes
     */
    @Override
    public void updateResourceScopes(API api, Set<String> oldLocalScopeKeys, Set<Scope> newLocalScopes,
                                     Set<URITemplate> oldURITemplates, Set<URITemplate> newURITemplates)
            throws APIManagementException {

        detachResourceScopes(api, oldURITemplates);
        // remove the old local scopes from the KM
        for (String oldScope : oldLocalScopeKeys) {
            deleteScope(oldScope);
        }
        //Register scopes
        for (Scope scope : newLocalScopes) {
            String scopeKey = scope.getKey();
            // Check if key already registered in KM. Scope Key may be already registered for a different version.
            if (!isScopeExists(scopeKey)) {
                //register scope in KM
                registerScope(scope);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Scope: " + scopeKey + " already registered in KM. Skipping registering scope.");
                }
            }
        }
        attachResourceScopes(api, newURITemplates);
    }

    /**
     * This method will be used to detach the resource scopes of an API and delete the local scopes of that API from
     * the authorization server.
     *
     * @param api          API   API
     * @param uriTemplates URITemplate Set with attach scopes to detach
     * @throws APIManagementException if an error occurs while detaching resource scopes of the API.
     */
    @Override
    public void detachResourceScopes(API api, Set<URITemplate> uriTemplates)
            throws APIManagementException {

        //TODO: remove after scope validation from swagger completes
        ApiMgtDAO.getInstance().removeResourceScopes(api.getId(), api.getContext(), uriTemplates, tenantDomain);
    }

    /**
     * This method will be used to delete a Scope in the authorization server.
     *
     * @param scopeName    Scope name
     * @throws APIManagementException if an error occurs while deleting the scope
     */
    @Override
    public void deleteScope(String scopeName) throws APIManagementException {

        // Get access token
        String accessToken = getAccessTokenForKeyMgt();
        String scopeEndpoint = getScopeManagementServiceEndpoint()
                + (APIConstants.KEY_MANAGER_OAUTH2_SCOPES_REST_API_SCOPE_NAME
                .replace(APIConstants.KEY_MANAGER_OAUTH2_SCOPES_SCOPE_NAME_PARAM, scopeName));

        HttpDelete httpDelete = new HttpDelete(scopeEndpoint);
        httpDelete.setHeader(HttpHeaders.AUTHORIZATION, getBearerAuthorizationHeader(accessToken));
        if (log.isDebugEnabled()) {
            log.debug("Invoking Scope Management REST API of KM: " + scopeEndpoint + " to delete scope "
                    + scopeName);
        }
        try (CloseableHttpResponse httpResponse = kmHttpClient.execute(httpDelete)) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                String responseString = readHttpResponseAsString(httpResponse);
                String errorMessage = "Error occurred while deleting scope: " + scopeName + " via: " + scopeEndpoint
                        + ". Error Status: " + statusCode + " . Error Response: " + responseString;
                throw new APIManagementException(errorMessage);
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while deleting scope: " + scopeName + " via " + scopeEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
    }

    /**
     * This method will be used to update a Scope in the authorization server.
     *
     * @param scope        Scope object
     * @throws APIManagementException if an error occurs while updating the scope
     */
    @Override
    public void updateScope(Scope scope) throws APIManagementException {

        // Get access token
        String accessToken = getAccessTokenForKeyMgt();
        String scopeKey = scope.getKey();
        String scopeEndpoint = getScopeManagementServiceEndpoint()
                + (APIConstants.KEY_MANAGER_OAUTH2_SCOPES_REST_API_SCOPE_NAME
                .replace(APIConstants.KEY_MANAGER_OAUTH2_SCOPES_SCOPE_NAME_PARAM, scopeKey));
        try {
            HttpPut httpPut = new HttpPut(scopeEndpoint);
            httpPut.setHeader(HttpHeaders.AUTHORIZATION, getBearerAuthorizationHeader(accessToken));
            httpPut.setHeader(HttpHeaders.CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            ScopeDTO scopeDTO = new ScopeDTO();
            scopeDTO.setDisplayName(scope.getName());
            scopeDTO.setDescription(scope.getDescription());
            if (scope.getRoles() != null) {
                scopeDTO.setBindings(Arrays.asList(scope.getRoles().split(",")));
            }
            StringEntity payload = new StringEntity(new Gson().toJson(scopeDTO));
            httpPut.setEntity(payload);
            if (log.isDebugEnabled()) {
                log.debug("Invoking Scope Management REST API of KM: " + scopeEndpoint + " to update scope "
                        + scopeKey);
            }
            try (CloseableHttpResponse httpResponse = kmHttpClient.execute(httpPut)) {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    String responseString = readHttpResponseAsString(httpResponse);
                    throw new APIManagementException("Error occurred while updating scope: " + scopeKey + " via: "
                            + scopeEndpoint + ". Error Status: " + statusCode + " . Error Response: " + responseString);
                }
            }
        } catch (IOException e) {
            String errorMessage = "Error occurred while updating scope: " + scopeKey + " via " + scopeEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
    }

    /**
     * This method will be used to check whether the a Scope exists for the given scope name in the authorization
     * server.
     *
     * @param scopeName    Scope Name
     * @return whether scope exists or not
     * @throws APIManagementException if an error occurs while checking the existence of the scope
     */
    @Override
    public boolean isScopeExists(String scopeName) throws APIManagementException {

        // Get access token
        String accessToken = getAccessTokenForKeyMgt();
        String scopeEndpoint = getScopeManagementServiceEndpoint()
                + (APIConstants.KEY_MANAGER_OAUTH2_SCOPES_REST_API_SCOPE_NAME
                .replace(APIConstants.KEY_MANAGER_OAUTH2_SCOPES_SCOPE_NAME_PARAM, scopeName));

        HttpHead httpHead = new HttpHead(scopeEndpoint);
        httpHead.setHeader(HttpHeaders.AUTHORIZATION, getBearerAuthorizationHeader(accessToken));
        if (log.isDebugEnabled()) {
            log.debug("Invoking Scope Management REST API of KM: " + scopeEndpoint + " to check scope existence of "
                    + scopeName);
        }
        try (CloseableHttpResponse httpResponse = kmHttpClient.execute(httpHead)) {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                if (statusCode != HttpStatus.SC_NOT_FOUND) {
                    String responseString = readHttpResponseAsString(httpResponse);
                    String errorMessage = "Error occurred while checking existence of scope: " + scopeName + " via: "
                            + scopeEndpoint + ". Error Status: " + statusCode + " . Error Response: " + responseString;
                    throw new APIManagementException(errorMessage);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Scope " + scopeName + " not found in authorization server " + scopeEndpoint);
                    }
                    return false;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Scope " + scopeName + " found in authorization server " + scopeEndpoint);
            }
            return true;
        } catch (IOException e) {
            String errorMessage = "Error occurred while checking existence of scope: " + scopeName + " via "
                    + scopeEndpoint;
            throw new APIManagementException(errorMessage, e);
        }
    }

    /**
     * This method will be used to validate the scope set provided and populate the additional parameters
     * (description and bindings) for each Scope object.
     *
     * @param scopes Scope set to validate
     * @throws APIManagementException if an error occurs while validating and populating
     */
    @Override
    public void validateScopes(Set<Scope> scopes) throws APIManagementException {

        for (Scope scope : scopes) {
            Scope sharedScope = getScopeByName(scope.getKey());
            scope.setName(sharedScope.getName());
            scope.setDescription(sharedScope.getDescription());
            scope.setRoles(sharedScope.getRoles());
        }
    }

    @Override
    public String getType() {

        return APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE;
    }

    /**
     * Return the value of the provided configuration parameter.
     *
     * @param parameter Parameter name
     * @return Parameter value
     */
    protected String getConfigurationParamValue(String parameter) {

        return (String) configuration.getParameter(parameter);
    }

    /**
     * Check whether Token partitioning is enabled.
     *
     * @return true/false
     */
    protected boolean checkAccessTokenPartitioningEnabled() {

        return APIUtil.checkAccessTokenPartitioningEnabled();
    }

    /**
     * Check whether user name assertion is enabled.
     *
     * @return true/false
     */
    protected boolean checkUserNameAssertionEnabled() {

        return APIUtil.checkUserNameAssertionEnabled();
    }

}
