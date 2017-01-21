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

package org.wso2.carbon.apimgt.core.dao.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.wso2.carbon.apimgt.core.dao.ApiSearchDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Default implementation of the ApiDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */
public class APISearchDaoPostgresImpl implements ApiSearchDAO {

    private static final String API_SUMMARY_SELECT = "SELECT UUID, PROVIDER, NAME, CONTEXT, VERSION, DESCRIPTION, " +
            "CURRENT_LC_STATUS, LIFECYCLE_INSTANCE_ID FROM AM_API";

    /**
     * Retrieves summary data of all available APIs that match the given search criteria.
     *
     * @param searchString The search string provided
     * @return {@link List < API >} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    @Override
    @SuppressFBWarnings ("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    public List<API> searchAPIs(String searchString) throws APIMgtDAOException {

        final String query = API_SUMMARY_SELECT
                + " WHERE textsearchable_index_col @@ to_tsquery(?)";
        //"SELECT * FROM FT_SEARCH(?, 0, 0)";

        try (Connection connection = DAOUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, searchString.toLowerCase(Locale.ENGLISH) + ":*");
            return constructAPISummaryList(statement);
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

    }

    /**
     * Retrieves summary data of all available APIs that match the given search criteria.
     *
     * @param attributeMap The search string provided
     * @return {@link List < API >} matching results
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    public List<API> attributeSearchAPIs(Map<String , String> attributeMap) throws APIMgtDAOException {

        StringBuffer searchQuery = new StringBuffer();
        try (Connection connection = DAOUtil.getConnection()) {
            DatabaseMetaData md = connection.getMetaData();
            Iterator<Map.Entry<String , String>> entries = attributeMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, String> entry = entries.next();
                if (!checkTableColumnExists(md, entry.getKey())) {
                    throw new APIMgtDAOException(
                            "Wrong search attribute. Attribute does not exist with name : " + entry.getKey());
                }
                searchQuery.append("LOWER(");
                searchQuery.append(entry.getKey());
                searchQuery.append(") LIKE ?");
                if (entries.hasNext()) {
                    searchQuery.append(" OR ");
                }
            }

            final String query = API_SUMMARY_SELECT + " WHERE " + searchQuery.toString();
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                int queryIndex = 1;
                for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
                    statement.setString(queryIndex, '%' + entry.getValue().toLowerCase(Locale.ENGLISH) + '%');
                    queryIndex++;
                }
                return constructAPISummaryList(statement);
            } catch (SQLException e) {
                throw new APIMgtDAOException(e);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }

    private List<API> constructAPISummaryList(PreparedStatement statement) throws SQLException {
        List<API> apiList = new ArrayList<>();
        //List<String> ids = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                //ids.add(rs.getString(1));
                API apiSummary = new API.APIBuilder(rs.getString("PROVIDER"), rs.getString("NAME"),
                        rs.getString("VERSION")).
                        id(rs.getString("UUID")).
                        context(rs.getString("CONTEXT")).
                        description(rs.getString("DESCRIPTION")).
                        lifeCycleStatus(rs.getString("CURRENT_LC_STATUS")).
                        lifecycleInstanceId(rs.getString("LIFECYCLE_INSTANCE_ID")).build();

                apiList.add(apiSummary);
            }
        }
        /*for (String s : ids) {
            log.info(s);
        }*/

        return apiList;
    }

    private boolean checkTableColumnExists (DatabaseMetaData databaseMetaData, String columnName) throws
            APIMgtDAOException {
        try (ResultSet rs = databaseMetaData.getColumns(null, null, AM_API_TABLE_NAME.toLowerCase(Locale.ENGLISH),
                columnName.toLowerCase(Locale.ENGLISH))) {
            return rs.next();
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }
    }
}
