/*
 *  Copyright (c), WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.dto;

import java.io.Serializable;

/**
 * This class represents the Basic Auth validation Info DTO.
 */
public class BasicAuthValidationInfoDTO implements Serializable {

    private static final long serialVersionUID = 12345L;

    private boolean isAuthenticated = false;
    private String hashedPassword = null;
    private String domainQualifiedUsername = null;
    private String[] userRoleList = null;
    private boolean cached;

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getDomainQualifiedUsername() {
        return domainQualifiedUsername;
    }

    public void setDomainQualifiedUsername(String domainQualifiedUsername) {
        this.domainQualifiedUsername = domainQualifiedUsername;
    }

    public String[] getUserRoleList() {
        return userRoleList;
    }

    public void setUserRoleList(String[] userRoleList) {
        this.userRoleList = userRoleList;
    }

    public boolean isCached() {

        return cached;
    }

    public void setCached(boolean cached) {

        this.cached = cached;
    }

    public String toString() {

        StringBuilder builder = new StringBuilder(20);
        builder.append("BasicAuthValidationInfoDTO = { isAuthenticated:").append(isAuthenticated).
                append(" , domainQualifiedUsername:").append(domainQualifiedUsername);

        if (userRoleList != null && userRoleList.length != 0) {
            builder.append(" , userRoleList:[");
            for (String role : userRoleList) {
                builder.append(role).append(',');
            }
            builder.replace(builder.length() - 1, builder.length() - 1, "]");
        } else {
            builder.append(']');
        }
        return builder.toString();
    }
}
