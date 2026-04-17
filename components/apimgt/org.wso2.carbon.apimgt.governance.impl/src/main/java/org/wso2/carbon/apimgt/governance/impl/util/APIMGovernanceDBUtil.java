/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovExceptionCodes;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Utility class for Governance database operations
 */
public class APIMGovernanceDBUtil {

    private static final Log log = LogFactory.getLog(APIMGovernanceDBUtil.class);

    private static volatile DataSource dataSource = null;

    private static final String DATA_SOURCE_NAME = "DataSourceName";

    /**
     * Initializes the data source
     *
     * @throws APIMGovernanceException if an error occurs while loading DB configuration
     */
    private static final String DEFAULT_DATASOURCE_JNDI = "jdbc/WSO2AM_DB";

    public static void initialize() throws APIMGovernanceException {
        if (dataSource != null) {
            return;
        }

        synchronized (APIMGovernanceDBUtil.class) {
            if (dataSource == null) {
                log.info("[GovernanceDB] Initializing governance data source...");

                // Step 1: resolve the JNDI datasource name from config (or fall back to default)
                String dataSourceName = null;
                try {
                    APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                            .getAPIMConfigurationService().getAPIManagerConfiguration();
                    dataSourceName = config.getAPIMGovernanceConfigurationDto().getDataSourceName();
                    log.info("[GovernanceDB] DataSource name from config: " + dataSourceName);
                } catch (Throwable t) {
                    // Catches NPE (config service not set), NoClassDefFoundError, etc.
                    log.warn("[GovernanceDB] Config service unavailable, falling back to default "
                            + "datasource name (" + DEFAULT_DATASOURCE_JNDI + "): " + t.getMessage());
                }

                if (dataSourceName == null) {
                    dataSourceName = DEFAULT_DATASOURCE_JNDI;
                    log.info("[GovernanceDB] Using default DataSource JNDI name: " + dataSourceName);
                }

                // Step 2: JNDI lookup
                try {
                    Context ctx = new InitialContext();
                    dataSource = (DataSource) ctx.lookup(dataSourceName);
                    log.info("[GovernanceDB] DataSource initialized successfully via JNDI: "
                            + dataSourceName);
                } catch (NamingException e) {
                    log.error("[GovernanceDB] JNDI lookup failed for: " + dataSourceName, e);
                    throw new APIMGovernanceException(APIMGovExceptionCodes.DATASOURCE_INACCESSIBLE,
                            e, dataSourceName);
                }
            }
        }
    }

    /**
     * Utility method to get a new database connection.
     * Performs lazy initialization if the datasource was not ready during startup.
     *
     * @return Connection
     * @throws SQLException if failed to get Connection
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }

        // Lazy re-initialization: datasource may not have been ready during component activation
        try {
            initialize();
        } catch (APIMGovernanceException e) {
            log.debug("[GovernanceDB] Lazy initialization attempt failed", e);
        }

        if (dataSource != null) {
            return dataSource.getConnection();
        }
        throw new SQLException("Data source is not configured properly.");
    }

    /**
     * Function converts IS to String
     * Used for handling inputstreams
     *
     * @param is - The Input Stream
     * @return - The inputStream as a string
     * @throws IOException - If an error occurs while converting the input stream to a string
     */
    public static String getStringFromInputStream(InputStream is) throws IOException {
        String str = null;
        str = IOUtils.toString(is, "UTF-8");
        return str;
    }


}
