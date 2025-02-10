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
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.impl.config.APIMGovernanceConfig;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;

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
    public static void initialize() throws APIMGovernanceException {
        if (dataSource != null) {
            return;
        }

        synchronized (APIMGovernanceDBUtil.class) {
            if (dataSource == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing data source");
                }
                APIMGovernanceConfig config = ServiceReferenceHolder.getInstance().
                        getGovernanceConfigurationService().getGovernanceConfig();
                String dataSourceName = config.getFirstProperty(DATA_SOURCE_NAME);

                if (dataSourceName != null) {
                    try {
                        Context ctx = new InitialContext();
                        dataSource = (DataSource) ctx.lookup(dataSourceName);
                    } catch (NamingException e) {
                        throw new APIMGovernanceException("Error while looking up the data " +
                                "source: " + dataSourceName, e);
                    }
                } else {
                    log.error(DATA_SOURCE_NAME + " not defined in api-manager.xml.");
                }
            }
        }
    }

    /**
     * Utility method to get a new database connection
     *
     * @return Connection
     * @throws SQLException if failed to get Connection
     */
    public static Connection getConnection() throws SQLException {
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
