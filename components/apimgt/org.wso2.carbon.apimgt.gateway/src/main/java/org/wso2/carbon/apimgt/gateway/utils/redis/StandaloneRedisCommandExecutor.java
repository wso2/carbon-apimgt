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
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.commands.ProtocolCommand;

import java.util.Map;

/**
 * Executes Redis commands against a single-endpoint (non cluster-mode) ElastiCache deployment.
 */
public class StandaloneRedisCommandExecutor implements RedisCommandExecutor {

    private final JedisPool jedisPool;

    public StandaloneRedisCommandExecutor(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public Object executeSearchCommand(ProtocolCommand command, String... args) throws APIManagementException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sendCommand(command, args);
        } catch (Exception e) {
            throw new APIManagementException("Error executing Redis command: " + command, e);
        }
    }

    @Override
    public Object executeSearchCommand(ProtocolCommand command, byte[]... args) throws APIManagementException {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.sendCommand(command, args);
        } catch (Exception e) {
            throw new APIManagementException("Error executing Redis command: " + command, e);
        }
    }

    @Override
    public void hsetBinary(byte[] key, Map<byte[], byte[]> fields) throws APIManagementException {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(key, fields);
        } catch (Exception e) {
            throw new APIManagementException("Error executing HSET", e);
        }
    }

    @Override
    public void expire(byte[] key, int seconds) throws APIManagementException {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.expire(key, seconds);
        } catch (Exception e) {
            throw new APIManagementException("Error executing EXPIRE", e);
        }
    }

    @Override
    public void close() {
        jedisPool.close();
    }
}
