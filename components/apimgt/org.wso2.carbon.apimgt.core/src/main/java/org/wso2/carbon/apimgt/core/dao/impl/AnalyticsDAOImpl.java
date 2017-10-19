/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.core.dao.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.AnalyticsDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.analytics.APICount;
import org.wso2.carbon.apimgt.core.models.analytics.APIInfo;
import org.wso2.carbon.apimgt.core.models.analytics.APISubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation class of AnalyticsDAO interface.
 */
public class AnalyticsDAOImpl implements AnalyticsDAO {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsDAOImpl.class);

    public AnalyticsDAOImpl() {
    }

    /**
     * @see AnalyticsDAO#getApplicationCount(Instant, Instant, String)
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<ApplicationCount> getApplicationCount(Instant fromTimestamp, Instant toTimestamp, String createdBy)
            throws APIMgtDAOException {
        final String query;
        if (StringUtils.isNotEmpty(createdBy)) {
            query = "SELECT COUNT(UUID) AS count, CREATED_TIME AS time " +
                    "FROM AM_APPLICATION " +
                    "WHERE (CREATED_TIME BETWEEN ? AND ?) " +
                    "AND CREATED_BY = ?" +
                    "GROUP BY CREATED_TIME " +
                    "ORDER BY CREATED_TIME ASC";
        } else {
            query = "SELECT COUNT(UUID) AS count, CREATED_TIME AS time " +
                    "FROM AM_APPLICATION " +
                    "WHERE (CREATED_TIME BETWEEN ? AND ?) " +
                    "GROUP BY CREATED_TIME " +
                    "ORDER BY CREATED_TIME ASC";
        }
        List<ApplicationCount> applicationCountList = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setTimestamp(1, Timestamp.from(fromTimestamp));
            statement.setTimestamp(2, Timestamp.from(toTimestamp));
            if (StringUtils.isNotEmpty(createdBy)) {
                statement.setString(3, createdBy);
            }
            log.debug("Executing query: {} ", query);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                long count = 0;
                while (rs.next()) {
                    ApplicationCount applicationCount = new ApplicationCount();
                    count += rs.getLong("count");
                    applicationCount.setTimestamp(rs.getTimestamp("time").getTime());
                    applicationCount.setCount(count);
                    applicationCountList.add(applicationCount);
                }
            }

        } catch (SQLException e) {
            String errorMsg = "Error while creating database connection/prepared-statement";
            throw new APIMgtDAOException(errorMsg, e);
        }
        return applicationCountList;
    }

    /**
     * @see AnalyticsDAO#getAPICount(Instant, Instant, String)
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<APICount> getAPICount(Instant fromTime, Instant toTime, String createdBy) throws APIMgtDAOException {
        final String query;
        if (StringUtils.isNotEmpty(createdBy)) {
            query = "SELECT COUNT(UUID) AS count, CREATED_TIME AS time " +
                    "FROM AM_API " +
                    "WHERE (CREATED_TIME BETWEEN ? AND ?) " +
                    "AND CREATED_BY = ? " +
                    "GROUP BY CREATED_TIME " +
                    "ORDER BY CREATED_TIME ASC";
        } else {
            query = "SELECT COUNT(UUID) AS count, CREATED_TIME AS time " +
                    "FROM AM_API " +
                    "WHERE (CREATED_TIME BETWEEN ? AND ?) " +
                    "GROUP BY CREATED_TIME " +
                    "ORDER BY CREATED_TIME ASC";
        }
        List<APICount> apiInfoList = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setTimestamp(1, Timestamp.from(fromTime));
            statement.setTimestamp(2, Timestamp.from(toTime));
            if (StringUtils.isNotEmpty(createdBy)) {
                statement.setString(3, createdBy);
            }
            log.debug("Executing query: {} ", query);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                long count = 0;
                while (rs.next()) {
                    APICount apiCount = new APICount();
                    count += rs.getLong("count");
                    apiCount.setTimestamp(rs.getTimestamp("time").getTime());
                    apiCount.setCount(count);
                    apiInfoList.add(apiCount);
                }
            }

        } catch (SQLException e) {
            throw new APIMgtDAOException("Error while creating database connection/prepared-statement", e);
        }
        return apiInfoList;
    }


    /**
     * @see AnalyticsDAO#getAPISubscriptionCount(Instant, Instant, String)
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<APISubscriptionCount> getAPISubscriptionCount(Instant fromTime, Instant toTime, String apiId)
            throws APIMgtDAOException {
        final String query;
        if (StringUtils.isNotEmpty(apiId)) {
            query = "SELECT api.UUID,api.NAME,api.VERSION,api.PROVIDER,count(subs.UUID) as COUNT " +
                    "FROM AM_SUBSCRIPTION subs,AM_API api " +
                    "WHERE api.UUID=subs.API_ID " +
                    "AND (subs.CREATED_TIME BETWEEN ? AND ?) " +
                    "AND subs.SUB_STATUS = 'ACTIVE' " +
                    "AND api.UUID=? " +
                    "GROUP BY api.UUID,api.NAME,api.VERSION,api.PROVIDER";
        } else {
            query = "SELECT api.UUID,api.NAME,api.VERSION,api.PROVIDER,count(subs.UUID) as COUNT " +
                    "FROM AM_SUBSCRIPTION subs,AM_API api " +
                    "WHERE api.UUID=subs.API_ID " +
                    "AND (subs.CREATED_TIME BETWEEN ? AND ?) " +
                    "AND subs.SUB_STATUS = 'ACTIVE' " +
                    "GROUP BY api.UUID,api.NAME,api.VERSION,api.PROVIDER";
        }
        List<APISubscriptionCount> apiSubscriptionCountList = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, Timestamp.from(fromTime));
            statement.setTimestamp(2, Timestamp.from(toTime));

            if (StringUtils.isNotEmpty(apiId)) {
                statement.setString(3, apiId);
            }
            log.debug("Executing query: {} ", query);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    APISubscriptionCount apiSubscriptionCount = new APISubscriptionCount();
                    apiSubscriptionCount.setId(rs.getString("UUID"));
                    apiSubscriptionCount.setName(rs.getString("NAME"));
                    apiSubscriptionCount.setVersion(rs.getString("VERSION"));
                    apiSubscriptionCount.setProvider(rs.getString("PROVIDER"));
                    apiSubscriptionCount.setCount(rs.getInt("COUNT"));
                    apiSubscriptionCountList.add(apiSubscriptionCount);
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Error while creating database connection/prepared-statement", e);
        }
        return apiSubscriptionCountList;
    }

    /**
     * @see AnalyticsDAO#getSubscriptionCount(Instant, Instant, String)
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<SubscriptionCount> getSubscriptionCount(Instant fromTimestamp, Instant
            toTimestamp, String createdBy) throws APIMgtDAOException {
        final String query;
        if (StringUtils.isNotEmpty(createdBy)) {
            query = "SELECT COUNT(subs.UUID) AS COUNT, subs.CREATED_TIME AS time " +
                    "FROM AM_SUBSCRIPTION subs, AM_API  api  " +
                    "WHERE (subs.CREATED_TIME BETWEEN ? AND ?) " +
                    "AND  subs.api_id=api.uuid " +
                    "AND subs.CREATED_BY= ? " +
                    "GROUP BY subs.CREATED_TIME ORDER BY subs.CREATED_TIME ASC";
        } else {
            query = "SELECT COUNT(subs.UUID) AS COUNT, subs.CREATED_TIME AS time " +
                    "FROM AM_SUBSCRIPTION subs, AM_API  api  " +
                    "WHERE (subs.CREATED_TIME BETWEEN ? AND ?) " +
                    "AND  subs.api_id=api.uuid " +
                    "GROUP BY subs.CREATED_TIME ORDER BY subs.CREATED_TIME ASC";
        }
        List<SubscriptionCount> subscriptionCountList = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, Timestamp.from(fromTimestamp));
            statement.setTimestamp(2, Timestamp.from(toTimestamp));
            if (StringUtils.isNotEmpty(createdBy)) {
                statement.setString(3, createdBy);
            }
            log.debug("Executing query: {} ", query);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    SubscriptionCount subscriptionCount = new SubscriptionCount();
                    subscriptionCount.setTimestamp(rs.getTimestamp("TIME").getTime());
                    subscriptionCount.setCount(rs.getInt("COUNT"));
                    subscriptionCountList.add(subscriptionCount);
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error while creating database connection/prepared-statement";
            throw new APIMgtDAOException(errorMsg, e);
        }
        return subscriptionCountList;
    }

    /**
     * @see AnalyticsDAO#getSubscriptionInfo(Instant, Instant, String)
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<SubscriptionInfo> getSubscriptionInfo(Instant fromTimestamp, Instant toTimestamp, String createdBy)
            throws APIMgtDAOException {
        final String query;
        if (StringUtils.isNotEmpty(createdBy)) {
            query = "SELECT sub.UUID, api.NAME as API_NAME, api.VERSION as API_VERSION, app.NAME " +
                    "as APP_NAME, app.DESCRIPTION, sub.CREATED_TIME , policy.DISPLAY_NAME , sub.SUB_STATUS " +
                    "FROM AM_API api, AM_SUBSCRIPTION sub, AM_APPLICATION app, AM_SUBSCRIPTION_POLICY policy " +
                    "WHERE (sub.CREATED_TIME BETWEEN ? AND ?) " +
                    "AND api.UUID = sub.API_ID " +
                    "AND sub.APPLICATION_ID = app.UUID " +
                    "AND sub.CREATED_BY= ? " +
                    "AND sub.TIER_ID = policy.UUID ";
        } else {
            query = "SELECT sub.UUID, api.NAME as API_NAME, api.VERSION as API_VERSION, app.NAME " +
                    "as APP_NAME, app.DESCRIPTION, sub.CREATED_TIME , policy.DISPLAY_NAME , sub.SUB_STATUS " +
                    "FROM AM_API api, AM_SUBSCRIPTION sub, AM_APPLICATION app, AM_SUBSCRIPTION_POLICY policy " +
                    "WHERE (sub.CREATED_TIME BETWEEN ? AND ?) " +
                    "AND api.UUID = sub.API_ID " +
                    "AND sub.APPLICATION_ID = app.UUID " +
                    "AND sub.TIER_ID = policy.UUID ";
        }

        List<SubscriptionInfo> subscriptionInfoList = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, Timestamp.from(fromTimestamp));
            statement.setTimestamp(2, Timestamp.from(toTimestamp));
            if (StringUtils.isNotEmpty(createdBy)) {
                statement.setString(3, createdBy);
            }
            log.debug("Executing query: {} ", query);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    SubscriptionInfo subscriptionInfo = new SubscriptionInfo();
                    subscriptionInfo.setId(rs.getString("UUID"));
                    subscriptionInfo.setName(rs.getString("API_NAME"));
                    subscriptionInfo.setVersion(rs.getString("API_VERSION"));
                    subscriptionInfo.setAppName(rs.getString("APP_NAME"));
                    subscriptionInfo.setDescription(rs.getString("DESCRIPTION"));
                    subscriptionInfo.setCreatedTime(rs.getTimestamp("CREATED_TIME").getTime());
                    subscriptionInfo.setSubscriptionStatus(rs.getString("SUB_STATUS"));
                    subscriptionInfo.setSubscriptionTier(rs.getString("DISPLAY_NAME"));
                    subscriptionInfoList.add(subscriptionInfo);
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Error while creating database connection/prepared-statement", e);
        }
        return subscriptionInfoList;
    }

    /**
     * @see AnalyticsDAO#getAPIInfo(Instant, Instant, String)
     */
    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<APIInfo> getAPIInfo(Instant fromTimestamp, Instant toTimestamp, String createdBy)
            throws APIMgtDAOException {
        final String query;
        if (StringUtils.isNotEmpty(createdBy)) {
            query = "SELECT UUID,PROVIDER,NAME,CONTEXT,VERSION,CREATED_TIME,CURRENT_LC_STATUS, LC_WORKFLOW_STATUS  " +
                    "FROM AM_API " +
                    "WHERE CREATED_TIME BETWEEN ? AND ? " +
                    "AND CREATED_BY = ? " +
                    "ORDER BY CREATED_TIME ASC";
        } else {
            query = "SELECT UUID,PROVIDER,NAME,CONTEXT,VERSION,CREATED_TIME,CURRENT_LC_STATUS, LC_WORKFLOW_STATUS  " +
                    "FROM AM_API " +
                    "WHERE CREATED_TIME BETWEEN ? AND ? " +
                    "ORDER BY CREATED_TIME ASC";
        }
        List<APIInfo> apiInfoList = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setTimestamp(1, Timestamp.from(fromTimestamp));
            statement.setTimestamp(2, Timestamp.from(toTimestamp));
            if (StringUtils.isNotEmpty(createdBy)) {
                statement.setString(3, createdBy);
            }
            log.debug("Executing query: {} ", query);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    APIInfo apiInfo = new APIInfo();
                    apiInfo.setId(rs.getString("UUID"));
                    apiInfo.setProvider(rs.getString("PROVIDER"));
                    apiInfo.setName(rs.getString("NAME"));
                    apiInfo.setContext(rs.getString("CONTEXT"));
                    apiInfo.setVersion(rs.getString("VERSION"));
                    apiInfo.setCreatedTime(rs.getTimestamp("CREATED_TIME").getTime());
                    apiInfo.setLifeCycleStatus(rs.getString("CURRENT_LC_STATUS"));
                    apiInfo.setWorkflowStatus(rs.getString("LC_WORKFLOW_STATUS"));
                    apiInfoList.add(apiInfo);
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Error while creating database connection/prepared-statement", e);
        }
        return apiInfoList;
    }

}
