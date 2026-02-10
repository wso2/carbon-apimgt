/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.gatekeeper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.gatekeeper.GatekeeperConstants;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceDBUtil;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing deduplication configuration from rulesets.
 * Caches the configuration and provides methods to refresh it.
 */
public class DeduplicationConfigService {

    private static final Log log = LogFactory.getLog(DeduplicationConfigService.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    private static volatile DeduplicationConfigService instance;

    // Cache of configuration per organization
    private final Map<String, DeduplicationConfig> configCache = new ConcurrentHashMap<>();

    // Cache expiry time in milliseconds (5 minutes)
    private static final long CACHE_EXPIRY_MS = 5 * 60 * 1000;
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

    // Cache for policy association check per organization
    private final Map<String, Boolean> policyAssociationCache = new ConcurrentHashMap<>();
    private final Map<String, Long> policyAssociationTimestamps = new ConcurrentHashMap<>();
    private static final long POLICY_ASSOCIATION_CACHE_EXPIRY_MS = 60 * 1000; // 1 minute

    private DeduplicationConfigService() {
        // Private constructor for singleton
    }

    /**
     * Gets the singleton instance.
     *
     * @return DeduplicationConfigService instance
     */
    public static DeduplicationConfigService getInstance() {
        if (instance == null) {
            synchronized (DeduplicationConfigService.class) {
                if (instance == null) {
                    instance = new DeduplicationConfigService();
                }
            }
        }
        return instance;
    }

    /**
     * Gets the deduplication configuration for an organization.
     * Uses cached value if available and not expired.
     *
     * @param organization The organization
     * @return DeduplicationConfig with threshold and other settings
     */
    public DeduplicationConfig getConfig(String organization) {
        String cacheKey = organization != null ? organization : "default";

        // Check if cached and not expired
        Long timestamp = cacheTimestamps.get(cacheKey);
        if (timestamp != null && (System.currentTimeMillis() - timestamp) < CACHE_EXPIRY_MS) {
            DeduplicationConfig cached = configCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }

        // Fetch from database
        DeduplicationConfig config = fetchConfigFromDatabase(organization);
        if (config != null) {
            configCache.put(cacheKey, config);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
        } else {
            // Use defaults
            config = new DeduplicationConfig();
            configCache.put(cacheKey, config);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());
        }

        return config;
    }

    /**
     * Invalidates the cache for an organization.
     * Call this when the deduplication ruleset is updated.
     *
     * @param organization The organization
     */
    public void invalidateCache(String organization) {
        String cacheKey = organization != null ? organization : "default";
        configCache.remove(cacheKey);
        cacheTimestamps.remove(cacheKey);
        policyAssociationCache.remove(cacheKey);
        policyAssociationTimestamps.remove(cacheKey);
        log.info("Invalidated deduplication config cache for organization: " + cacheKey);
    }

    /**
     * Invalidates all cached configurations.
     */
    public void invalidateAllCaches() {
        configCache.clear();
        cacheTimestamps.clear();
        policyAssociationCache.clear();
        policyAssociationTimestamps.clear();
        log.info("Invalidated all deduplication config caches");
    }

    /**
     * Checks if the deduplication ruleset is associated with any active governance policy
     * for the given organization. This is used to determine whether dedup checks should run.
     *
     * @param organization The organization
     * @return true if the dedup ruleset is associated with at least one policy
     */
    public boolean isDeduplicationPolicyActive(String organization) {
        String cacheKey = organization != null ? organization : "default";
        
        // Check cache
        Long timestamp = policyAssociationTimestamps.get(cacheKey);
        if (timestamp != null && (System.currentTimeMillis() - timestamp) < POLICY_ASSOCIATION_CACHE_EXPIRY_MS) {
            Boolean cached = policyAssociationCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
        }
        
        boolean isActive = checkPolicyAssociationInDB(organization);
        policyAssociationCache.put(cacheKey, isActive);
        policyAssociationTimestamps.put(cacheKey, System.currentTimeMillis());
        return isActive;
    }

