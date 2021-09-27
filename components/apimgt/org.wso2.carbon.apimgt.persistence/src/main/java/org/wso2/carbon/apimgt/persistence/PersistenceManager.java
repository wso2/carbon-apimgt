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

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.persistence.internal.ServiceReferenceHolder;

public class PersistenceManager {
    private static final Log log = LogFactory.getLog(PersistenceManager.class);
    
    private static APIPersistence persistence = null;

    public static APIPersistence getPersistenceInstance(Map<String, String> configs, Properties properties) {
        if (log.isDebugEnabled()) {
            log.debug("Persistence configs " + Arrays.asList(configs));
        }
        if (persistence == null) {
            ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();
            serviceReferenceHolder.setPersistenceConfigs(configs);
            if (serviceReferenceHolder.getApiPersistence() != null) {
                persistence = serviceReferenceHolder.getApiPersistence();
            } else {
                if (persistence == null) {
                    persistence = new RegistryPersistenceImpl(properties);
                }
            }
        }
        return persistence;
    }
}
