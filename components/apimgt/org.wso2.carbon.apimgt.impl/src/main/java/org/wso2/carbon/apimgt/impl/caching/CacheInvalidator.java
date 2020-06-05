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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIKey;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.HashSet;
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
            Set<String> activeTokens = new HashSet<>();

            try {
                consumerKeys = ApiMgtDAO.getInstance().getConsumerKeysOfApplication(appId);
                Application application = ApiMgtDAO.getInstance().getLightweightApplicationById(appId);
                String tenantDomain = MultitenantUtils.getTenantDomain(application.getSubscriber().getName());
                Set<APIKey> keyMappingsFromApplicationId =
                        ApiMgtDAO.getInstance().getKeyMappingsFromApplicationId(appId);
                for (APIKey apiKey : keyMappingsFromApplicationId) {
                    KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance(tenantDomain,apiKey.getKeyManager());
                    if (keyManager != null) {
                        for (String consumerKey : consumerKeys) {
                            Set<String> tempTokens;
                            tempTokens = keyManager.getActiveTokensByConsumerKey(consumerKey);
                            if (tempTokens != null) {
                                activeTokens.addAll(tempTokens);
                            }
                        }
                    }
                }
            } catch (APIManagementException e) {
                log.error("Error occurred while getting Active Tokens", e);
                return;
            }

            if (activeTokens.isEmpty()) {
                return;
            }

            APIAuthenticationAdminClient client = new APIAuthenticationAdminClient();
            client.invalidateCachedTokens(activeTokens);
            return;

        }
    }
}
