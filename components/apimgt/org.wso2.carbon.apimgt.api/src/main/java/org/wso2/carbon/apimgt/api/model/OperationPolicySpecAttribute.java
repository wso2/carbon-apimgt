/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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


package org.wso2.carbon.apimgt.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OperationPolicySpecAttribute {

    public enum AttributeType {
        String,
        Integer,
        Boolean,
        Enum,
        Map
    }

    private String name = null;
    private String displayName = null;
    private String description = null;
    private String validationRegex = null;
    private AttributeType type = null;
    private String defaultValue;
    private List<String> allowedValues = new ArrayList<>();
    private boolean required = false;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getValidationRegex() {

        return validationRegex;
    }

    public void setValidationRegex(String validationRegex) {

        this.validationRegex = validationRegex;
    }

    public AttributeType getType() {

        return type;
    }

    public void setType(AttributeType type) {

        this.type = type;
    }

    public String getDefaultValue() {

        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {

        this.defaultValue = defaultValue;
    }

    public List<String> getAllowedValues() {

        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {

        this.allowedValues = allowedValues;
    }

    public boolean isRequired() {

        return required;
    }

    public void setRequired(boolean required) {

        this.required = required;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (!(o instanceof OperationPolicySpecAttribute))
            return false;
        OperationPolicySpecAttribute policySpecAttributeObj = (OperationPolicySpecAttribute) o;
        return required == policySpecAttributeObj.required && name.equals(policySpecAttributeObj.name)
                && Objects.equals(displayName, policySpecAttributeObj.displayName) && Objects.equals(description,
                policySpecAttributeObj.description) && Objects.equals(validationRegex,
                policySpecAttributeObj.validationRegex) && type.equals(policySpecAttributeObj.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, displayName, description, validationRegex, type, required);
    }
}
