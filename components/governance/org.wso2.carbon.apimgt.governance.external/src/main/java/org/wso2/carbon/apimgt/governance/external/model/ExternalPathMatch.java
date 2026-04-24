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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Match result for a simple JSON-path lookup.
 */
public class ExternalPathMatch {

    private final JsonNode value;
    private final String path;

    public ExternalPathMatch(JsonNode value, String path) {

        this.value = value;
        this.path = path;
    }

    public JsonNode getValue() {

        return value;
    }

    public String getPath() {

        return path;
    }
}
