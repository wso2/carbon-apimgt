/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.persistence;

import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;

import java.util.Map;
import java.util.Properties;

public class PersistenceManager {

    private static APIPersistence apiPersistenceInstance;

    public static APIPersistence getPersistenceInstance(Map<String, String> configs, Properties properties) {

        synchronized (RegistryPersistenceImpl.class) {
            if (apiPersistenceInstance == null) {
                String databaseType = configs.get(PersistenceConstants.REGISTRY_CONFIG_TYPE);
                ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();
                serviceReferenceHolder.setPersistenceConfigs(configs);
                if (PersistenceConstants.REGISTRY_CONFIG_TYPE_MONGODB.equalsIgnoreCase(databaseType)) {
                    apiPersistenceInstance = serviceReferenceHolder.getApiPersistence();
                } else {
                    if (apiPersistenceInstance == null) {
                        apiPersistenceInstance = new RegistryPersistenceImpl(properties);
                    }
                }
            }
            return apiPersistenceInstance;
        }
    }
}
