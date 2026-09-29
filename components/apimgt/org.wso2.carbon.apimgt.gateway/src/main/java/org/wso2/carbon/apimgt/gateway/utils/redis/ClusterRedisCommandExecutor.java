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
import redis.clients.jedis.util.SafeEncoder;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Executes Redis commands against a cluster-mode-enabled ElastiCache deployment. FT.CREATE/FT.SEARCH
 * are sent once to a single reachable node: an index name with no hash tag (ours has none) is treated
 * by the valkey-search module as a cluster-wide index, so it replicates FT.CREATE to every shard and
 * merges FT.SEARCH results across shards on its own — no client-side fan-out needed.
 *
 * FT.CREATE is a write, so it (and FT.INFO/FT.SEARCH, which we route the same way since valkey-search's
 * cross-shard behavior on a replica isn't documented/verified) must land on a primary — jedisCluster's own
 * getClusterNodes() doesn't distinguish primaries from replicas, so we ask the cluster directly via
 * CLUSTER SLOTS and cache the result until a command actually fails. CLUSTER SLOTS is used over CLUSTER
 * NODES because the master is always the first node entry per slot range - a protocol guarantee - so
 * there's no flag text (e.g. "master,fail" for a node that's down but not yet evicted) to misparse.
 */
public class ClusterRedisCommandExecutor implements RedisCommandExecutor {

    private static final Log log = LogFactory.getLog(ClusterRedisCommandExecutor.class);

    private final JedisCluster jedisCluster;
    private volatile List<Map.Entry<String, JedisPool>> cachedPrimaryNodes;

    public ClusterRedisCommandExecutor(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    @Override
    public Object executeSearchCommand(ProtocolCommand command, String... args) throws APIManagementException {
        return executeOnPrimary(command, jedis -> jedis.sendCommand(command, args));
    }

    @Override
    public Object executeSearchCommand(ProtocolCommand command, byte[]... args) throws APIManagementException {
        return executeOnPrimary(command, jedis -> jedis.sendCommand(command, args));
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
    public void delete(byte[] key) throws APIManagementException {
        try {
            jedisCluster.del(key);
        } catch (Exception e) {
            throw new APIManagementException("Error executing DEL", e);
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

    /**
     * Runs {@code action} against a known primary, falling back to the next known primary if the
     * chosen one rejects the command (e.g. READONLY, a MOVED/ASK redirect, or a connection failure).
     * If every cached primary fails, the primary list is refreshed once (in case a failover changed
     * it) and retried before giving up.
     */
    private Object executeOnPrimary(ProtocolCommand command, Function<Jedis, Object> action)
            throws APIManagementException {
        Exception lastError = null;
        for (boolean refreshed : new boolean[] {false, true}) {
            for (Map.Entry<String, JedisPool> primary : getPrimaryNodes(refreshed)) {
                try (Jedis jedis = primary.getValue().getResource()) {
                    return action.apply(jedis);
                } catch (Exception e) {
                    lastError = e;
                    log.warn("Redis command " + command + " failed on primary " + primary.getKey(), e);
                }
            }
        }
        throw new APIManagementException("Error executing Redis command: " + command +
                " - no writable ElastiCache primary node reachable", lastError);
    }

    /**
     * Returns the cluster's current primary nodes, asking any one reachable node for CLUSTER SLOTS
     * (cluster topology is gossiped, so any node can answer) and caching the result. Pass
     * forceRefresh=true to bypass the cache, e.g. after a primary has just failed a command.
     */
    private List<Map.Entry<String, JedisPool>> getPrimaryNodes(boolean forceRefresh) throws APIManagementException {
        List<Map.Entry<String, JedisPool>> primaries = cachedPrimaryNodes;
        if (!forceRefresh && primaries != null && !primaries.isEmpty()) {
            return primaries;
        }
        Map<String, JedisPool> allNodes = jedisCluster.getClusterNodes();
        if (allNodes.isEmpty()) {
            throw new APIManagementException("No reachable ElastiCache cluster nodes");
        }
        Exception lastError = null;
        for (Map.Entry<String, JedisPool> node : allNodes.entrySet()) {
            try (Jedis jedis = node.getValue().getResource()) {
                primaries = parsePrimaries(jedis.clusterSlots(), allNodes);
                if (!primaries.isEmpty()) {
                    cachedPrimaryNodes = primaries;
                    return primaries;
                }
            } catch (Exception e) {
                lastError = e;
            }
        }
        throw new APIManagementException("No primary ElastiCache cluster nodes found", lastError);
    }

    /**
     * Parses a CLUSTER SLOTS reply: one entry per slot range, shaped [startSlot, endSlot, master,
     * replica...], where master/replica are themselves [ip, port, nodeId, ...]. The master is always
     * index 2 - a protocol guarantee, not something we infer from node state text - so a node that's
     * failed and been replaced simply doesn't appear here (it owns no slots), with no flag to check.
     */
    private static List<Map.Entry<String, JedisPool>> parsePrimaries(List<Object> clusterSlotsReply,
            Map<String, JedisPool> allNodes) {
        Set<String> masterAddresses = new LinkedHashSet<>();
        for (Object slotRangeObj : clusterSlotsReply) {
            List<Object> slotRange = (List<Object>) slotRangeObj;
            if (slotRange.size() < 3) {
                continue;
            }
            List<Object> master = (List<Object>) slotRange.get(2);
            String ip = SafeEncoder.encode((byte[]) master.get(0));
            long port = (Long) master.get(1);
            masterAddresses.add(ip + ":" + port);
        }
        List<Map.Entry<String, JedisPool>> primaries = new ArrayList<>();
        for (String address : masterAddresses) {
            JedisPool pool = allNodes.get(address);
            if (pool != null) {
                primaries.add(new AbstractMap.SimpleEntry<>(address, pool));
            }
        }
        return primaries;
    }
}
