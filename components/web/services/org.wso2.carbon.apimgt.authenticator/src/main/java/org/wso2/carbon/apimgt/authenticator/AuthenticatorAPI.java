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
import org.wso2.carbon.apimgt.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.authenticator.dto.ErrorDTO;
import org.wso2.carbon.apimgt.authenticator.utils.AuthUtil;
import org.wso2.carbon.apimgt.authenticator.utils.bean.AuthResponseBean;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.ApplicationUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.rest.api.common.APIConstants;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
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
     * This method redirects the user to the IS-SSO Login.
     */
    @GET
    @Path("/dcr")
    @Produces(MediaType.TEXT_HTML)
    public Response redirect(@Context Request request) {
        String appContext = AuthUtil.getAppContext(request);
        String userName = "admin";
        String scopes =
                "apim:api_view,apim:api_create,apim:api_publish,apim:tier_view,apim:tier_manage," +
                        "apim:subscription_view,apim:subscription_block,apim:subscribe,openid";
        Long validityPeriod = 3600L;
        OAuthApplicationInfo oAuthApplicationInfo;
        SSOLoginServiceStub ssoLoginServiceStub;
        feign.Response response;
        try {
            oAuthApplicationInfo = createDCRApplication(appContext.substring(1) , userName , validityPeriod);
            if (oAuthApplicationInfo != null) {
                String oAuthApplicationClientId = oAuthApplicationInfo.getClientId();
                String oAuthApplicationCallBackURL = oAuthApplicationInfo.getCallbackUrl();
                // String oAuthApplicationClientSecret = oAuthApplicationInfo.getClientSecret();
                // List<String> oAuthApplicationGrantTypes = oAuthApplicationInfo.getGrantTypes();

                ssoLoginServiceStub = SSOLoginServiceStubFactory.getSSOLoginServiceStub();
                response = ssoLoginServiceStub.getAutherizationCode(
                        "code", oAuthApplicationClientId,
                        oAuthApplicationCallBackURL, "read");
                if (response == null) {
                    System.out.print("Error");
                } else if (response.status() == 200) {
                    JsonObject oAuthData = new JsonObject();
                    oAuthData.addProperty("client_id" , oAuthApplicationClientId);
                    oAuthData.addProperty("callback_URL" , oAuthApplicationCallBackURL);
                    oAuthData.addProperty("scopes" , scopes);
                    String responseBody = response.body().toString();
                    System.out.print(responseBody);
                    return Response.ok().entity(oAuthData).build();
                }
                // Get the Autherization Code
                // Get Access Token
                // Redirect to the store/apis page (callback URL)
            }
            return Response.ok(oAuthApplicationInfo).build();
        } catch (KeyManagementException e) {
            ErrorDTO errorDTO = AuthUtil.getErrorDTO(e.getErrorHandler(), null);
            log.error(e.getMessage(), e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (APIManagementException e) {
            ErrorDTO errorDTO = AuthUtil.getErrorDTO(e.getErrorHandler(), null);
            log.error(e.getMessage(), e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * This method creates a DCR application.
     * @return OAUthApplicationInfo - An object with DCR Application information
     */
    private OAuthApplicationInfo createDCRApplication(String clientName, String userName, Long validityPeriod)
            throws APIManagementException {
        KeyManager keyManager = APIManagerFactory.getInstance().getKeyManager();
        OAuthAppRequest oAuthAppRequest = null;
        OAuthApplicationInfo oAuthApplicationInfo;
        try {
            oAuthAppRequest = ApplicationUtils
                    .createOauthAppRequest(clientName, userName, "https://localhost:9443/carbon/admin/login.jsp",
                            null);
            oAuthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.VALIDITY_PERIOD, validityPeriod);
            oAuthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.APP_KEY_TYPE, "application");
            oAuthApplicationInfo = keyManager.createApplication(oAuthAppRequest);
            // Authorization Code grant type (with CallBackURL) has set manually in the SP,
            // Do it using code when creating the application
        } catch (KeyManagementException e) {
            String errorMsg = "Error while creating the keys for OAuth application : " + clientName;
            log.error(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            throw new KeyManagementException(errorMsg, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        } catch (APIManagementException e) {
            String errorMsg = "Error while creating the OAuth application : " + clientName;
            log.error(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            throw new APIManagementException(errorMsg, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
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
