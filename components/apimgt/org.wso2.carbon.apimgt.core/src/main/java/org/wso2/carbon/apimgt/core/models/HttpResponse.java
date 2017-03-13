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
 * /
 */

package org.wso2.carbon.apimgt.core.models;

import java.util.List;
import java.util.Map;

/**
 * Class to represents basic Http response properties.
 */
public class HttpResponse {

    private int responseCode;
    private String responseMessage;
    private Map<String, List<String>> headerFields;
    private String results;

    /**
     * Constructor.
     */
    public HttpResponse() {

    }

    /**
     * To get response code.
     *
     * @return Response code (int value)
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * To set a response code.
     *
     * @param responseCode Response code (int value)
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * To get response message.
     *
     * @return Response message
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * To set a response message.
     *
     * @param responseMessage Response message
     */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * To get all the header fields as a {@code Map<String, List<String>>}.
     *
     * @return Map of header fields
     */
    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }

    /**
     * To set header fields.
     *
     * @param headerFields Map of header fields
     */
    public void setHeaderFields(Map<String, List<String>> headerFields) {
        this.headerFields = headerFields;
    }

    /**
     * To get http results.
     *
     * @return Results String
     */
    public String getResults() {
        return results;
    }

    /**
     * To set http results.
     *
     * @param results Results String
     */
    public void setResults(String results) {
        this.results = results;
    }
}
