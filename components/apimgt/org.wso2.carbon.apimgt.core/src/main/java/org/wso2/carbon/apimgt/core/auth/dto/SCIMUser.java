/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.auth.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

/**
 * Model of SCIM user
 */
public class SCIMUser {

    @SerializedName("schemas")
    private List<String> schemas;
    @SerializedName("userName")
    private String userName;
    @SerializedName("password")
    private String password;  //note: this can't be a char[] because then feign reads is as a multi-valued attribute
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private SCIMName name;
    @SerializedName("emails")
    private List<SCIMUserEmails> emails;
    @SerializedName("groups")
    private List<SCIMUserGroups> groups;

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SCIMName getName() {
        return name;
    }

    public void setName(SCIMName name) {
        this.name = name;
    }

    public String getUsername() {
        return userName;
    }

    public void setUsername(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<SCIMUserEmails> getEmails() {
        return this.emails;
    }

    public void setEmails(List<SCIMUserEmails> emails) {
        this.emails = emails;
    }

    public List<SCIMUserGroups> getGroups() {
        return groups;
    }

    public void setGroups(List<SCIMUserGroups> groups) {
        this.groups = groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SCIMUser)) {
            return false;
        }
        SCIMUser scimUser = (SCIMUser) o;
        return Objects.equals(userName, scimUser.userName) &&
                Objects.equals(id, scimUser.id) &&
                Objects.equals(name, scimUser.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, id, name);
    }

    /**
     * This inner class contains the model of SCIM user name
     */
    public static class SCIMName {
        private String givenName;
        private String familyName;

        public SCIMName() {
            givenName = "";
            familyName = "";
        }

        public SCIMName(String givenName, String familyName) {
            this.givenName = givenName;
            this.familyName = familyName;
        }

        public String getGivenName() {
            return givenName;
        }

        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        public String getFamilyName() {
            return familyName;
        }

        public void setFamilyName(String familyName) {
            this.familyName = familyName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof SCIMName)) {
                return false;
            }
            SCIMName scimName = (SCIMName) o;
            return Objects.equals(givenName, scimName.givenName) &&
                    Objects.equals(familyName, scimName.familyName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(givenName, familyName);
        }
    }

    /**
     * This inner class contains the model of SCIM user emails
     */
    public static class SCIMUserEmails {

        private String value;
        private String type;
        private boolean primary;

        /**
         * Constructor
         *
         * @param value   Emails address
         * @param type    Type of email (eg. home, work)
         * @param primary Is primary email
         */
        public SCIMUserEmails(String value, String type, boolean primary) {
            this.value = value;
            this.type = type;
            this.primary = primary;
        }

        public String getValue() {
            return this.value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getType() {
            return this.type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }
    }

    /**
     * This inner class contains the model of SCIM groups of a user
     */
    public static class SCIMUserGroups {

        private String value;
        private String display;

        /**
         * Constructor
         *
         * @param value    Id of the group
         * @param display   Name of the group
         */
        public SCIMUserGroups(String value, String display) {
            this.value = value;
            this.display = display;
        }

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
