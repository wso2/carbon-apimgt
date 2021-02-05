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
package org.wso2.carbon.apimgt.gateway.extension.listener.model.dto;

import org.wso2.carbon.apimgt.gateway.extension.listener.PayloadHandler;

import java.io.InputStream;
import java.util.Map;

public class MsgInfoDTO {

    Map<String, String> headers;
    PayloadHandler payloadHandler;
    String electedResource;
    String httpMethod;
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

    public String getElectedResource() {

        return electedResource;
    }

    public void setElectedResource(String electedResource) {

        this.electedResource = electedResource;
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
}

