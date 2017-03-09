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
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;

import java.util.Collections;
import java.util.LinkedHashMap;
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

    private APIMgtAdminService apiMgtAdminService;

    private static final int MAX_PROVIDERS = 50;
    private static final int MAX_CONSUMERS = 500;

    // Thread safe Cache for API Providers
    private static Map<String, APIPublisher> providers =
            Collections.synchronizedMap(new LinkedHashMap<String, APIPublisher>
                                        (MAX_PROVIDERS + 1, 1, false) {
        // This method is called just after a new entry has been added
        @Override
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_PROVIDERS;
        }
    });

    // Thread safe Cache for API Consumers
    private static Map<String, APIStore> consumers = Collections.synchronizedMap(new LinkedHashMap<String, APIStore>
            (MAX_CONSUMERS + 1, 1, false) {
        // This method is called just after a new entry has been added
        @Override
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_CONSUMERS;
        }
    });


    private APIManagerFactory() {

    }

    public static APIManagerFactory getInstance() {
        return instance;
    }

    private APIPublisher newProvider(String username) throws APIManagementException {
        try {
            UserAwareAPIPublisher userAwareAPIPublisher = new UserAwareAPIPublisher(username, DAOFactory.getApiDAO(),
                    DAOFactory.getApplicationDAO(), DAOFactory.getAPISubscriptionDAO(), DAOFactory.getPolicyDAO(),
                    DAOFactory.getLabelDAO());

            // Register all the observers which need to observe 'Publisher' component
            userAwareAPIPublisher.registerObserver(new EventLogger());
            userAwareAPIPublisher.registerObserver(new FunctionTrigger(DAOFactory.getFunctionDAO(),
                    new RestCallUtilImpl()));

            return userAwareAPIPublisher;
        } catch (APIMgtDAOException e) {
            log.error("Couldn't Create API Provider", e);
            throw new APIMgtDAOException("Couldn't Create API Provider", ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

    }

    private APIMgtAdminServiceImpl newAPIMgtAdminService() throws APIManagementException {
        try {
            return new APIMgtAdminServiceImpl(DAOFactory.getAPISubscriptionDAO(), DAOFactory.getPolicyDAO(),
                    DAOFactory.getApiDAO());
        } catch (APIMgtDAOException e) {
            log.error("Couldn't create API Management Admin Service", e);
            throw new APIMgtDAOException("Couldn't create API Management Admin Service",
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }

    }

    private APIStore newConsumer(String username) throws APIManagementException {
        // if (username.equals(ANONYMOUS_USER)) {
        // username = null;
        // }
        try {
            UserAwareAPIStore userAwareAPIStore = new UserAwareAPIStore(username, DAOFactory.getApiDAO(),
                    DAOFactory.getApplicationDAO(), DAOFactory.getAPISubscriptionDAO(),
                    DAOFactory.getPolicyDAO(), DAOFactory.getTagDAO(), DAOFactory.getLabelDAO());

            // Register all the observers which need to observe 'Store' component
            userAwareAPIStore.registerObserver(new EventLogger());
            userAwareAPIStore.registerObserver(new FunctionTrigger(DAOFactory.getFunctionDAO(),
                    new RestCallUtilImpl()));

            return userAwareAPIStore;
        } catch (APIMgtDAOException e) {
            log.error("Couldn't Create API Consumer", e);
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

    public APIMgtAdminService getAPIMgtAdminService() throws APIManagementException {
        if (apiMgtAdminService == null) {
            synchronized (this) {
                apiMgtAdminService = newAPIMgtAdminService();
            }
        }
        return apiMgtAdminService;
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

    public APIGatewayPublisher getGateway() {
        return new APIGatewayPublisherImpl();
    }
}
