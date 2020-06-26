/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway.utils;

import org.wso2.carbon.apimgt.gateway.mediators.oauth.client.TokenResponse;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

/**
 * Redis Cache utility class to connect to Redis Server, add and get items
 */
public class RedisCache {
    private static JedisPool jedisPool;
    private static Jedis jedis;

    /**
     * RedisCache constructor to create new Jedis and JedisPool instances
     * with the given credentials
     * @param host Host name of the Redis server
     * @param port Port used by the Redis server
     * @param password Password for the Redis Server
     */
    public RedisCache(String host, Integer port, char[] password) {
        if (password != null) {
            jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
        } else {
            jedisPool = new JedisPool(host, port);
        }

        jedis = new Jedis(host, port);

        if (password != null) {
            jedis.auth(String.valueOf(password));
        }
    }

    /**
     * Add TokenResponse object to Redis Cache
     * @param uuid Unique Identifier for TokenResponse object
     * @param tokenResponse Token Response object
     */
    public void addTokenResponse(String uuid, TokenResponse tokenResponse) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hmset(uuid, tokenResponse.toMap());
        }
    }

    /**
     * Get TokenResponse object from Redis Cache
     * @param uuid Unique Identifier for TokenResponse object
     * @return TokenResponse object
     */
    public TokenResponse getTokenResponseById(String uuid) {
        TokenResponse tokenResponse = null;
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> tokenMap = jedis.hgetAll(uuid);
            if (tokenMap.size() != 0) {
                tokenResponse = new TokenResponse(tokenMap);
            }
        }
        return tokenResponse;
    }

    /**
     * Drop the Redis Cache connection
     */
    public void stopRedisCacheSession() {
        jedis.flushDB();
        jedisPool.destroy();
        jedis.close();
    }
}
