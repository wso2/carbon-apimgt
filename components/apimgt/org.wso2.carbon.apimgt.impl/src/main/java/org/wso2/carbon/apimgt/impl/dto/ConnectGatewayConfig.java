/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

import java.net.URI;

/**
 * One connect-with-token config: one self-hosted Universal Gateway (registration_token + optional name, display_name, url, etc.).
 * Used when multiple gateways are configured via [[apim.universal_gateway.connect]].
 * {@code url} is required: the base URL where the gateway will be accessible (e.g. https://gw.example.com:8243); validated on set.
 */
public class ConnectGatewayConfig {
    private String registrationToken = "";
    private String name = "";
    private String displayName = "";
    private String description = "";
    private String url;

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
     * Base URL where the gateway will be accessible (e.g. https://gw.example.com:8243). Required for connect config.
     * Must be http or https with a valid host. Throws if malformed.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the base URL; validates format (http/https, non-empty host). Null or blank clears the field.
     * For [[apim.universal_gateway.connect]] entries, url is required and validated when config is loaded.
     *
     * @throws IllegalArgumentException if url is non-blank and malformed (e.g. invalid scheme, missing host)
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
            String scheme = u.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException("scheme must be http or https");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid URL in apim.universal_gateway.connect: \"" + url + "\" - " + e.getMessage(), e);
        }
        this.url = trimmed;
    }
}
