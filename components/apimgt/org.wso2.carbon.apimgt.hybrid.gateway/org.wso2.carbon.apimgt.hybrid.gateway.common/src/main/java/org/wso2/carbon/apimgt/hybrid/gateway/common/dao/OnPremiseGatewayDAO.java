/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.hybrid.gateway.common.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.common.config.ConfigManager;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.OnPremiseGatewayConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Class for Database Access
 */
public class OnPremiseGatewayDAO {
    Log log = LogFactory.getLog(OnPremiseGatewayDAO.class);
    private static final String insertIntoAPIPublishEvents = "INSERT INTO AM_API_LC_PUBLISH_EVENTS " +
            "(TENANT_DOMAIN, API_ID, EVENT_TIME) VALUES (?, ?, ?)";
    private static final String selectAllAPIPublishEvents = "SELECT DISTINCT API_ID FROM AM_API_LC_PUBLISH_EVENTS " +
            "WHERE TENANT_DOMAIN = (?) AND EVENT_TIME > (?)";

    public static OnPremiseGatewayDAO getInstance() {
        return new OnPremiseGatewayDAO();
    }

    /**
     * Adds an API publish/re-publish event to the database
     *
     * @param tenantDomain tenant domain
     * @param apiId        API identifier
     * @throws OnPremiseGatewayException OnPremiseGatewayException
     */
    public void addAPIPublishEvent(String tenantDomain, String apiId) throws OnPremiseGatewayException {
        if (log.isDebugEnabled()) {
            log.debug("Adding publish/re-publish event of API: " + apiId);
        }
        Connection conn = null;
        PreparedStatement ps = null;
        boolean isAutoCommitEnabled = false;
        try {
            conn = APIMgtDBUtil.getConnection();
            isAutoCommitEnabled = conn.getAutoCommit();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(insertIntoAPIPublishEvents);
            ps.setString(1, tenantDomain);
            ps.setString(2, apiId);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            throw new OnPremiseGatewayException(
                    "Failed to insert publish/re-publish event of the API " + apiId + " of the tenant domain " +
                            tenantDomain, e);
        } finally {
            try {
                conn.setAutoCommit(isAutoCommitEnabled);
            } catch (SQLException e) {
                log.warn("Failed to reset auto commit state of database connection to the previous state." +
                                tenantDomain, e);
            }
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * Adds an API publish/re-publish event to the database
     *
     * @param tenantDomain tenant domain
     * @return true if insert operation was a success
     * @throws OnPremiseGatewayException OnPremiseGatewayException
     */
    public JSONArray getAPIPublishEvents(String tenantDomain) throws OnPremiseGatewayException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        JSONArray resultObj = new JSONArray();
        try {
            String timeDurationResponse =
                    ConfigManager.getConfigManager()
                            .getProperty(OnPremiseGatewayConstants.UPDATED_API_INFO_RETRIEVAL_DURATION);
            int timeDuration;
            if (timeDurationResponse == null) {
                timeDuration = OnPremiseGatewayConstants.DEFAULT_UPDATED_API_INFO_RETRIEVAL_DURATION;
            } else {
                timeDuration = Integer.parseInt(timeDurationResponse) * 60 * 1000;
            }
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(selectAllAPIPublishEvents);
            ps.setString(1, tenantDomain);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis() - timeDuration));
            resultSet = ps.executeQuery();
            while (resultSet.next()) {
                resultObj.add(resultSet.getString("API_ID"));
            }
        } catch (SQLException e) {
            throw new OnPremiseGatewayException(
                    "Failed to retrieve API publish/re-publish events of the tenant domain " +
                            tenantDomain, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return resultObj;
    }
}
