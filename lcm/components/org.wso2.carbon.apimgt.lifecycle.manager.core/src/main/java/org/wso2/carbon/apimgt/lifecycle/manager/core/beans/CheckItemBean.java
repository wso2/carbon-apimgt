/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.wso2.carbon.apimgt.lifecycle.manager.core.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * This bean holds the data checklist items for a particular lifecycle state which are defined in lifecycle
 * configuration.
 */
public class CheckItemBean {

    private List<PermissionBean> permissionsBeans;
    private String name;
    private List<CustomCodeBean> validationBeans;
    private List<String> targets;
    private boolean value;

    public CheckItemBean() {
        this.permissionsBeans = new ArrayList<PermissionBean>();
        this.name = "";
        this.validationBeans = new ArrayList<CustomCodeBean>();
        this.targets = new ArrayList<String>();
        this.value = false;
    }

    public List<String> getTargets() {
        return targets;
    }

    public void setTargets(List<String> targets) {
        this.targets = targets;
    }

    public List<CustomCodeBean> getValidationBeans() {
        return validationBeans;
    }

    public void setValidationBeans(List<CustomCodeBean> validationBeans) {
        this.validationBeans = validationBeans;
    }

    public List<PermissionBean> getPermissionsBeans() {
        return permissionsBeans;
    }

    public void setPermissionsBeans(List<PermissionBean> permissionsBeans) {
        this.permissionsBeans = permissionsBeans;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}
