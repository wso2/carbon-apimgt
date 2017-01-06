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

import org.wso2.carbon.apimgt.core.models.DocumentInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides data access to Doc Meta data related tables
 */
class DocMetaDataDAO {

    static List<DocumentInfo> getDocumentInfoList(Connection connection, String apiID) throws SQLException {
        final String query = "SELECT meta.UUID, meta.NAME, meta.SUMMARY, meta.TYPE, meta.OTHER_TYPE_NAME, " +
                "meta.SOURCE_URL, meta.SOURCE_TYPE, meta.VISIBILITY " +
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
                            sourceType(DocumentInfo.SourceType.valueOf(rs.getString("SOURCE_TYPE"))).
                            visibility(DocumentInfo.Visibility.valueOf(rs.getString("VISIBILITY"))).build());
                }
            }
        }

        return metaDataList;
    }

    static DocumentInfo getDocumentInfo(Connection connection, String docID) throws SQLException {
        final String query = "SELECT AM_API_DOC_META_DATA.UUID, AM_API_DOC_META_DATA.NAME, AM_API_DOC_META_DATA" +
                ".SUMMARY, AM_API_DOC_META_DATA.TYPE, AM_API_DOC_META_DATA.OTHER_TYPE_NAME, AM_API_DOC_META_DATA" +
                ".SOURCE_URL, AM_API_DOC_META_DATA.SOURCE_TYPE, AM_API_DOC_META_DATA.VISIBILITY,AM_API_RESOURCES" +
                ".DATA_TYPE FROM AM_API_DOC_META_DATA INNER JOIN AM_API_RESOURCES ON AM_API_DOC_META_DATA.UUID = " +
                "AM_API_RESOURCES.UUID WHERE  AM_API_DOC_META_DATA.UUID = ?";

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
                            fileName(rs.getString("DATA_TYPE")).build();
                }
            }
        }

        return null;
    }

    static DocumentInfo checkDocument(Connection connection, String docID) throws SQLException {
        final String query = "SELECT UUID, NAME, SUMMARY, TYPE, OTHER_TYPE_NAME, SOURCE_URL, SOURCE_TYPE, VISIBILITY " +
                "FROM AM_API_DOC_META_DATA WHERE UUID = ?";

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
                            visibility(DocumentInfo.Visibility.valueOf(rs.getString("VISIBILITY"))).build();
                }
            }
        }

        return null;
    }


    static void addDocumentInfo(Connection connection, DocumentInfo documentInfo) throws SQLException {
        final String query = "INSERT INTO AM_API_DOC_META_DATA (UUID, NAME, SUMMARY, TYPE, OTHER_TYPE_NAME, " +
                "SOURCE_URL, SOURCE_TYPE, VISIBILITY) VALUES (?,?,?,?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, documentInfo.getId());
            statement.setString(2, documentInfo.getName());
            statement.setString(3, documentInfo.getSummary());
            statement.setString(4, documentInfo.getType().toString());
            statement.setString(5, documentInfo.getOtherType());
            statement.setString(6, documentInfo.getSourceURL());
            statement.setString(7, documentInfo.getSourceType().toString());
            statement.setString(8, documentInfo.getVisibility().toString());

            statement.execute();
        }
    }

}
