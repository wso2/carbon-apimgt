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
import org.wso2.carbon.apimgt.core.dao.AnalyticsDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *  Implementation class of AnalyticsDao interface
 */
public class AnalyticsDAOImpl implements AnalyticsDAO {

    public AnalyticsDAOImpl() {
    }

    @Override
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<ApplicationCount> getApplicationCount(String createdBy, String subscribedTo, String fromTimestamp,
            String toTimestamp) throws APIMgtDAOException {

        final String query;
        if (createdBy.equals("all")) {
            query = "SELECT COUNT(UUID) AS count, CREATED_TIME AS time FROM AM_APPLICATION WHERE "
                    + "CREATED_TIME BETWEEN ? AND ? GROUP BY CREATED_TIME ORDER BY CREATED_TIME ASC";
        } else {
            query = "SELECT COUNT(UUID) AS count, CREATED_TIME AS time FROM AM_APPLICATION WHERE "
                    + "(CREATED_TIME BETWEEN ? AND ?) AND CREATED_BY = ? GROUP BY CREATED_TIME ORDER BY "
                    + "CREATED_TIME ASC";
        }

        List<ApplicationCount> applicationCountList = new ArrayList<>();
        try (Connection connection = DAOUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            try {
                Timestamp fromTime = new Timestamp(dateFormat.parse(fromTimestamp).getTime());
                Timestamp toTime = new java.sql.Timestamp(dateFormat.parse(toTimestamp).getTime());
                statement.setTimestamp(1, fromTime);
                statement.setTimestamp(2, toTime);
                if (!createdBy.equals("all")) {
                    statement.setString(3, createdBy);
                }
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
                String errorMsg = "Error while retrieving ApplicationCount information from db";
                throw new APIMgtDAOException(errorMsg, e);
            } catch (ParseException e) {
                String errorMsg = "Error while parsing timestamp while retrieving ApplicationCount information from db";
                throw new APIMgtDAOException(errorMsg, e);
            }
        } catch (SQLException e) {
            String errorMsg = "Error while creating database connection/prepared-statement";
            throw new APIMgtDAOException(errorMsg, e);
        }
        return applicationCountList;
    }

}
