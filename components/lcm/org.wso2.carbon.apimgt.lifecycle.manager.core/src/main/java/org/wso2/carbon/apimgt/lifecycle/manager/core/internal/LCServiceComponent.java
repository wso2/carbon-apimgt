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
import org.wso2.carbon.apimgt.lifecycle.manager.core.LCCrudManager;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifeCycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.services.LCManagementService;
import org.wso2.carbon.apimgt.lifecycle.manager.core.services.LCManagementServiceImpl;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.JDBCPersistenceManager;
import org.wso2.carbon.ndatasource.core.DataSourceService;

/**
 *
 * @scr.component name="org.wso2.carbon.lifecycle.component" immediate="true"
 * @scr.reference name="datasources.service"
 *                interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setDataSourceService" unbind="unsetDataSourceService"
 **/

public class LCServiceComponent {

    private static Log log = LogFactory.getLog(LCServiceComponent.class);

    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(LCManagementService.class, new LCManagementServiceImpl(), null);
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
        testAddinLC();
        testGettingLCList();
        testGetLCContent();
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

    private void testAddinLC(){
        LCManagementService lcManagementService = new LCManagementServiceImpl();
        String content =
                "<aspect name=\"ServerLifecycle\" class=\"org.wso2.carbon.governance.registry.extensions.aspects.DefaultLifeCycle\">\n"
                + "    <configuration type=\"literal\">\n" + "        <lifecycle>\n"
                + "            <scxml xmlns=\"http://www.w3.org/2005/07/scxml\"\n"
                + "                   version=\"1.0\"\n" + "                   initialstate=\"Active\">\n"
                + "                <state id=\"Active\">\n"
                + "                    <transition event=\"Deactivate\" target=\"Off\"/>\n"
                + "                </state>\n" + "                <state id=\"Off\">\n"
                + "                    <transition event=\"Activate\" target=\"Active\"/>\n"
                + "                </state>\n" + "            </scxml>\n" + "        </lifecycle>\n"
                + "    </configuration>\n" + "</aspect>";
        try {
            lcManagementService.createLifecycle(content);
        } catch (LifeCycleException e) {
            log.error(e);
        }
    }

    private void testGettingLCList() {
        LCManagementService lcManagementService = new LCManagementServiceImpl();
        try {
            lcManagementService.getLifecycleList();
        } catch (LifeCycleException e) {
            log.error(e);
        }

    }

    private void testGetLCContent() {
        LCManagementService lcManagementService = new LCManagementServiceImpl();
        try {
            lcManagementService.getLifecycleConfiguration("EndpointLifeCycleNew");
        } catch (LifeCycleException e) {
            log.error(e);
        }
    }


}
