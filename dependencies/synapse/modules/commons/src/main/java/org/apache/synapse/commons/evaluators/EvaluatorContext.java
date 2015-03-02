/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.commons.evaluators;

import org.apache.axis2.transport.http.util.URIEncoderDecoder;
import org.apache.axis2.context.MessageContext;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the information about the HTTP request. Created on per request basis and
 * passed to each and every evaluator.
 */
public class EvaluatorContext {

    private String url;
    private Map<String, String> headers;
    private Map<String, String> params;
    private MessageContext messageContext;
    private Map<String,Object> properties;

    /**
     * Creates the Evalutor context with the URL and the set of HTTP headers
     * @param url url
     * @param headers HTTP header as a Name, Value map
     */
    public EvaluatorContext(String url, Map<String, String> headers) {
        this.url = url;
        this.headers = headers;
    }

    /**
     * Get the complete URL
     * @return URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get all the HTTP headers
     * @return all the HTTP headers as name value pairs
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Get all the HTTP parameters
     * @return all the HTTP parameter as Name Value pairs
     */
    public Map<String, String> getParams() {
        return params;
    }

    /**
     * Return the value of a HTTP parameter
     * @param name name of the parameter
     * @return value of the parameter
     * @throws UnsupportedEncodingException if cannot decode the URLs
     */
    public String getParam(String name) throws UnsupportedEncodingException {
        if (params == null) {
            // build the params
            params = new HashMap<String, String>();

            int i = url.indexOf("?");
            if (i > -1) {
                String queryString = url.substring(i + 1);

                if (queryString != null && !queryString.equals("")) {
                    String httpParams[] = queryString.split("&");

                    if (httpParams == null || httpParams.length == 0) {
                        return "";
                    }

                    for (String param : httpParams) {
                        String temp[] = param.split("=");
                        if (temp != null && temp.length >= 2) {
                            params.put(temp[0], URIEncoderDecoder.decode(temp[1]));
                        }
                    }
                }
            }
        }
        return params.get(name);
    }

    /**
     * Get the HTTP header value for the Header name
     * @param name name of the header
     * @return header value
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Get the message context associated with this evaluator context
     *
     * @return an Axis2 MessageContext instance or null
     */
    public MessageContext getMessageContext() {
        return messageContext;
    }

    /**
     * Get the value of the named property
     *
     * @param name Name of the property
     * @return A string property value or null
     */
    public Object getProperty(String name) {
        if (properties != null) {
            return properties.get(name);
        }
        return null;
    }

    /**
     * Set the URL
     * @param url to be set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Set the headers
     * @param headers as a HeaderName, HeaderValue pair map
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Set all the HTTP URL parameters
     * @param params as a ParameterName, ParameterValue pair map
     */
    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    /**
     * Set the current Axis2 MessageContext to this evaluator context
     *
     * @param messageContext an Axis2 MessageContext object
     */
    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    /**
     * Associate a set of properties with this evaluator context
     *
     * @param properties a Properties map
     */
    public void setProperties(Map<String,Object> properties) {
        this.properties = properties;
    }
}
