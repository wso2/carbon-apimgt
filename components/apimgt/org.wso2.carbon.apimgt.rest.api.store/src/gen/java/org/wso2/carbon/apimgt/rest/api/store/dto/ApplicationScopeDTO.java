/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.store.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * Application Scope.
 */
public class ApplicationScopeDTO {

    @ApiModelProperty(example = "apim:fileread", value = "Key of scope")
    private String key = null;
    @ApiModelProperty(example = "apim file read", value = "Name of the scope")
    private String name = null;
    @ApiModelProperty(example = "admin, role1", value = "Roles scope is bounded to")
    private String roles = null;
    @ApiModelProperty(value = "Description of the scope")
    private String description = null;

    /**
     * Key of scope
     *
     * @return key
     **/
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Name of the scope
     *
     * @return name
     **/
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Roles scope is bounded to
     *
     * @return roles
     **/
    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    /**
     * Description of the scope
     *
     * @return description
     **/
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "class ApplicationScope {\n" + "    key: " + toIndentedString(key) + "\n" + "    name: "
                + toIndentedString(name) + "\n" + "    roles: " + toIndentedString(roles) + "\n" + "    description: "
                + toIndentedString(description) + "\n" + "}";
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
