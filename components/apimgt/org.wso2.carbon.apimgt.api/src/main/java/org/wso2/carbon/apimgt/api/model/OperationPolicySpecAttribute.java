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

import java.util.Objects;

public class OperationPolicySpecAttribute {

    private String name;
    private String displayName;
    private String description;
    private String validationRegex;
    private String type;
    private boolean required;

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

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public boolean isRequired() {

        return required;
    }

    public void setRequired(boolean required) {

        this.required = required;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof OperationPolicySpecAttribute)) return false;
        OperationPolicySpecAttribute that = (OperationPolicySpecAttribute) o;
        return required == that.required &&
                name.equals(that.name) &&
                Objects.equals(displayName, that.displayName) &&
                Objects.equals(description, that.description) &&
                Objects.equals(validationRegex, that.validationRegex) &&
                type.equals(that.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, displayName, description, validationRegex, type, required);
    }
}
