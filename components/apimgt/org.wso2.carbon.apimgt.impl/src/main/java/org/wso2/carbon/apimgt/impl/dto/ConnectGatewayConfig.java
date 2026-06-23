/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.net.URI;

/**
 * One connect-with-token config entry for a self-hosted Platform Gateway.
 * Used when multiple gateways are configured via {@code [[apim.platform_gateway.connect]]}.
 * {@code url} is required: the base URL where the gateway will be accessible (e.g. https://gw.example.com:8243).
 * {@code organization} is optional: tenant/org domain for gateway ownership (same as Admin Portal create).
 * When omitted, defaults to {@link MultitenantConstants#SUPER_TENANT_DOMAIN_NAME}. Set to
 * {@code WSO2-ALL-TENANTS} explicitly for a shared gateway visible to all tenants.
 */
public class ConnectGatewayConfig {
    private String registrationToken = "";
    private String name = "";
    private String displayName = "";
    private String description = "";
    private String url;
    private String organization = "";

    public String getRegistrationToken() {
        return registrationToken;
    }

    public void setRegistrationToken(String registrationToken) {
        this.registrationToken = registrationToken != null ? registrationToken : "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName != null ? displayName : "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    /**
     * Tenant organization for this gateway (e.g. {@code carbon.super}, {@code t.com}, or {@code WSO2-ALL-TENANTS}).
     */
    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization != null ? organization : "";
    }

    /**
     * Resolved organization for persistence: configured value or super-tenant default.
     */
    public String resolveOrganization() {
        if (organization != null && !organization.isBlank()) {
            return organization.trim();
        }
        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    }

    /**
     * Base URL where the gateway will be accessible (e.g. https://gw.example.com:8243). Required for connect config.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the base URL; validates format (http/https, non-empty host). Null or blank clears the field.
     *
     * @throws IllegalArgumentException if url is non-blank and malformed
     */
    public void setUrl(String url) {
        if (url == null || url.isBlank()) {
            this.url = null;
            return;
        }
        String trimmed = url.trim();
        try {
            URI u = URI.create(trimmed);
            if (u.getHost() == null || u.getHost().isEmpty()) {
                throw new IllegalArgumentException("missing host");
            }
            if (u.getUserInfo() != null && !u.getUserInfo().isEmpty()) {
                throw new IllegalArgumentException("user-info not allowed in base URL");
            }
            if (u.getQuery() != null && !u.getQuery().isEmpty()) {
                throw new IllegalArgumentException("query not allowed in base URL");
            }
            if (u.getFragment() != null && !u.getFragment().isEmpty()) {
                throw new IllegalArgumentException("fragment not allowed in base URL");
            }
            String scheme = u.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException("scheme must be http or https");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid URL in apim.platform_gateway.connect: \"" + url + "\" - " + e.getMessage(), e);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid URL in apim.platform_gateway.connect: \"" + url + "\" - " + e.getMessage(), e);
        }
        this.url = trimmed;
    }
}
