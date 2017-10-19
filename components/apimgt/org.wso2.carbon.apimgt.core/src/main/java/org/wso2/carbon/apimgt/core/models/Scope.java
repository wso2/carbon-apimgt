/***********************************************************************************************************************
 *
 *  *
 *  *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *   WSO2 Inc. licenses this file to you under the Apache License,
 *  *   Version 2.0 (the "License"); you may not use this file except
 *  *   in compliance with the License.
 *  *   You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing,
 *  *  software distributed under the License is distributed on an
 *  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  *  KIND, either express or implied.  See the License for the
 *  *  specific language governing permissions and limitations
 *  *  under the License.
 *  *
 *
 */

package org.wso2.carbon.apimgt.core.models;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Objects;

/**
 * This class contains the model of scope
 */
public class Scope implements Serializable {

    private static final long serialVersionUID = 5737132983639722942L;
    String key;
    String name;
    String roles;
    String description;
    int id;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Scope scope = (Scope) o;
        return id == scope.id &&
                Objects.equals(key, scope.key) &&
                Objects.equals(name, scope.name) &&
                Objects.equals(roles, scope.roles) &&
                Objects.equals(description, scope.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, name, roles, description, id);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("key", key)
                .append("name", name)
                .append("roles", roles)
                .append("description", description)
                .append("id", id)
                .toString();
    }
}

