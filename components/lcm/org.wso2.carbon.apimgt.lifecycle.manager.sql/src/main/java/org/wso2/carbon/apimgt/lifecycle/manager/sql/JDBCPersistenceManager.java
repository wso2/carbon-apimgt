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
package org.wso2.carbon.apimgt.lifecycle.manager.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;

import org.wso2.carbon.apimgt.lifecycle.manager.sql.constants.Constants;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.utils.LCDatabaseCreator;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;

public class JDBCPersistenceManager {
    private static Log log = LogFactory.getLog(JDBCPersistenceManager.class);
    private static JDBCPersistenceManager instance;
    private static DataSourceService carbonDataSourceService;

    private JDBCPersistenceManager() {

    }

    public static DataSourceService getCarbonDataSourceService() {
        return carbonDataSourceService;
    }

    public static void setCarbonDataSourceService(
            DataSourceService dataSourceService) {
        carbonDataSourceService = dataSourceService;
    }

    public static JDBCPersistenceManager getInstance() throws Exception {
        if (instance == null) {
            synchronized (JDBCPersistenceManager.class) {
                if (instance == null) {
                    instance = new JDBCPersistenceManager();
                }
            }
        }
        return instance;
    }

    public void initializeDatabase() throws Exception {

        CarbonDataSource cds = carbonDataSourceService
                .getDataSource(Constants.LIFECYCLE_DB_NAME);

        if (log.isDebugEnabled()) {
            log.debug("Initializing Database: " + Constants.LIFECYCLE_DB_NAME);
        }

        DataSource dataSource = (DataSource) cds.getDSObject();

        LCDatabaseCreator dbInitializer = new LCDatabaseCreator(dataSource);

        dbInitializer.createLifecycleDatabase();

    }
}
