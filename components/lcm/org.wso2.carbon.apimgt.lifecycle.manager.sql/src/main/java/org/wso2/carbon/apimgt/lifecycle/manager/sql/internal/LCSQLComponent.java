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
package org.wso2.carbon.apimgt.lifecycle.manager.sql.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.lifecycle.manager.core.services.LCManagerService;
import org.wso2.carbon.apimgt.lifecycle.manager.core.services.LCManagerServiceImpl;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.JDBCPersistenceManager;
import org.wso2.carbon.ndatasource.core.DataSourceService;

/**
 *
 * @scr.component name="org.wso2.carbon.social.component" immediate="true"
 * @scr.reference name="datasources.service"
 *                interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setDataSourceService" unbind="unsetDataSourceService"
 **/

public class LCSQLComponent {

    private static Log log = LogFactory.getLog(LCSQLComponent.class);

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(LCManagerService.class, new LCManagerServiceImpl(), null);
        if (log.isDebugEnabled()) {
            log.debug("Social Activity service is activated  with SQL Implementation");
        }

        JDBCPersistenceManager jdbcPersistenceManager;
        try {
            jdbcPersistenceManager = JDBCPersistenceManager.getInstance();
            jdbcPersistenceManager.initializeDatabase();
        } catch (Exception e) {
            log.error("Failed to initilize database. " + e);
        }
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the DataSourceService");
        }
        JDBCPersistenceManager.setCarbonDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the DataSourceService");
        }
        JDBCPersistenceManager.setCarbonDataSourceService(null);
    }
}
