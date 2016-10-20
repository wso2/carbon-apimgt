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

package org.wso2.carbon.apimgt.impl.caching;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Can be used in Store for Invalidating Gateway Cache. When calling {@code invalidateCacheForApp} a new task will be
 * created for fetching active token created for the App and deleting those from the Cache. This thread will run
 * asynchronously.
 */
public class CacheInvalidator {

    private static final Log log = LogFactory.getLog(CacheInvalidator.class);


    // Cached Thread pool was used, since removing/adding subscriptions is not a frequent operation,
    // and we don't want a fix number of threads running all the time.
    private Executor cacheInvalidationPool = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("Store-CacheInvalidation");
            return thread;
        }
    });

    private CacheInvalidator() {
    }

    private static class CacheInvalidationHolder {
        private static final CacheInvalidator INSTANCE = new CacheInvalidator();
    }

    public static CacheInvalidator getInstance() {
        return CacheInvalidationHolder.INSTANCE;
    }

    /**
     * When called an async thread will be created to invalidate the token for the App.
     *
     * @param appId
     */
    public void invalidateCacheForApp(int appId) {
        CacheInvalidationTask task = new CacheInvalidationTask();
        task.setAppId(appId);
        cacheInvalidationPool.execute(task);
    }

    private class CacheInvalidationTask implements Runnable {

        private int appId = -1;

        public void setAppId(int appId) {
            this.appId = appId;
        }

        @Override
        public void run() {

            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();

            // If Gateway environments are not set, Cache invalidation will not happen.
            if (config == null || config.getApiGatewayEnvironments().isEmpty()) {
                return;
            }

            Set<String> consumerKeys = null;
            Set<String> activeTokens = null;

            try {
                consumerKeys = ApiMgtDAO.getInstance().getConsumerKeysOfApplication(appId);
                activeTokens = new HashSet<String>();
                for (String consumerKey : consumerKeys) {
                    Set<String> tempTokens = KeyManagerHolder.getKeyManagerInstance().
                            getActiveTokensByConsumerKey(consumerKey);
                    if (tempTokens != null) {
                        activeTokens.addAll(tempTokens);
                    }
                }
            } catch (APIManagementException e) {
                log.error("Error occurred while getting Active Tokens", e);
                return;
            }

            if (activeTokens.isEmpty()) {
                return;
            }

            Map<String, Environment> gatewayEnvs = config.getApiGatewayEnvironments();

            for (Environment environment : gatewayEnvs.values()) {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Started invalidation tokens for environment: " + environment.getName());
                    }
                    APIAuthenticationAdminClient client = new APIAuthenticationAdminClient(environment);
                    client.invalidateCachedTokens(activeTokens);
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully called AdminService for revoking tokens : " + environment.getName());
                    }
                } catch (AxisFault axisFault) {
                    //log and ignore since we do not have to halt the user operation due to cache invalidation failures.
                    log.error("Error occurred while invalidating Token Cache for environment " + environment.getName(),
                              axisFault);
                }
            }

            return;

        }
    }


}
