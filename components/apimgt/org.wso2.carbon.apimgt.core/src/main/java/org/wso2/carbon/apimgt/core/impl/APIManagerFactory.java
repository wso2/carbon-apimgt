/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIConsumer;
import org.wso2.carbon.apimgt.core.api.APIManager;
import org.wso2.carbon.apimgt.core.api.APIProvider;
import org.wso2.carbon.apimgt.core.dao.APIManagementDAOException;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.util.Map;

/**
 * 
 * Creates API Producers and API Consumers.
 *
 */
public class APIManagerFactory {

    private static final Logger log = LoggerFactory.getLogger(APIManagerFactory.class);

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
        try {
            return new UserAwareAPIProvider(username, DAOFactory.getApiDAO(), DAOFactory.getApplicationDAO(),
                    DAOFactory.getAPISubscriptionDAO());
        } catch (APIManagementDAOException e) {
            APIUtils.logAndThrowException("Couldn't Create API Provider", log);
        }
        return null;
    }

    private APIConsumer newConsumer(String username) throws APIManagementException {
        // if (username.equals(ANONYMOUS_USER)) {
        // username = null;
        // }
        try {
            return new UserAwareAPIConsumer(username, DAOFactory.getApiDAO(), DAOFactory.getApplicationDAO(),
                    DAOFactory.getAPISubscriptionDAO());
        } catch (APIManagementDAOException e) {
            APIUtils.logAndThrowException("Couldn't Create API Consumer", log);
        }
        return null;

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
        // if (manager != null) {
        // try {
        // manager.cleanup();
        // } catch (APIManagementException ignore) {
        //
        // }
        // }
    }

    private static class APIManagerCache<T> extends LRUCache<String, T> {

        private static final long serialVersionUID = -232359641908536526L;

        public APIManagerCache(int maxEntries) {
            super(maxEntries);
        }

        protected void handleRemovableEntry(Map.Entry<String, T> entry) {
            log.warn(" To be implemented ");
            // try {
            // ((APIManager) entry.getValue()).cleanup();
            // } catch (APIManagementException e) {
            // log.warn("Error while cleaning up APIManager instance", e);
            // }
        }
    }
}
