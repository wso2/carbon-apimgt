/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.lifecycle.manager.beans;

/**
 * This bean holds the data for transition inputs for a particular lifecycle state which are defined in lifecycle
 * configuration.
 */
public class InputBean {

    private String name;

    private boolean isRequired;

    private String label;

    private String placeHolder;

    private String tooltip;

    private String regex;

    private String values;

    private String forTarget;

    public InputBean(String name, boolean isRequired, String label, String placeHolder, String tooltip, String regex,
            String values, String forTarget) {
        this.name = name;
        this.isRequired = isRequired;
        this.label = label;
        this.placeHolder = placeHolder;
        this.tooltip = tooltip;
        this.regex = regex;
        this.values = values;
        this.forTarget = forTarget;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(String placeHolder) {
        this.placeHolder = placeHolder;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public String getForTarget() {
        return forTarget;
    }

    public void setForTarget(String forTarget) {
        this.forTarget = forTarget;
    }

}
