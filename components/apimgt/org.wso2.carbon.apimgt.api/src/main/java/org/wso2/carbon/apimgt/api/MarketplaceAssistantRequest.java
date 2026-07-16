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

package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.API;

import java.util.HashMap;
import java.util.Map;

/**
 * Carries the context of a Marketplace Assistant request to a {@link MarketplaceAssistant} implementation.
 * <p>
 * Passing a context object rather than positional parameters keeps the SPI stable: an implementation reads only the
 * fields it needs (ignoring, for example, {@link #getUsername()} if its AI service does not require it), and new
 * fields can be added over time without breaking existing implementations. Any information not modelled as a typed
 * field can be supplied through {@link #getAdditionalProperties()}.
 * <p>
 * The {@code query}/{@code history}/{@code organization}/{@code username} fields carry chat context (execute,
 * getApiCount), while {@code api}/{@code uuid}/{@code tenantDomain}/{@code version}/{@code visibleRoles} carry the
 * context of an API publish/delete to the vector store (publishAPI, deleteAPI). An implementation reads only the
 * fields relevant to the operation being invoked.
 */
public class MarketplaceAssistantRequest {

    private String query;
    private String history;
    private String organization;
    private String username;
    private API api;
    private String uuid;
    private String tenantDomain;
    private String version;
    private String visibleRoles;
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public API getApi() {
        return api;
    }

    public void setApi(API api) {
        this.api = api;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(String visibleRoles) {
        this.visibleRoles = visibleRoles;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void addProperty(String key, Object value) {
        additionalProperties.put(key, value);
    }

    public Object getProperty(String key) {
        return additionalProperties.get(key);
    }
}
