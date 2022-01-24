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

public class OperationPolicySpecAttribute {

    private String attributeName;
    private String attributeDisplayName;
    private String attributeDescription;
    private String attributeValidationRegex;
    private String attributeType;
    private boolean required;

    public String getAttributeName() {

        return attributeName;
    }

    public void setAttributeName(String attributeName) {

        this.attributeName = attributeName;
    }

    public String getAttributeDisplayName() {

        return attributeDisplayName;
    }

    public void setAttributeDisplayName(String attributeDisplayName) {

        this.attributeDisplayName = attributeDisplayName;
    }

    public String getAttributeDescription() {

        return attributeDescription;
    }

    public void setAttributeDescription(String attributeDescription) {

        this.attributeDescription = attributeDescription;
    }

    public String getAttributeValidationRegex() {

        return attributeValidationRegex;
    }

    public void setAttributeValidationRegex(String attributeValidationRegex) {

        this.attributeValidationRegex = attributeValidationRegex;
    }

    public String getAttributeType() {

        return attributeType;
    }

    public void setAttributeType(String attributeType) {

        this.attributeType = attributeType;
    }

    public boolean isRequired() {

        return required;
    }

    public void setRequired(boolean required) {

        this.required = required;
    }
}
