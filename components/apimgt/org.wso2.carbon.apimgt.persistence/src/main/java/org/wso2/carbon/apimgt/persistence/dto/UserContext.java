/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.apimgt.persistence.dto;

import java.util.Map;

public class UserContext {
    String username;
    Organization organization;
    String[] roles;
    // domain name, role

    Map<String, Object> properties;

    public UserContext(String userame, Organization organization, Map<String, Object> properties, String[] roles) {
        this.username = userame;
        this.organization = organization;
        this.properties = properties;
        this.roles = roles;
    }

    public String getUserame() {
        return username;
    }

    public Organization getOrganization() {
        return organization;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String[] getRoles() {
        return roles;
    }
}
