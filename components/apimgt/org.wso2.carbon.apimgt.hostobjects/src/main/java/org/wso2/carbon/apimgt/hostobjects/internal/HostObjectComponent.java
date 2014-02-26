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
package org.wso2.carbon.apimgt.hostobjects.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.hostobjects.APIStoreHostObject;
import org.wso2.carbon.apimgt.hostobjects.HostObjectUtils;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="org.wso2.apimgt.hostobjects" immediate="true"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 */
public class HostObjectComponent {

    private static final Log log = LogFactory.getLog(HostObjectComponent.class);

    private static APIManagerConfiguration configuration = null;

    protected void activate(ComponentContext componentContext) {
       if (log.isDebugEnabled()){
           log.debug("HostObjectComponent activated");
       }
    }

    protected void deactivate(ComponentContext componentContext) {
       if (log.isDebugEnabled()){
           log.debug("HostObjectComponent deactivated");
       }
    }

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service bound to the API host objects");
        }
        configuration = amcService.getAPIManagerConfiguration();
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from the API host objects");
        }
        configuration = null;
    }

    public static APIManagerConfiguration getAPIManagerConfiguration() {
        return configuration;
    }

     protected void setConfigurationContextService(ConfigurationContextService configCtx) {
        HostObjectUtils.setConfigContextService(configCtx);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configCtx) {
        HostObjectUtils.setConfigContextService(null);
    }
}
