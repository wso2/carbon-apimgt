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

package org.wso2.carbon.apimgt.keymgt.internal;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.handlers.DefaultKeyValidationHandler;
import org.wso2.carbon.apimgt.keymgt.handlers.KeyValidationHandler;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceReferenceHolder {
    private static final Log log = LogFactory.getLog(ServiceReferenceHolder.class);
    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private APIManagerConfigurationService amConfigurationService;
    private OutputEventAdapterService outputEventAdapterService;
    private Map<String, KeyValidationHandler> keyValidationHandlerMap = new ConcurrentHashMap<>();
    private RealmService realmService;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public APIManagerConfigurationService getAPIManagerConfigurationService() {
        return amConfigurationService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigurationService) {
        this.amConfigurationService = amConfigurationService;
    }

    public OutputEventAdapterService getOutputEventAdapterService() {
        return outputEventAdapterService;
    }

    public void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        this.outputEventAdapterService = outputEventAdapterService;
    }

    public void addKeyValidationHandler(String tenantDomain, KeyValidationHandler keyValidationHandler) {

        keyValidationHandlerMap.put(tenantDomain, keyValidationHandler);
    }

    public void removeKeyValidationHandler(String tenantDomain) {

        keyValidationHandlerMap.remove(tenantDomain);
    }

    public KeyValidationHandler getKeyValidationHandler(String tenantDomain) {

        if (keyValidationHandlerMap.containsKey(tenantDomain)) {
            return keyValidationHandlerMap.get(tenantDomain);
        }

        KeyValidationHandler keyValidationHandler = null;
        String className = amConfigurationService.getAPIManagerConfiguration().getFirstProperty
                (APIConstants.KEY_VALIDATION_HANDLER_CLASSNAME);
        try {
            if (StringUtils.isNotEmpty(className)) {
                keyValidationHandler = (KeyValidationHandler) APIUtil.getClassInstance(className);
            } else {
                keyValidationHandler = new DefaultKeyValidationHandler();
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            log.error("Key validation handler object creation error", e);
        }
        keyValidationHandlerMap.put(tenantDomain, keyValidationHandler);
        return keyValidationHandler;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }
}
