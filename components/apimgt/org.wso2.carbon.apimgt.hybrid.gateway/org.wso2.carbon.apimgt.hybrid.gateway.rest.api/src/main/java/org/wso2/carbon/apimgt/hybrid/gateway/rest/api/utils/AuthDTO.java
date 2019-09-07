/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.rest.api.utils;


import javax.ws.rs.core.Response;

/**
 * DTO for Auth Information
 */
public class AuthDTO {
    private String username;
    private String tenantDomain;
    private boolean authenticated = false;
    private Response.Status responseStatus;
    private String message;
    private String description;

    public AuthDTO(String username, String tenantDomain, boolean authenticated, Response.Status responseStatus,
                   String message, String description) {
        this.username = username;
        this.tenantDomain = tenantDomain;
        this.authenticated = authenticated;
        this.responseStatus = responseStatus;
        this.message = message;
        this.description = description;
    }

    public String getUsername() {
        return username;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public Response.Status getResponseStatus() {
        return responseStatus;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
