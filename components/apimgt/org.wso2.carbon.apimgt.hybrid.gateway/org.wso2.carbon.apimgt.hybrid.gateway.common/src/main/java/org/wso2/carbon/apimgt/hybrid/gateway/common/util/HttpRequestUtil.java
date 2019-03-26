/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.common.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Utility methods for HTTP requests
 */
public class HttpRequestUtil {

    private static final Log log = LogFactory.getLog(HttpRequestUtil.class);
    private static final String NOT_FOUND_ERROR_MSG =
            "Failed with HTTP error code : " + HttpURLConnection.HTTP_NOT_FOUND + " (Not Found). URI is incorrect.";
    private static final String AUTH_ERROR_MSG =
            "Failed with HTTP error code : " + HttpURLConnection.HTTP_UNAUTHORIZED +
                    " (Unauthorized). Credentials used are incorrect.";

    private static final String AUTH_FORBIDDEN_ERROR_MSG =
            "Failed with HTTP error code : " + HttpURLConnection.HTTP_UNAUTHORIZED +
                    " (Unauthorized). User does not have permissions to perform the action.";

    private HttpRequestUtil() {
    }

    /**
     * Executes the HTTPMethod with retry.
     *
     * @param httpClient     HTTPClient
     * @param httpMethod     HTTPMethod
     * @param retryCount No of retries
     * @return response. it will return an empty string if response body is null
     * @throws OnPremiseGatewayException throws {@link OnPremiseGatewayException}
     */
    public static String executeHTTPMethodWithRetry(HttpClient httpClient, HttpRequestBase httpMethod, int retryCount)
            throws OnPremiseGatewayException {

        String result = OnPremiseGatewayConstants.EMPTY_STRING;
        HttpResponse response;
        int executionCount = 0;
        String methodName = httpMethod.getMethod();
        String uri = getURI(httpMethod);

        //Add an unique identifier as a custom header for distinguishing requests from different micro gateways.
        String token = ConfigManager.getConfigManager()
                .getProperty(OnPremiseGatewayConstants.API_REQUEST_UNIQUE_IDENTIFIER);
        if (StringUtils.isNotBlank(token) && !(OnPremiseGatewayConstants.API_REQUEST_UNIQUE_IDENTIFIER_HOLDER
                .equals(token))) {
            if (log.isDebugEnabled()) {
                log.debug("Adding unique identifier as an header to the http " + methodName + " request.");
            }
            httpMethod.addHeader(OnPremiseGatewayConstants.APT_REQUEST_TOKEN_HEADER, token);
        }
        do {
            try {
                executionCount++;
                response = httpClient.execute(httpMethod);
                if (log.isDebugEnabled()) {
                    log.debug(
                            "HTTP response code for the " + methodName + " request to URL: " + uri + " is " + response);
                }
                result = handleResponse(response, methodName, true, executionCount, retryCount, uri);
                if (!OnPremiseGatewayConstants.EMPTY_STRING.equals(result)) {
                    return result;
                }
            } catch (IOException e) {
                handleExceptionWithRetry(executionCount, retryCount, methodName, uri, e);
            } finally {
                httpMethod.releaseConnection();
            }
        } while (executionCount < retryCount);
        return result;
    }

    /**
     * Executes HTTPMethod without retry
     *
     * @param httpClient HTTPClient
     * @param httpMethod HTTPMethod
     * @return response. it will return an empty string if response body is null
     * @throws OnPremiseGatewayException throws {@link OnPremiseGatewayException}
     */
    public static String executeHTTPMethod(HttpClient httpClient, HttpRequestBase httpMethod)
            throws OnPremiseGatewayException {

        String result;
        HttpResponse response;
        String uri = getURI(httpMethod);
        String methodName = httpMethod.getMethod();

        //Add an unique identifier as an custom header for distinguishing requests from different micro gateways.
        String token = ConfigManager.getConfigManager()
                .getProperty(OnPremiseGatewayConstants.API_REQUEST_UNIQUE_IDENTIFIER);
        if (StringUtils.isNotBlank(token) && !(OnPremiseGatewayConstants.API_REQUEST_UNIQUE_IDENTIFIER_HOLDER
                .equals(token))) {
            if (log.isDebugEnabled()) {
                log.debug("Adding unique identifier as an header to the http " + methodName + " request.");
            }
            httpMethod.addHeader(OnPremiseGatewayConstants.APT_REQUEST_TOKEN_HEADER, token);
        }
        try {
            response = httpClient.execute(httpMethod);
            if (log.isDebugEnabled()) {
                log.debug("HTTP response code for the " + methodName + " request: " + uri + " is " + response);
            }
            result = handleResponse(response, methodName, false, 0, 0, uri);
        } catch (IOException e) {
            throw new OnPremiseGatewayException(methodName + " request failed for URI: " + uri, e);
        } finally {
            httpMethod.releaseConnection();
        }
        return result;
    }

