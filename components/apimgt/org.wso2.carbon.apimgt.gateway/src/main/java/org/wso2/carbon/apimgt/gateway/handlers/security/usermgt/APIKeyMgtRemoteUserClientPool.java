/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.usermgt;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;

public class APIKeyMgtRemoteUserClientPool {
    private static final Log log = LogFactory.getLog(APIKeyMgtRemoteUserClientPool.class);

    private static final APIKeyMgtRemoteUserClientPool instance = new APIKeyMgtRemoteUserClientPool();

    private final ObjectPool clientPool;
    private static int maxIdle;

    private APIKeyMgtRemoteUserClientPool() {
        String maxIdleClients = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getFirstProperty
                ("APIKeyValidator.ConnectionPool.MaxIdle");
        String initIdleCapacity = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getFirstProperty
                ("APIKeyValidator.ConnectionPool.InitIdleCapacity");
        int initIdleCapSize;
        if (StringUtils.isNotEmpty(maxIdleClients)) {
            maxIdle = Integer.parseInt(maxIdleClients);
        } else {
            maxIdle = 50;
        }
        if (StringUtils.isNotEmpty(initIdleCapacity)) {
            initIdleCapSize = Integer.parseInt(initIdleCapacity);
        } else {
            initIdleCapSize = 20;
        }
        log.debug("Initializing remote user client pool");
        clientPool = new StackObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                log.debug("Initializing new APIKeyMgtRemoteUserClient instance");
                return new APIKeyMgtRemoteUserClient();
            }
        }, maxIdle, initIdleCapSize);
    }

    public static APIKeyMgtRemoteUserClientPool getInstance() {
        return instance;
    }

    public APIKeyMgtRemoteUserClient get() throws Exception {
        if (log.isTraceEnabled()) {
            int active = clientPool.getNumActive();
            if (active >= maxIdle) {
                log.trace("Remote user pool size is :" + active);
            }
        }
        return (APIKeyMgtRemoteUserClient) clientPool.borrowObject();
    }

    public void release(APIKeyMgtRemoteUserClient client) throws Exception {
        clientPool.returnObject(client);
    }

    public void cleanup() {
        try {
            clientPool.close();
        } catch (Exception e) {
            log.warn("Error while cleaning up the object pool", e);
        }
    }
}
