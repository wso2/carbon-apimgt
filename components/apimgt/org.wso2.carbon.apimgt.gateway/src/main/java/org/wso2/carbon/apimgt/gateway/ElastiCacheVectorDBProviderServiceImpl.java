/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.VectorDBProviderService;
import org.wso2.carbon.apimgt.api.dto.VectorDBProviderConfigurationDTO;
import org.wso2.carbon.apimgt.gateway.utils.redis.ClusterRedisCommandExecutor;
import org.wso2.carbon.apimgt.gateway.utils.redis.RediSearchCommand;
import org.wso2.carbon.apimgt.gateway.utils.redis.RedisCommandExecutor;
import org.wso2.carbon.apimgt.gateway.utils.redis.StandaloneRedisCommandExecutor;
import org.wso2.carbon.apimgt.impl.APIConstants;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.SafeEncoder;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * This class is responsible for interacting with AWS ElastiCache (Redis/Valkey vector search,
 * i.e. the RediSearch-compatible FT.CREATE/FT.SEARCH command set) to create indexes, store
 * embeddings, and retrieve similar responses. Supports both standalone and cluster-mode
 * ElastiCache deployments.
 */
public class ElastiCacheVectorDBProviderServiceImpl implements VectorDBProviderService {
    private static final Log log = LogFactory.getLog(ElastiCacheVectorDBProviderServiceImpl.class);

    // Matches the retry count Jedis itself defaults to for cluster-mode MOVED/ASK redirection.
    private static final int DEFAULT_CLUSTER_MAX_ATTEMPTS = 5;
    private static final String TAG_QUERY_SPECIAL_CHARS = ",.<>{}\"':;!@#$%^&*()-+=~[]";

    // Jedis pool tuning properties under [apim.ai.vector_db_provider.properties]. Defaults match
    // JedisPoolConfig's own out-of-the-box values so behavior is unchanged unless explicitly set.
    private static final String JEDIS_POOL_MAX_TOTAL = "jedis.pool.max_total";
    private static final String JEDIS_POOL_MAX_TOTAL_DEFAULT = "8";
    private static final String JEDIS_POOL_MAX_IDLE = "jedis.pool.max_idle";
    private static final String JEDIS_POOL_MAX_IDLE_DEFAULT = "8";
    private static final String JEDIS_POOL_MIN_IDLE = "jedis.pool.min_idle";
    private static final String JEDIS_POOL_MIN_IDLE_DEFAULT = "0";
    private static final String JEDIS_POOL_TEST_ON_BORROW = "jedis.pool.test_on_borrow";
    private static final String JEDIS_POOL_TEST_ON_BORROW_DEFAULT = "false";
    private static final String JEDIS_POOL_TEST_ON_RETURN = "jedis.pool.test_on_return";
    private static final String JEDIS_POOL_TEST_ON_RETURN_DEFAULT = "false";
    private static final String JEDIS_POOL_TEST_WHILE_IDLE = "jedis.pool.test_while_idle";
    private static final String JEDIS_POOL_TEST_WHILE_IDLE_DEFAULT = "true";
    private static final String JEDIS_POOL_BLOCK_WHEN_EXHAUSTED = "jedis.pool.block_when_exhausted";
    private static final String JEDIS_POOL_BLOCK_WHEN_EXHAUSTED_DEFAULT = "true";
    private static final String JEDIS_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS =
            "jedis.pool.min_evictable_idle_time_millis";
    private static final String JEDIS_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS_DEFAULT = "60000";
    private static final String JEDIS_POOL_TIME_BETWEEN_EVICTION_RUNS_MILLIS =
            "jedis.pool.time_between_eviction_runs_millis";
    private static final String JEDIS_POOL_TIME_BETWEEN_EVICTION_RUNS_MILLIS_DEFAULT = "30000";
    private static final String JEDIS_POOL_NUM_TESTS_PER_EVICTION_RUN = "jedis.pool.num_tests_per_eviction_run";
    private static final String JEDIS_POOL_NUM_TESTS_PER_EVICTION_RUN_DEFAULT = "-1";

    private RedisCommandExecutor executor;
    private String indexName;
    private String keyPrefix;
    private int dimension;
    private int ttl;
    private final Gson gson = new Gson();

