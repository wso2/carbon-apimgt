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

package org.wso2.carbon.apimgt.gateway.handlers.security.keys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;

public class APIKeyValidatorClientPool {

    private static final Log log = LogFactory.getLog(APIKeyValidatorClientPool.class);

    private static final APIKeyValidatorClientPool instance = new APIKeyValidatorClientPool();

    private final ObjectPool clientPool;

    private APIKeyValidatorClientPool() {
        log.debug("Initializing API key validator client pool");
        clientPool = new StackObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                log.debug("Initializing new APIKeyValidatorClient instance");
                return new APIKeyValidatorClient();
            }
        }, 50, 20);
    }

    public static APIKeyValidatorClientPool getInstance() {
        return instance;
    }

    public APIKeyValidatorClient get() throws Exception {
        return (APIKeyValidatorClient) clientPool.borrowObject();
    }

    public void release(APIKeyValidatorClient client) throws Exception {
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
