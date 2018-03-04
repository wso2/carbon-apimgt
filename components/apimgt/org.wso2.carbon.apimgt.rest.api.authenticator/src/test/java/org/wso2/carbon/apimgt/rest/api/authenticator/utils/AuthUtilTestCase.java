/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.authenticator.utils;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.rest.api.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.dto.ErrorDTO;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

public class AuthUtilTestCase {
    @Test
    public void testExtractTokenFromHeaders() {
        // Happy Path
        //// Mocked request object from the client
        Request request = Mockito.mock(Request.class);
        Mockito.when(request.getHeader(AuthenticatorConstants.AUTHORIZATION_HTTP_HEADER))
                .thenReturn("bearer xxx-partial-token-1-xxx");
        Mockito.when(request.getHeader(AuthenticatorConstants.COOKIE_HEADER))
                .thenReturn("WSO2_AM_TOKEN_2_Development=-xxx-partial-token-2-dev-xxx; "
                        + "WSO2_AM_TOKEN_2_Production=-xxx-partial-token-2-prod-xxx");

        String token = AuthUtil.extractTokenFromHeaders(request, "WSO2_AM_TOKEN_2", "Production");
        Assert.assertEquals("xxx-partial-token-1-xxx-xxx-partial-token-2-prod-xxx", token);

        // Error Path
        Mockito.when(request.getHeader(AuthenticatorConstants.AUTHORIZATION_HTTP_HEADER))
                .thenReturn("bearer-xxx-partial-token-1-xxx");
        token = AuthUtil.extractTokenFromHeaders(request, "WSO2_AM_TOKEN_2", "Production");
        Assert.assertNull(token);

        // Error Path
        Mockito.when(request.getHeader(AuthenticatorConstants.AUTHORIZATION_HTTP_HEADER))
                .thenReturn("xxx-partial-token-1-xxx");
        token = AuthUtil.extractTokenFromHeaders(request, "WSO2_AM_TOKEN_2", "Production");
        Assert.assertNull(token);

        // Error Path
        Mockito.when(request.getHeader(AuthenticatorConstants.AUTHORIZATION_HTTP_HEADER))
                .thenReturn(null);
        token = AuthUtil.extractTokenFromHeaders(request, "WSO2_AM_TOKEN_2", "Production");
        Assert.assertNull(token);
    }

    @Test
    public void testCookieBuilder() {
        NewCookie expectedCookie, actualCookie;

        //// Http only, secured cookie with no expiry
        expectedCookie = new NewCookie("WSO2_AM_TOKEN_2_Development",
                "xxx-Access-Token-2-xxx; path=/logout/store; HttpOnly; Secure; ");
        actualCookie = AuthUtil.cookieBuilder("WSO2_AM_TOKEN_2", "xxx-Access-Token-2-xxx",
                "/logout/store", true, true, "", "Development");
        Assert.assertEquals(expectedCookie, actualCookie);

        //// HTTP only, unsecured, expired cookie
        expectedCookie = new NewCookie("WSO2_AM_TOKEN_2_Development",
                "xxx-Access-Token-2-xxx; path=/logout/store; Expires=Thu, 01-Jan-1970 00:00:01 GMT; HttpOnly; ");
        actualCookie = AuthUtil.cookieBuilder("WSO2_AM_TOKEN_2", "xxx-Access-Token-2-xxx",
                "/logout/store", false, true, "Expires=Thu, 01-Jan-1970 00:00:01 GMT", "Development");
        Assert.assertEquals(expectedCookie, actualCookie);

        //// Javascript accessible, unsecured cookie
        expectedCookie = new NewCookie("WSO2_AM_TOKEN_2_Development",
                "xxx-Access-Token-2-xxx; path=/logout/publisher; ");
        actualCookie = AuthUtil.cookieBuilder("WSO2_AM_TOKEN_2", "xxx-Access-Token-2-xxx",
                "/logout/publisher", false, false, null, "Development");
        Assert.assertEquals(expectedCookie, actualCookie);
    }

    @Test
    public void testGetContextPaths() {
        // Happy Path
        //// expect the same map object when call the method again
        Map<String, String> expectedContextPaths = AuthUtil.getContextPaths("publisher");
        Map<String, String> actualContextPaths = AuthUtil.getContextPaths("publisher");
        Assert.assertSame(expectedContextPaths, actualContextPaths);

        //// expect for publisher app
        expectedContextPaths = new HashMap<>();
        expectedContextPaths.put("APP_CONTEXT", "/publisher");
        expectedContextPaths.put("LOGOUT_CONTEXT", "/login/logout/publisher");
        expectedContextPaths.put("LOGIN_CONTEXT", "/login/token/publisher");
        expectedContextPaths.put("REST_API_CONTEXT", "/api/am/publisher");
        Assert.assertEquals(expectedContextPaths, actualContextPaths);

        //// expect for editor app
        actualContextPaths = AuthUtil.getContextPaths("editor");
        expectedContextPaths = new HashMap<>();
        expectedContextPaths.put("APP_CONTEXT", "/editor");
        expectedContextPaths.put("LOGOUT_CONTEXT", "/login/logout/editor");
        expectedContextPaths.put("LOGIN_CONTEXT", "/login/token/editor");
        expectedContextPaths.put("REST_API_CONTEXT", "/api/am/publisher");
        Assert.assertEquals(expectedContextPaths, actualContextPaths);
    }

    @Test
    public void testGetErrorDTO(){
        ErrorHandler errorHandler = new ErrorHandler() {
            @Override
            public long getErrorCode() {
                return 1234567890L;
            }

            @Override
            public String getErrorMessage() {
                return "xxx-error-message-xxx";
            }

            @Override
            public String getErrorDescription() {
                return "xxx-error-description-xxx";
            }
        };
        HashMap<String, String> paramList = new HashMap<>();
        paramList.put("param_1", "xxx-param_1-xxx");
        paramList.put("param_2", "xxx-param_2-xxx");

        //// expected error dto
        ErrorDTO expectedErrorDTO = new ErrorDTO();
        expectedErrorDTO.setCode(1234567890L);
        expectedErrorDTO.setMessage("xxx-error-message-xxx");
        expectedErrorDTO.setDescription("xxx-error-description-xxx");
        expectedErrorDTO.setMoreInfo(paramList);

        ErrorDTO actualErrorDTO = AuthUtil.getErrorDTO(errorHandler, paramList);
        Assert.assertEquals(expectedErrorDTO.getCode(), actualErrorDTO.getCode());
        Assert.assertEquals(expectedErrorDTO.getMessage(), actualErrorDTO.getMessage());
        Assert.assertEquals(expectedErrorDTO.getDescription(), actualErrorDTO.getDescription());
        Assert.assertEquals(expectedErrorDTO.getMoreInfo(), actualErrorDTO.getMoreInfo());
    }

    @Test
    public void testGetHttpOnlyCookieHeader() {
        Cookie cookie = new Cookie("WSO2_AM_REFRESH_TOKEN_1_Development", "xxx-refresh-token-1-xxx");
        String httpOnlyCookieHeader = AuthUtil.getHttpOnlyCookieHeader(cookie);
        Assert.assertEquals("WSO2_AM_REFRESH_TOKEN_1_Development=xxx-refresh-token-1-xxx; HttpOnly",
                httpOnlyCookieHeader);
    }
}
