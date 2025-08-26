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
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

/**
 * Pool for managing BasicAuthClient instances to improve performance and resource utilization.
 */
public class BasicAuthClientPool {

    private static final Log log = LogFactory.getLog(BasicAuthClientPool.class);

    private static final BasicAuthClientPool instance = new BasicAuthClientPool();

    private final ObjectPool basicAuthClientPool;
    private static int maxIdle;

    private BasicAuthClientPool() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        String maxIdleClients = config.getFirstProperty("BasicAuthValidator.ConnectionPool.MaxIdle");
        String initIdleCapacity = config.getFirstProperty("BasicAuthValidator.ConnectionPool.InitIdleCapacity");
        int initIdleCapSize;

        if (StringUtils.isNotEmpty(maxIdleClients)) {
            maxIdle = Integer.parseInt(maxIdleClients);
        } else {
            maxIdle = 100;
        }

        if (StringUtils.isNotEmpty(initIdleCapacity)) {
            initIdleCapSize = Integer.parseInt(initIdleCapacity);
        } else {
            initIdleCapSize = 50;
        }

        if (log.isDebugEnabled()) {
            log.debug("BasicAuth client pool configuration - maxIdle: " + maxIdle +
                    ", initIdleCapacity: " + initIdleCapSize);
        }

        basicAuthClientPool = new StackObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing new BasicAuthClient instance");
                }
                return new BasicAuthClient();
            }
        }, maxIdle, initIdleCapSize);
    }

    /**
     * Get the singleton instance of BasicAuthClientPool.
     *
     * @return BasicAuthClientPool instance
     */
    public static BasicAuthClientPool getInstance() {
        return instance;
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
        return (BasicAuthClient) basicAuthClientPool.borrowObject();
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
        } catch (Exception e) {
            log.warn("Error while cleaning up the BasicAuth client pool", e);
        }
    }
}
