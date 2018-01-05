/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.impl;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.core.api.RestCallUtil;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.HttpResponse;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

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
import javax.ws.rs.core.MediaType;

/**
 * Utility class which provides basic methods needed to make a RESTful call to a service.
 * Acts as a REST client.
 * Specifically used in this project to enable inter-cloud REST communication.
 */
public class RestCallUtilImpl implements RestCallUtil {

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse loginRequest(URI uri, String username, String password, MediaType acceptContentType)
            throws APIManagementException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }

        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod(APIMgtConstants.FunctionsConstants.POST);
            httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.CONTENT_TYPE,
                    MediaType.APPLICATION_JSON);
            httpConnection.setDoOutput(true);
            if (acceptContentType != null) {
                httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.ACCEPT,
                        acceptContentType.toString());
            }

            JSONObject loginInfoJsonObj = new JSONObject();
            loginInfoJsonObj.put(APIMgtConstants.FunctionsConstants.USERNAME, username);
            loginInfoJsonObj.put(APIMgtConstants.FunctionsConstants.PASSWORD, password);

            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(loginInfoJsonObj.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            return getResponse(httpConnection);
        } catch (IOException e) {
            throw new APIManagementException("Connection not established properly ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse rsaSignedFetchUserRequest(URI uri, String username,
                                                  String userTenantDomain, String rsaSignedToken,
                                                  MediaType acceptContentType) throws APIManagementException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        if (username == null) {
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
            loginInfoJsonObj.put(APIMgtConstants.FunctionsConstants.USERNAME, username);
            loginInfoJsonObj.put(APIMgtConstants.FunctionsConstants.USER_TENANT_DOMAIN, userTenantDomain);

            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod(APIMgtConstants.FunctionsConstants.POST);
            httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.CONTENT_TYPE,
                    MediaType.APPLICATION_JSON);
            httpConnection.setDoOutput(true);
            httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.RSA_SIGNED_TOKEN, rsaSignedToken);
            if (acceptContentType != null) {
                httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.ACCEPT,
                        acceptContentType.toString());
            }

            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(loginInfoJsonObj.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            return getResponse(httpConnection);
        } catch (IOException e) {
            throw new APIManagementException("Connection not established properly ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> captureCookies(HttpResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("The response must not be null");
        }
        List<String> cookies = null;
        Map<String, List<String>> headerFields = response.getHeaderFields();
        Set<String> headerFieldsSet = headerFields.keySet();
        String headerFieldKey = headerFieldsSet.stream().filter(
                APIMgtConstants.FunctionsConstants.SET_COOKIE::equalsIgnoreCase).findAny().orElse(null);
        if (headerFieldKey != null) {
            cookies = headerFields.get(headerFieldKey);
        }
        return cookies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse getRequest(URI uri, MediaType acceptContentType, List<String> cookies)
            throws APIManagementException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod(APIMgtConstants.FunctionsConstants.GET);
            httpConnection.setDoOutput(true);
            if (acceptContentType != null) {
                httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.ACCEPT,
                        acceptContentType.toString());
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    httpConnection.addRequestProperty(APIMgtConstants.FunctionsConstants.COOKIE,
                            cookie.split(";", 2)[0]);
                }
            }
            return getResponse(httpConnection);
        } catch (IOException e) {
            throw new APIManagementException("Connection not established properly ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse postRequest(URI uri, MediaType acceptContentType, List<String> cookies, Entity entity,
            MediaType payloadContentType, Map<String, String> headers) throws APIManagementException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        if (payloadContentType == null) {
            throw new IllegalArgumentException("Payload content type must not be null");
        }
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod(APIMgtConstants.FunctionsConstants.POST);
            httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.CONTENT_TYPE,
                    payloadContentType.toString());
            httpConnection.setDoOutput(true);
            if (acceptContentType != null) {
                httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.ACCEPT,
                        acceptContentType.toString());
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    httpConnection.addRequestProperty(APIMgtConstants.FunctionsConstants.COOKIE,
                            cookie.split(";", 2)[0]);
                }
            }

            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    httpConnection.addRequestProperty(header.getKey(), header.getValue());
                }
            }
            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(entity.getEntity().toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            return getResponse(httpConnection);
        } catch (IOException e) {
            throw new APIManagementException("Connection not established properly ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse putRequest(URI uri, MediaType acceptContentType, List<String> cookies,
                                   Entity entity, MediaType payloadContentType) throws APIManagementException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        if (payloadContentType == null) {
            throw new IllegalArgumentException("Payload content type must not be null");
        }
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod(APIMgtConstants.FunctionsConstants.PUT);
            httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.CONTENT_TYPE,
                    payloadContentType.toString());
            httpConnection.setDoOutput(true);
            if (acceptContentType != null) {
                httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.ACCEPT,
                        acceptContentType.toString());
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    httpConnection.addRequestProperty(APIMgtConstants.FunctionsConstants.COOKIE,
                            cookie.split(";", 2)[0]);
                }
            }

            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(entity.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            return getResponse(httpConnection);
        } catch (IOException e) {
            throw new APIManagementException("Connection not established properly ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpResponse deleteRequest(URI uri, MediaType acceptContentType, List<String> cookies)
            throws APIManagementException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }

        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod(APIMgtConstants.FunctionsConstants.DELETE);
            httpConnection.setDoOutput(true);
            if (acceptContentType != null) {
                httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.ACCEPT,
                        acceptContentType.toString());
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    httpConnection.addRequestProperty(APIMgtConstants.FunctionsConstants.COOKIE,
                            cookie.split(";", 2)[0]);
                }
            }

            return getResponse(httpConnection);
        } catch (IOException e) {
            throw new APIManagementException("Connection not established properly ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * To get a response from service.
     *
     * @param httpConnection Connection used to make the request
     * @return HttpResponse from service
     * @throws IOException In case of any failures, when trying to get the response from service
     */
    private HttpResponse getResponse(HttpURLConnection httpConnection) throws IOException {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(httpConnection.getResponseCode());
        response.setResponseMessage(httpConnection.getResponseMessage());
        if (response.getResponseCode() / 100 == 2) {
            try (BufferedReader responseBuffer =
                         new BufferedReader(new InputStreamReader(httpConnection.getInputStream(),
                                 StandardCharsets.UTF_8))) {
                StringBuilder results = new StringBuilder();
                String line;
                while ((line = responseBuffer.readLine()) != null) {
                    results.append(line).append("\n");
                }
                response.setHeaderFields(httpConnection.getHeaderFields());
                response.setResults(results.toString());
            }
        }
        return response;
    }
}
