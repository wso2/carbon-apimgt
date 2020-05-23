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

package org.wso2.carbon.apimgt.internal.service.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.internal.service.dto.BlockConditionsDTO;
import org.wso2.carbon.apimgt.internal.service.dto.IPLevelDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTListDTO;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public final class BlockConditionDBUtil {

    private static final Log log = LogFactory.getLog(BlockConditionDBUtil.class);

    private static volatile DataSource dataSource = null;
    private static BlockConditionsDTO blockConditionsDTO = null;
    private static Set<String> keyTemplates;
    private static final String GET_GLOBAL_POLICY_KEY_TEMPLATES = " SELECT KEY_TEMPLATE FROM AM_POLICY_GLOBAL";

    public static void initialize() throws Exception {

        if (dataSource != null) {
            return;
        }
        Properties properties = new Properties();
        properties.load(new ClassPathResource("../throttle.properties").getInputStream());
        String dataSourceName = (String) properties.get("block.datasource.name");
        synchronized (BlockConditionDBUtil.class) {
            if (dataSource == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing data source");
                }

                if (dataSourceName != null) {
                    try {
                        Context ctx = new InitialContext();
                        dataSource = (DataSource) ctx.lookup(dataSourceName);
                    } catch (NamingException e) {
                        throw new Exception("Error while looking up the data " +
                                "source: " + dataSourceName, e);
                    }
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
        } else {
            try {
                initialize();
                return dataSource.getConnection();

            } catch (Exception e) {
                throw new SQLException("Data source is not configured properly.", e);
            }
        }

    }

    public static BlockConditionsDTO getBlockConditions() {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List api = new ArrayList();
        List application = new ArrayList();
        List<IPLevelDTO> ip = new ArrayList();
        List user = new ArrayList();
        List custom = new ArrayList();
        String sqlQuery = "select * from AM_BLOCK_CONDITIONS";
        try {
            conn = BlockConditionDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            rs = ps.executeQuery();
            while (rs.next()) {

                String type = rs.getString("TYPE");
                String value = rs.getString("VALUE");
                String enabled = rs.getString("ENABLED");
                String tenantDomain = rs.getString("DOMAIN");
                int conditionId = rs.getInt("CONDITION_ID");
                if (Boolean.parseBoolean(enabled)) {
                    if ("API".equals(type)) {
                        api.add(value);
                    } else if ("APPLICATION".equals(type)) {
                        application.add(value);
                    } else if ("IP".equals(type)) {
                        IPLevelDTO ipLevelDTO = new IPLevelDTO();
                        ipLevelDTO.setTenantDomain(tenantDomain);
                        ipLevelDTO.setId(conditionId);
                        JsonElement iplevelJson = new JsonParser().parse(value);
                        if (iplevelJson instanceof JsonPrimitive) {
                            JsonPrimitive fixedIp = (JsonPrimitive) iplevelJson;
                            ipLevelDTO.setFixedIp(fixedIp.getAsString());
                            ipLevelDTO.setInvert(Boolean.FALSE);
                            ipLevelDTO.setType(APIConstants.BLOCKING_CONDITIONS_IP);
                        } else if (iplevelJson instanceof JsonObject) {
                            JsonObject ipBlockingJson = (JsonObject) iplevelJson;
                            if (ipBlockingJson.has(APIConstants.BLOCK_CONDITION_FIXED_IP)) {
                                ipLevelDTO.setType(APIConstants.BLOCKING_CONDITIONS_IP);
                                ipLevelDTO.setFixedIp(
                                        ipBlockingJson.get(APIConstants.BLOCK_CONDITION_FIXED_IP).getAsString());
                            }
                            if (ipBlockingJson.has(APIConstants.BLOCK_CONDITION_START_IP)) {
                                ipLevelDTO.setType(APIConstants.BLOCK_CONDITION_IP_RANGE);
                                ipLevelDTO.setStartingIp(
                                        ipBlockingJson.get(APIConstants.BLOCK_CONDITION_START_IP).getAsString());
                            }
                            if (ipBlockingJson.has(APIConstants.BLOCK_CONDITION_ENDING_IP)) {
                                ipLevelDTO.setEndingIp(
                                        ipBlockingJson.get(APIConstants.BLOCK_CONDITION_ENDING_IP).getAsString());
                            }
                            if (ipBlockingJson.has(APIConstants.BLOCK_CONDITION_INVERT)) {
                                ipLevelDTO.setInvert(
                                        ipBlockingJson.get(APIConstants.BLOCK_CONDITION_INVERT).getAsBoolean());
                            }
                        }
                        ip.add(ipLevelDTO);
                    } else if ("USER".equals(type)) {
                        user.add(value);
                    } else if ("CUSTOM".equals(type)) {
                        custom.add(value);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error while executing SQL", e);
        } finally {
            BlockConditionDBUtil.closeAllConnections(ps, conn, rs);
        }
        BlockConditionDBUtil.blockConditionsDTO = new BlockConditionsDTO();
        blockConditionsDTO.setApi(api);
        blockConditionsDTO.setApplication(application);
        blockConditionsDTO.setIp(ip);
        blockConditionsDTO.setUser(user);
        blockConditionsDTO.setCustom(custom);
        return blockConditionsDTO;
    }

    public static void closeAllConnections(PreparedStatement preparedStatement, Connection connection,
                                           ResultSet resultSet) {

        closeConnection(connection);
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
    }

    /**
     * Close Connection
     *
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
     *
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
     *
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

    public static BlockConditionsDTO getBlockConditionsDTO() {

        return getBlockConditions();

    }

    public static Set<String> getKeyTemplates() {

        return getGlobalPolicyKeyTemplates();

    }

    /**
     * Retrieves global policy key templates for the given tenantID
     *
     * @return list of KeyTemplates
     */
    public static Set<String> getGlobalPolicyKeyTemplates() {

        keyTemplates = new HashSet<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = BlockConditionDBUtil.getConnection();

            String sqlQuery = GET_GLOBAL_POLICY_KEY_TEMPLATES;

            ps = conn.prepareStatement(sqlQuery);
            rs = ps.executeQuery();
            while (rs.next()) {
                keyTemplates.add(rs.getString("KEY_TEMPLATE"));
            }
        } catch (SQLException e) {
            log.error("Error while executing SQL", e);
        } finally {
            BlockConditionDBUtil.closeAllConnections(ps, conn, rs);
        }
        return keyTemplates;
    }

    /**
     * Fetches all revoked JWTs from DB.
     *
     * @return list fo revoked JWTs
     */
    static RevokedJWTListDTO getRevokedJWTs() {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        RevokedJWTListDTO revokedJWTListDTO = new RevokedJWTListDTO();
        String sqlQuery = "SELECT SIGNATURE,EXPIRY_TIMESTAMP FROM AM_REVOKED_JWT";
        try {
            conn = BlockConditionDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            rs = ps.executeQuery();
            while (rs.next()) {
                String signature = rs.getString("SIGNATURE");
                Long expiryTimestamp = rs.getLong("EXPIRY_TIMESTAMP");
                RevokedJWTDTO revokedJWTDTO = new RevokedJWTDTO();
                revokedJWTDTO.setJwtSignature(signature);
                revokedJWTDTO.setExpiryTime(expiryTimestamp);
                revokedJWTListDTO.add(revokedJWTDTO);
            }
        } catch (SQLException e) {
            log.error("Error while fetching revoked JWTs from database. ", e);
        } finally {
            closeAllConnections(ps, conn, rs);
        }
        return revokedJWTListDTO;
    }
}
