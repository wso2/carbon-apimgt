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

package org.wso2.carbon.apimgt.gateway.utils.redis;

import org.wso2.carbon.apimgt.api.APIManagementException;
import redis.clients.jedis.commands.ProtocolCommand;

import java.util.Map;

/**
 * Abstracts standalone vs cluster-mode Redis connectivity for the ElastiCache vector DB provider,
 * so the provider implementation doesn't branch on deployment mode. RediSearch (FT.*) commands are
 * sent via the raw {@code sendCommand} methods since Jedis 3.3 has no typed API for them; HSET/EXPIRE
 * use the native typed Jedis/JedisCluster APIs.
 *
 * <p>FT.CREATE and FT.SEARCH are index-wide operations rather than slot-bound keys, but a single
 * call to either is sufficient even in cluster mode: the underlying valkey-search module treats an
 * index name with no hash tag (ours has none) as a cluster-wide index, replicating FT.CREATE to every
 * shard and transparently merging FT.SEARCH results across shards server-side.</p>
 */
public interface RedisCommandExecutor {

    /**
     * Sends a RediSearch (FT.*) command with string arguments, e.g. FT.CREATE, FT.INFO.
     *
     * @param command the RediSearch command to execute
     * @param args    string-encoded command arguments
     * @return the raw reply from Redis/valkey-search
     * @throws APIManagementException if the command could not be executed
     */
    Object executeSearchCommand(ProtocolCommand command, String... args) throws APIManagementException;

    /**
     * Sends a RediSearch (FT.*) command with binary arguments, e.g. FT.SEARCH with a raw vector payload.
     *
     * @param command the RediSearch command to execute
     * @param args    binary-encoded command arguments
     * @return the raw reply from Redis/valkey-search
     * @throws APIManagementException if the command could not be executed
     */
    Object executeSearchCommand(ProtocolCommand command, byte[]... args) throws APIManagementException;

    /**
     * Writes a hash's fields using binary keys/values, used to store an embedding and its response.
     *
     * @param key    the hash key
     * @param fields field name/value pairs to set on the hash
     * @throws APIManagementException if the HSET could not be executed
     */
    void hsetBinary(byte[] key, Map<byte[], byte[]> fields) throws APIManagementException;

    /**
     * Sets a TTL on a key so cached entries are automatically evicted.
     *
     * @param key     the key to expire
     * @param seconds time-to-live in seconds
     * @throws APIManagementException if the EXPIRE could not be executed
     */
    void expire(byte[] key, int seconds) throws APIManagementException;

    /**
     * Deletes a key, used as best-effort cleanup when a preceding operation on the same key
     * (e.g. EXPIRE) fails after the key was already written.
     *
     * @param key the key to delete
     * @throws APIManagementException if the DEL could not be executed
     */
    void delete(byte[] key) throws APIManagementException;

    /**
     * Releases the underlying Redis connection(s)/pool held by this executor.
     */
    void close();
}