    /**
     * Checks if any governance policy has the deduplication ruleset associated.
     */
    private boolean checkPolicyAssociationInDB(String organization) {
        String org = organization != null ? organization : "carbon.super";
        
        String sql = "SELECT COUNT(*) AS CNT FROM GOV_POLICY_RULESET pr " +
                "JOIN GOV_RULESET r ON pr.RULESET_ID = r.RULESET_ID " +
                "WHERE r.RULE_CATEGORY = 'GENERIC' " +
                "AND (r.ORGANIZATION = ? OR r.ORGANIZATION = 'carbon.super')";
        
        try (Connection conn = APIMGovernanceDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, org);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("CNT");
                    if (log.isDebugEnabled()) {
                        log.debug("Deduplication policy association count for org " + org + ": " + count);
                    }
                    return count > 0;
                }
            }
        } catch (Exception e) {
            log.warn("Error checking deduplication policy association: " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Stack trace: ", e);
            }
        }
        
        // Default to true if we can't check (fail-open to avoid silently disabling)
        return true;
    }

    /**
     * Fetches the configuration from the deduplication ruleset in the database.
     * First tries to find by RULE_CATEGORY = 'DEDUPLICATION', then falls back to name-based search.
     *
     * @param organization The organization
     * @return DeduplicationConfig or null if not found
     */
    private DeduplicationConfig fetchConfigFromDatabase(String organization) {
        String org = organization != null ? organization : "carbon.super";
        
        // First try to find by DEDUPLICATION category
        DeduplicationConfig config = fetchByCategory(org);
        if (config != null) {
            log.info("Found deduplication config by RULE_CATEGORY for organization: " + org);
            return config;
        }
        
        // Fallback: search by ruleset name containing 'deduplication' or 'duplicate'
        config = fetchByName(org);
        if (config != null) {
            log.info("Found deduplication config by ruleset NAME for organization: " + org);
            return config;
        }
        
        log.warn("No deduplication ruleset found in database for organization: " + org 
                + ". Using default threshold: " + GatekeeperConstants.DEFAULT_SIMILARITY_THRESHOLD);
        return null;
    }
    
    /**
     * Fetches config by RULE_CATEGORY = 'GENERIC'.
     */
    private DeduplicationConfig fetchByCategory(String organization) {
        String sql = "SELECT rc.CONTENT FROM GOV_RULESET r " +
                "JOIN GOV_RULESET_CONTENT rc ON r.RULESET_ID = rc.RULESET_ID " +
                "WHERE r.RULE_CATEGORY = 'GENERIC' " +
                "AND (r.ORGANIZATION = ? OR r.ORGANIZATION = 'carbon.super') " +
                "ORDER BY CASE WHEN r.ORGANIZATION = ? THEN 0 ELSE 1 END " +
                "LIMIT 1";

        try (Connection conn = APIMGovernanceDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, organization);
            ps.setString(2, organization);

            if (log.isDebugEnabled()) {
                log.debug("Executing SQL to find deduplication config by category for org: " + organization);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] contentBytes = rs.getBytes("CONTENT");
                    if (contentBytes != null) {
                        String content = new String(contentBytes, StandardCharsets.UTF_8);
                        if (log.isDebugEnabled()) {
                            log.debug("Found ruleset content by category, length: " + content.length());
                        }
                        return parseConfig(content);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error fetching deduplication config by category: " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Stack trace: ", e);
            }
        }
        return null;
    }
    
    /**
     * Fetches config by ruleset name containing 'deduplication' or 'duplicate'.
     * This is a fallback for cases where RULE_CATEGORY is not set correctly.
     */
    private DeduplicationConfig fetchByName(String organization) {
        String sql = "SELECT rc.CONTENT FROM GOV_RULESET r " +
                "JOIN GOV_RULESET_CONTENT rc ON r.RULESET_ID = rc.RULESET_ID " +
                "WHERE (LOWER(r.NAME) LIKE '%deduplication%' OR LOWER(r.NAME) LIKE '%duplicate%') " +
                "AND (r.ORGANIZATION = ? OR r.ORGANIZATION = 'carbon.super') " +
                "ORDER BY CASE WHEN r.ORGANIZATION = ? THEN 0 ELSE 1 END " +
                "LIMIT 1";

        try (Connection conn = APIMGovernanceDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, organization);
            ps.setString(2, organization);

            if (log.isDebugEnabled()) {
                log.debug("Executing SQL to find deduplication config by name for org: " + organization);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] contentBytes = rs.getBytes("CONTENT");
                    if (contentBytes != null) {
                        String content = new String(contentBytes, StandardCharsets.UTF_8);
                        if (log.isDebugEnabled()) {
                            log.debug("Found ruleset content by name, length: " + content.length());
                        }
                        return parseConfig(content);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error fetching deduplication config by name: " + e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Stack trace: ", e);
            }
        }
        return null;
    }

    /**
     * Parses the YAML configuration content.
     * Supports both root-level keys (like GatekeeperValidationEngine) and nested 'deduplication' section.
     *
     * @param yamlContent The YAML content string
     * @return DeduplicationConfig parsed from YAML
     */
    @SuppressWarnings("unchecked")
    private DeduplicationConfig parseConfig(String yamlContent) {
        try {
            Map<String, Object> content = yamlMapper.readValue(yamlContent, Map.class);
            DeduplicationConfig config = new DeduplicationConfig();

            // First try root-level keys (as used by GatekeeperValidationEngine)
            if (content.containsKey(GatekeeperConstants.RulesetConfig.SIMILARITY_THRESHOLD)) {
                config.setSimilarityThreshold(
                        parseDouble(content.get(GatekeeperConstants.RulesetConfig.SIMILARITY_THRESHOLD)));
            }

            if (content.containsKey(GatekeeperConstants.RulesetConfig.ENABLED)) {
                config.setEnabled(Boolean.TRUE.equals(
                        content.get(GatekeeperConstants.RulesetConfig.ENABLED)));
            }

            if (content.containsKey(GatekeeperConstants.RulesetConfig.MODE)) {
                config.setMode(String.valueOf(content.get(GatekeeperConstants.RulesetConfig.MODE)));
            }

            if (content.containsKey(GatekeeperConstants.RulesetConfig.NUM_HASH_FUNCTIONS)) {
                config.setNumHashFunctions(
                        Integer.parseInt(String.valueOf(
                                content.get(GatekeeperConstants.RulesetConfig.NUM_HASH_FUNCTIONS))));
            }

            if (content.containsKey(GatekeeperConstants.RulesetConfig.NUM_BANDS)) {
                config.setNumBands(
                        Integer.parseInt(String.valueOf(
                                content.get(GatekeeperConstants.RulesetConfig.NUM_BANDS))));
            }

            // Also support nested 'deduplication' section for flexibility
            if (content.containsKey("deduplication")) {
                Map<String, Object> dedupConfig = (Map<String, Object>) content.get("deduplication");
                parseNestedConfig(dedupConfig, config);
            }

            log.info("Parsed deduplication config: threshold=" + config.getSimilarityThreshold()
                    + ", mode=" + config.getMode() + ", enabled=" + config.isEnabled());

            return config;

        } catch (Exception e) {
            log.warn("Error parsing deduplication config YAML: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parses nested deduplication config section.
     */
    private void parseNestedConfig(Map<String, Object> dedupConfig, DeduplicationConfig config) {
        if (dedupConfig.containsKey(GatekeeperConstants.RulesetConfig.SIMILARITY_THRESHOLD)) {
            config.setSimilarityThreshold(
                    parseDouble(dedupConfig.get(GatekeeperConstants.RulesetConfig.SIMILARITY_THRESHOLD)));
        }

        if (dedupConfig.containsKey(GatekeeperConstants.RulesetConfig.ENABLED)) {
            config.setEnabled(Boolean.TRUE.equals(
                    dedupConfig.get(GatekeeperConstants.RulesetConfig.ENABLED)));
        }

        if (dedupConfig.containsKey(GatekeeperConstants.RulesetConfig.MODE)) {
            config.setMode(String.valueOf(dedupConfig.get(GatekeeperConstants.RulesetConfig.MODE)));
        }

        if (dedupConfig.containsKey(GatekeeperConstants.RulesetConfig.NUM_HASH_FUNCTIONS)) {
            config.setNumHashFunctions(
                    Integer.parseInt(String.valueOf(
                            dedupConfig.get(GatekeeperConstants.RulesetConfig.NUM_HASH_FUNCTIONS))));
        }

        if (dedupConfig.containsKey(GatekeeperConstants.RulesetConfig.NUM_BANDS)) {
            config.setNumBands(
                    Integer.parseInt(String.valueOf(
                            dedupConfig.get(GatekeeperConstants.RulesetConfig.NUM_BANDS))));
        }

        if (dedupConfig.containsKey("shingle_size")) {
            config.setShingleSize(
                    Integer.parseInt(String.valueOf(dedupConfig.get("shingle_size"))));
        }

        if (dedupConfig.containsKey("high_confidence_threshold")) {
            config.setHighConfidenceThreshold(
                    parseDouble(dedupConfig.get("high_confidence_threshold")));
        }
    }

    private double parseDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }

    /**
     * Configuration holder for deduplication settings.
     */
    public static class DeduplicationConfig {
        private boolean enabled = true;
        private double similarityThreshold = GatekeeperConstants.DEFAULT_SIMILARITY_THRESHOLD;
        private double highConfidenceThreshold = 0.99;
        private String mode = "audit";
        private int numHashFunctions = GatekeeperConstants.DEFAULT_NUM_HASH_FUNCTIONS;
        private int numBands = GatekeeperConstants.DEFAULT_NUM_BANDS;
        private int shingleSize = GatekeeperConstants.NGRAM_SIZE;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public double getSimilarityThreshold() {
            return similarityThreshold;
        }

        public void setSimilarityThreshold(double similarityThreshold) {
            // Validate threshold bounds
            if (similarityThreshold >= GatekeeperConstants.MIN_SIMILARITY_THRESHOLD &&
                    similarityThreshold <= GatekeeperConstants.MAX_SIMILARITY_THRESHOLD) {
                this.similarityThreshold = similarityThreshold;
            }
        }

        public double getHighConfidenceThreshold() {
            return highConfidenceThreshold;
        }

        public void setHighConfidenceThreshold(double highConfidenceThreshold) {
            this.highConfidenceThreshold = highConfidenceThreshold;
        }

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public int getNumHashFunctions() {
            return numHashFunctions;
        }

        public void setNumHashFunctions(int numHashFunctions) {
            this.numHashFunctions = numHashFunctions;
        }

        public int getNumBands() {
            return numBands;
        }

        public void setNumBands(int numBands) {
            this.numBands = numBands;
        }

        public int getShingleSize() {
            return shingleSize;
        }

        public void setShingleSize(int shingleSize) {
            this.shingleSize = shingleSize;
        }
    }
}
