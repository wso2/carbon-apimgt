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

package org.wso2.carbon.apimgt.impl.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIManagerDatabaseException;
import org.wso2.carbon.apimgt.api.WorkflowStatus;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class APIMgtDBUtil {

    private static final Log log = LogFactory.getLog(APIMgtDBUtil.class);

    private static volatile DataSource dataSource = null;
    private static final String DB_CHECK_SQL = "SELECT * FROM AM_SUBSCRIBER";
    
    private static final String DATA_SOURCE_NAME = "DataSourceName";

    /**
     * Initializes the data source
     *
     * @throws APIManagementException if an error occurs while loading DB configuration
     */
    public static void initialize() throws APIManagerDatabaseException {
        if (dataSource != null) {
            return;
        }

        synchronized (APIMgtDBUtil.class) {
            if (dataSource == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing data source");
                }
                APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfigurationService().getAPIManagerConfiguration();
                String dataSourceName = config.getFirstProperty(DATA_SOURCE_NAME);

                if (dataSourceName != null) {
                    try {
                        Context ctx = new InitialContext();
                        dataSource = (DataSource) ctx.lookup(dataSourceName);
                    } catch (NamingException e) {
                        throw new APIManagerDatabaseException("Error while looking up the data " +
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
     * @throws java.sql.SQLException if failed to get Connection
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        throw new SQLException("Data source is not configured properly.");
    }

    /**
     * Utility method to close the connection streams.
     * @param preparedStatement PreparedStatement
     * @param connection Connection
     * @param resultSet ResultSet
     */
    public static void closeAllConnections(PreparedStatement preparedStatement, Connection connection,
                                           ResultSet resultSet) {
        closeConnection(connection);
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
    }

    /**
     * Close Connection
     * @param dbConnection Connection
     */
    private static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close database connection. Continuing with " +
                        "others. - " + e.getMessage(), e);
            }
        }
    }

    /**
     * Close ResultSet
     * @param resultSet ResultSet
     */
    private static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close ResultSet  - " + e.getMessage(), e);
            }
        }

    }

    /**
     * Close PreparedStatement
     * @param preparedStatement PreparedStatement
     */
    public static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close PreparedStatement. Continuing with" +
                        " others. - " + e.getMessage(), e);
            }
        }

    }

    /**
     * Function converts IS to String
     * Used for handling blobs
     * @param is - The Input Stream
     * @return - The inputStream as a String
     */
    public static String getStringFromInputStream(InputStream is) {
        String str = null;
        try {
            str = IOUtils.toString(is, "UTF-8");
        } catch (IOException e) {
            log.error("Error occurred while converting input stream to string.", e);
        }
        return str;
    }

    /**
     * Function converts IS to byte[]
     * Used for handling inputstreams
     * @param is - The Input Stream
     * @return - The inputStream as a byte array
     */
    public static byte[] getBytesFromInputStream(InputStream is) {
        byte[] byteArray = null;
        try {
            byteArray = IOUtils.toByteArray(is);
        } catch (IOException e) {
            log.error("Error occurred while converting input stream to byte array.", e);
        }
        return byteArray;
    }

    /**
     * Set autocommit state of the connection
     * @param dbConnection Connection
     * @param autoCommit autoCommitState
     */
    public static void setAutoCommit(Connection dbConnection, boolean autoCommit) {
        if (dbConnection != null) {
            try {
                dbConnection.setAutoCommit(autoCommit);
            } catch (SQLException e) {
                log.error("Could not set auto commit back to initial state", e);
            }
        }
    }

    /**
     * Handle connection rollback logic. Rethrow original exception so that it can be handled centrally.
     * @param connection Connection
     * @param error Error message to be logged
     * @param e Original SQLException
     * @throws SQLException
     */
    public static void rollbackConnection(Connection connection, String error, SQLException e) throws SQLException {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                // rollback failed
                log.error(error, rollbackException);
            }
            // Rethrow original exception so that it can be handled in the common catch clause of the calling method
            throw e;
        }
    }

    /**
     * Handle connection rollback logic. Rethrow original exception so that it can be handled centrally.
     * @param rs result set
     * @throws SQLException sql exception
     * @throws APIManagementException api management exception
     */
    public static List<APIRevisionDeployment> mergeRevisionDeploymentDTOs(ResultSet rs) throws APIManagementException,
            SQLException {
        List<APIRevisionDeployment> apiRevisionDeploymentList = new ArrayList<>();
        Map<String, APIRevisionDeployment> uniqueSet = new HashMap<>();
        while (rs.next()) {
            APIRevisionDeployment apiRevisionDeployment;
            String environmentName = rs.getString("NAME");

            // If the gateway defined in the deployment.toml file has been decommissioned, ignore all revision
            // deployments for that gateway
            if (StringUtils.isEmpty(rs.getString("VHOST"))) {
                Map<String, Environment> readOnlyEnvironments = APIUtil.getReadOnlyEnvironments();
                if (readOnlyEnvironments.get(environmentName) == null) {
                    continue;
                }
            }
            String vhost = VHostUtils.resolveIfNullToDefaultVhost(environmentName,
                    rs.getString("VHOST"));
            String revisionUuid = rs.getString("REVISION_UUID");
            String uniqueKey = (environmentName != null ? environmentName : "") +
                    (vhost != null ? vhost : "") + (revisionUuid != null ? revisionUuid : "");
            String revisionStatus = rs.getString("REVISION_STATUS");
            WorkflowStatus status = null;
            if (revisionStatus != null) {
                switch (revisionStatus) {
                case "CREATED":
                    status = WorkflowStatus.CREATED;
                    break;
                case "APPROVED":
                    status = WorkflowStatus.APPROVED;
                    break;
                case "REJECTED":
                    status = WorkflowStatus.REJECTED;
                    break;
                default:
                    // Handle the case where revisionStatus is not one of the expected values
                    break;
                }
            }
            if (!uniqueSet.containsKey(uniqueKey)) {
                apiRevisionDeployment = new APIRevisionDeployment();
                apiRevisionDeployment.setDeployment(environmentName);
                apiRevisionDeployment.setVhost(vhost);
                apiRevisionDeployment.setRevisionUUID(revisionUuid);
                apiRevisionDeployment.setStatus(status);
                apiRevisionDeployment.setDisplayOnDevportal(rs.getBoolean("DISPLAY_ON_DEVPORTAL"));
                apiRevisionDeployment.setDeployedTime(rs.getString("DEPLOY_TIME"));
                apiRevisionDeployment.setSuccessDeployedTime(rs.getString("DEPLOYED_TIME"));
                apiRevisionDeploymentList.add(apiRevisionDeployment);
                uniqueSet.put(uniqueKey, apiRevisionDeployment);
            } else {
                apiRevisionDeployment = uniqueSet.get(uniqueKey);
                if (!apiRevisionDeployment.isDisplayOnDevportal()) {
                    apiRevisionDeployment.setDisplayOnDevportal(rs.getBoolean("DISPLAY_ON_DEVPORTAL"));
                }
                if (apiRevisionDeployment.getDeployedTime() == null) {
                    apiRevisionDeployment.setDeployedTime(rs.getString("DEPLOY_TIME"));
                }
                if (apiRevisionDeployment.getSuccessDeployedTime() == null) {
                    apiRevisionDeployment.setSuccessDeployedTime(rs.getString("DEPLOYED_TIME"));
                }
            }
        }
        return  apiRevisionDeploymentList;
    }

    /**
     * Converts a JSON Object String to a String Map
     *
     * @param jsonString    JSON String
     * @return              String Map
     * @throws APIManagementException if errors occur during parsing the json string
     */
    public static Map<String, Object> convertJSONStringToMap(String jsonString) throws APIManagementException {
        Map<String, Object> map = null;
        if (StringUtils.isNotEmpty(jsonString)) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                map = objectMapper.readValue(jsonString, Map.class);
            } catch (IOException e) {
                String msg = "Error while parsing JSON string";
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
        }
        return map;
    }
}
