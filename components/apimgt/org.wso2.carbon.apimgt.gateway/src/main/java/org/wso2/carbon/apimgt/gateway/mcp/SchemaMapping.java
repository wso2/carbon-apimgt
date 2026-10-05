/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.gateway.mcp;

import java.util.ArrayList;
import java.util.List;

public class SchemaMapping {

    private List<String> pathParams = new ArrayList<>();
    private List<Param> queryParams = new ArrayList<>();
    private List<Param> headerParams = new ArrayList<>();
    private boolean hasBody;
    private String contentType;

    public List<String> getPathParams() {
        return pathParams;
    }

    public void setPathParams(List pathParams) {
        this.pathParams = pathParams;
    }

    public List<Param> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<Param> queryParams) {
        this.queryParams = queryParams;
    }

    public List<Param> getHeaderParams() {
        return headerParams;
    }

    public void setHeaderParams(List<Param> headerParams) {
        this.headerParams = headerParams;
    }

    public boolean isHasBody() {
        return hasBody;
    }

    public void setHasBody(boolean hasBody) {
        this.hasBody = hasBody;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
