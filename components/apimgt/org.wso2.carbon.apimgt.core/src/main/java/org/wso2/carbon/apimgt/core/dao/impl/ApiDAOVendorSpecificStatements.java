/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.core.dao.impl;

import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.dao.SearchType;
import org.wso2.carbon.apimgt.core.dao.SecondarySearchType;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 *  Interface for getting SQL Statement strings. Implementation of the interface could return different values based
 *  on DB vendor type being used
 */
public interface ApiDAOVendorSpecificStatements {

    /**
     * Returns the query string to be used for the search query. This is required to construct the PreparedStatement
     * which will be created externally
     * @param roleCount Number of roles to be passed to query
     * @return String
     */
    String getPermissionBasedApiFullTextSearchQuery(int roleCount);

    /**
     * Returns the query string to be used for the attribute search query. This is required to construct the
     * PreparedStatement which will be created externally
     * @param attributeMap Search attributes to be queried
     * @param secondaryAttributeMap Search attributes that are not present in AM_API table
     * @param roleCount Number of roles to be passed to query
     * @return String
     */
    String getPermissionBasedApiAttributeSearchQuery(Map<SearchType, String> attributeMap,
            Map<SecondarySearchType, String> secondaryAttributeMap, int roleCount);

    /**
     * Format supplied search string to DB compatible value
     * @param statement SQL PreparedStatement
     * @param roles roles assigned to the user doing the search
     * @param user user doing the search
     * @param searchString search string supplied
     * @param apiType API type to be considered for the search
     * @param offset result pagination offset
     * @param limit result pagination limit
     * @throws SQLException if DB error occurs
     */
    void setPermissionBasedApiFullTextSearchStatement(PreparedStatement statement, Set<String> roles, String user,
                                                      String searchString, ApiType apiType, int offset, int limit)
                                                        throws SQLException;

    /**
     * Set parameters of the PreparedStatement created for the attribute search query
     * @param statement SQL PreparedStatement
     * @param roles roles assigned to the user doing the search
     * @param user user doing the search
     * @param attributeMap Search attributes to be queried
     * @param secondaryAttributeMap Secondary set of attributes wich are not stored in AM_API table
     * @param apiType API type to be considered for the search
     * @param offset result pagination offset
     * @param limit result pagination limit
     * @throws SQLException if DB error occurs
     */
    void setPermissionBasedApiAttributeSearchStatement(PreparedStatement statement, Set<String> roles, String user,
            Map<SearchType, String> attributeMap, Map<SecondarySearchType, String> secondaryAttributeMap,
            ApiType apiType, int offset, int limit) throws SQLException;

    /**
     * Returns the query string to be used for the visibility based full text search query. This is required
     * to construct the PreparedStatement which will be created externally.
     *
     * @param roleCount Number of roles to be passed to query
     * @param labelCount Number of labels to be passed to query
     * @return Constructed query string
     */
    String getVisibilityBasedApiFullTextSearchQuery(int roleCount, int labelCount);

    /**
     * Set parameters of the PreparedStatement created for the visibility based full text search query
     *
     * @param statement SQL PreparedStatement
     * @param roles roles assigned to the user doing the search
     * @param labels labels associated with the API
     * @param searchString
     * @param offset
     * @param limit
     * @throws SQLException
     */
    void setVisibilityBasedApiFullTextSearchStatement(PreparedStatement statement, Set<String> roles,
                                                      Set<String> labels, String searchString, int offset,
                                                      int limit) throws SQLException;

    /**
     * Returns the query string to be used for the visibility based attribute search query. This is required
     * to construct the PreparedStatement which will be created externally.
     *
     * @param roleCount Number of roles to be passed to query
     * @param labelCount Number of labels to be passed to query
     * @param attributeMap Search attributes to be queried
     * @return Constructed query string
     */
    String getVisibilityBasedApiAttributeSearchQuery(int roleCount, int labelCount,
                                                     Map<SearchType, String> attributeMap);

    /**
     * Set parameters of the PreparedStatement created for the visibility based attribute search query
     *
     * @param statement SQL PreparedStatement
     * @param roles roles assigned to the user doing the search
     * @param labels labels associated with the API
     * @param attributeMap map containing the attributes and search queries for those attributes
     * @param offset the starting point of the search results
     * @param limit number of search results that will be returned
     * @throws SQLException if DB error occurs
     */
    void setVisibilityBasedApiAttributeSearchStatement(PreparedStatement statement, Set<String> roles,
                                                       Set<String> labels, Map<SearchType, String> attributeMap,
                                                       int offset, int limit) throws SQLException;

