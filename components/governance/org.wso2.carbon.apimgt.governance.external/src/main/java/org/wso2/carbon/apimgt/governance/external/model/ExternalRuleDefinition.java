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

package org.wso2.carbon.apimgt.governance.external.model;

import java.util.Map;

/**
 * Model for a single external governance rule.
 */
public class ExternalRuleDefinition {

    private String serviceUrl;
    private String targetPath;
    private String severity;
    private Integer timeout;
    private Integer retry;
    private Map<String, Object> headers;
    private ExternalRequestPayload payload;
    private ExternalResponseDefinition response;
    private String description;
    private String message;

    public String getServiceUrl() {

        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {

        this.serviceUrl = serviceUrl;
    }

    public String getTargetPath() {

        return targetPath;
    }

    public void setTargetPath(String targetPath) {

        this.targetPath = targetPath;
    }

    public String getSeverity() {

        return severity;
    }

    public void setSeverity(String severity) {

        this.severity = severity;
    }

    public Integer getTimeout() {

        return timeout;
    }

    public void setTimeout(Integer timeout) {

        this.timeout = timeout;
    }

    public Integer getRetry() {

        return retry;
    }

    public void setRetry(Integer retry) {

        this.retry = retry;
    }

    public Map<String, Object> getHeaders() {

        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {

        this.headers = headers;
    }

    public ExternalRequestPayload getPayload() {

        return payload;
    }

    public void setPayload(ExternalRequestPayload payload) {

        this.payload = payload;
    }

    public ExternalResponseDefinition getResponse() {

        return response;
    }

    public void setResponse(ExternalResponseDefinition response) {

        this.response = response;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }
}
