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
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.configuration.APIMConfigurationService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.authenticator.factories.AuthenticatorAPIFactory;
import org.wso2.carbon.apimgt.rest.api.authenticator.utils.AuthUtil;
import org.wso2.carbon.apimgt.rest.api.authenticator.utils.bean.AuthResponseBean;
import org.wso2.carbon.apimgt.rest.api.common.APIConstants;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FormDataParam;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * This class provides access token during login from store app.
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
     * This method authenticate the user for store app.
     */
    @OPTIONS
    @POST
    @Path("/token/{appName}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
    public Response authenticate(@Context Request request, @PathParam("appName") String appName,
                                 @FormDataParam("username") String userName, @FormDataParam("password") String password,
                                 @FormDataParam("assertion") String assertion, @FormDataParam("grant_type") String grantType,
                                 @FormDataParam("validity_period") String validityPeriod,
                                 @FormDataParam("remember_me") boolean isRememberMe, @FormDataParam("scopes") String scopesList) {
        try {
            AuthenticatorService authenticatorService = AuthenticatorAPIFactory.getInstance().getService();
            IdentityProvider identityProvider = APIManagerFactory.getInstance().getIdentityProvider();
            AuthResponseBean authResponseBean;
            Map<String, NewCookie> cookies = new HashMap<>();

            String refreshToken = null;
            if (AuthenticatorConstants.REFRESH_GRANT.equals(grantType)) {
                String environmentName = APIMConfigurationService.getInstance()
                        .getEnvironmentConfigurations().getEnvironmentLabel();
                refreshToken = AuthUtil
                        .extractTokenFromHeaders(request, AuthenticatorConstants.REFRESH_TOKEN_2, environmentName);
                if (refreshToken == null) {
                    ErrorDTO errorDTO = new ErrorDTO();
                    errorDTO.setCode(ExceptionCodes.INVALID_AUTHORIZATION_HEADER.getErrorCode());
                    errorDTO.setMessage(ExceptionCodes.INVALID_AUTHORIZATION_HEADER.getErrorMessage());
                    return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
                }
            }

            Map<String, String> contextPaths = AuthUtil.getContextPaths(appName);
            AccessTokenInfo accessTokenInfo = authenticatorService.getTokens(appName, grantType, userName, password,
                    refreshToken, Long.parseLong(validityPeriod), null, assertion, identityProvider);
            authResponseBean = authenticatorService.getResponseBeanFromTokenInfo(accessTokenInfo);
            authenticatorService.setupAccessTokenParts(cookies, authResponseBean, accessTokenInfo.getAccessToken(),
                    contextPaths, false);

            String refreshTokenNew = accessTokenInfo.getRefreshToken();
            // Refresh token is not set to cookie if remember me is not set.
            if (refreshTokenNew != null && (AuthenticatorConstants.REFRESH_GRANT.equals(grantType) || (
                    AuthenticatorConstants.PASSWORD_GRANT.equals(grantType) && isRememberMe))) {
                authenticatorService.setupRefreshTokenParts(cookies, refreshTokenNew, contextPaths);
                return Response.ok(authResponseBean, MediaType.APPLICATION_JSON)
                        .cookie(cookies.get(AuthenticatorConstants.Context.REST_API_CONTEXT),
                                cookies.get(AuthenticatorConstants.Context.LOGOUT_CONTEXT),
                                cookies.get(AuthenticatorConstants.Context.APP_CONTEXT),
                                cookies.get(AuthenticatorConstants.Context.LOGIN_CONTEXT))
                        .header(AuthenticatorConstants.REFERER_HEADER,
                                (request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null && request
                                        .getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER)
                                        .equals(request.getHeader(AuthenticatorConstants.REFERER_HEADER))) ?
                                        "" :
                                        request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null ?
                                                request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) :
                                                "")
                        .build();
            } else {
                return Response.ok(authResponseBean, MediaType.APPLICATION_JSON)
                        .cookie(cookies.get(AuthenticatorConstants.Context.REST_API_CONTEXT),
                                cookies.get(AuthenticatorConstants.Context.LOGOUT_CONTEXT))
                        .header(AuthenticatorConstants.
                                        REFERER_HEADER,
                                (request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null && request
                                        .getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER)
                                        .equals(request.getHeader(AuthenticatorConstants.REFERER_HEADER))) ?
                                        "" :
                                        request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) != null ?
                                                request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER) :
                                                "")
                        .build();
            }
        } catch (APIManagementException e) {
            ErrorDTO errorDTO = AuthUtil.getErrorDTO(e.getErrorHandler(), null);
            log.error(e.getMessage(), e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO)
                    .build();
        }
    }

    @OPTIONS
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/logout/{appName}")
    public Response logout(@Context Request request, @PathParam("appName") String appName) {
        Map<String, String> contextPaths = AuthUtil.getContextPaths(appName);

        String environmentName = APIMConfigurationService.getInstance()
                .getEnvironmentConfigurations().getEnvironmentLabel();
        String accessToken = AuthUtil
                .extractTokenFromHeaders(request, AuthenticatorConstants.ACCESS_TOKEN_2, environmentName);

        if (accessToken != null) {
            try {
                AuthenticatorService authenticatorService = AuthenticatorAPIFactory.getInstance().getService();
                authenticatorService.revokeAccessToken(appName, accessToken);
                // Lets invalidate all the cookies saved.
                NewCookie logoutContextCookie = AuthUtil
                        .cookieBuilder(AuthenticatorConstants.ACCESS_TOKEN_2, "",
                                contextPaths.get(AuthenticatorConstants.Context.LOGOUT_CONTEXT), true, true,
                                AuthenticatorConstants.COOKIE_EXPIRE_TIME, environmentName);
                NewCookie restContextCookie = AuthUtil
                        .cookieBuilder(APIConstants.AccessTokenConstants.AM_TOKEN_MSF4J, "",
                                contextPaths.get(AuthenticatorConstants.Context.REST_API_CONTEXT), true, true,
                                AuthenticatorConstants.COOKIE_EXPIRE_TIME, environmentName);
                NewCookie refreshTokenCookie = AuthUtil
                        .cookieBuilder(AuthenticatorConstants.REFRESH_TOKEN_1, "",
                                contextPaths.get(AuthenticatorConstants.Context.APP_CONTEXT), true, false,
                                AuthenticatorConstants.COOKIE_EXPIRE_TIME, environmentName);
                NewCookie refreshTokenHttpOnlyCookie = AuthUtil
                        .cookieBuilder(AuthenticatorConstants.REFRESH_TOKEN_2, "",
                                contextPaths.get(AuthenticatorConstants.Context.APP_CONTEXT), true, true,
                                AuthenticatorConstants.COOKIE_EXPIRE_TIME, environmentName);
                return Response.ok().cookie(logoutContextCookie, restContextCookie, refreshTokenCookie,
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
    @OPTIONS
    @GET
    @Path("/login/{appName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response redirect(@Context Request request, @PathParam("appName") String appName) {
        try {
            AuthenticatorService authenticatorService = AuthenticatorAPIFactory.getInstance().getService();
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
     * @param request           Request to call /callback api
     * @param appName           Name of the application (publisher/store/admin)
     * @param authorizationCode Authorization-Code
     * @return Response - Response with redirect URL
     */
    @OPTIONS
    @GET
    @Path("/callback/{appName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response callback(@Context Request request, @PathParam("appName") String appName,
                             @QueryParam("code") String authorizationCode) {
        String grantType = KeyManagerConstants.AUTHORIZATION_CODE_GRANT_TYPE;
        try {
            AuthenticatorService authenticatorService = AuthenticatorAPIFactory.getInstance().getService();
            AuthResponseBean authResponseBean;
            Map<String, NewCookie> cookies = new HashMap<>();

            Map<String, String> contextPaths = AuthUtil.getContextPaths(appName);
            AccessTokenInfo accessTokenInfo = authenticatorService.getTokens(appName, grantType,
                    null, null, null, 0, authorizationCode, null, null);
            authResponseBean = authenticatorService.getResponseBeanFromTokenInfo(accessTokenInfo);
            authenticatorService.setupAccessTokenParts(cookies, authResponseBean, accessTokenInfo.getAccessToken(),
                    contextPaths, true);

            log.debug("Set cookies for {} application.", appName);
            if (AuthenticatorConstants.PUBLISHER_APPLICATION.equals(appName) || AuthenticatorConstants.STORE_APPLICATION.equals(appName)) {
                URI targetURIForRedirection = authenticatorService.getUIServiceRedirectionURI(appName, authResponseBean);
                return Response.status(Response.Status.FOUND)
                        .header(HttpHeaders.LOCATION, targetURIForRedirection)
                        .cookie(cookies.get(AuthenticatorConstants.Context.REST_API_CONTEXT),
                                cookies.get(AuthenticatorConstants.Context.LOGOUT_CONTEXT))
                        .build();
            } else {
                URI targetURIForRedirection = authenticatorService.getUIServiceRedirectionURI(appName, null);
                return Response.status(Response.Status.FOUND)
                        .header(HttpHeaders.LOCATION, targetURIForRedirection).entity(authResponseBean)
                        .cookie(cookies.get(AuthenticatorConstants.Context.REST_API_CONTEXT),
                                cookies.get(AuthenticatorConstants.Context.LOGOUT_CONTEXT),
                                cookies.get(AuthenticatorConstants.AUTH_USER))
                        .build();
            }

        } catch (APIManagementException e) {
            ErrorDTO errorDTO = AuthUtil.getErrorDTO(e.getErrorHandler(), null);
            log.error(e.getMessage(), e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            return Response.status(e.getIndex()).build();
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
