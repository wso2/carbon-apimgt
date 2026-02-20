/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO that holds metadata for a constraint field.
 * Used to define fields that support validation constraints in Key Manager application configurations.
 */
public class ConstraintConfigDto {

    private String name;           // Matches the form field name (e.g., "application_access_token_expiry_time")
    private String label;          // UI Label (e.g., "Access Token Expiry")
    private String type;           // UI Type (e.g., "input", "select")
    private String tooltip;        // UI Tooltip
    private AppConfigConstraintType constraintType; // Suggested Validator (e.g., RANGE, REGEX, ENUM)
    private List values = new ArrayList<>();
    private boolean multiple;
    private Map<String, Object> defaultConstraints = new HashMap<>(); // Optional defaults

    public ConstraintConfigDto(String name, String type, List values, boolean multiple, String label,
                               String tooltip, AppConfigConstraintType constraintType, Map<String, Object> defaultConstraints) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.tooltip = tooltip;
        this.constraintType = constraintType;
        this.values = values;
        this.multiple = multiple;
        if (defaultConstraints != null) {
            this.defaultConstraints.putAll(defaultConstraints);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String toolTip) {
        this.tooltip = toolTip;
    }

    public AppConfigConstraintType getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(AppConfigConstraintType constraintType) {
        this.constraintType = constraintType;
    }

    public List getValues() {
        return values;
    }

    public void setValues(List values) {
        this.values = values;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public Map<String, Object> getDefaultConstraints() {
        return defaultConstraints;
    }

    public void setDefaultConstraints(Map<String, Object> defaultConstraints) {
        if (defaultConstraints != null) {
            this.defaultConstraints = defaultConstraints;
        } else {
            this.defaultConstraints = new HashMap<>();
        }
    }

    public void addDefaultConstraint(String key, Object value) {
        this.defaultConstraints.put(key, value);
    }
}
