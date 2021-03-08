/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.common.gateway.dto;

import org.wso2.carbon.apimgt.common.gateway.extensionlistener.PayloadHandler;

import java.util.Map;

/**
 * Representation of Message Information of Request/Response.
 */
public class MsgInfoDTO {

    // Transport Headers
    Map<String, String> headers;
    // Reference object to consume payload if needed
    PayloadHandler payloadHandler;
    // invoked resource
    String resource;
    // resource template
    String electedResource;
    String httpMethod;
    // Unique Id to identify the message
    String messageId;

    public PayloadHandler getPayloadHandler() {

        return payloadHandler;
    }

    public void setPayloadHandler(PayloadHandler payloadHandler) {

        this.payloadHandler = payloadHandler;
    }

    public Map<String, String> getHeaders() {

        return headers;
    }

    public void setHeaders(Map<String, String> headers) {

        this.headers = headers;
    }

    public String getResource() {

        return resource;
    }

    public void setResource(String resource) {

        this.resource = resource;
    }

    public String getHttpMethod() {

        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {

        this.httpMethod = httpMethod;
    }

    public String getMessageId() {

        return messageId;
    }

    public void setMessageId(String messageId) {

        this.messageId = messageId;
    }

    public String getElectedResource() {

        return electedResource;
    }

    public void setElectedResource(String electedResource) {

        this.electedResource = electedResource;
    }
}