    default String getStoreAPIsByLabelJoinQuery(int labelCount) {

        if (labelCount > 0) {
            return " INNER JOIN AM_API_LABEL_MAPPING LM ON UUID=LM.API_ID" +
                    " WHERE LM.LABEL_ID IN ( SELECT LABEL_ID FROM AM_LABELS WHERE LABEL_NAME IN (" +
                    DAOUtil.getParameterString(labelCount) +  ") AND TYPE_NAME ='STORE')";
        } else {
            return " INNER JOIN AM_API_LABEL_MAPPING LM ON UUID=LM.API_ID" +
                    " WHERE LM.LABEL_ID IN ( SELECT LABEL_ID FROM AM_LABELS WHERE TYPE_NAME ='STORE')";
        }
    }

    /**
     * Maps the search attribute specified with the relevant DB table/column that needs to be queried.
     * This default implementation is suitable for most DB implementations and can be overridden to support specific
     * requirements of a DB implementation.
     *
     * @param attributeKey Key fo the attribute that is being searched
     * @return DBTableMetaData which contains the relevant DB table/column combination
     */
    default DBTableMetaData mapSearchAttributesToDB(SearchType attributeKey) {
        DBTableMetaData metaData = new DBTableMetaData();

        if (SearchType.TAG == attributeKey) {
            //if the search is related to tags, need to check NAME column in AM_TAGS table
            metaData.setTableName(SQLConstants.AM_TAGS_TABLE_NAME);
            metaData.setColumnName(APIMgtConstants.TAG_NAME_COLUMN.toUpperCase(Locale.ENGLISH));
        } else if (SearchType.SUBCONTEXT == attributeKey) {
            //if the search is related to subcontext, need to check URL_PATTERN column in
            //AM_API_OPERATION_MAPPING table
            metaData.setTableName(SQLConstants.AM_API_OPERATION_MAPPING_TABLE_NAME);
            metaData.setColumnName(APIMgtConstants.URL_PATTERN_COLUMN.toUpperCase(Locale.ENGLISH));
        } else {
            //if the search is related to any other attribute, need to check that attribute
            //in AM_API table
            metaData.setTableName(SQLConstants.AM_API_TABLE_NAME);
            metaData.setColumnName(attributeKey.toString().toUpperCase(Locale.ENGLISH));
        }

        return metaData;
    }

    default String getSecondaryAttributesSearchQuery(Map<SecondarySearchType, String> secondaryAttributeMap) {
        String extendedQuery = "";
        if (secondaryAttributeMap != null && secondaryAttributeMap.containsKey(SecondarySearchType.GATEWAYLABEL)) {
            extendedQuery = SecondaryAttributeSearch.getLabelAttributeQuery(APIMgtConstants.LABEL_TYPE_GATEWAY);
        } else if (secondaryAttributeMap != null && secondaryAttributeMap.containsKey(SecondarySearchType.STORELABEL)) {
            extendedQuery = SecondaryAttributeSearch.getLabelAttributeQuery(APIMgtConstants.LABEL_TYPE_STORE);
        }
        return extendedQuery;
    }

    default int setSecondaryAttributeBasedSearchStatement(PreparedStatement statement,
            Map<SecondarySearchType, String> attributeMap) throws SQLException {
        int index = 0;

        for (Map.Entry<SecondarySearchType, String> entry : attributeMap.entrySet()) {
            entry.setValue('%' + entry.getValue().toLowerCase(Locale.ENGLISH) + '%');
            statement.setString(++index, entry.getValue());
        }
        return index;
    }

    /**
     *  Class to perform secondary level attribute search. This class will return common set of queries irrespective
     *  of the database type.
     */
    class SecondaryAttributeSearch {

        private static String getLabelAttributeQuery(String labelType) {
            return " LEFT JOIN AM_API_LABEL_MAPPING LM  ON UUID=LM.API_ID LEFT JOIN"
                    + "  AM_LABELS AL ON LM.LABEL_ID = AL.LABEL_ID WHERE AL.TYPE_NAME = '" + labelType
                    + "' AND LOWER (AL" + ".NAME) LIKE ?";
        }
    }

}
