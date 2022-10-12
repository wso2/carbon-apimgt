/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.apk.apimgt.user.ctx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.user.ctx.builder.UserContextBuilder;
import org.wso2.apk.apimgt.user.ctx.util.UserContextConstants;
import org.wso2.apk.apimgt.user.exceptions.UserException;
import org.wso2.apk.apimgt.user.mgt.internal.UserManagerHolder;

import java.util.Arrays;
import java.util.Map;

public class UserContext {
    private static final Log logger = LogFactory.getLog(UserContext.class);
    private static final ThreadLocal<UserContext> currentUserContext = ThreadLocal.withInitial(UserContext::new);

    private String username;
    private int organizationId;
    private String organization;
    private String[] roles;
    private Map<String, String> claims;

    private UserContext() {
    }

    private UserContext(UserContextBuilder builder) {
        Map<String, Object> properties = null;
        try {
            properties = builder.getProperties();
        } catch (UserException e) {
            logger.error("Error occurred while reading user claims via the UserContextBuilder.", e);
        }

        if (properties == null) {
            logger.error("No user claims retrieved via the UserContextBuilder.");
            return;
        }

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
                case UserContextConstants.ATTRIB_CLAIMS:
                    this.claims = (Map<String, String>) properties.get(UserContextConstants.ATTRIB_CLAIMS);
            }
        }
        if (this.organization != null) {
            try {
                this.organizationId = UserManagerHolder.getUserManager().getTenantId(this.organization);
            } catch (UserException e) {
                logger.error("Error while getting the organization Id.", e);
            }
        }
    }

    public static void initThreadLocalUserContext(UserContextBuilder builder) {
        logger.debug("Initializing the UserContext");
        currentUserContext.set(new UserContext(builder));
    }

    public static UserContext getThreadLocalUserContext() {
        return currentUserContext.get();
    }

    public String getUsername() {
        return username;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public String getOrganization() {
        return organization;
    }

    public String[] getRoles() {
        return roles;
    }

    public String getClaim(String claimUri) {
        return claims.get(claimUri);
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
                ", claims=" + claims +
                '}';
    }
}

