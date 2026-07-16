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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.commands.ProtocolCommand;

import java.util.Map;

/**
 * Executes Redis commands against a cluster-mode-enabled ElastiCache deployment. FT.CREATE/FT.SEARCH
 * are sent once to a single reachable node: an index name with no hash tag (ours has none) is treated
 * by the valkey-search module as a cluster-wide index, so it replicates FT.CREATE to every shard and
 * merges FT.SEARCH results across shards on its own — no client-side fan-out needed.
 */
public class ClusterRedisCommandExecutor implements RedisCommandExecutor { // TODO Test the Cluster mode

    private static final Log log = LogFactory.getLog(ClusterRedisCommandExecutor.class);

    private final JedisCluster jedisCluster;

    public ClusterRedisCommandExecutor(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    @Override
    public Object executeSearchCommand(ProtocolCommand command, String... args) throws APIManagementException {
        Map.Entry<String, JedisPool> node = getFirstNode();
        try (Jedis jedis = node.getValue().getResource()) {
            return jedis.sendCommand(command, args);
        } catch (Exception e) {
            throw new APIManagementException("Error executing Redis command: " + command, e);
        }
    }

    @Override
    public Object executeSearchCommand(ProtocolCommand command, byte[]... args) throws APIManagementException {
        Map.Entry<String, JedisPool> node = getFirstNode();
        try (Jedis jedis = node.getValue().getResource()) {
            return jedis.sendCommand(command, args);
        } catch (Exception e) {
            throw new APIManagementException("Error executing Redis command: " + command, e);
        }
    }

    @Override
    public void hsetBinary(byte[] key, Map<byte[], byte[]> fields) throws APIManagementException {
        try {
            jedisCluster.hset(key, fields);
        } catch (Exception e) {
            throw new APIManagementException("Error executing HSET", e);
        }
    }

    @Override
    public void expire(byte[] key, int seconds) throws APIManagementException {
        try {
            jedisCluster.expire(key, seconds);
        } catch (Exception e) {
            throw new APIManagementException("Error executing EXPIRE", e);
        }
    }

    @Override
    public void close() {
        try {
            jedisCluster.close();
        } catch (Exception e) {
            log.warn("Error closing JedisCluster", e);
        }
    }

    private Map.Entry<String, JedisPool> getFirstNode() throws APIManagementException {
        return jedisCluster.getClusterNodes().entrySet().stream().findFirst()
                .orElseThrow(() -> new APIManagementException("No reachable ElastiCache cluster nodes"));
    }
}