    /**
     * Initialize the AWS ElastiCache Vector DB provider with configuration.
     */
    @Override
    public void init(VectorDBProviderConfigurationDTO providerConfig) throws APIManagementException {
        log.debug("Initializing AWS ElastiCache Vector DB provider");
        Map<String, String> properties = providerConfig.getProperties();
        String host = properties.get(APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_HOST);
        String portStr = properties.get(APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_PORT);
        String username = properties.get(APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_USERNAME);
        String password = properties.get(APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_PASSWORD);
        boolean sslEnabled = Boolean.parseBoolean(
                properties.get(APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_SSL_ENABLED));
        boolean clusterModeEnabled = Boolean.parseBoolean(
                properties.get(APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_CLUSTER_MODE_ENABLED));

        List<String> missingParams = new ArrayList<>();
        if (host == null) {
            missingParams.add("'host'");
        }
        if (portStr == null) {
            missingParams.add("'port'");
        }
        if (!missingParams.isEmpty()) {
            throw new IllegalArgumentException("Missing required ElastiCache configuration parameter(s): " +
                    String.join(", ", missingParams));
        }
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid ElastiCache port: '" + portStr + "'", nfe);
        }

        String ttlStr = properties.getOrDefault(APIConstants.AI.VECTOR_DB_PROVIDER_TTL,
                APIConstants.AI.VECTOR_DB_PROVIDER_TTL_DEFAULT);
        try {
            ttl = Integer.parseInt(ttlStr);
        } catch (NumberFormatException nfe) {
            log.warn("Invalid TTL value '" + ttlStr + "', falling back to default: " +
                    APIConstants.AI.VECTOR_DB_PROVIDER_TTL_DEFAULT);
            ttl = Integer.parseInt(APIConstants.AI.VECTOR_DB_PROVIDER_TTL_DEFAULT);
        }

