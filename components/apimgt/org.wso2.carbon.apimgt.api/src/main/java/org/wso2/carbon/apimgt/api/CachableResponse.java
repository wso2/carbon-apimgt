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

public class CachableResponse implements Serializable {
        /**
         * This holds the reference to the response for json
         */
        private byte[] responsePayload = null;

        /**
         * This holds the hash value of the request payload which is calculated form the specified DigestGenerator, and is
         * used to index the cached response
         */
        private String requestHash;

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
         * States whether response is json or not
         */
        private boolean json;

        /**
         * The maximum size of the messages to be cached. This is specified in bytes.
         */
        private int maxMessageSize = -1;

        /**
         * The protocol type used in caching.
         */
        private String protocolType = APIConstants.AIAPIConstants.HTTP_PROTOCOL_TYPE;

        /**
         * This is used to store the http method of the request.
         */
        private String httpMethod = null;

        /**
         * The compiled pattern for the regex of the responseCodes.
         */
        private String responseCodePattern;

        /**
         * This is used to store the originated time of the response.
         */
        private long responseFetchedTime;

        /**
         * This specifies whether the mediator should honor cache-control header.
         */
        private boolean cacheControlEnabled;

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
         * This method gives the hash value of the request payload stored in the cache
         *
         * @return String hash of the request payload
         */
        public String getRequestHash() {
        return requestHash;
    }

        /**
         * This method sets the hash of the request to the cache
         *
         * @param requestHash - hash of the request payload to be set as an String
         */
        public void setRequestHash(String requestHash) {
        this.requestHash = requestHash;
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
         * @return whether the response is json or not
         */
        public boolean isJson() {
        return json;
    }

        /**
         * Sets whether the response content-type is json or not
         *
         * @param json whether the response content-type is json or not
         */
        public void setJson(boolean json) {
        this.json = json;
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
        public Object getStatusReason() {
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
         * @return the protocol type of the messages
         */
        public String getProtocolType() {
        return protocolType;
    }

        /**
         * This method sets protocolType of the messages.
         *
         * @param protocolType protocol type of the messages.
         */
        public void setProtocolType(String protocolType) {
        this.protocolType = protocolType;
    }

        /**
         * @return The regex expression of the HTTP response code of the messages to be cached
         */
        public Pattern getResponseCodePattern() {
        return Pattern.compile(responseCodePattern);
    }

        /**
         * This method sets the response codes that needs to be cached.
         *
         * @param responseCodePattern the response codes pattern to be cached in regex form.
         */
        public void setResponseCodePattern(String responseCodePattern) {
        this.responseCodePattern = responseCodePattern;
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
         * @return the http method of the request
         */
        public String getHttpMethod() {
        return httpMethod;
    }

        /**
         * @param httpMethod the http method of the request
         */
        public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
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
         * This method returns whether cache-control is enabled or not.
         *
         * @return whether cache-control is enabled or not.
         */
        public boolean isCacheControlEnabled() {
        return cacheControlEnabled;
    }

        /**
         * This method sets whether cache-control is enabled or not.
         *
         * @param cacheControlEnabled whether cache-control is enabled or not.
         */
        public void setCacheControlEnabled(boolean cacheControlEnabled) {
        this.cacheControlEnabled = cacheControlEnabled;
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
