/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.internal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.tasks.UploadedUsagePublisherExecutorTask;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.util.MicroGatewayAPIUsageConstants;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.service.RealmService;
import java.util.Timer;
import java.util.TimerTask;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "micro.api.gateway.usage.component", 
         immediate = true)
public class APIUsagePublisherComponent {

    private static final Log log = LogFactory.getLog(APIUsagePublisherComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {
        // usage data publishing is enabled thorough a property.
        try {
            ConfigManager configManager = ConfigManager.getConfigManager();
            String isUsageDataPublishingEnabled = configManager.getProperty(MicroGatewayAPIUsageConstants.IS_UPLOADED_USAGE_DATA_PUBLISH_ENABLED_PROPERTY);
            if (StringUtils.equals("true", isUsageDataPublishingEnabled)) {
                int usagePublishFrequency = MicroGatewayAPIUsageConstants.DEFAULT_UPLOADED_USAGE_PUBLISH_FREQUENCY;
                String usagePublishFrequencyProperty = configManager.getProperty(MicroGatewayAPIUsageConstants.UPLOADED_USAGE_PUBLISH_FREQUENCY_PROPERTY);
                if (StringUtils.isNotBlank(usagePublishFrequencyProperty)) {
                    try {
                        usagePublishFrequency = Integer.parseInt(usagePublishFrequencyProperty);
                    } catch (NumberFormatException e) {
                        log.error("Error while parsing the system property: " + MicroGatewayAPIUsageConstants.UPLOADED_USAGE_PUBLISH_FREQUENCY_PROPERTY + " to integer. Using default usage publish frequency configuration: " + MicroGatewayAPIUsageConstants.DEFAULT_UPLOADED_USAGE_PUBLISH_FREQUENCY, e);
                    }
                }
                TimerTask usagePublisherTask = new UploadedUsagePublisherExecutorTask();
                Timer timer = new Timer();
                timer.schedule(usagePublisherTask, 0, usagePublishFrequency);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Micro GW API Usage data publishing is disabled.");
                }
            }
        } catch (OnPremiseGatewayException e) {
            log.error("Unexpected error occurred while reading properties from the config file. Micro GW API Usage " + "data publishing is disabled.", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Micro gateway API Usage Publisher bundle is activated.");
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Micro gateway API Usage Publisher bundle is de-activated ");
        }
    }

    /**
     * Set APIManager Configuration service to the bundle's {@link ServiceReferenceHolder}
     *
     * @param service APIManager Configuration service
     */
    @Reference(
             name = "api.manager.config.service", 
             service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService service) {
        log.debug("API manager configuration service bound to the API usage handler");
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(service);
    }

    /**
     * Unset APIManager Configuration service in the bundle's {@link ServiceReferenceHolder}
     *
     * @param service APIManager Configuration service
     */
    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {
        log.debug("API manager configuration service unbound from the API usage handler");
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

    /**
     * Set the RealmService to the the bundle's {@link ServiceReferenceHolder}
     *
     * @param realmService Realm Service
     */
    @Reference(
             name = "user.realm.service", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("Realm service initialized");
        }
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unset the RealmService in the the bundle's {@link ServiceReferenceHolder}
     *
     * @param realmService Realm Service
     */
    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
    }

    /**
     * Set task service
     *
     * @param taskService task service
     * @throws RegistryException
     */
    @Reference(
             name = "ntask.component", 
             service = org.wso2.carbon.ntask.core.service.TaskService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetTaskService")
    protected void setTaskService(TaskService taskService) throws RegistryException {
        if (log.isDebugEnabled()) {
            log.debug("TaskService is acquired");
        }
        ServiceReferenceHolder.getInstance().setTaskService(taskService);
    }

    /**
     * Remove task service
     *
     * @param taskService task service
     */
    protected void unsetTaskService(TaskService taskService) {
        ServiceReferenceHolder.getInstance().setTaskService(null);
    }
}

