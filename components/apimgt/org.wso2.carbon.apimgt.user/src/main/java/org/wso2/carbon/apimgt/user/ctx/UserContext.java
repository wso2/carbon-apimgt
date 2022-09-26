/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.user.ctx;

import org.wso2.carbon.apimgt.user.ctx.builder.UserContextBuilder;
import org.wso2.carbon.apimgt.user.ctx.util.UserContextConstants;

import java.util.Arrays;
import java.util.Map;

public class UserContext {
    private static final ThreadLocal<UserContext> currentUserContext = ThreadLocal.withInitial(UserContext::new);

    private String username;
    private String organization;
    private String[] roles;
    private UserContextBuilder builder;

    private UserContext() {
    }

    private UserContext(UserContextBuilder builder) {
        this.builder = builder;

        // build the UserContext object
        Map<String, Object> properties = builder.getProperties();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            switch (entry.getKey()) {
                case UserContextConstants.ATTRIB_USERNAME:
                    this.username = (String) properties.get(UserContextConstants.ATTRIB_USERNAME);
                    break;
                case UserContextConstants.ATTRIB_ORGANIZATION:
                    this.organization = (String) properties.get(UserContextConstants.ATTRIB_ORGANIZATION);
                    break;
                case UserContextConstants.ATTRIB_ROLES:
                    this.roles = (String[]) properties.get(UserContextConstants.ATTRIB_ROLES);
                    break;
            }
        }
    }

    public static void initThreadLocalUserContext(UserContextBuilder builder) {
        currentUserContext.set(new UserContext(builder));
    }

    public static UserContext getThreadLocalUserContext() {
        return currentUserContext.get();
    }

    public String getUsername() {
        return username;
    }

    public String getOrganization() {
        return organization;
    }

    public String[] getRoles() {
        return roles;
    }

    public String getClaim(String claimUri) {
        return this.builder.getClaim(claimUri);
    }

    public boolean hasRole(String roleName) {
        return Arrays.asList(roles).contains(roleName);
    }

    @Override
    public String toString() {
        return "UserContext{" +
                "username='" + username + '\'' +
                ", organization='" + organization + '\'' +
                ", roles=" + Arrays.toString(roles) +
                '}';
    }
}