    private static String handleResponse(HttpResponse response, String methodName, boolean retry, int executionCount,
                                         int retryCount, String uri) throws OnPremiseGatewayException {
        switch (response.getStatusLine().getStatusCode()) {
            case HttpStatus.SC_OK:
                return handleSuccessCase(response);

            case HttpStatus.SC_CREATED:
                return handleSuccessCase(response);

            case HttpStatus.SC_ACCEPTED:
                return handleSuccessCase(response);

            case HttpStatus.SC_NOT_FOUND:
                throw new OnPremiseGatewayException(NOT_FOUND_ERROR_MSG);

            case HttpStatus.SC_UNAUTHORIZED:
                throw new OnPremiseGatewayException(AUTH_ERROR_MSG);

            case HttpStatus.SC_FORBIDDEN:
                throw new OnPremiseGatewayException(AUTH_FORBIDDEN_ERROR_MSG);

            default:
                if (retry) {
                    handleDefaultCaseWithRetry(executionCount, response, retryCount, methodName, uri);
                } else {
                    throw new OnPremiseGatewayException(
                            methodName + " request failed for URI: " + uri + " with HTTP error code : " + response);
                }
        }
        return OnPremiseGatewayConstants.EMPTY_STRING;
    }

    /**
     * Handles the default HTTP 2XX response
     *
     * @param response http response
     * @return validates the response
     * @throws OnPremiseGatewayException throws {@link OnPremiseGatewayException}
     */
    private static String handleSuccessCase(HttpResponse response) throws OnPremiseGatewayException {
        HttpEntity entity = response.getEntity();
        try {
            return entity != null ? EntityUtils.toString(entity, OnPremiseGatewayConstants.DEFAULT_CHARSET) : null;
        } catch (IOException e) {
            throw new OnPremiseGatewayException("Error occurred constructing the response string");
        }
    }

    /**
     * Handles the exceptions with retries
     *
     * @param executionCount number of retries configured
     * @param retryCount     current retry
     * @param methodName     HTTP method name
     * @param uri            URI used
     * @param ex             exception thrown
     * @throws OnPremiseGatewayException throws {@link OnPremiseGatewayException}
     */
    private static void handleExceptionWithRetry(int executionCount, int retryCount, String methodName, String uri,
                                                 Exception ex) throws OnPremiseGatewayException {
        if (executionCount >= retryCount) {
            throw new OnPremiseGatewayException(methodName + " request failed for the maximum no. of attempts(" +
                    retryCount + ") for URL: " + uri, ex);
        } else {
            log.warn(methodName + " request failed for URL: " + uri + " with exception : " + ex.getMessage() +
                    ". Retry attempt: " + executionCount + "/" + retryCount);
        }
    }

    /**
     * Handles the default case of the switch cases
     *
     * @param executionCount number of retries configured
     * @param response       http response
     * @param retryCount     current retry
     * @param methodName     HTTP method name
     * @param uri            URI used
     * @throws OnPremiseGatewayException throws {@link OnPremiseGatewayException}
     */
    private static void handleDefaultCaseWithRetry(int executionCount, HttpResponse response, int retryCount,
                                                   String methodName, String uri) throws OnPremiseGatewayException {
        if (executionCount >= retryCount) {
            throw new OnPremiseGatewayException(methodName + " request failed for the " + retryCount +
                    " attempt for URI: " + uri + " with HTTP error code: " + response.getStatusLine().getStatusCode());
        } else {
            log.warn(methodName + " request failed for URI: " + uri + " with HTTP error code: " +
                    response.getStatusLine().getStatusCode() + ". Retry: " + executionCount + "/" + retryCount);
        }
    }

    /**
     * Get URI from the http method
     *
     * @param httpMethod http method
     * @return URI string
     */
    private static String getURI(HttpRequestBase httpMethod) {
        return httpMethod.getURI().toString();
    }
}
