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

package org.wso2.carbon.apimgt.keymgt.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;

public class SubscriberKeyMgtClientPool {

    private static final Log log = LogFactory.getLog(SubscriberKeyMgtClientPool.class);

    private final ObjectPool clientPool;

    private String serverURL;

    private String username;

    private String password;

    public SubscriberKeyMgtClientPool() {
        log.debug("Initializing API Key Management Client Pool");
        clientPool = new StackObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                log.debug("Initializing new SubscriberKeyMgtClient instance");
                return new SubscriberKeyMgtClient(serverURL, username, password);
            }
        }, 20, 5);
    }


    public SubscriberKeyMgtClient get() throws Exception {
        return (SubscriberKeyMgtClient) clientPool.borrowObject();
    }

    public void release(SubscriberKeyMgtClient client) {
        try {
            clientPool.returnObject(client);
        } catch (Exception e) {
            log.error("Error occurred while returning client back to pool.");
        }
    }

    public void cleanup() {
        try {
            clientPool.close();
        } catch (Exception e) {
            log.warn("Error while cleaning up the object pool", e);
        }
    }

    public void setConfiguration(KeyManagerConfiguration configuration) {
        this.serverURL = (String) configuration.getParameter("ServerURL");
        this.username = (String) configuration.getParameter("Username");
        this.password = (String) configuration.getParameter("Password");
    }
}
