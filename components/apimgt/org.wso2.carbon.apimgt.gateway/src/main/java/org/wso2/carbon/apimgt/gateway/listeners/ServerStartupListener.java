/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.service.APIThrottleDataServiceImpl;
import org.wso2.carbon.apimgt.impl.caching.CacheInvalidationServiceImpl;
import org.wso2.carbon.apimgt.gateway.service.RevokedTokenDataImpl;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.gateway.webhooks.SubscriptionsDataServiceImpl;
import org.wso2.carbon.apimgt.impl.caching.CacheInvalidationService;
import org.wso2.carbon.apimgt.impl.webhooks.SubscriptionsDataService;
import org.wso2.carbon.core.ServerStartupObserver;

public class ServerStartupListener implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(ServerStartupListener.class);

    @Override
    public void completingServerStartup() {
    }

    @Override
    public void completedServerStartup() {
        // This prevents errors in an All in one setup caused by the ThrottleDataPublisher trying to connect to the
        // event receiver, before the event receiver has been started on completion of server startup.
        ServiceReferenceHolder.getInstance().setThrottleDataPublisher(new ThrottleDataPublisher());
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
        APIThrottleDataServiceImpl throttleDataServiceImpl =
                new APIThrottleDataServiceImpl(throttleDataHolder);
        CacheInvalidationService cacheInvalidationService = new CacheInvalidationServiceImpl();
        // Register APIThrottleDataService so that ThrottleData maps are available to other components.
        ServiceReferenceHolder.getInstance().setCacheInvalidationService(cacheInvalidationService);
        ServiceReferenceHolder.getInstance().setAPIThrottleDataService(throttleDataServiceImpl);
        ServiceReferenceHolder.getInstance().setThrottleDataHolder(throttleDataHolder);
        ServiceReferenceHolder.getInstance().setRevokedTokenService(new RevokedTokenDataImpl());
        SubscriptionsDataService subscriptionsDataService = new SubscriptionsDataServiceImpl();
        ServiceReferenceHolder.getInstance().setSubscriptionsDataService(subscriptionsDataService);
        log.debug("APIThrottleDataService Registered...");
    }
}

