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
 * Request payload configuration for an external rule call.
 */
public class ExternalRequestPayload {

    private String method;
    private String contentPath;
    private Object template;
    private Map<String, Object> headers;

    public String getMethod() {

        return method;
    }

    public void setMethod(String method) {

        this.method = method;
    }

    public String getContentPath() {

        return contentPath;
    }

    public void setContentPath(String contentPath) {

        this.contentPath = contentPath;
    }

    public Object getTemplate() {

        return template;
    }

    public void setTemplate(Object template) {

        this.template = template;
    }

    public Map<String, Object> getHeaders() {

        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {

        this.headers = headers;
    }
}
