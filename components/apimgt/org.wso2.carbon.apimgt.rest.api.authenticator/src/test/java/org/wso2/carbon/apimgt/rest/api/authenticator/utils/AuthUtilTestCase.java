package org.wso2.carbon.apimgt.rest.api.authenticator.utils;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.rest.api.authenticator.constants.AuthenticatorConstants;
import org.wso2.carbon.apimgt.rest.api.authenticator.dto.ErrorDTO;
import org.wso2.msf4j.Request;

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

    }

    @Test
    public void testGetHttpOnlyCookieHeader() {
        Cookie cookie = new Cookie("WSO2_AM_REFRESH_TOKEN_1_Development", "xxx-refresh-token-1-xxx");
        String httpOnlyCookieHeader = AuthUtil.getHttpOnlyCookieHeader(cookie);
        Assert.assertEquals("WSO2_AM_REFRESH_TOKEN_1_Development=xxx-refresh-token-1-xxx; HttpOnly",
                httpOnlyCookieHeader);
    }
}
