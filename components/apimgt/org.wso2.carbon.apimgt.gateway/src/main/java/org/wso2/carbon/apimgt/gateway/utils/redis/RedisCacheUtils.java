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

package org.wso2.carbon.apimgt.gateway.utils.redis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.Map;

/**
 * Utility class singleton to connect to Redis Server, and perform general operations
 */
public class RedisCacheUtils {
    private static final Log log = LogFactory.getLog(RedisCacheUtils.class);

    private static final RedisCacheUtils instance = new RedisCacheUtils();

    private static JedisPool jedisPool;

    /**
     * Private constructor
     */
    private RedisCacheUtils(){
    }

    /**
     * RedisCacheUtils constructor to create new JedisPool instance
     * with a Redis Server
     * @param host Host name of the Redis Server
     * @param port Port used by the Redis Server
     */
    public RedisCacheUtils(String host, int port) {
        jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
    }

    /**
     * RedisCacheUtils constructor to create new JedisPool instance
     * with a Redis Server requiring authorization
     * @param host Host name of the Redis Server
     * @param port Port used by the Redis Server
     * @param timeout Timeout of the Redis Server connection
     * @param username Username of the user of the Redis Server
     * @param password Password of the user of the Redis Server
     * @param database Database ID in the Redis Server
     * @param ssl Boolean value to check if ssl is enabled or not
     */
    public RedisCacheUtils(String host, int port, int timeout, String username, char[] password,
            int database, boolean ssl) {
        jedisPool = new JedisPool(new JedisPoolConfig(), host, port,
                timeout, username, String.valueOf(password), database, ssl);
    }

    /**
     * Get RedisCacheUtils Instance
     * @return RedisCacheUtils Instance
     */
    public static RedisCacheUtils getInstance() {
        return instance;
    }

    /**
     * Save a string key-value pair in Redis
     * @param key Key of the value to be saved
     * @param value Value to be saved
     */
    public void setValue(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        }
    }

    /**
     * Retrieve a string key-value pair in Redis
     * @param key Key of the value to be retrieved
     * @return String value retrieved
     */
    public String getValue(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    /**
     * Check whether key exists in Redis
     * @param key Key to be searched
     * @return boolean value whether key exists or not
     */
    public boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }

    /**
     * Delete key from Redis if it exists
     * @param key Key to be deleted
     * @return Returns 1 if deleted, 0 if not
     */
    public Long deleteKey(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(key);
        }
    }

    /**
     * Rename key in Redis if exists
     * @param oldKey Existing key value
     * @param newKey New key value
     */
    public void renameKey(String oldKey, String newKey) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.rename(oldKey, newKey);
        }
    }

    /**
     * Set timeout for a key so that after the timeout has
     * expired, the key will automatically be deleted
     * @param key Key to be set a timeout value
     * @param seconds Timeout value in seconds
     * @return Return 1 if timeout was set successfully, 0 if not
     */
    public Long expireKeyIn(String key, int seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expire(key, seconds);
        }
    }

    /**
     * Get the time to live for a specific key
     * @param key Key to check the time to live
     * @return Returns 2 if the key does not exist,
     * 1 if it does exist but has not expire set,
     * or any other value indicating the time to live
     * in seconds
     */
    public Long getTimeToLive(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ttl(key);
        }
    }

    /**
     * Add a field with a value for a specific key
     * @param key Key to be updated with the field
     * @param field Field to be updated with a value
     * @param value Value of the specified field
     */
    public void addFieldValue(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(key, field, value);
        }
    }

    /**
     * Get the value associated with a field stored at the key
     * @param key Key at which the field-value pair is stored
     * @param field Field which contains the value to be retrieved
     * @return Value associated with the field
     */
    public String getFieldValue(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        }
    }

    /**
     * Add a map into Redis
     * @param key Key at which the map should be saved
     * @param map Map to be saved
     */
    public void addMap(String key, Map<String, String> map) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(key, map);
        }
    }

    /**
     * Get map associated with a key
     * @param key Key at which the map is saved
     * @return Map associated with the key
     */
    public Map<String, String> getMap(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> map = jedis.hgetAll(key);
            if (map.size() != 0) {
                return map;
            } else {
                return null;
            }
        }
    }

    /**
     * Add Serializable object into Redis
     * @param key Key at which the object should be associated with
     * @param object Serializable object to be saved
     */
    public void addObject(String key, Object object) {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] data = new ObjectMapper().writeValueAsBytes(object);
            jedis.set(key.getBytes(), data);
        } catch (JsonProcessingException e) {
            log.error("Error while converting object to byte array", e);
        }
    }

    /**
     * Get Serializable object from Redis
     * @param key Key at which the object is saved
     * @return Serializable object associated with the key
     */
    public Object getObject(String key, Class objectType) {
        try (Jedis jedis = jedisPool.getResource()) {
            byte[] objectBytes = jedis.get(key.getBytes());
            if (objectBytes != null) {
                return new ObjectMapper().readValue(objectBytes, objectType);
            } else {
                return null;
            }
        } catch (JsonParseException e) {
            log.error("Error while parsing object from Redis Cache", e);
        } catch (JsonMappingException e) {
            log.error("Error while mapping object to provided class type: " + objectType, e);
        } catch (IOException e) {
            log.error("Error while reading value from Redis Cache", e);
        }
        return null;
    }

    /**
     * Drop the Redis Server connection
     */
    public void stopRedisCacheSession() {
        jedisPool.destroy();
    }
}
