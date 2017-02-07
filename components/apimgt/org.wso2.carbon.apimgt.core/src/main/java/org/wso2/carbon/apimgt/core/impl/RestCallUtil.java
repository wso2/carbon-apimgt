package org.wso2.carbon.apimgt.core.impl;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.core.exception.CookieNotFoundException;
import org.wso2.carbon.apimgt.core.models.ContentType;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Map;

public class RestCallUtil {

    private RestCallUtil() {

    }

    public static Response loginRequest(String URL, String userName, String password) {
        if (URL == null) {
            throw new IllegalArgumentException("The URL must not be null");
        }
        if (userName == null) {
            throw new IllegalArgumentException("UserName must not be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        JSONObject loginInfoJsonObj = new JSONObject();
        loginInfoJsonObj.put("userName", userName);
        loginInfoJsonObj.put("password", password);
        Invocation.Builder invocationBuilder = target.request(MediaType.TEXT_PLAIN);
        return invocationBuilder.post(Entity.json(loginInfoJsonObj.toJSONString()));
    }

    public static Response rsaSignedFetchUserRequest(String URL, String userName, String userTenantDomain, String
            rsaSignedToken) {
        if (URL == null) {
            throw new IllegalArgumentException("The URL must not be null");
        }
        if (userName == null) {
            throw new IllegalArgumentException("UserName must not be null");
        }
        if (userTenantDomain == null) {
            throw new IllegalArgumentException("User tenant domain must not be null");
        }
        if (rsaSignedToken == null) {
            throw new IllegalArgumentException("RSA signed token must not be null");
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        JSONObject loginInfoJsonObj = new JSONObject();
        loginInfoJsonObj.put("userName", userName);
        loginInfoJsonObj.put("userTenantDomain", userTenantDomain);
        Invocation.Builder invocationBuilder = target.request(MediaType.TEXT_PLAIN).header("rsaSignedToken",
                rsaSignedToken);
        return invocationBuilder.post(Entity.json(loginInfoJsonObj.toJSONString()));
    }

    public static Cookie captureCookie(Response response) throws CookieNotFoundException {
        if (response == null) {
            throw new IllegalArgumentException("The response must not be null");
        }
        Map<String, NewCookie> cookies = response.getCookies();
        if (cookies.isEmpty()) {
            throw new CookieNotFoundException("No cookies found");
        }
        Map.Entry<String, NewCookie> cookieEntry = cookies.entrySet().iterator().next();
        return cookieEntry.getValue();
    }

    public static Response getRequest(String URL, Cookie cookie, ContentType requestContentType) {
        if (URL == null) {
            throw new IllegalArgumentException("The URL must not be null");
        }
        if (requestContentType == null) {
            throw new IllegalArgumentException("The request content type must not be null");
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        Invocation.Builder invocationBuilder = target.request(requestContentType.getMediaType());
        return invocationBuilder.cookie(cookie).get();
    }

    public static Response postRequest(String URL, Cookie cookie, ContentType requestContentType, Entity entity) {
        if (URL == null) {
            throw new IllegalArgumentException("The URL must not be null");
        }
        if (requestContentType == null) {
            throw new IllegalArgumentException("The request content type must not be null");
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        Invocation.Builder invocationBuilder = target.request(requestContentType.getMediaType());
        return invocationBuilder.cookie(cookie).post(entity);
    }

    public static Response putRequest(String URL, Cookie cookie, ContentType requestContentType, Entity entity) {
        if (URL == null) {
            throw new IllegalArgumentException("The URL must not be null");
        }
        if (requestContentType == null) {
            throw new IllegalArgumentException("The request content type must not be null");
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        Invocation.Builder invocationBuilder = target.request(requestContentType.getMediaType());
        return invocationBuilder.cookie(cookie).put(entity);
    }

    public static Response deleteRequest(String URL, Cookie cookie, ContentType requestContentType) {
        if (URL == null) {
            throw new IllegalArgumentException("The URL must not be null");
        }
        if (requestContentType == null) {
            throw new IllegalArgumentException("The request content type must not be null");
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        Invocation.Builder invocationBuilder = target.request(requestContentType.getMediaType());
        return invocationBuilder.cookie(cookie).delete();
    }
}
