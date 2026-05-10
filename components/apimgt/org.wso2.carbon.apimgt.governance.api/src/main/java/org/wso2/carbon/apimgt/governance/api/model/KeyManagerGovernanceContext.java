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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Normalized view of a Key Manager for Devportal Governance enforcement and rule
 * authoring. The resolver fills this from {@code KeyManagerConfigurationDTO} so
 * every governance hook reads the same canonical shape regardless of the KM
 * type (Resident, WSO2-IS, third-party, custom out-of-band).
 *
 * <p>Lookups MUST go through this context — never read
 * {@code keyManagerConfig.getAdditionalProperties()} ad-hoc, otherwise the
 * UUID-vs-name fallback, blank → Resident default, and capability flag
 * normalization will drift between callers.</p>
 */
public class KeyManagerGovernanceContext {

    /** Logical action a governance evaluation is being performed for. */
    public enum Action {
        OAUTH_APP_CREATE,
        OAUTH_APP_MAP,
        OAUTH_APP_UPDATE,
        OAUTH_APP_DELETE,
        OAUTH_APP_CLEANUP,
        ACCESS_TOKEN_GENERATE,
        CONSUMER_SECRET_REGEN
    }

    private String uuid;
    private String name;
    private String displayName;
    private String alias;
    private String type;
    private boolean enabled;
    private boolean resident;
    private boolean global;
    private List<String> availableGrantTypes = Collections.emptyList();

    // KM capability flags (sourced from KeyManagerConfigurationDTO.additionalProperties)
    // — see APIConstants.KeyManager.* for the canonical key names.
    private boolean enableOAuthAppCreation = true;
    private boolean enableMapOAuthConsumerApps = false;
    private boolean enableTokenGeneration = true;
    private boolean enableTokenHash = false;
    private boolean enableTokenEncryption = false;
    private boolean enableMultipleClientSecrets = false;
    private Integer clientSecretCount;
    private Long clientSecretExpiresIn;
    private boolean bypassClientCredentials = false;
    private boolean selfValidateJwt = false;

    /** {@code jwt}, {@code reference}, or {@code custom}. */
    private String validationType;
    /** Hint at token shape (opaque vs JWT) advertised by the KM. */
    private String tokenFormatString;

    /** Per-OAuth-client {@code additionalProperties} keys this KM supports. */
    private List<String> supportedAdditionalPropertyKeys = Collections.emptyList();

    /** Raw passthrough so rule authors can target KM properties not yet promoted to first-class fields. */
    private Map<String, Object> rawProperties = new LinkedHashMap<>();

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isResident() {
        return resident;
    }

    public void setResident(boolean resident) {
        this.resident = resident;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public List<String> getAvailableGrantTypes() {
        return new ArrayList<>(availableGrantTypes);
    }

    public void setAvailableGrantTypes(List<String> availableGrantTypes) {
        this.availableGrantTypes = availableGrantTypes == null
                ? Collections.emptyList() : new ArrayList<>(availableGrantTypes);
    }

    public boolean isEnableOAuthAppCreation() {
        return enableOAuthAppCreation;
    }

    public void setEnableOAuthAppCreation(boolean enableOAuthAppCreation) {
        this.enableOAuthAppCreation = enableOAuthAppCreation;
    }

    public boolean isEnableMapOAuthConsumerApps() {
        return enableMapOAuthConsumerApps;
    }

    public void setEnableMapOAuthConsumerApps(boolean enableMapOAuthConsumerApps) {
        this.enableMapOAuthConsumerApps = enableMapOAuthConsumerApps;
    }

    public boolean isEnableTokenGeneration() {
        return enableTokenGeneration;
    }

    public void setEnableTokenGeneration(boolean enableTokenGeneration) {
        this.enableTokenGeneration = enableTokenGeneration;
    }

    public boolean isEnableTokenHash() {
        return enableTokenHash;
    }

    public void setEnableTokenHash(boolean enableTokenHash) {
        this.enableTokenHash = enableTokenHash;
    }

    public boolean isEnableTokenEncryption() {
        return enableTokenEncryption;
    }

    public void setEnableTokenEncryption(boolean enableTokenEncryption) {
        this.enableTokenEncryption = enableTokenEncryption;
    }

    public boolean isEnableMultipleClientSecrets() {
        return enableMultipleClientSecrets;
    }

    public void setEnableMultipleClientSecrets(boolean enableMultipleClientSecrets) {
        this.enableMultipleClientSecrets = enableMultipleClientSecrets;
    }

    public Integer getClientSecretCount() {
        return clientSecretCount;
    }

    public void setClientSecretCount(Integer clientSecretCount) {
        this.clientSecretCount = clientSecretCount;
    }

    public Long getClientSecretExpiresIn() {
        return clientSecretExpiresIn;
    }

    public void setClientSecretExpiresIn(Long clientSecretExpiresIn) {
        this.clientSecretExpiresIn = clientSecretExpiresIn;
    }

    public boolean isBypassClientCredentials() {
        return bypassClientCredentials;
    }

    public void setBypassClientCredentials(boolean bypassClientCredentials) {
        this.bypassClientCredentials = bypassClientCredentials;
    }

    public boolean isSelfValidateJwt() {
        return selfValidateJwt;
    }

    public void setSelfValidateJwt(boolean selfValidateJwt) {
        this.selfValidateJwt = selfValidateJwt;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public String getTokenFormatString() {
        return tokenFormatString;
    }

    public void setTokenFormatString(String tokenFormatString) {
        this.tokenFormatString = tokenFormatString;
    }

    public List<String> getSupportedAdditionalPropertyKeys() {
        return new ArrayList<>(supportedAdditionalPropertyKeys);
    }

    public void setSupportedAdditionalPropertyKeys(List<String> supportedAdditionalPropertyKeys) {
        this.supportedAdditionalPropertyKeys = supportedAdditionalPropertyKeys == null
                ? Collections.emptyList() : new ArrayList<>(supportedAdditionalPropertyKeys);
    }

    public Map<String, Object> getRawProperties() {
        return new LinkedHashMap<>(rawProperties);
    }

    public void setRawProperties(Map<String, Object> rawProperties) {
        this.rawProperties = rawProperties == null ? new LinkedHashMap<>() : new LinkedHashMap<>(rawProperties);
    }

    /**
     * Serializable shape used directly inside Spectral payloads. Matches the field names
     * rule authors write JSONPath against — keep stable.
     */
    public Map<String, Object> toPayloadMap() {

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uuid", uuid);
        map.put("name", name);
        map.put("displayName", displayName);
        map.put("alias", alias);
        map.put("type", type);
        map.put("enabled", enabled);
        map.put("isResident", resident);
        map.put("isGlobal", global);
        map.put("availableGrantTypes", availableGrantTypes);
        map.put("enableOAuthAppCreation", enableOAuthAppCreation);
        map.put("enableMapOAuthConsumerApps", enableMapOAuthConsumerApps);
        map.put("enableTokenGeneration", enableTokenGeneration);
        map.put("enableTokenHash", enableTokenHash);
        map.put("enableTokenEncryption", enableTokenEncryption);
        map.put("enableMultipleClientSecrets", enableMultipleClientSecrets);
        map.put("clientSecretCount", clientSecretCount);
        map.put("clientSecretExpiresIn", clientSecretExpiresIn);
        map.put("bypassClientCredentials", bypassClientCredentials);
        map.put("selfValidateJwt", selfValidateJwt);
        map.put("validationType", validationType);
        map.put("tokenFormatString", tokenFormatString);
        map.put("supportedAdditionalPropertyKeys", supportedAdditionalPropertyKeys);
        return map;
    }
}
