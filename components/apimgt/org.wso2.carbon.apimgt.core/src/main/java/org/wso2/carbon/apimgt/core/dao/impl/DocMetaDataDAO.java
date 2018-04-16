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

import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides data access to Doc Meta data related tables
 */
class DocMetaDataDAO {
    private static final String AM_API_DOC_META_DATA_TABLE_NAME = "AM_API_DOC_META_DATA";

    static List<DocumentInfo> getDocumentInfoList(Connection connection, String apiID) throws SQLException {
        final String query = "SELECT meta.UUID, meta.NAME, meta.SUMMARY, meta.TYPE, meta.OTHER_TYPE_NAME, " +
                "meta.SOURCE_URL, meta.FILE_NAME, meta.SOURCE_TYPE, meta.VISIBILITY " +
                "FROM AM_API_DOC_META_DATA meta, AM_API_RESOURCES rec WHERE " +
                "meta.UUID = rec.UUID AND rec.API_ID = ?";

        List<DocumentInfo> metaDataList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                while (rs.next()) {
                    metaDataList.add(new DocumentInfo.Builder().
                            id(rs.getString("UUID")).
                            name(rs.getString("NAME")).
                            summary(rs.getString("SUMMARY")).
                            type(DocumentInfo.DocType.valueOf(rs.getString("TYPE"))).
                            otherType(rs.getString("OTHER_TYPE_NAME")).
                            sourceURL(rs.getString("SOURCE_URL")).
                            fileName(rs.getString("FILE_NAME")).
                            sourceType(DocumentInfo.SourceType.valueOf(rs.getString("SOURCE_TYPE"))).
                            visibility(DocumentInfo.Visibility.valueOf(rs.getString("VISIBILITY"))).build());
                }
            }
        }

        return metaDataList;
    }

    /**
     * Update doc info
     *
     * @param connection   DB connection
     * @param documentInfo document info
     * @param updatedBy    user who performs the action
     * @throws SQLException
     */
    static void updateDocInfo(Connection connection, DocumentInfo documentInfo, String updatedBy) throws SQLException {
        deleteDOCPermission(connection, documentInfo.getId());
        addDOCPermission(connection, documentInfo.getPermissionMap(), documentInfo.getId());
        final String query = "UPDATE AM_API_DOC_META_DATA SET NAME = ?, SUMMARY = ?, TYPE = ?, "
                + "OTHER_TYPE_NAME = ?, SOURCE_URL = ?, FILE_NAME = ?, SOURCE_TYPE = ?, VISIBILITY = ?, "
                + "UPDATED_BY = ?, LAST_UPDATED_TIME = ? WHERE UUID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, documentInfo.getName());
            statement.setString(2, documentInfo.getSummary());
            statement.setString(3, documentInfo.getType().toString());
            statement.setString(4, documentInfo.getOtherType());
            statement.setString(5, documentInfo.getSourceURL());
            statement.setString(6, documentInfo.getFileName());
            statement.setString(7, documentInfo.getSourceType().toString());
            statement.setString(8, documentInfo.getVisibility().toString());
            statement.setString(9, updatedBy);
            statement.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(11, documentInfo.getId());
            statement.execute();
        }
    }

    static DocumentInfo getDocumentInfo(Connection connection, String docID) throws SQLException {
        final String query = "SELECT AM_API_DOC_META_DATA.UUID, AM_API_DOC_META_DATA.NAME, AM_API_DOC_META_DATA" +
                ".SUMMARY, AM_API_DOC_META_DATA.TYPE, AM_API_DOC_META_DATA.OTHER_TYPE_NAME, AM_API_DOC_META_DATA" +
                ".SOURCE_URL, AM_API_DOC_META_DATA.FILE_NAME, AM_API_DOC_META_DATA.SOURCE_TYPE, AM_API_DOC_META_DATA" +
                ".VISIBILITY FROM AM_API_DOC_META_DATA WHERE AM_API_DOC_META_DATA.UUID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, docID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                while (rs.next()) {
                    return new DocumentInfo.Builder().
                            id(rs.getString("UUID")).
                            name(rs.getString("NAME")).
                            summary(rs.getString("SUMMARY")).
                            type(DocumentInfo.DocType.valueOf(rs.getString("TYPE"))).
                            otherType(rs.getString("OTHER_TYPE_NAME")).
                            sourceURL(rs.getString("SOURCE_URL")).
                            sourceType(DocumentInfo.SourceType.valueOf(rs.getString("SOURCE_TYPE"))).
                            visibility(DocumentInfo.Visibility.valueOf(rs.getString("VISIBILITY"))).
                            fileName(rs.getString("FILE_NAME")).build();
                }
            }
        }

        return null;
    }

    static DocumentInfo checkDocument(Connection connection, String docID) throws SQLException {
        final String query = "SELECT UUID, NAME, SUMMARY, TYPE, OTHER_TYPE_NAME, SOURCE_URL, FILE_NAME, SOURCE_TYPE, " +
                "VISIBILITY FROM AM_API_DOC_META_DATA WHERE UUID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, docID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                while (rs.next()) {
                    return new DocumentInfo.Builder().
                            id(rs.getString("UUID")).
                            name(rs.getString("NAME")).
                            summary(rs.getString("SUMMARY")).
                            type(DocumentInfo.DocType.valueOf(rs.getString("TYPE"))).
                            otherType(rs.getString("OTHER_TYPE_NAME")).
                            sourceURL(rs.getString("SOURCE_URL")).
                            fileName(rs.getString("FILE_NAME")).
                            sourceType(DocumentInfo.SourceType.valueOf(rs.getString("SOURCE_TYPE"))).
                            visibility(DocumentInfo.Visibility.valueOf(rs.getString("VISIBILITY"))).build();
                }
            }
        }

        return null;
    }


    static void addDocumentInfo(Connection connection, DocumentInfo documentInfo) throws SQLException {
        final String query = "INSERT INTO AM_API_DOC_META_DATA (UUID, NAME, SUMMARY, TYPE, OTHER_TYPE_NAME, " +
                "SOURCE_URL, FILE_NAME, SOURCE_TYPE, VISIBILITY, CREATED_BY, CREATED_TIME, UPDATED_BY, " + 
                "LAST_UPDATED_TIME) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, documentInfo.getId());
            statement.setString(2, documentInfo.getName());
            statement.setString(3, documentInfo.getSummary());
            statement.setString(4, documentInfo.getType().toString());
            statement.setString(5, documentInfo.getOtherType());
            statement.setString(6, documentInfo.getSourceURL());
            statement.setString(7, documentInfo.getFileName());
            statement.setString(8, documentInfo.getSourceType().toString());
            statement.setString(9, documentInfo.getVisibility().toString());
            statement.setString(10, documentInfo.getCreatedBy());
            statement.setTimestamp(11, Timestamp.from(documentInfo.getCreatedTime()));
            statement.setString(12, documentInfo.getUpdatedBy());
            statement.setTimestamp(13, Timestamp.from(documentInfo.getLastUpdatedTime()));
            statement.execute();
            addDOCPermission(connection, documentInfo.getPermissionMap(), documentInfo.getId());
        }

    }

    static String getLastUpdatedTimeOfDocument(String documentId) throws APIMgtDAOException {
        return EntityDAO.getLastUpdatedTimeOfResourceByUUID(AM_API_DOC_META_DATA_TABLE_NAME, documentId);
    }

    private static void deleteDOCPermission(Connection connection, String docID) throws SQLException {
        final String query = "DELETE FROM AM_DOC_GROUP_PERMISSION WHERE DOC_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, docID);
            statement.execute();
        }
    }

    /**
     * Add DOC permission
     * @param connection connection
     * @param permissionMap  permission map
     * @param docId document Id.
     * @throws SQLException
     */
    private static void addDOCPermission(Connection connection, HashMap permissionMap, String docId) throws
            SQLException {
        final String query = "INSERT INTO AM_DOC_GROUP_PERMISSION (DOC_ID, GROUP_ID, PERMISSION) VALUES (?, ?, ?)";
        Map<String, Integer> map = permissionMap;
        if (permissionMap != null) {
            if (permissionMap.size() > 0) {
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        statement.setString(1, docId);
                        statement.setString(2, entry.getKey());
                        //if permission value is UPDATE or DELETE we by default give them read permission also.
                        if (entry.getValue() < APIMgtConstants.Permission.READ_PERMISSION && entry.getValue() != 0) {
                            statement.setInt(3, entry.getValue() + APIMgtConstants.Permission.READ_PERMISSION);
                        } else {
                            statement.setInt(3, entry.getValue());
                        }
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
            }
        } else {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, docId);
                statement.setString(2, APIMgtConstants.Permission.EVERYONE_GROUP);
                statement.setInt(3, 7);
                statement.execute();
            }
        }

    }

}
