/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.lifecycle.manager.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifeCycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.services.LCManagementService;
import org.wso2.carbon.apimgt.lifecycle.manager.core.services.LCManagementServiceImpl;
import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LCUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.exception.LCManagerDatabaseException;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.utils.LCMgtDBUtil;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 *
 * @scr.component name="org.wso2.carbon.lifecycle.component" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 **/

public class LCServiceComponent {

    private static Log log = LogFactory.getLog(LCServiceComponent.class);

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(LCManagementService.class, new LCManagementServiceImpl(), null);
        if (log.isDebugEnabled()) {
            log.debug("Social Activity service is activated  with SQL Implementation");
        }

        try {
            LCMgtDBUtil.initialize();
            LCUtils.initiateLCMap();

        } catch (LCManagerDatabaseException e) {
            log.error("Failed to initialize database. " + e);
        } catch (LifeCycleException e) {
            log.error("Failed to initialize lifecycle map for all tenants. " + e);
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.getInstance().setContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.getInstance().setContextService(null);
    }
}

