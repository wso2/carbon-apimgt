/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.api.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceExceptionCodes;

public class DefaultRuleset {
    private String id;
    private String name;
    private String message;
    private String description;
    private String appliesTo;
    private String documentationLink;
    private String provider;
    private Object rulesetContent;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(String appliesTo) {
        this.appliesTo = appliesTo;
    }

    public String getDocumentationLink() {
        return documentationLink;
    }

    public void setDocumentationLink(String documentationLink) {
        this.documentationLink = documentationLink;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Object getRulesetContent() {
        return rulesetContent;
    }

    public void setRulesetContent(Object rulesetContent) {
        this.rulesetContent = rulesetContent;
    }

    public String getRulesetContentAsString() throws GovernanceException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Convert raw ruleset content to JSON string.
            return objectMapper.writeValueAsString(rulesetContent);
        } catch (JsonProcessingException e) {
            throw new GovernanceException(GovernanceExceptionCodes.ERROR_WHILE_LOADING_DEFAULT_RULESET_CONTENT);
        }
    }
}
