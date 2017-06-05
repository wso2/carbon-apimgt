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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
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
import org.wso2.carbon.apimgt.core.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.rest.api.common.APIConstants;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FormDataParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
     * This method redirects the user to the SSO Login or to the Default Login.
     */
    @GET
    @Path("/dcr")
    @Produces(MediaType.APPLICATION_JSON)
    public Response redirect(@Context Request request) {
        String appContext = AuthUtil.getAppContext(request);
        String userName = "admin";
        //String password = "admin";
        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("code");
        /*String[] scopes = {
                "apim:api_view", "apim:api_create", "apim:api_publish", "apim:tier_view", "apim:tier_manage",
                "apim:subscription_view", "apim:subscription_block", "apim:subscribe",
                "openid"
        };*/
        Long validityPeriod = 3600L;
        OAuthApplicationInfo oAuthApplicationInfo;
        try {
            oAuthApplicationInfo = createDCRApplication(appContext.substring(1) , userName ,
                    validityPeriod , grantTypes);

            //String oAuthApplicationClientSecret = oAuthApplicationInfo.getClientSecret();
            String oAuthApplicationClientId = oAuthApplicationInfo.getClientId();
            String oAuthApplicationCallBackURL = oAuthApplicationInfo.getCallbackUrl();
            List<String> oAuthApplicationGrantTypes = oAuthApplicationInfo.getGrantTypes();

            for (int i = 0; i < oAuthApplicationGrantTypes.size(); i++) {
                /*if (oAuthApplicationGrantTypes.get(i) == "password") {
                    //Call Default Login
                    //Get Access Token
                    //After successfully login, Redirect to store/apis page (callback URL)
                }*/
                if (oAuthApplicationGrantTypes.get(i).equals("code")) {
                    //Call SSO-IS Login
                    //Break the URL and get client_id, etc from HTTP Request Headers
                    String getAutherizationCodeURL = "https://localhost:9443/oauth2/authorize?response_type=code&"
                            + "client_id=" + oAuthApplicationClientId
                            + "&redirect_uri=" + oAuthApplicationCallBackURL + "&scope=read";
                    HttpClient client = HttpClientBuilder.create().build();
                    HttpGet getRequest = new HttpGet(getAutherizationCodeURL);
                    HttpResponse response = client.execute(getRequest);
                    if (response.getStatusLine().getStatusCode() == 200) {
                        System.out.print("Success!");
                        //Get the Autherization Code
                        //Get Access Token
                        //Redirect to the store/apis page (callback URL)
                    }
                }
            }
            return Response.ok(oAuthApplicationInfo, MediaType.APPLICATION_JSON_TYPE).build();
        } catch (KeyManagementException e) {
            ErrorDTO errorDTO = AuthUtil.getErrorDTO(e.getErrorHandler(), null);
            log.error(e.getMessage(), e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        } catch (ClientProtocolException e) {
            log.error(e.getMessage(), e);
            //Look into this Response
            return Response.status(e.hashCode()).build();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            //Look into this Response
            return Response.status(e.hashCode()).build();
        }
    }

    /**
     * This method creates a DCR application.
     * @return OAUthApplicationInfo - An object with DCR Application information
     */
    private OAuthApplicationInfo createDCRApplication(String clientName, String userName, Long validityPeriod,
                                                      List<String> grantTypes) throws KeyManagementException {
        KeyManager keyManager = KeyManagerHolder.getAMLoginKeyManagerInstance();
        OAuthAppRequest oauthAppRequest = null;

        oauthAppRequest = AuthUtil
                .createOauthAppRequest(clientName, userName, "https://localhost:9292/store/apis",
                        null);
        oauthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.VALIDITY_PERIOD, validityPeriod);
        oauthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.APP_KEY_TYPE, "application");
        oauthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.OAUTH_CLIENT_GRANTS, grantTypes);
        OAuthApplicationInfo oAuthApplicationInfo;
        try {
            oAuthApplicationInfo = keyManager.createApplication(oauthAppRequest);
            return oAuthApplicationInfo;
        } catch (KeyManagementException e) {
            String errorMsg = "Error while creating the OAuth application : " + clientName;
            log.error(errorMsg, e, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            throw new KeyManagementException(errorMsg, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }
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
