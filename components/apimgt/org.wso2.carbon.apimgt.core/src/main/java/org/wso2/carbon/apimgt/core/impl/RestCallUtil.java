package org.wso2.carbon.apimgt.core.impl;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.core.models.ContentType;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RestCallUtil {

    private RestCallUtil() {

    }

    public static Response loginRequest(URI uri, String userName, String password, ContentType requestContentType) {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        if (userName == null) {
            throw new IllegalArgumentException("UserName must not be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(uri);
        JSONObject loginInfoJsonObj = new JSONObject();
        loginInfoJsonObj.put("userName", userName);
        loginInfoJsonObj.put("password", password);
        Invocation.Builder invocationBuilder = requestContentType == null ? target.request() :
                target.request(requestContentType.getMediaType());
        return invocationBuilder.post(Entity.json(loginInfoJsonObj.toJSONString()));
    }

    public static Response rsaSignedFetchUserRequest(URI uri, String userName, String userTenantDomain,
                                                     String rsaSignedToken, ContentType requestContentType) {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
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
        WebTarget target = client.target(uri);
        JSONObject loginInfoJsonObj = new JSONObject();
        loginInfoJsonObj.put("userName", userName);
        loginInfoJsonObj.put("userTenantDomain", userTenantDomain);
        Invocation.Builder invocationBuilder = requestContentType == null ? target.request() :
                target.request(requestContentType.getMediaType());
        return invocationBuilder.header("rsaSignedToken", rsaSignedToken).
                post(Entity.json(loginInfoJsonObj.toJSONString()));
    }

    public static List<Cookie> captureCookies(Response response) {
        if (response == null) {
            throw new IllegalArgumentException("The response must not be null");
        }
        List<Cookie> cookies = new ArrayList<>();
        Map<String, NewCookie> responseCookies = response.getCookies();
        cookies.addAll(responseCookies.values());
        return cookies;
    }

    public static Response getRequest(URI uri, ContentType requestContentType, List<Cookie> cookies) {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(uri);
        Invocation.Builder invocationBuilder = requestContentType == null ? target.request() :
                target.request(requestContentType.getMediaType());
        if (cookies != null && !cookies.isEmpty()) {
            for (Cookie cookie : cookies) {
                invocationBuilder = invocationBuilder.cookie(cookie);
            }
        }
        return invocationBuilder.get();
    }

    public static Response postRequest(URI uri, ContentType requestContentType, List<Cookie> cookies, Entity entity) {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(uri);
        Invocation.Builder invocationBuilder = requestContentType == null ? target.request() :
                target.request(requestContentType.getMediaType());
        if (cookies != null && !cookies.isEmpty()) {
            for (Cookie cookie : cookies) {
                invocationBuilder = invocationBuilder.cookie(cookie);
            }
        }
        return invocationBuilder.post(entity);
    }

    public static Response putRequest(URI uri, ContentType requestContentType, List<Cookie> cookies, Entity entity) {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(uri);
        Invocation.Builder invocationBuilder = requestContentType == null ? target.request() :
                target.request(requestContentType.getMediaType());
        if (cookies != null && !cookies.isEmpty()) {
            for (Cookie cookie : cookies) {
                invocationBuilder = invocationBuilder.cookie(cookie);
            }
        }
        return invocationBuilder.put(entity);
    }

    public static Response deleteRequest(URI uri, ContentType requestContentType, List<Cookie> cookies) {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(uri);
        Invocation.Builder invocationBuilder = requestContentType == null ? target.request() :
                target.request(requestContentType.getMediaType());
        if (cookies != null && !cookies.isEmpty()) {
            for (Cookie cookie : cookies) {
                invocationBuilder = invocationBuilder.cookie(cookie);
            }
        }
        return invocationBuilder.delete();
    }
}
