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

package org.wso2.carbon.apimgt.gateway.handlers.security.basicauth;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

/**
 * Pool for managing BasicAuthClient instances to improve performance and resource utilization.
 */
public class BasicAuthClientPool {

    private static final Log log = LogFactory.getLog(BasicAuthClientPool.class);

    private static class Holder {
        private static final BasicAuthClientPool INSTANCE = new BasicAuthClientPool();
    }

    private final GenericObjectPool<BasicAuthClient> basicAuthClientPool;
    private static int maxIdle;

    private BasicAuthClientPool() {

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        String maxIdleClients = config.getFirstProperty(
                APIMgtGatewayConstants.BASIC_AUTH_VALIDATOR_CONNECTION_POOL_MAX_IDLE);
        String initIdleCapacity = config.getFirstProperty(
                APIMgtGatewayConstants.BASIC_AUTH_VALIDATOR_CONNECTION_POOL_INIT_IDLE_CAPACITY);
        int initIdleCapSize;

        try {
            maxIdle = StringUtils.isNotEmpty(maxIdleClients) ? Integer.parseInt(maxIdleClients) : 100;
        } catch (NumberFormatException nfe) {
            log.warn("Invalid MaxIdle '" + maxIdleClients + "'. Falling back to 100");
            maxIdle = 100;
        }
        // Derive or read maxActive (a true concurrency cap)
        String maxActiveProp = config.getFirstProperty(
                APIMgtGatewayConstants.BASIC_AUTH_VALIDATOR_CONNECTION_POOL_MAX_ACTIVE);
        int maxActive;
        if (StringUtils.isNotEmpty(maxActiveProp)) {
            try {
                maxActive = Integer.parseInt(maxActiveProp);
            } catch (NumberFormatException nfe) {
                log.warn("Invalid value for maxActive (\"" + maxActiveProp
                        + "\"). Falling back to maxIdle: " + maxIdle);
                maxActive = maxIdle;
            }
        } else {
            maxActive = maxIdle; // sensible default to cap concurrency
        }
        String maxWaitProp = config.getFirstProperty(
                APIMgtGatewayConstants.BASIC_AUTH_VALIDATOR_CONNECTION_POOL_MAX_WAIT_MILLIS);
        long maxWaitMillis;
        try {
            maxWaitMillis = StringUtils.isNotEmpty(maxWaitProp) ? Long.parseLong(maxWaitProp) : 30000L;
        } catch (NumberFormatException nfe) {
            log.warn("Invalid MaxWaitMillis '" + maxWaitProp + "'. Falling back to 30000");
            maxWaitMillis = 30000L;
        }

        try {
            initIdleCapSize = StringUtils.isNotEmpty(initIdleCapacity) ? Integer.parseInt(initIdleCapacity) : 50;
        } catch (NumberFormatException nfe) {
            log.warn("Invalid InitIdleCapacity '" + initIdleCapacity + "'. Falling back to 50");
            initIdleCapSize = 50;
        }

        if (log.isDebugEnabled()) {
            log.debug("BasicAuth client pool configuration - maxIdle: " + maxIdle +
                    ", initIdleCapacity: " + initIdleCapSize);
        }

        // Create GenericObjectPool with Commons Pool 2 configuration
        GenericObjectPoolConfig<BasicAuthClient> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(0);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(false);
        poolConfig.setTestWhileIdle(false);

        basicAuthClientPool = new GenericObjectPool<>(new BasePooledObjectFactory<BasicAuthClient>() {
            @Override
            public BasicAuthClient create() throws Exception {

                if (log.isDebugEnabled()) {
                    log.debug("Initializing new BasicAuthClient instance");
                }
                return new BasicAuthClient();
            }

            @Override
            public PooledObject<BasicAuthClient> wrap(BasicAuthClient obj) {

                return new DefaultPooledObject<>(obj);
            }
        }, poolConfig);

        // Pre-fill idle objects to honor initIdleCapacity
        for (int i = 0; i < initIdleCapSize; i++) {
            try {
                basicAuthClientPool.addObject();
            } catch (Exception ignore) {
                break;
            }
        }
    }

    /**
     * Get the singleton instance of BasicAuthClientPool.
     *
     * @return BasicAuthClientPool instance
     */
    public static BasicAuthClientPool getInstance() {

        return Holder.INSTANCE;
    }

    /**
     * Get a BasicAuthClient from the pool.
     *
     * @return BasicAuthClient instance
     * @throws Exception if unable to get client from pool
     */
    public BasicAuthClient get() throws Exception {

        if (log.isTraceEnabled()) {
            int active = basicAuthClientPool.getNumActive();
            if (active >= maxIdle) {
                log.trace("BasicAuth validation pool size is: " + active);
            }
        }
        return basicAuthClientPool.borrowObject();
    }

    /**
     * Return a BasicAuthClient to the pool.
     *
     * @param client the BasicAuthClient to return
     * @throws Exception if unable to return client to pool
     */
    public void release(BasicAuthClient client) throws Exception {

        if (client != null) {
            basicAuthClientPool.returnObject(client);
        }
    }

    /**
     * Cleanup the pool and release all resources.
     */
    public void cleanup() {

        try {
            basicAuthClientPool.close();
            log.info("BasicAuthClientPool cleaned up successfully");
        } catch (Exception e) {
            log.warn("Error while cleaning up the BasicAuth client pool", e);
        }
    }
}
