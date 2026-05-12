/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.external.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.external.model.ExternalEvaluationContext;
import org.wso2.carbon.apimgt.governance.external.model.ExternalRuleDefinition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP client for external governance rule validation calls.
 */
public class ExternalServiceClient {

    private static final Log log = LogFactory.getLog(ExternalServiceClient.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final int CONNECT_TIMEOUT_MILLIS = 5000;
    private static final int READ_TIMEOUT_MILLIS = 10000;
    private static final int DEFAULT_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY_MILLIS = 500L;

    public JsonNode invoke(ExternalRuleDefinition ruleDefinition, ExternalEvaluationContext evaluationContext,
                           Object requestBody, Map<String, String> headers) throws ExternalServiceException {

        String method = resolveMethod(ruleDefinition);
        int timeoutMillis = resolveTimeout(ruleDefinition);
        int retryAttempts = resolveRetryAttempts(ruleDefinition);
        Map<String, String> effectiveHeaders = new LinkedHashMap<>();
        if (headers != null) {
            effectiveHeaders.putAll(headers);
        }
        if (!containsHeader(effectiveHeaders, "Content-Type")) {
            effectiveHeaders.put("Content-Type", "application/json");
        }
        if (log.isDebugEnabled()) {
            log.debug("Prepared external validation headers for target "
                    + evaluationContext.getTargetIdentifier() + ": "
                    + maskSensitiveHeaders(effectiveHeaders));
        }

        String requestPayload;
        try {
            requestPayload = JSON_MAPPER.writeValueAsString(requestBody);
        } catch (IOException e) {
            throw new ExternalServiceException("Failed to serialize request body for target `"
                    + evaluationContext.getTargetIdentifier() + "`", e);
        }

        long retryDelay = INITIAL_RETRY_DELAY_MILLIS;
        ExternalServiceException lastFailure = null;
        for (int attempt = 1; attempt <= retryAttempts; attempt++) {
            long attemptStartTime = System.currentTimeMillis();
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Calling external validation service. ruleTarget="
                            + evaluationContext.getTargetIdentifier() + ", serviceUrl="
                            + ruleDefinition.getServiceUrl() + ", attempt=" + attempt
                            + "/" + retryAttempts + ", timeout=" + timeoutMillis + "ms");
                }
                return executeHttpCall(ruleDefinition.getServiceUrl(), method, requestPayload, effectiveHeaders,
                        timeoutMillis);
            } catch (ExternalServiceException e) {
                lastFailure = e;
                if (e.isAuthenticationFailure()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Skipping retries for target " + evaluationContext.getTargetIdentifier()
                                + " because the external service returned an authentication failure.");
                    }
                    break;
                }
                if (attempt >= retryAttempts) {
                    break;
                }
                long waitBeforeRetryMillis = resolveWaitBeforeRetry(attemptStartTime, timeoutMillis, retryDelay);
                if (log.isDebugEnabled()) {
                    log.debug("External validation call failed on attempt " + attempt + " for target "
                            + evaluationContext.getTargetIdentifier() + ". Retrying in "
                            + waitBeforeRetryMillis + "ms. Cause: " + e.getMessage());
                }
                sleepBeforeRetry(waitBeforeRetryMillis, e);
                retryDelay *= 2;
            }
        }

        throw lastFailure != null ? lastFailure
                : new ExternalServiceException("External validation request failed for target `"
                + evaluationContext.getTargetIdentifier() + "`");
    }

    private JsonNode executeHttpCall(String serviceUrl, String method, String requestPayload,
                                     Map<String, String> headers, int timeoutMillis)
            throws ExternalServiceException {

        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(serviceUrl).openConnection();
            connection.setConnectTimeout(timeoutMillis);
            connection.setReadTimeout(timeoutMillis);
            connection.setRequestMethod(method);
            connection.setDoInput(true);
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                connection.setRequestProperty(headerEntry.getKey(), headerEntry.getValue());
            }

            if (requiresRequestBody(method)) {
                connection.setDoOutput(true);
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(requestPayload.getBytes(StandardCharsets.UTF_8));
                }
            }

            int statusCode = connection.getResponseCode();
            String responseBody = readResponseBody(statusCode, connection);
            if (statusCode < 200 || statusCode >= 300) {
                throw new ExternalServiceException("External validation service returned HTTP " + statusCode
                        + (responseBody != null && !responseBody.isEmpty() ? ": " + responseBody : ""),
                        statusCode);
            }
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new ExternalServiceException("External validation service returned an empty response body");
            }

            if (log.isDebugEnabled()) {
                log.debug("External validation service returned HTTP " + statusCode);
            }
            return JSON_MAPPER.readTree(responseBody);
        } catch (SocketTimeoutException e) {
            throw new ExternalServiceException("External validation service timed out", e);
        } catch (IOException e) {
            throw new ExternalServiceException("External validation service call failed: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponseBody(int statusCode, HttpURLConnection connection) throws IOException {

        InputStream responseStream = statusCode >= 200 && statusCode < 300
                ? connection.getInputStream() : connection.getErrorStream();
        if (responseStream == null) {
            return null;
        }

        StringBuilder responseBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                responseBuilder.append(line);
            }
        }
        return responseBuilder.toString();
    }

    private boolean containsHeader(Map<String, String> headers, String expectedHeader) {

        for (String headerName : headers.keySet()) {
            if (expectedHeader.equalsIgnoreCase(headerName)) {
                return true;
            }
        }
        return false;
    }

    private String maskSensitiveHeaders(Map<String, String> headers) {

        StringBuilder headerLog = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            if (!first) {
                headerLog.append(", ");
            }
            first = false;
            headerLog.append(headerEntry.getKey()).append("=");
            if (isSensitiveHeader(headerEntry.getKey())) {
                headerLog.append(maskHeaderValue(headerEntry.getValue()));
            } else {
                headerLog.append(headerEntry.getValue());
            }
        }
        headerLog.append("}");
        return headerLog.toString();
    }

    private boolean isSensitiveHeader(String headerName) {

        if (headerName == null) {
            return false;
        }
        String normalizedHeaderName = headerName.toLowerCase(Locale.ENGLISH);
        return normalizedHeaderName.contains("authorization")
                || normalizedHeaderName.contains("key")
                || normalizedHeaderName.contains("token");
    }

    private String maskHeaderValue(String headerValue) {

        if (headerValue == null) {
            return "null";
        }
        if (headerValue.length() <= 12) {
            return "***(" + headerValue.length() + " chars)";
        }
        return headerValue.substring(0, 6) + "..." + headerValue.substring(headerValue.length() - 4)
                + " (" + headerValue.length() + " chars)";
    }

    private boolean requiresRequestBody(String method) {

        return !("GET".equals(method) || "DELETE".equals(method));
    }

    private String resolveMethod(ExternalRuleDefinition ruleDefinition) {

        String method = ruleDefinition.getPayload() != null ? ruleDefinition.getPayload().getMethod() : null;
        if (method == null || method.trim().isEmpty()) {
            return "POST";
        }
        return method.trim().toUpperCase(Locale.ENGLISH);
    }

    private int resolveTimeout(ExternalRuleDefinition ruleDefinition) {

        if (ruleDefinition.getTimeout() != null && ruleDefinition.getTimeout() > 0) {
            return ruleDefinition.getTimeout();
        }
        return Math.max(CONNECT_TIMEOUT_MILLIS, READ_TIMEOUT_MILLIS);
    }

    private int resolveRetryAttempts(ExternalRuleDefinition ruleDefinition) {

        if (ruleDefinition.getRetry() != null && ruleDefinition.getRetry() > 0) {
            return ruleDefinition.getRetry();
        }
        return DEFAULT_RETRY_ATTEMPTS;
    }

    private long resolveWaitBeforeRetry(long attemptStartTime, int timeoutMillis, long retryDelay) {

        long elapsedTimeMillis = System.currentTimeMillis() - attemptStartTime;
        long remainingAttemptWindowMillis = timeoutMillis - elapsedTimeMillis;
        if (remainingAttemptWindowMillis > retryDelay) {
            if (log.isDebugEnabled()) {
                log.debug("Extending retry wait to honor configured timeout window. elapsed="
                        + elapsedTimeMillis + "ms, remaining=" + remainingAttemptWindowMillis + "ms");
            }
            return remainingAttemptWindowMillis;
        }
        return retryDelay;
    }

    private void sleepBeforeRetry(long retryDelay, ExternalServiceException failure)
            throws ExternalServiceException {

        try {
            Thread.sleep(retryDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("External validation retry was interrupted", failure);
        }
    }

    /**
     * Exception type for external service failures.
     */
    public static class ExternalServiceException extends Exception {

        private final Integer statusCode;

        public ExternalServiceException(String message) {

            super(message);
            this.statusCode = null;
        }

        public ExternalServiceException(String message, Throwable cause) {

            super(message, cause);
            this.statusCode = null;
        }

        public ExternalServiceException(String message, int statusCode) {

            super(message);
            this.statusCode = statusCode;
        }

        public boolean isAuthenticationFailure() {

            return Integer.valueOf(HttpURLConnection.HTTP_UNAUTHORIZED).equals(statusCode)
                    || Integer.valueOf(HttpURLConnection.HTTP_FORBIDDEN).equals(statusCode);
        }
    }
}
