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

import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * This method authenticate the user.
 *
 */
public class AuthenticatorAPI implements Microservice {

    /**
     * This method authenticate the user.
     *
     */
    @POST
    @Path ("/token")
    @Produces ("application/json")
    @Consumes ("application/x-www-form-urlencoded")
    public Response authenticate(
            @FormParam ("username") String userName, @FormParam ("password") String password, @FormParam ("scope")
            String scope) {
        IntrospectService introspectService = new IntrospectService();
        int a = 1;
        String accessToken = introspectService.getAccessToken(userName, password, scope);
        String part1 = accessToken.substring(0, accessToken.length() / 2);
        String part2 = accessToken.substring(accessToken.length() / 2 + 1);
        NewCookie cookie = new NewCookie("token1", part1 + "; path=/; domain=http://localhost:9090");
        NewCookie cookie2 = new NewCookie("token2", part2 + "; path=/; domain=http://localhost:9090; HttpOnly");
        return Response.ok(new IntrospectService().getAccessTokenData(userName, password, scope), "application/json")
                .cookie(cookie, cookie2).build();

    }
}
