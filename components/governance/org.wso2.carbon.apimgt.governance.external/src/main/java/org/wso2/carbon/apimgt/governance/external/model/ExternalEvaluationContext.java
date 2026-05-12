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
 * Context for one external rule evaluation.
 */
public class ExternalEvaluationContext {

    private final JsonNode targetNode;
    private final String targetPath;
    private final JsonNode valueNode;
    private final String valuePath;
    private final String targetIdentifier;

    public ExternalEvaluationContext(JsonNode targetNode, String targetPath, JsonNode valueNode, String valuePath,
                                     String targetIdentifier) {

        this.targetNode = targetNode;
        this.targetPath = targetPath;
        this.valueNode = valueNode;
        this.valuePath = valuePath;
        this.targetIdentifier = targetIdentifier;
    }

    public JsonNode getTargetNode() {

        return targetNode;
    }

    public String getTargetPath() {

        return targetPath;
    }

    public JsonNode getValueNode() {

        return valueNode;
    }

    public String getValuePath() {

        return valuePath;
    }

    public String getTargetIdentifier() {

        return targetIdentifier;
    }
}
