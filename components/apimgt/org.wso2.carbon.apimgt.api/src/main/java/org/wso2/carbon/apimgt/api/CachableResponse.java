/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.api;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class represents a cached response in the API Management system.
 * It contains the response payload, timeout, HTTP headers, status code,
 * and other relevant information for caching purposes.
 */
public class CachableResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * This holds the reference to the response for json
     */
    private byte[] responsePayload = null;

    /**
     * This holds the timeout period of the cached response which will be used at the next refresh time in order to
     * generate the expireTimeMillis
     */
    private long timeout;

    /**
     * This holds the HTTP Header Properties of the response.
     */
    private Map<String, Object> headerProperties;

    /**
     * The HTTP status code number of the response
     */
    private String statusCode;

    /**
     * The HTTP response's Reason- Phrase that is sent by the backend.
     */
    private String statusReason;

    /**
     * The maximum size of the messages to be cached. This is specified in bytes.
     */
    private int maxMessageSize = -1;

    /**
     * The compiled pattern for the regex of the responseCodes.
     */
    private String responseCodePattern;
    private Pattern compiledPattern;

    /**
     * This is used to store the originated time of the response.
     */
    private long responseFetchedTime;

    /**
     * This specifies whether an age header needs to be included in the cached response.
     */
    private boolean addAgeHeaderEnabled;

    /**
     * Sets the responsePayload and the headerProperties to null
     */
    public void clean() {
        responsePayload = null;
        headerProperties = null;
    }

    /**
     * This method gives the cached response payload for json as a byte array
     *
     * @return byte[] representing the cached response payload for json
     */
    public byte[] getResponsePayload() {
        return responsePayload;
    }

    /**
     * This method sets the response payload to the cache as a byte array
     *
     * @param responsePayload - response payload to be stored in to the cache as a byte array
     */
    public void setResponsePayload(byte[] responsePayload) {
        this.responsePayload = responsePayload;
    }

    /**
     * This method gives the timeout period in milliseconds
     *
     * @return timeout in milliseconds
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * This method sets the timeout period as milliseconds
     *
     * @param timeout - millisecond timeout period to be set
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * This method gives the HTTP Header Properties of the response
     *
     * @return Map<String, Object> representing the HTTP Header Properties
     */
    public Map<String, Object> getHeaderProperties() {
        return headerProperties;
    }

    /**
     * This method sets the HTTP Header Properties of the response
     *
     * @param headerProperties HTTP Header Properties to be stored in to cache as a map
     */
    public void setHeaderProperties(Map<String, Object> headerProperties) {
        this.headerProperties = headerProperties;
    }

    /**
     * @return HTTP status code number of the response
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the HTTP status code number of the response
     *
     * @param statusCode HTTP status code number of the response
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @return HTTP response's Reason- Phrase that is sent by the backend.
     */
    public String getStatusReason() {
        return statusReason;
    }

    /**
     * Sets the HTTP response's Reason-Phrase that is sent by the backend.
     *
     * @param statusReason HTTP response's Reason-Phrase that is sent by the backend.
     */
    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    /**
     * @return The regex expression of the HTTP response code of the messages to be cached
     */
    public Pattern getResponseCodePattern() {
        if (compiledPattern == null && responseCodePattern != null) {
            compiledPattern = Pattern.compile(responseCodePattern);
        }
        return compiledPattern;
    }

    /**
     * This method sets the response codes that needs to be cached.
     *
     * @param responseCodePattern the response codes pattern to be cached in regex form.
     */
    public void setResponseCodePattern(String responseCodePattern) {
        this.responseCodePattern = responseCodePattern;
        this.compiledPattern = null;
    }

    /**
     * This method gives the maximum size of the messages to be cached in bytes.
     *
     * @return maximum size of the messages to be cached in bytes.
     */
    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    /**
     * This method sets the maximum size of the messages to be cached in bytes.
     *
     * @param maxMessageSize maximum size of the messages to be set in bytes.
     */
    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    /**
     * This method returns the originated time of the response.
     *
     * @return the originated time of the response.
     */
    public long getResponseFetchedTime() {
        return responseFetchedTime;
    }

    /**
     * This method sets the originated time of the response.
     *
     * @param responseFetchedTime originated time of the response.
     */
    public void setResponseFetchedTime(long responseFetchedTime) {
        this.responseFetchedTime = responseFetchedTime;
    }

    /**
     * This method returns whether an Age header needs to be included or not.
     *
     * @return whether an Age header needs to be included or not.
     */
    public boolean isAddAgeHeaderEnabled() {
        return addAgeHeaderEnabled;
    }

    /**
     * This method sets whether an Age header needs to be included or not.
     *
     * @param addAgeHeaderEnabled whether an Age header needs to be included or not.
     */
    public void setAddAgeHeaderEnabled(boolean addAgeHeaderEnabled) {
        this.addAgeHeaderEnabled = addAgeHeaderEnabled;
    }
}
