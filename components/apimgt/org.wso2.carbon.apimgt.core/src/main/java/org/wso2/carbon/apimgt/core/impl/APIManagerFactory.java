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
import org.wso2.carbon.apimgt.core.api.APIGatewayPublisher;
import org.wso2.carbon.apimgt.core.api.APIManager;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;

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

    private APIManagerCache<APIPublisher> providers = new APIManagerCache<>(50);
    private APIManagerCache<APIStore> consumers = new APIManagerCache<>(500);

    private APIManagerFactory() {

    }

    public static APIManagerFactory getInstance() {
        return instance;
    }

    private APIPublisher newProvider(String username) throws APIManagementException {
        try {
            return new UserAwareAPIPublisher(username, DAOFactory.getApiDAO(), DAOFactory.getApplicationDAO(),
                    DAOFactory.getAPISubscriptionDAO(), DAOFactory.getPolicyDAO());
        } catch (APIMgtDAOException e) {
            log.error("Couldn't Create API Provider");
            throw new APIMgtDAOException("Couldn't Create API Provider", ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

    }

    private APIStore newConsumer(String username) throws APIManagementException {
        // if (username.equals(ANONYMOUS_USER)) {
        // username = null;
        // }
        try {
            return new UserAwareAPIStore(username, DAOFactory.getApiDAO(), DAOFactory.getApplicationDAO(),
                    DAOFactory.getAPISubscriptionDAO(), DAOFactory.getPolicyDAO());

        } catch (APIMgtDAOException e) {
            log.error("Couldn't Create API Consumer");
            throw new APIMgtDAOException("Couldn't Create API Consumer", ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    public APIPublisher getAPIProvider(String username) throws APIManagementException {
        APIPublisher provider = providers.get(username);
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

    public APIStore getAPIConsumer() throws APIManagementException {
        return getAPIConsumer(ANONYMOUS_USER);
    }

    public APIStore getAPIConsumer(String username) throws APIManagementException {
        APIStore consumer = consumers.get(username);
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
            for (APIStore consumer : consumers.values()) {
                cleanupSilently(consumer);
            }
            consumers.clear();
        } finally {
            consumers.release();
        }

        providers.exclusiveLock();
        try {
            for (APIPublisher provider : providers.values()) {
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

    public APIGatewayPublisher getGateway() {
        return new APIGatewayPublisherImpl();
    }
}
