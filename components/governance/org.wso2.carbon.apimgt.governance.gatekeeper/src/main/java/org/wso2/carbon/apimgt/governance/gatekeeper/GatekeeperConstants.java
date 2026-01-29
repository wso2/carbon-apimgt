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

package org.wso2.carbon.apimgt.governance.gatekeeper;

/**
 * Constants for the Gatekeeper module.
 */
public final class GatekeeperConstants {

    private GatekeeperConstants() {
        // Private constructor to prevent instantiation
    }

    /**
     * Default similarity threshold for MinHash LSH comparison.
     * APIs with Jaccard similarity above this threshold are considered duplicates.
     */
    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.95;

    /**
     * Minimum similarity threshold allowed.
     */
    public static final double MIN_SIMILARITY_THRESHOLD = 0.5;

    /**
     * Maximum similarity threshold allowed.
     */
    public static final double MAX_SIMILARITY_THRESHOLD = 1.0;

    /**
     * Number of hash functions to use for MinHash signature.
     * Higher values increase accuracy but consume more memory.
     */
    public static final int DEFAULT_NUM_HASH_FUNCTIONS = 128;

    /**
     * Number of bands for LSH indexing.
     * More bands = higher recall, fewer bands = higher precision.
     */
    public static final int DEFAULT_NUM_BANDS = 16;

    /**
     * N-gram size for shingling.
     */
    public static final int NGRAM_SIZE = 3;

    /**
     * Rule category for deduplication rulesets.
     */
    public static final String DEDUPLICATION_RULE_CATEGORY = "DEDUPLICATION";

    /**
     * Ruleset YAML configuration keys.
     */
    public static final class RulesetConfig {
        public static final String SIMILARITY_THRESHOLD = "similarity_threshold";
        public static final String NUM_HASH_FUNCTIONS = "num_hash_functions";
        public static final String NUM_BANDS = "num_bands";
        public static final String ENABLED = "enabled";

        private RulesetConfig() {
        }
    }

    /**
     * Conflict report field names.
     */
    public static final class ConflictReport {
        public static final String MATCHED_API_NAME = "matchedApiName";
        public static final String MATCHED_API_UUID = "matchedApiUuid";
        public static final String MATCHED_API_CONTEXT = "matchedApiContext";
        public static final String MATCHED_API_VERSION = "matchedApiVersion";
        public static final String SIMILARITY_SCORE = "similarityScore";
        public static final String METADATA_SIMILARITY = "metadataSimilarity";
        public static final String PATH_SIMILARITY = "pathSimilarity";
        public static final String SCHEMA_SIMILARITY = "schemaSimilarity";

        private ConflictReport() {
        }
    }

    /**
     * Database constants.
     */
    public static final class Database {
        public static final String TABLE_NAME = "AM_API_MINHASH";
        public static final String COLUMN_API_UUID = "API_UUID";
        public static final String COLUMN_SIGNATURE_BLOB = "SIGNATURE_BLOB";
        public static final String COLUMN_ORGANIZATION = "ORGANIZATION";
        public static final String COLUMN_CREATED_TIME = "CREATED_TIME";
        public static final String COLUMN_UPDATED_TIME = "UPDATED_TIME";

        private Database() {
        }
    }

    /**
     * API Definition fields to prune (boilerplate removal).
     */
    public static final class PruneFields {
        public static final String INFO_CONTACT = "info.contact";
        public static final String INFO_LICENSE = "info.license";
        public static final String INFO_TERMS_OF_SERVICE = "info.termsOfService";
        public static final String SERVERS = "servers";
        public static final String EXTERNAL_DOCS = "externalDocs";
        public static final String SECURITY = "security";

        private PruneFields() {
        }
    }
}
