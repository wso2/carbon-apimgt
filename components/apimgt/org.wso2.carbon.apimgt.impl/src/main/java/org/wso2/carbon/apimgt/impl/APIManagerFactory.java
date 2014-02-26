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

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.APIManager;
import org.wso2.carbon.apimgt.impl.utils.LRUCache;

import java.util.Map;

public class APIManagerFactory {

    private static final Log log = LogFactory.getLog(APIManagerFactory.class);

    private static final String ANONYMOUS_USER = "__wso2.am.anon__";

    private static final APIManagerFactory instance = new APIManagerFactory();

    private APIManagerCache<APIProvider> providers = new APIManagerCache<APIProvider>(50);
    private APIManagerCache<APIConsumer> consumers = new APIManagerCache<APIConsumer>(500);

    private APIManagerFactory() {

    }

    public static APIManagerFactory getInstance() {
        return instance;
    }

    private APIProvider newProvider(String username) throws APIManagementException {
        return new UserAwareAPIProvider(username);
    }

    private APIConsumer newConsumer(String username) throws APIManagementException {
        if (username.equals(ANONYMOUS_USER)) {
            username = null;
        }
        return new UserAwareAPIConsumer(username);

    }

    public APIProvider getAPIProvider(String username) throws APIManagementException {
        APIProvider provider = providers.get(username);
        if (provider == null) {
            synchronized (username.intern()) {
                provider = providers.get(username);
                if (provider != null) {
                    return provider;
                }

                provider = newProvider(username);
                providers.put(username, provider);
            }
        }
        return provider;
    }

    public APIConsumer getAPIConsumer() throws APIManagementException {
        return getAPIConsumer(ANONYMOUS_USER);
    }

    public APIConsumer getAPIConsumer(String username) throws APIManagementException {
        APIConsumer consumer = consumers.get(username);
        if (consumer == null) {
            synchronized (username.intern()) {
                consumer = consumers.get(username);
                if (consumer != null) {
                    return consumer;
                }

                consumer = newConsumer(username);
                consumers.put(username, consumer);
            }
        }
        return consumer;
    }

    public void clearAll() {
        consumers.exclusiveLock();
        try {
            for (APIConsumer consumer : consumers.values()) {
                cleanupSilently(consumer);
            }
            consumers.clear();
        } finally {
            consumers.release();
        }

        providers.exclusiveLock();
        try {
            for (APIProvider provider : providers.values()) {
                cleanupSilently(provider);
            }
            providers.clear();
        } finally {
            providers.release();
        }
    }

    private void cleanupSilently(APIManager manager) {
        if (manager != null) {
            try {
                manager.cleanup();
            } catch (APIManagementException ignore) {

            }
        }
    }

    private class APIManagerCache<T> extends LRUCache<String,T> {

        public APIManagerCache(int maxEntries) {
            super(maxEntries);
        }

        protected void handleRemovableEntry(Map.Entry<String,T> entry) {
            try {
                ((APIManager) entry.getValue()).cleanup();
            } catch (APIManagementException e) {
                log.warn("Error while cleaning up APIManager instance", e);
            }
        }
    }
}
