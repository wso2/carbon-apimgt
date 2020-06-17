/*
*  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
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

/**
 * This class represent the Label
 */
public class Label {
    private String labelId;
    private String name;
    private String tenantId;
    private String description;
    private List<String> accessUrls = new ArrayList<>();

    public Label() {
    }

    public String getLabelId() {
        return labelId;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<String> getAccessUrls() {
        return accessUrls;
    }

    public void setAccessUrls(List<String> accessUrls) {
        this.accessUrls = accessUrls;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        Label other = (Label) obj;
        if (!Objects.equals(this.name, other.name)) return false;
        if (!Objects.equals(this.labelId, other.labelId)) return false;
        if (!Objects.equals(this.description, other.description)) return false;
        if (!Objects.equals(this.tenantId, other.tenantId)) return false;
        if (!Objects.equals(this.accessUrls, other.accessUrls)) return false;
        return true;
    }
}
