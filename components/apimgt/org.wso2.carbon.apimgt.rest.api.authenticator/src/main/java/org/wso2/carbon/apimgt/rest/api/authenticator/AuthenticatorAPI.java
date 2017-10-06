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

package org.wso2.carbon.apimgt.rest.api.authenticator;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models.APIMStoreConfigurations;
import org.wso2.carbon.apimgt.rest.api.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.authenticator.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.authenticator.utils.AuthUtil;
import org.wso2.carbon.apimgt.rest.api.authenticator.utils.bean.AuthResponseBean;
import org.wso2.carbon.apimgt.rest.api.authenticator.utils.bean.EnvironmentConfigBean;
import org.wso2.carbon.apimgt.rest.api.common.APIConstants;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FormDataParam;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * This class provides access token during login from store app.
 *
 */

@Component(
        name = "org.wso2.carbon.apimgt.rest.api.authenticator.AuthenticatorAPI",
        service = Microservice.class,
        immediate = true
)
@Path("/login")
public class AuthenticatorAPI implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatorAPI.class);

    /**
     *
     *
     */
    @OPTIONS
    @Path("/token/{appName}")
    public Response auth(){
      return Response.ok()
                .header("Access-Control-Allow-Origin", "https://localhost:9292")
                .header("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD")
                .header("Access-Control-Allow-Headers", "Accept, Accept-Encoding, Accept-Language," +
                        " authorization, Cache-Control, Connection, Cookie, Host, Pragma, Referer, User-Agent, Set-Cookie")
                .header("Access-Control-Allow-Credentials", "true")
                .build();

    }

    /**
     * This method authenticate the user for store app.
     *
     */
    @POST
    @Path ("/token/{appName}")
    @Produces (MediaType.APPLICATION_JSON)
    @Consumes ({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA })
    public Response authenticate(@Context Request request, @PathParam("appName") String appName,
            @FormDataParam ("username") String userName, @FormDataParam ("password") String password,
            @FormDataParam ("grant_type") String grantType, @FormDataParam ("validity_period") String validityPeriod,
            @FormDataParam ("remember_me") boolean isRememberMe, @FormDataParam ("scopes") String scopesList) {
        try {
            KeyManager keyManager = APIManagerFactory.getInstance().getKeyManager();
            AuthenticatorService authenticatorService = new AuthenticatorService(keyManager);
            AuthResponseBean authResponseBean = new AuthResponseBean();
            String appContext = "/" + appName;
            String restAPIContext;
            if (appContext.contains(AuthenticatorConstants.EDITOR_APPLICATION) ||
                    request.getUri().contains(AuthenticatorConstants.PUBLISHER_APPLICATION)) {
                restAPIContext = AuthenticatorConstants.REST_CONTEXT + "/" +
                        AuthenticatorConstants.PUBLISHER_APPLICATION;
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
                    return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).header("Access-Control-Allow-Origin", "https://localhost:9292").build();
                }
            }
            AccessTokenInfo accessTokenInfo = authenticatorService.getTokens(appContext.substring(1),
                    null, grantType, userName, password, refToken,
                    Long.parseLong(validityPeriod));
            authenticatorService.setAccessTokenData(authResponseBean, accessTokenInfo);
            String accessToken = accessTokenInfo.getAccessToken();
            String refreshToken = accessTokenInfo.getRefreshToken();

            // The access token is stored as two cookies in client side. One is a normal cookie and other is a http
            // only cookie. Hence we need to split the access token
            APIMConfigurations environmentConfigurations = APIMConfigurationService.getInstance().getApimConfigurations();
            String part1 = accessToken.substring(0, accessToken.length() / 2);
            String part2 = accessToken.substring(accessToken.length() / 2);
            NewCookie cookieWithAppContext = AuthUtil
                    .cookieBuilder(AuthenticatorConstants.ACCESS_TOKEN_1 + "_" + environmentConfigurations.getEnvironmentName(),
                            part1, appContext, true, false, "");
            authResponseBean.setPartialToken(part1);
            NewCookie httpOnlyCookieWithAppContext = AuthUtil
                    .cookieBuilder(AuthenticatorConstants.ACCESS_TOKEN_2 + "_" + environmentConfigurations.getEnvironmentName(),
                            part2, appContext, true, true, "");
            NewCookie restAPIContextCookie = AuthUtil
                    .cookieBuilder(APIConstants.AccessTokenConstants.AM_TOKEN_MSF4J + "_" +environmentConfigurations.getEnvironmentName(),
                            part2, restAPIContext, true, true,
                            "");
            NewCookie refreshTokenCookie, refreshTokenHttpOnlyCookie;
            // Refresh token is not set to cookie if remember me is not set.
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
                                refreshTokenCookie, refreshTokenHttpOnlyCookie).header("Access-Control-Allow-Origin", "https://localhost:9292").header(AuthenticatorConstants.
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
                        .cookie(cookieWithAppContext, httpOnlyCookieWithAppContext, restAPIContextCookie).header("Access-Control-Allow-Origin", "https://localhost:9292")
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
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).header("Access-Control-Allow-Origin", "https://localhost:9292").build();
        }
    }

    @POST
    @Produces (MediaType.APPLICATION_JSON)
    @Path ("/logout/{appName}")
    public Response logout(@Context Request request, @PathParam("appName") String appName) {
        String appContext = "/" + appName;
        String restAPIContext;
        if (appContext.contains(AuthenticatorConstants.EDITOR_APPLICATION)) {
            restAPIContext = AuthenticatorConstants.REST_CONTEXT + "/" + AuthenticatorConstants.PUBLISHER_APPLICATION;
        } else {
            restAPIContext = AuthenticatorConstants.REST_CONTEXT + appContext;
        }
        String accessToken = AuthUtil
                .extractTokenFromHeaders(request.getHeaders(), AuthenticatorConstants.ACCESS_TOKEN_2);
        if (accessToken != null) {
            try {
                KeyManager keyManager = APIManagerFactory.getInstance().getKeyManager();
                AuthenticatorService authenticatorService = new AuthenticatorService(keyManager);
                authenticatorService.revokeAccessToken(appContext.substring(1), accessToken);
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

    /**
     * This method provides the DCR application information to the SSO-IS login.
     *
     * @param request Request to call the /login api
     * @return Response - Response object with OAuth data
     */
    @GET
    @Path("/login/{appName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response redirect(@Context Request request, @PathParam("appName") String appName) {
        try {
            KeyManager keyManager = APIManagerFactory.getInstance().getKeyManager();
            AuthenticatorService authenticatorService = new AuthenticatorService(keyManager);
            JsonObject oAuthData = authenticatorService.getAuthenticationConfigurations(appName);
            if (oAuthData.size() == 0) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Error while creating the OAuth application!").build();
            } else {
                return Response.status(Response.Status.OK).entity(oAuthData).build();
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
    @Path("/callback/{appName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response callback(@Context Request request, @PathParam("appName") String appName) {
        String appContext = "/" + appName;
        String restAPIContext;
        if (AuthenticatorConstants.EDITOR_APPLICATION.equals(appName) ||
                request.getUri().contains(AuthenticatorConstants.PUBLISHER_APPLICATION)) {
            restAPIContext = AuthenticatorConstants.REST_CONTEXT + "/" + AuthenticatorConstants.PUBLISHER_APPLICATION;
        } else {
            restAPIContext = AuthenticatorConstants.REST_CONTEXT + appContext;
        }
        String requestURL = (String) request.getProperty(AuthenticatorConstants.REQUEST_URL);

        APIMStoreConfigurations storeConfigs = ServiceReferenceHolder.getInstance().getAPIMStoreConfiguration();
        AuthResponseBean authResponseBean = new AuthResponseBean();
        String grantType = KeyManagerConstants.AUTHORIZATION_CODE_GRANT_TYPE;
        try {
            KeyManager keyManager = APIManagerFactory.getInstance().getKeyManager();
            AuthenticatorService authenticatorService = new AuthenticatorService(keyManager);
            AccessTokenInfo accessTokenInfo = authenticatorService.getTokens(appName, requestURL, grantType,
                    null, null, null, 0);
            if (StringUtils.isEmpty(accessTokenInfo.toString())) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Access token generation failed!").build();
            } else {
                authenticatorService.setAccessTokenData(authResponseBean, accessTokenInfo);
                String accessToken = accessTokenInfo.getAccessToken();
                if (log.isDebugEnabled()) {
                    log.debug("Received access token for " + appName + " application.");
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
                    log.debug("Set cookies for " + appName + " application.");
                }
                // Redirect to the store/apis page (redirect URL)
                URI targetURIForRedirection = new URI(storeConfigs.getApimBaseUrl() + appName);
                return Response.status(Response.Status.FOUND)
                        .header(HttpHeaders.LOCATION, targetURIForRedirection).entity(authResponseBean)
                        .cookie(cookieWithAppContext, httpOnlyCookieWithAppContext,
                                restAPIContextCookie, authUserCookie)
                        .build();
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
    @GET
    @Produces (MediaType.APPLICATION_JSON)
    @Path ("/infoenv")
    public Response configInfo () {

        APIMConfigurations environmentConfigurations = APIMConfigurationService.getInstance().getApimConfigurations();
        EnvironmentConfigBean environmentConfigBean = new EnvironmentConfigBean();

        environmentConfigBean.setEnvironments(environmentConfigurations.getEnvironments());

        return Response.ok(environmentConfigBean, MediaType.APPLICATION_JSON).build();
    }

}