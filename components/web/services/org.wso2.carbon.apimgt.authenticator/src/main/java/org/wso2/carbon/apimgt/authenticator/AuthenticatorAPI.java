/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.authenticator;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.authenticator.configuration.models.APIMStoreConfigurations;
import org.wso2.carbon.apimgt.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.authenticator.dto.ErrorDTO;
import org.wso2.carbon.apimgt.authenticator.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.authenticator.utils.AuthUtil;
import org.wso2.carbon.apimgt.authenticator.utils.bean.AuthResponseBean;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.impl.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.rest.api.common.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FormDataParam;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * This class provides access token during login from store app.
 *
 */
@Path("/login")
public class AuthenticatorAPI implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatorAPI.class);

    /**
     * This method authenticate the user for store app.
     *
     */
    @POST
    @Path ("/token")
    @Produces (MediaType.APPLICATION_JSON)
    @Consumes ({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA })
    public Response authenticate(@Context Request request, @FormDataParam ("username") String userName,
            @FormDataParam ("password") String password, @FormDataParam ("grant_type") String grantType,
            @FormDataParam ("validity_period") String validityPeriod,
            @FormDataParam ("remember_me") boolean isRememberMe, @FormDataParam ("scopes") String scopesList) {
        try {
            LoginTokenService loginTokenService = new LoginTokenService();
            AuthResponseBean authResponseBean = new AuthResponseBean();
            String appContext = AuthUtil.getAppContext(request);
            String restAPIContext;
            if (appContext.contains("editor")) {
                restAPIContext = AuthenticatorConstants.REST_CONTEXT + "/publisher";
            } else {
                restAPIContext = AuthenticatorConstants.REST_CONTEXT + appContext;
            }
            String refToken = null;
            if (AuthenticatorConstants.REFRESH_GRANT.equals(grantType)) {
                refToken = AuthUtil
                        .extractTokenFromHeaders(request.getHeaders(), AuthenticatorConstants.REFRESH_TOKEN_2);
                if (refToken == null) {
                    ErrorDTO errorDTO = new ErrorDTO();
                    errorDTO.setCode(ExceptionCodes.INVALID_AUTHORIZATION_HEADER.getErrorCode());
                    errorDTO.setMessage(ExceptionCodes.INVALID_AUTHORIZATION_HEADER.getErrorMessage());
                    return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
                }
            }
            String tokens = loginTokenService
                    .getTokens(authResponseBean, appContext.substring(1), userName, password, grantType, refToken,
                            Long.parseLong(validityPeriod));
            String accessToken = tokens.split(":")[0];
            String refreshToken = null;
            if (tokens.split(":").length > 1) {
                refreshToken = tokens.split(":")[1];
            }

            // The access token is stored as two cookies in client side. One is a normal cookie and other is a http
            // only cookie. Hence we need to split the access token
            String part1 = accessToken.substring(0, accessToken.length() / 2);
            String part2 = accessToken.substring(accessToken.length() / 2);
            NewCookie cookieWithAppContext = AuthUtil
                    .cookieBuilder(AuthenticatorConstants.ACCESS_TOKEN_1, part1, appContext, true, false, "");
            authResponseBean.setPartialToken(part1);
            NewCookie httpOnlyCookieWithAppContext = AuthUtil
                    .cookieBuilder(AuthenticatorConstants.ACCESS_TOKEN_2, part2, appContext, true, true, "");
            NewCookie restAPIContextCookie = AuthUtil
                    .cookieBuilder(APIConstants.AccessTokenConstants.AM_TOKEN_MSF4J, part2, restAPIContext, true, true,
                            "");
            NewCookie refreshTokenCookie, refreshTokenHttpOnlyCookie;
            // refresh token is not set to cookie if remember me is not set.
            if (refreshToken != null && (AuthenticatorConstants.REFRESH_GRANT.equals(grantType) || (
                    AuthenticatorConstants.PASSWORD_GRANT.equals(grantType) && isRememberMe))) {
                String refTokenPart1 = refreshToken.substring(0, refreshToken.length() / 2);
                String refTokenPart2 = refreshToken.substring(refreshToken.length() / 2);
                refreshTokenCookie = AuthUtil
                        .cookieBuilder(AuthenticatorConstants.REFRESH_TOKEN_1, refTokenPart1, appContext, true, false,
                                "");
                refreshTokenHttpOnlyCookie = AuthUtil
                        .cookieBuilder(AuthenticatorConstants.REFRESH_TOKEN_2, refTokenPart2, appContext, true, true,
                                "");
                return Response.ok(authResponseBean, MediaType.APPLICATION_JSON)
                        .cookie(cookieWithAppContext, httpOnlyCookieWithAppContext, restAPIContextCookie,
                                refreshTokenCookie, refreshTokenHttpOnlyCookie).header(AuthenticatorConstants.
                                        REFERER_HEADER,
                                (request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null && request
                                        .getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER)
                                        .equals(request.getHeader(AuthenticatorConstants.REFERER_HEADER))) ?
                                        "" :
                                        request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null ?
                                                request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) :
                                                "").build();
            } else {
                return Response.ok(authResponseBean, MediaType.APPLICATION_JSON)
                        .cookie(cookieWithAppContext, httpOnlyCookieWithAppContext, restAPIContextCookie)
                        .header(AuthenticatorConstants.
                                        REFERER_HEADER,
                                (request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null && request
                                        .getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER)
                                        .equals(request.getHeader(AuthenticatorConstants.REFERER_HEADER))) ?
                                        "" :
                                        request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null ?
                                                request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) :
                                                "").build();
            }
        } catch (APIManagementException e) {

            ErrorDTO errorDTO = AuthUtil.getErrorDTO(e.getErrorHandler(), null);

            log.error(e.getMessage(), e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * This method provides the DCR application information to the SSO-IS login.
     *
     * @param request Request to call the /login api
     * @return Response - Response object with OAuth data
     */
    @GET
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response redirect(@Context Request request) {
        String appContext = AuthUtil.getAppContext(request);
        String appName = appContext.substring(1);
        List<String> grantTypes = new ArrayList<>();
        grantTypes.add(KeyManagerConstants.PASSWORD_GRANT_TYPE);
        grantTypes.add(KeyManagerConstants.AUTHORIZATION_CODE_GRANT_TYPE);
        grantTypes.add(KeyManagerConstants.REFRESH_GRANT_TYPE);
        OAuthApplicationInfo oAuthApplicationInfo;
        try {
            String storeRestAPI = RestApiUtil.getStoreRestAPIResource();
            APIDefinition apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
            Map<String, Scope> storeScopesMap = apiDefinitionFromSwagger20.getScopes(storeRestAPI);
            String scopes = String.join(" ", storeScopesMap.keySet());
            scopes = scopes + " " + KeyManagerConstants.OPEN_ID_CONNECT_SCOPE;
            oAuthApplicationInfo = createDCRApplication(appName, grantTypes);
            if (oAuthApplicationInfo != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Created DCR Application successfully for " + appName + ".");
                }
                String oAuthApplicationClientId = oAuthApplicationInfo.getClientId();
                String oAuthApplicationCallBackURL = oAuthApplicationInfo.getCallBackURL();

                JsonObject oAuthData = new JsonObject();
                oAuthData.addProperty(KeyManagerConstants.OAUTH_CLIENT_ID, oAuthApplicationClientId);
                oAuthData.addProperty(KeyManagerConstants.OAUTH_CALLBACK_URIS, oAuthApplicationCallBackURL);
                oAuthData.addProperty(KeyManagerConstants.TOKEN_SCOPES , scopes);
                APIMStoreConfigurations storeConfigs = ServiceReferenceHolder.getInstance().getAPIMStoreConfiguration();
                oAuthData.addProperty("isSSOEnabled" , storeConfigs.isSsoEnabled());
                return Response.status(Response.Status.OK).entity(oAuthData).build();
            } else {
                log.error("No information available in OAuth application.");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error while creating the OAuth application!").build();
            }
        } catch (APIManagementException e) {
            ErrorDTO errorDTO = AuthUtil.getErrorDTO(e.getErrorHandler(), null);
            log.error(e.getMessage(), e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * This is the API which IDP redirects the user after authentication.
     *
     * @param request Request to call /callback api
     * @return Response - Response with redirect URL
     */
    @GET
    @Path("/callback")
    @Produces(MediaType.APPLICATION_JSON)
    public Response callback(@Context Request request) {
        String appContext = AuthUtil.getAppContext(request);
        String appName = appContext.substring(1);
        String restAPIContext;
        APIMStoreConfigurations storeConfigs = ServiceReferenceHolder.getInstance().getAPIMStoreConfiguration();
        if (appContext.contains("editor") || request.getUri().contains("publisher")) {
            restAPIContext = AuthenticatorConstants.REST_CONTEXT + "/publisher";
        } else {
            restAPIContext = AuthenticatorConstants.REST_CONTEXT + appContext;
        }
        String grantType = KeyManagerConstants.AUTHORIZATION_CODE_GRANT_TYPE;
        String callBackURL = storeConfigs.getApimBaseUrl() + appName + "/auth/apis/login/callback";
        Long validityPeriod = 1000L;
        // Get the Authorization Code
        String requestURL = (String) request.getProperty("REQUEST_URL");
        String authorizationCode = requestURL.split("=")[1].split("&")[0];
        if (log.isDebugEnabled()) {
            log.debug("Authorization Code for the app " + appName + ": " + authorizationCode);
        }
            try {
                if (authorizationCode != null) {
                    // Set scopes
                    String storeRestAPI = RestApiUtil.getStoreRestAPIResource();
                    APIDefinition apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
                    Map<String, Scope> storeScopesMap = apiDefinitionFromSwagger20.getScopes(storeRestAPI);
                    String scopes = String.join(" ", storeScopesMap.keySet());
                    scopes = scopes + " " + KeyManagerConstants.OPEN_ID_CONNECT_SCOPE;
                    // Get Access & Refresh Tokens
                    LoginTokenService loginTokenService = new LoginTokenService();
                    AccessTokenRequest accessTokenRequest = new AccessTokenRequest();
                    AuthResponseBean authResponseBean = new AuthResponseBean();
                    // TODO: Get Consume Key & Secret without creating a new app, from the IS side
                    Map<String, String> consumerKeySecretMap = loginTokenService.getConsumerKeySecret(appName);
                    if (log.isDebugEnabled()) {
                        log.debug("Received consumer key & secret for " + appName + "application.");
                    }
                    accessTokenRequest.setClientId(consumerKeySecretMap.get("CONSUMER_KEY"));
                    accessTokenRequest.setClientSecret(consumerKeySecretMap.get("CONSUMER_SECRET"));
                    accessTokenRequest.setGrantType(grantType);
                    accessTokenRequest.setAuthorizationCode(authorizationCode);
                    accessTokenRequest.setValidityPeriod(validityPeriod);
                    accessTokenRequest.setScopes(scopes);
                    accessTokenRequest.setCallbackURI(callBackURL);
                    AccessTokenInfo accessTokenInfo = APIManagerFactory.getInstance().getKeyManager()
                            .getNewAccessToken(accessTokenRequest);
                    loginTokenService.setAccessTokenData(authResponseBean, accessTokenInfo);
                    String accessToken = accessTokenInfo.getAccessToken();
                    if (log.isDebugEnabled()) {
                        log.debug("Received access token for " + appName + "application.");
                    }
                    // Set Access Token cookies
                    String part1 = accessToken.substring(0, accessToken.length() / 2);
                    String part2 = accessToken.substring(accessToken.length() / 2);
                    NewCookie cookieWithAppContext = AuthUtil
                            .cookieBuilder(AuthenticatorConstants.ACCESS_TOKEN_1, part1, appContext,
                                    true, false, "future");
                    NewCookie httpOnlyCookieWithAppContext = AuthUtil
                            .cookieBuilder(AuthenticatorConstants.ACCESS_TOKEN_2, part2, appContext,
                                    true, true, "future");
                    NewCookie restAPIContextCookie = AuthUtil
                            .cookieBuilder(APIConstants.AccessTokenConstants.AM_TOKEN_MSF4J, part2, restAPIContext,
                                    true, true, "future");
                    String authUser = authResponseBean.getAuthUser();
                    NewCookie authUserCookie = AuthUtil
                            .cookieBuilder(AuthenticatorConstants.AUTH_USER, authUser, appContext, true, false, "");
                    if (log.isDebugEnabled()) {
                        log.debug("Set cookies for " + appName + "application.");
                    }
                    // Redirect to the store/apis page (redirect URL)
                    URI targetURIForRedirection = new URI(storeConfigs.getApimBaseUrl() + appName);
                    return Response.status(Response.Status.FOUND)
                            .header(HttpHeaders.LOCATION, targetURIForRedirection).entity(authResponseBean)
                            .cookie(cookieWithAppContext, httpOnlyCookieWithAppContext,
                                    restAPIContextCookie, authUserCookie)
                            .build();
                } else {
                    log.error("No Authorization Code available.");
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Error while receiving authorization code!").build();
                }
            } catch (APIManagementException e) {
                ErrorDTO errorDTO = AuthUtil.getErrorDTO(e.getErrorHandler(), null);
                log.error(e.getMessage(), e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            } catch (URISyntaxException e) {
                log.error(e.getMessage(), e);
                return Response.status(e.getIndex()).build();
            }
    }

    /**
     * This method creates a DCR application.
     *
     * @param clientName Name of the application to be created
     * @param grantTypes List of grant types of the application
     * @return OAUthApplicationInfo - An object with DCR Application information
     */
    private OAuthApplicationInfo createDCRApplication(String clientName, List<String> grantTypes)
            throws APIManagementException {
        KeyManager keyManager = APIManagerFactory.getInstance().getKeyManager();
        APIMStoreConfigurations storeConfigs = ServiceReferenceHolder.getInstance().getAPIMStoreConfiguration();
        String callBackURL = storeConfigs.getApimBaseUrl() + clientName + "/auth/apis/login/callback";
        OAuthApplicationInfo oAuthApplicationInfo;
        try {
            // Here the keyType:"Application" will be passed as a default value
            // for the oAuthAppRequest constructor argument.
            // This value is not related to DCR application creation.
            OAuthAppRequest oAuthAppRequest = new OAuthAppRequest(clientName,
                    callBackURL, "Application",
                    grantTypes);
            oAuthApplicationInfo = keyManager.createApplication(oAuthAppRequest);
        } catch (KeyManagementException e) {
            String errorMsg = "Error while creating the keys for OAuth application : " + clientName;
            log.error(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            throw new APIManagementException(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }
        return oAuthApplicationInfo;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/revoke")
    public Response logout(@Context Request request) {
        String appContext = AuthUtil.getAppContext(request);
        String restAPIContext;
        if (appContext.contains("editor")) {
            restAPIContext = AuthenticatorConstants.REST_CONTEXT + "/publisher";
        } else {
            restAPIContext = AuthenticatorConstants.REST_CONTEXT + appContext;
        }
        String accessToken = AuthUtil
                .extractTokenFromHeaders(request.getHeaders(), AuthenticatorConstants.ACCESS_TOKEN_2);
        if (accessToken != null) {
            try {
                LoginTokenService loginTokenService = new LoginTokenService();
                loginTokenService.revokeAccessToken(appContext.substring(1), accessToken);
                // Lets invalidate all the cookies saved.
                NewCookie appContextCookie = AuthUtil
                        .cookieBuilder(AuthenticatorConstants.ACCESS_TOKEN_2, "", appContext, true, true,
                                AuthenticatorConstants.COOKIE_EXPIRE_TIME);
                NewCookie restContextCookie = AuthUtil
                        .cookieBuilder(APIConstants.AccessTokenConstants.AM_TOKEN_MSF4J, "", restAPIContext, true, true,
                                AuthenticatorConstants.COOKIE_EXPIRE_TIME);
                NewCookie refreshTokenCookie = AuthUtil
                        .cookieBuilder(AuthenticatorConstants.REFRESH_TOKEN_1, "", appContext, true, false,
                                AuthenticatorConstants.COOKIE_EXPIRE_TIME);
                NewCookie refreshTokenHttpOnlyCookie = AuthUtil
                        .cookieBuilder(AuthenticatorConstants.REFRESH_TOKEN_2, "", appContext, true, true,
                                AuthenticatorConstants.COOKIE_EXPIRE_TIME);
                return Response.ok().cookie(appContextCookie, restContextCookie, refreshTokenCookie,
                        refreshTokenHttpOnlyCookie).build();
            } catch (APIManagementException e) {
                ErrorDTO errorDTO = AuthUtil.getErrorDTO(e.getErrorHandler(), null);
                log.error(e.getMessage(), e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            }
        }
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(ExceptionCodes.INVALID_AUTHORIZATION_HEADER.getErrorCode());
        errorDTO.setMessage(ExceptionCodes.INVALID_AUTHORIZATION_HEADER.getErrorMessage());
        return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
    }
}
