/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.apimgt.core.services", 
         immediate = true)
public class APPManagerCoreComponent {

    // TODO refactor caching implementation
    private static final Log log = LogFactory.getLog(APPManagerCoreComponent.class);

    private static APIManagerConfiguration configuration = null;

    @Activate
    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("API Manager Core Component activated");
        }
        log.info("API Manager Core Component started successfully");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("API Manager Core Component deactivated");
        }
        log.info("API Manager Core Component stopped");
    }

    @Reference(
             name = "api.manager.config.service", 
             service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API Manager configuration service bound to the API Manager core component");
        }
        if (amcService != null) {
            configuration = amcService.getAPIManagerConfiguration();
            ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
            log.info("API Manager configuration service bound successfully");
        } else {
            log.warn("Null API Manager configuration service provided during binding");
        }
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API Manager configuration service unbound from the API Manager core component");
        }
        configuration = null;
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
        log.info("API Manager configuration service unbound successfully");
    }
}

