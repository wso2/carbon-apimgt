package org.wso2.carbon.apimgt.core.impl;

import org.json.simple.JSONObject;

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

    public static Response loginRequest(String URL, String name, String userTenantDomain) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        JSONObject loginInfoJsonObj = new JSONObject();
        loginInfoJsonObj.put("userName", name);
        loginInfoJsonObj.put("userTenantDomain", userTenantDomain);
        Invocation.Builder invocationBuilder = target.request(MediaType.TEXT_PLAIN);
        return invocationBuilder.post(Entity.json(loginInfoJsonObj.toJSONString()));
    }
    public static Cookie captureCookie(Response response) {
        Map<String, NewCookie> cookies = response.getCookies();
        if(!cookies.isEmpty()) {
            // We are sure that we only get one cookie from the service
            Map.Entry<String, NewCookie> cookieEntry = cookies.entrySet().iterator().next();
            return cookieEntry.getValue();
        }
        else {
            System.out.println("Error: No cookies found");
            return null;
        }
    }

    public static Response getRequest(String URL, Cookie cookie, ContentType requestContentType) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        Invocation.Builder invocationBuilder =  target.request(requestContentType.getMediaType());
        return invocationBuilder.cookie(cookie).get();
    }

    public static Response postRequest(String URL, Cookie cookie, ContentType requestContentType, Entity entity ) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        Invocation.Builder invocationBuilder =  target.request(requestContentType.getMediaType());
        return invocationBuilder.cookie(cookie).post(entity);
    }

    public static Response putRequest(String URL, Cookie cookie, ContentType requestContentType, Entity entity ) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        Invocation.Builder invocationBuilder =  target.request(requestContentType.getMediaType());
        return invocationBuilder.cookie(cookie).put(entity);
    }

    public static Response deleteRequest(String URL, Cookie cookie, ContentType requestContentType) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(URL);
        Invocation.Builder invocationBuilder =  target.request(requestContentType.getMediaType());
        return invocationBuilder.cookie(cookie).delete();
    }
}
