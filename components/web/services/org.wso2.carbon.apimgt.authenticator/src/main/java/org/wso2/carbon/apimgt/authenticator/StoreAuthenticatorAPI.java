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

import org.wso2.carbon.apimgt.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.authenticator.utils.AuthUtil;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FormDataParam;

import java.util.List;
import javax.ws.rs.Consumes;
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
public class StoreAuthenticatorAPI implements Microservice {

    /**
     * This method authenticate the user for store app.
     *
     */
    @POST
    @Path ("/token")
    @Produces ("application/json")
    @Consumes ({ MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA })
    public Response authenticate(@Context Request request,
            @FormDataParam ("username") String userName, @FormDataParam ("password") String password,
            @FormDataParam ("scopes") List<String> scopes) {
        IntrospectService introspectService = new IntrospectService();
        String accessToken = introspectService.getAccessToken(userName, password, scopes.toArray(new String[0]));
        String part1 = accessToken.substring(0, accessToken.length() / 2);
        String part2 = accessToken.substring(accessToken.length() / 2 + 1);
        NewCookie cookie = new NewCookie(AuthenticatorConstants.TOKEN_1,
                part1 + "; path=" + AuthUtil.getAppContext("store") + "; domain=" + request.getProperty(
                        AuthenticatorConstants.REMOTE_HOST_HEADER));
        NewCookie cookie2 = new NewCookie(AuthenticatorConstants.TOKEN_2,
                part2 + "; path=" + AuthUtil.getAppContext("store") + "; domain=" + request.getProperty(
                        AuthenticatorConstants.REMOTE_HOST_HEADER) + "; "
                        + AuthenticatorConstants.HTTP_ONLY_COOKIE);
        return Response
                .ok(new IntrospectService().getAccessTokenData(userName, password, scopes.toArray(new String[0])),
                        MediaType.APPLICATION_JSON).cookie(cookie, cookie2)
                .header(AuthenticatorConstants.REFERER_HEADER, request.getHeader(AuthenticatorConstants.REFERER_HEADER)
                        .equals(request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER)) ?
                        "" :
                        request.getHeader(AuthenticatorConstants.X_ALT_REFERER_HEADER)).build();

    }
}