        if (clusterModeEnabled) {
            executor = new ClusterRedisCommandExecutor(createJedisCluster(host, port, username, password,
                    sslEnabled, properties));
        } else {
            executor = new StandaloneRedisCommandExecutor(createJedisPool(host, port, username, password,
                    sslEnabled, properties));
        } // TODO Test Redis OSS Variant, just tested the ValKey variant
        log.info("Initialized AWS ElastiCache Vector DB provider with endpoint: " + host + ":" + port +
                " (clusterMode=" + clusterModeEnabled + ")");
    }

    @Override
    public String getType() {
        return APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_TYPE;
    }

    @Override
    public void close() throws APIManagementException {
        if (executor != null) {
            log.debug("Closing AWS ElastiCache Vector DB provider");
            executor.close();
        }
    }

    /**
     * Create a RediSearch vector index in ElastiCache if it does not already exist.
     */
    @Override
    public void createIndex(Map<String, String> config) throws APIManagementException {
        log.info("Creating ElastiCache vector index");
        String dimStr = config.get(APIConstants.AI.VECTOR_DB_PROVIDER_EMBEDDING_DIMENSION);
        if (dimStr == null) {
            throw new APIManagementException("Missing required config: '" +
                    APIConstants.AI.VECTOR_DB_PROVIDER_EMBEDDING_DIMENSION + "'");
        }
        try {
            dimension = Integer.parseInt(dimStr);
        } catch (NumberFormatException nfe) {
            throw new APIManagementException("Invalid embedding dimension: '" + dimStr + "'", nfe);
        }
        if (dimension <= 0) {
            throw new APIManagementException("Embedding dimension must be > 0. Received: " + dimension);
        }
        // dimension varies by embedding provider/model (e.g. 1024 for Mistral's mistral-embed, 1536 for
        // OpenAI's text-embedding-3-small) and RediSearch requires a fixed DIM per index, so it's baked
        // into the index/key-prefix name - switching providers safely gets its own index instead of
        // colliding with one sized for a different model.
        indexName = APIConstants.AI.VECTOR_INDEX_PREFIX + dimension +
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_INDEX_SUFFIX;
        keyPrefix = APIConstants.AI.VECTOR_INDEX_PREFIX + dimension +
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_KEY_PREFIX_SUFFIX;

        if (indexExists(indexName)) {
            log.info("Index already exists: " + indexName);
            return;
        }

        List<String> vectorAttributes = Arrays.asList(
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_TYPE_KEYWORD,
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_FLOAT32,
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_DIM, String.valueOf(dimension),
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_DISTANCE_METRIC,
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_L2,
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_M,
                String.valueOf(APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_HNSW_M),
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_EF_CONSTRUCTION,
                String.valueOf(APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_HNSW_EF_CONSTRUCTION));

        List<String> args = new ArrayList<>(Arrays.asList(
                indexName,
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_ON,
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_HASH,
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_PREFIX, "1", keyPrefix,
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_SCHEMA,
                APIConstants.AI.VECTOR_DB_PROVIDER_API_ID,
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_TAG,
                APIConstants.AI.VECTOR_DB_PROVIDER_CREATED_AT,
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_NUMERIC,
                APIConstants.AI.VECTOR_DB_PROVIDER_EMBEDDING,
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_VECTOR,
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_HNSW, String.valueOf(vectorAttributes.size())));
        args.addAll(vectorAttributes);

        executor.executeSearchCommand(RediSearchCommand.FT_CREATE, args.toArray(new String[0]));
        log.info("Successfully created index: " + indexName);
    }

    private boolean indexExists(String indexName) {
        try {
            executor.executeSearchCommand(RediSearchCommand.FT_INFO, indexName);
            return true;
        } catch (APIManagementException e) {
            return false;
        }
    }

    @Override
    public <T extends Serializable> void store(double[] embeddings, T response, Map<String, String> filter)
            throws APIManagementException {
        if (embeddings == null || embeddings.length != dimension) {
            throw new APIManagementException("Invalid embedding dimension. Expected: " + dimension +
                    ", Received: " + (embeddings != null ? embeddings.length : "null"));
        }
        if (filter == null || !filter.containsKey(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID)) {
            throw new APIManagementException("Missing required filter: 'api_id'");
        }
        String apiId = filter.get(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID);
        if (log.isDebugEnabled()) {
            log.debug("Storing embeddings in ElastiCache for API ID: " + apiId);
        }

        String id = UUID.randomUUID().toString();
        byte[] key = SafeEncoder.encode(keyPrefix + id);

        Map<byte[], byte[]> fields = new LinkedHashMap<>();
        fields.put(SafeEncoder.encode(APIConstants.AI.VECTOR_DB_PROVIDER_ID), SafeEncoder.encode(id));
        fields.put(SafeEncoder.encode(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID), SafeEncoder.encode(apiId));
        fields.put(SafeEncoder.encode(APIConstants.AI.VECTOR_DB_PROVIDER_CREATED_AT),
                SafeEncoder.encode(String.valueOf(System.currentTimeMillis())));
        fields.put(SafeEncoder.encode(APIConstants.AI.VECTOR_DB_PROVIDER_EMBEDDING), toVectorBytes(embeddings));
        fields.put(SafeEncoder.encode(APIConstants.AI.VECTOR_DB_PROVIDER_RESPONSE),
                SafeEncoder.encode(gson.toJson(response)));

        try {
            executor.hsetBinary(key, fields);
            executor.expire(key, ttl);
            log.info("Successfully stored response in ElastiCache for API ID: " + apiId);
        } catch (APIManagementException e) {
            try {
                executor.delete(key);
            } catch (APIManagementException deleteEx) {
                log.warn("Failed to clean up orphaned key after store failure for API ID " + apiId, deleteEx);
            }
            String errorMsg = "Error storing embeddings in ElastiCache for API ID " + apiId + ": " + e.getMessage();
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Retrieve the most similar response from the vector database.
     */
    @Override
    public <T extends Serializable> T retrieve(double[] embeddings, Map<String, String> filter)
            throws APIManagementException {
        if (embeddings == null || embeddings.length != dimension) {
            throw new APIManagementException("Invalid embedding dimension. Expected: " + dimension +
                    ", Received: " + (embeddings != null ? embeddings.length : "null"));
        }
        if (filter == null || !filter.containsKey(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID)
                || !filter.containsKey(APIConstants.AI.VECTOR_DB_PROVIDER_THRESHOLD)) {
            throw new APIManagementException("Missing required filter: 'api_id' or 'threshold'");
        }
        String apiId = filter.get(APIConstants.AI.VECTOR_DB_PROVIDER_API_ID);
        double threshold;
        try {
            threshold = Double.parseDouble(filter.get(APIConstants.AI.VECTOR_DB_PROVIDER_THRESHOLD));
        } catch (NumberFormatException nfe) {
            throw new APIManagementException("Invalid threshold value in filter", nfe);
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieving similar response from ElastiCache for API ID: " + apiId);
        }

        // RediSearch's KNN syntax has no built-in radius/range filter here, so the L2 distance
        // threshold (mirroring Zilliz's "radius" semantics) is applied client-side on the score.
        String queryExpr = "(@" + APIConstants.AI.VECTOR_DB_PROVIDER_API_ID + ":{" + escapeTagValue(apiId) +
                "})=>[KNN 1 @" + APIConstants.AI.VECTOR_DB_PROVIDER_EMBEDDING + " $" +
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_VECTOR_PARAM_NAME + " AS " +
                APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_SCORE_ALIAS + "]";

        // No SORTBY: it only accepts indexed schema fields, not the ad-hoc KNN "AS score" alias,
        // and KNN results are already returned in ascending vector-distance order by default.
        byte[][] args = {
                SafeEncoder.encode(indexName),
                SafeEncoder.encode(queryExpr),
                SafeEncoder.encode(APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_PARAMS),
                SafeEncoder.encode("2"),
                SafeEncoder.encode(APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_VECTOR_PARAM_NAME),
                toVectorBytes(embeddings),
                SafeEncoder.encode(APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_DIALECT),
                SafeEncoder.encode("2")
        };

        try {
            Object reply = executor.executeSearchCommand(RediSearchCommand.FT_SEARCH, args);
            ScoredResponse best = parseTopResult((List<Object>) reply);
            if (best == null || best.score > threshold) {
                log.debug("No similar responses found in ElastiCache");
                return null;
            }
            log.debug("Successfully retrieved similar response from ElastiCache");
            return (T) best.response;
        } catch (Exception e) {
            String errorMsg = "Error retrieving response from ElastiCache (index: " + indexName +
                    ", filter: " + filter + "): " + e.getMessage();
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    private static ScoredResponse parseTopResult(List<Object> reply) {
        if (reply == null || reply.isEmpty() || reply.size() < 3) {
            return null;
        }
        long total = (Long) reply.get(0);
        if (total == 0) {
            return null;
        }
        List<Object> fields = (List<Object>) reply.get(2);
        String response = null;
        Double score = null;
        for (int i = 0; i < fields.size() - 1; i += 2) {
            String fieldName = SafeEncoder.encode((byte[]) fields.get(i));
            if (APIConstants.AI.VECTOR_DB_PROVIDER_RESPONSE.equals(fieldName)) {
                response = SafeEncoder.encode((byte[]) fields.get(i + 1));
            } else if (APIConstants.AI.VECTOR_DB_PROVIDER_ELASTICACHE_SCORE_ALIAS.equals(fieldName)) {
                score = Double.parseDouble(SafeEncoder.encode((byte[]) fields.get(i + 1)));
            }
        }
        return (response != null && score != null) ? new ScoredResponse(response, score) : null;
    }

    private static String escapeTagValue(String value) {
        StringBuilder escaped = new StringBuilder();
        for (char c : value.toCharArray()) {
            if (TAG_QUERY_SPECIAL_CHARS.indexOf(c) >= 0) {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    private static byte[] toVectorBytes(double[] embeddings) {
        ByteBuffer buffer = ByteBuffer.allocate(embeddings.length * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (double value : embeddings) {
            buffer.putFloat((float) value);
        }
        return buffer.array();
    }

    private static JedisPool createJedisPool(String host, int port, String username, String password,
            boolean sslEnabled, Map<String, String> properties) {
        JedisPoolConfig poolConfig = buildJedisPoolConfig(properties);
        if (StringUtils.isNotEmpty(username)) {
            return new JedisPool(poolConfig, host, port, Protocol.DEFAULT_TIMEOUT, username, password,
                    Protocol.DEFAULT_DATABASE, sslEnabled);
        }
        return new JedisPool(poolConfig, host, port, Protocol.DEFAULT_TIMEOUT, password,
                Protocol.DEFAULT_DATABASE, sslEnabled);
    }

    private static JedisCluster createJedisCluster(String host, int port, String username, String password,
            boolean sslEnabled, Map<String, String> properties) {
        Set<HostAndPort> nodes = Collections.singleton(new HostAndPort(host, port));
        JedisPoolConfig poolConfig = buildJedisPoolConfig(properties);
        return new JedisCluster(nodes, Protocol.DEFAULT_TIMEOUT, Protocol.DEFAULT_TIMEOUT,
                DEFAULT_CLUSTER_MAX_ATTEMPTS, username, password, null, poolConfig, sslEnabled);
    }

    /**
     * Builds a {@link JedisPoolConfig} from the "jedis.pool.*" properties, falling back to
     * JedisPoolConfig's own defaults for anything not explicitly set.
     */
    private static JedisPoolConfig buildJedisPoolConfig(Map<String, String> properties) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(parseIntProperty(properties, JEDIS_POOL_MAX_TOTAL, JEDIS_POOL_MAX_TOTAL_DEFAULT));
        poolConfig.setMaxIdle(parseIntProperty(properties, JEDIS_POOL_MAX_IDLE, JEDIS_POOL_MAX_IDLE_DEFAULT));
        poolConfig.setMinIdle(parseIntProperty(properties, JEDIS_POOL_MIN_IDLE, JEDIS_POOL_MIN_IDLE_DEFAULT));
        poolConfig.setTestOnBorrow(parseBooleanProperty(properties, JEDIS_POOL_TEST_ON_BORROW,
                JEDIS_POOL_TEST_ON_BORROW_DEFAULT));
        poolConfig.setTestOnReturn(parseBooleanProperty(properties, JEDIS_POOL_TEST_ON_RETURN,
                JEDIS_POOL_TEST_ON_RETURN_DEFAULT));
        poolConfig.setTestWhileIdle(parseBooleanProperty(properties, JEDIS_POOL_TEST_WHILE_IDLE,
                JEDIS_POOL_TEST_WHILE_IDLE_DEFAULT));
        poolConfig.setBlockWhenExhausted(parseBooleanProperty(properties, JEDIS_POOL_BLOCK_WHEN_EXHAUSTED,
                JEDIS_POOL_BLOCK_WHEN_EXHAUSTED_DEFAULT));
        poolConfig.setMinEvictableIdleTimeMillis(parseLongProperty(properties,
                JEDIS_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS, JEDIS_POOL_MIN_EVICTABLE_IDLE_TIME_MILLIS_DEFAULT));
        poolConfig.setTimeBetweenEvictionRunsMillis(parseLongProperty(properties,
                JEDIS_POOL_TIME_BETWEEN_EVICTION_RUNS_MILLIS, JEDIS_POOL_TIME_BETWEEN_EVICTION_RUNS_MILLIS_DEFAULT));
        poolConfig.setNumTestsPerEvictionRun(parseIntProperty(properties, JEDIS_POOL_NUM_TESTS_PER_EVICTION_RUN,
                JEDIS_POOL_NUM_TESTS_PER_EVICTION_RUN_DEFAULT));
        if (log.isDebugEnabled()) {
            log.debug("Resolved Jedis pool config: maxTotal=" + poolConfig.getMaxTotal() +
                    ", maxIdle=" + poolConfig.getMaxIdle() +
                    ", minIdle=" + poolConfig.getMinIdle() +
                    ", testOnBorrow=" + poolConfig.getTestOnBorrow() +
                    ", testOnReturn=" + poolConfig.getTestOnReturn() +
                    ", testWhileIdle=" + poolConfig.getTestWhileIdle() +
                    ", blockWhenExhausted=" + poolConfig.getBlockWhenExhausted() +
                    ", minEvictableIdleTimeMillis=" + poolConfig.getMinEvictableIdleTimeMillis() +
                    ", timeBetweenEvictionRunsMillis=" + poolConfig.getTimeBetweenEvictionRunsMillis() +
                    ", numTestsPerEvictionRun=" + poolConfig.getNumTestsPerEvictionRun());
        }
        return poolConfig;
    }

    private static int parseIntProperty(Map<String, String> properties, String key, String defaultValue) {
        String rawValue = properties.getOrDefault(key, defaultValue);
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException nfe) {
            log.warn("Invalid value for '" + key + "': '" + rawValue + "', falling back to default: " +
                    defaultValue);
            return Integer.parseInt(defaultValue);
        }
    }

    private static long parseLongProperty(Map<String, String> properties, String key, String defaultValue) {
        String rawValue = properties.getOrDefault(key, defaultValue);
        try {
            return Long.parseLong(rawValue);
        } catch (NumberFormatException nfe) {
            log.warn("Invalid value for '" + key + "': '" + rawValue + "', falling back to default: " +
                    defaultValue);
            return Long.parseLong(defaultValue);
        }
    }

    private static boolean parseBooleanProperty(Map<String, String> properties, String key, String defaultValue) {
        String rawValue = properties.getOrDefault(key, defaultValue);
        if (!"true".equalsIgnoreCase(rawValue) && !"false".equalsIgnoreCase(rawValue)) {
            log.warn("Invalid value for '" + key + "': '" + rawValue + "', falling back to default: " +
                    defaultValue);
            return Boolean.parseBoolean(defaultValue);
        }
        return Boolean.parseBoolean(rawValue);
    }

    private static final class ScoredResponse {
        private final String response;
        private final double score;

        private ScoredResponse(String response, double score) {
            this.response = response;
            this.score = score;
        }
    }
}
