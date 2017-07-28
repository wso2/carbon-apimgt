/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.core.auth.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model of SCIM group
 */
public class SCIMGroup {

    @SerializedName("schemas")
    private List<String> schemas;
    @SerializedName("id")
    private String id;
    @SerializedName("displayName")
    private String displayName;
    @SerializedName("members")
    private List<SCIMGroupMembers> members;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<SCIMGroupMembers> getMembers() {
        return members;
    }

    public void setMembers(List<SCIMGroupMembers> members) {
        this.members = members;
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    /**
     * This inner class contains the model of members of SCIM group
     */
    public static class SCIMGroupMembers {

        private String value;
        private String display;

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getDisplay() {
            return this.display;
        }

        public void setDisplay(String display) {
            this.display = display;
        }

    }

}
