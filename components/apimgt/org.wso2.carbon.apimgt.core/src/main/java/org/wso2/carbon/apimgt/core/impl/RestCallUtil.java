package org.wso2.carbon.apimgt.core.impl;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.core.models.ContentType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Entity;

/**
 * Utility class which handles basics of a rest call.
 * Specifically used in this project to enable inter-cloud rest communication.
 */
public class RestCallUtil {

    private RestCallUtil() {

    }

    public static HttpResponse loginRequest(URI uri, String userName, String password, ContentType
            requestContentType) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        if (userName == null) {
            throw new IllegalArgumentException("UserName must not be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }

        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod("POST");
            if (requestContentType != null) {
                httpConnection.setRequestProperty("Accept", requestContentType.getMediaType());
            }

            JSONObject loginInfoJsonObj = new JSONObject();
            loginInfoJsonObj.put("userName", userName);
            loginInfoJsonObj.put("password", password);

            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(loginInfoJsonObj.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            return getResponse(httpConnection);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    public static HttpResponse rsaSignedFetchUserRequest(URI uri, String userName,
                                                         String userTenantDomain, String rsaSignedToken,
                                                         ContentType requestContentType) throws IOException {
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

        HttpURLConnection httpConnection = null;
        try {
            JSONObject loginInfoJsonObj = new JSONObject();
            loginInfoJsonObj.put("userName", userName);
            loginInfoJsonObj.put("userTenantDomain", userTenantDomain);

            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("rsaSignedToken", rsaSignedToken);
            if (requestContentType != null) {
                httpConnection.setRequestProperty("Accept", requestContentType.getMediaType());
            }

            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(loginInfoJsonObj.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            return getResponse(httpConnection);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }

    }

    public static List<String> captureCookies(HttpResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("The response must not be null");
        }
        List<String> cookies = null;
        Map<String, List<String>> headerFields = response.getHeaderFields();
        Set<String> headerFieldsSet = headerFields.keySet();
        String headerFieldKey = headerFieldsSet.stream().filter("Set-Cookie"::equalsIgnoreCase).findAny().orElse(null);
        if (headerFieldKey != null) {
            cookies = headerFields.get(headerFieldKey);
        }
        return cookies;
    }

    public static HttpResponse getRequest(URI uri, ContentType requestContentType, List<String> cookies)
            throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod("GET");
            if (requestContentType != null) {
                httpConnection.setRequestProperty("Accept", requestContentType.getMediaType());
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    httpConnection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
            }
            return getResponse(httpConnection);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    public static HttpResponse postRequest(URI uri, ContentType requestContentType, List<String> cookies,
                                           Entity entity) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }

        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod("POST");
            if (requestContentType != null) {
                httpConnection.setRequestProperty("Accept", requestContentType.getMediaType());
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    httpConnection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
            }

            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(entity.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            return getResponse(httpConnection);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    public static HttpResponse putRequest(URI uri, ContentType requestContentType, List<String> cookies,
                                          Entity entity) throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod("PUT");
            if (requestContentType != null) {
                httpConnection.setRequestProperty("Accept", requestContentType.getMediaType());
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    httpConnection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
            }

            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(entity.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            return getResponse(httpConnection);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    public static HttpResponse deleteRequest(URI uri, ContentType requestContentType, List<String> cookies)
            throws IOException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }

        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod("DELETE");
            if (requestContentType != null) {
                httpConnection.setRequestProperty("Accept", requestContentType.getMediaType());
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    httpConnection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
                }
            }

            return getResponse(httpConnection);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    private static HttpResponse getResponse(HttpURLConnection httpConnection) throws IOException {
        HttpResponse response = new HttpResponse();
        try (BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(httpConnection.getInputStream(),
                StandardCharsets.UTF_8))) {
            StringBuilder results = new StringBuilder();
            String line;
            while ((line = responseBuffer.readLine()) != null) {
                results.append(line).append("\n");
            }

            response.setResponseCode(httpConnection.getResponseCode());
            response.setResponseMessage(httpConnection.getResponseMessage());
            response.setHeaderFields(httpConnection.getHeaderFields());
            response.setResults(results.toString());
        }
        return response;
    }
}
