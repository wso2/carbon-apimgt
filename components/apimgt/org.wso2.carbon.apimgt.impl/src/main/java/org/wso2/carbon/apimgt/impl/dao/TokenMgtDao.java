/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.apimgt.impl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Utils associated with Token Validation, Generation operations.
 */
public class TokenMgtDao {

    private static final Log log = LogFactory.getLog(TokenMgtDao.class);

    public static AccessTokenInfo getAccessTokenForConsumerId(String consumerKey) throws APIManagementException {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        AccessTokenInfo tokenInfo = null;

        String tokenQuery = "SELECT * FROM " +
                            " CONSUMER_KEY_ACCESS_TOKEN_MAPPING WHERE CONSUMER_KEY = ?";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(tokenQuery);

            ps.setString(1, consumerKey);

            rs = ps.executeQuery();
            while (rs.next()) {
                tokenInfo = new AccessTokenInfo();
                tokenInfo.setValidityPeriod(rs.getLong("VALIDITY_PERIOD"));
                tokenInfo.setAccessToken(rs.getString("ACCESS_TOKEN"));
            }

        } catch (SQLException e) {
            log.error("Error occurred while fetching Token. Query : " + tokenQuery, e);
            throw new APIManagementException(e.getMessage());
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }

        return tokenInfo;
    }


    public static void insertAccessTokenForConsumerKey(String consumerKey, AccessTokenInfo tokenInfo)
            throws APIManagementException {

        // Returning if the access token is not generated.
        if (tokenInfo == null || tokenInfo.getAccessToken() == null) {
            log.error("Access Token not set properly.");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;


        String insertToken = "INSERT INTO " +
                             " CONSUMER_KEY_ACCESS_TOKEN_MAPPING (CONSUMER_KEY,ACCESS_TOKEN,VALIDITY_PERIOD)" +
                             "  VALUES(?,?,?)";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(insertToken);
            conn.setAutoCommit(false);
            ps.setString(1, consumerKey);
            ps.setString(2, tokenInfo.getAccessToken());
            ps.setLong(3, tokenInfo.getValidityPeriod());

            ps.execute();
            ps.close();
            conn.commit();

        } catch (SQLException e) {
            log.error("Error occurred while storing Access Token : " + insertToken, e);
            throw new APIManagementException(e.getMessage());
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }

    }

    public static void deleteTokenForConsumerKey(String consumerKey) throws APIManagementException {

        if (consumerKey == null || consumerKey.isEmpty()) {
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String deleteToken =
                "DELETE FROM CONSUMER_KEY_ACCESS_TOKEN_MAPPING " +
                "WHERE " +
                "   CONSUMER_KEY = ?";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(deleteToken);
            conn.setAutoCommit(false);
            ps.setString(1, consumerKey);

            ps.execute();

            ps.close();
            conn.commit();

        } catch (SQLException e) {
            log.error("Error occurred while deleting Access Token : " + deleteToken, e);
            throw new APIManagementException(e.getMessage());
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
    }

    public static void updateTokenForConsumerKey(String consumerKey, AccessTokenInfo tokenInfo) throws
                                                                                                APIManagementException {
        // Returning if the access token is not generated.
        if (tokenInfo == null || tokenInfo.getAccessToken() == null) {
            log.error("Access Token not set properly.");
            return;
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;


        String updateToken =
                "UPDATE CONSUMER_KEY_ACCESS_TOKEN_MAPPING " +
                " SET " +
                "   ACCESS_TOKEN = ? " +
                "   ,VALIDITY_PERIOD = ? " +
                "WHERE " +
                "   CONSUMER_KEY = ?";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(updateToken);
            conn.setAutoCommit(false);
            ps.setString(1, tokenInfo.getAccessToken());
            ps.setLong(2, tokenInfo.getValidityPeriod());
            ps.setString(3, consumerKey);

            ps.executeUpdate();

            ps.close();
            conn.commit();

        } catch (SQLException e) {
            log.error("Error occurred while updating Access Token : " + updateToken, e);
            throw new APIManagementException(e.getMessage());
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }

    }
}
