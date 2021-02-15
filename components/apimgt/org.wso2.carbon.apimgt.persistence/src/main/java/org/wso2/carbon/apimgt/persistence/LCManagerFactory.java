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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.persistence.exceptions.PersistenceException;
import org.wso2.carbon.apimgt.persistence.utils.RegistryLCManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.xml.sax.SAXException;

public class LCManagerFactory {

    private static final Log log = LogFactory.getLog(LCManagerFactory.class);
    private static LCManagerFactory instance;

    private LCManagerFactory() {

    }

    public static LCManagerFactory getInstance() {
        if (instance == null) {
            instance = new LCManagerFactory();
        }
        return instance;
    }

    public RegistryLCManager getLCManager() throws PersistenceException {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String cacheName = tenantDomain + "_" + APIConstants.LC_CACHE_NAME;
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Cache lcCache = Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER)
                .getCache(APIConstants.LC_CACHE_NAME);
        RegistryLCManager lcManager = (RegistryLCManager) lcCache.get(cacheName);
        if (lcManager != null) {
            log.debug("Lifecycle info servered from Cache.");
            return lcManager;
        } else {
            try {
                log.debug("Lifecycle info not found in Cache.");
                lcManager = new RegistryLCManager(tenantId);
                lcCache.put(cacheName, lcManager);
                return lcManager;
            } catch (RegistryException | XMLStreamException | ParserConfigurationException | SAXException
                    | IOException e) {
                throw new PersistenceException("Error while accessing the lifecycle resource ", e);
            }
        }
    }
}
