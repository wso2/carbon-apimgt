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

import org.wso2.carbon.apimgt.core.models.ArtifactResourceMetaData;
import org.wso2.carbon.apimgt.core.models.ResourceCategory;
import org.wso2.carbon.apimgt.core.models.ResourceVisibility;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class ApiResourceDAO {

    static List<ArtifactResourceMetaData> getDocResourceMetaDataList(Connection connection, String apiID)
                                                                                        throws SQLException {
        final String query = "SELECT a.UUID , a.RESOURCE_NAME, a.DESCRIPTION, b.RESOURCE_CATEGORY, " +
                "a.DATA_TYPE, a.VISIBILITY " +
                "FROM AM_API_RESOURCES a, AM_RESOURCE_CATEGORIES b " +
                "WHERE a.API_ID = ? AND a.RESOURCE_CATEGORY_ID IN " +
                "(SELECT RESOURCE_CATEGORY_ID FROM AM_RESOURCE_CATEGORIES WHERE RESOURCE_CATEGORY LIKE ?)";

        List<ArtifactResourceMetaData> metaDataList = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setString(2, "DOC_%");
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                while (rs.next()) {
                    metaDataList.add(new ArtifactResourceMetaData.Builder().
                            id(rs.getString("UUID")).
                            name(rs.getString("RESOURCE_NAME")).
                            description(rs.getString("DESCRIPTION")).
                            category(ResourceCategory.valueOf(rs.getString("RESOURCE_CATEGORY"))).
                            dataType(rs.getString("DATA_TYPE")).
                            visibility(ResourceVisibility.valueOf(rs.getString("VISIBILITY"))).build());
                }
            }
        }

        return metaDataList;
    }

    static ArtifactResourceMetaData getResourceMetaData(Connection connection, String resourceID)
            throws SQLException {
        final String query = "SELECT a.UUID , a.RESOURCE_NAME, a.DESCRIPTION, b.RESOURCE_CATEGORY, " +
                "a.DATA_TYPE, a.VISIBILITY " +
                "FROM AM_API_RESOURCES a, AM_RESOURCE_CATEGORIES b " +
                "WHERE a.UUID = ? AND a.RESOURCE_CATEGORY_ID = b.RESOURCE_CATEGORY_ID";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return new ArtifactResourceMetaData.Builder().
                            id(rs.getString("UUID")).
                            name(rs.getString("RESOURCE_NAME")).
                            description(rs.getString("DESCRIPTION")).
                            category(ResourceCategory.valueOf(rs.getString("RESOURCE_CATEGORY"))).
                            dataType(rs.getString("DATA_TYPE")).
                            visibility(ResourceVisibility.valueOf(rs.getString("VISIBILITY"))).build();
                }
            }
        }

        return null;
    }

    static void addResourceMetaData(Connection connection, String apiID, ArtifactResourceMetaData metaData)
                                                                                                throws SQLException {
        final String query = "INSERT INTO AM_API_RESOURCES (UUID, API_ID, RESOURCE_NAME, DESCRIPTION, " +
                "RESOURCE_CATEGORY_ID, VISIBILITY) VALUES (?,?,?,?,?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, metaData.getId());
            statement.setString(2, apiID);
            statement.setString(3, metaData.getName());
            statement.setString(4, metaData.getDescription());
            statement.setInt(5, ResourceCategoryDAO.getResourceCategoryID(connection, metaData.getCategory()));
            statement.setString(6, metaData.getVisibility().toString());

            statement.execute();
        }
    }


    static String getUniqueTextResourceForCategory(Connection connection, String apiID,
                                                   ResourceCategory resourceCategory) throws SQLException {
        final String query = "SELECT RESOURCE_TEXT_VALUE FROM AM_API_RESOURCES WHERE API_ID = ? AND " +
                "RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, resourceCategory));
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("RESOURCE_TEXT_VALUE");
                }
            }
        }

        return null;
    }

    static String getTextResource(Connection connection, String resourceID) throws SQLException {
        final String query = "SELECT RESOURCE_TEXT_VALUE FROM AM_API_RESOURCES WHERE UUID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("RESOURCE_TEXT_VALUE");
                }
            }
        }

        return null;
    }

    static void addUniqueTextResourceForCategory(Connection connection, String apiID, String resourceName,
                                                 String description, ResourceCategory resourceCategory,
                                                 String visibility, String resourceValue) throws SQLException {
        if (isUniqueResourceForCategoryExists(connection, apiID, resourceCategory)) {
            throw new SQLException("Unique Resource Category " + resourceCategory.toString() + " already exists for " +
                    "api id " + apiID);
        }

        final String query = "INSERT INTO AM_API_RESOURCES (UUID, API_ID, RESOURCE_NAME, DESCRIPTION, " +
                "RESOURCE_CATEGORY_ID, VISIBILITY, RESOURCE_TEXT_VALUE) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, apiID);
            statement.setString(3, resourceName);
            statement.setString(4, description);
            statement.setInt(5, ResourceCategoryDAO.getResourceCategoryID(connection, resourceCategory));
            statement.setString(6, visibility);
            statement.setString(7, resourceValue);

            statement.execute();
        }
    }

    static void updateUniqueTextResourceForCategory(Connection connection, String apiID,
                                                    ResourceCategory category,
                                                    String resourceValue) throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_TEXT_VALUE = ? WHERE " +
                "API_ID = ? AND RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceValue);
            statement.setString(2, apiID);
            statement.setInt(3, ResourceCategoryDAO.getResourceCategoryID(connection, category));

            statement.execute();
        }
    }

    static void updateTextResource(Connection connection, String resourceID, String resourceValue)
            throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_TEXT_VALUE = ? WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceValue);
            statement.setString(2, resourceID);

            statement.execute();
        }
    }

    static InputStream getUniqueBinaryResourceForCategory(Connection connection, String apiID,
                                                          ResourceCategory category) throws SQLException {
        final String query = "SELECT RESOURCE_BINARY_VALUE FROM AM_API_RESOURCES WHERE API_ID = ? AND " +
                "RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getBlob("RESOURCE_BINARY_VALUE").getBinaryStream();
                }
            }
        }

        return null;
    }

    static InputStream getBinaryResource(Connection connection, String resourceID) throws SQLException {
        final String query = "SELECT RESOURCE_BINARY_VALUE FROM AM_API_RESOURCES WHERE UUID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getBlob("RESOURCE_BINARY_VALUE").getBinaryStream();
                }
            }
        }

        return null;
    }

    static void addUniqueBinaryResourceForCategory(Connection connection, String apiID, String resourceName,
                                                   String description, ResourceCategory category, String visibility,
                                                   InputStream resourceValue) throws SQLException {
        final String query = "INSERT INTO AM_API_RESOURCES (UUID, API_ID, RESOURCE_NAME, DESCRIPTION, " +
                "RESOURCE_CATEGORY_ID, VISIBILITY, RESOURCE_BINARY_VALUE) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, apiID);
            statement.setString(3, resourceName);
            statement.setString(4, description);
            statement.setInt(5, ResourceCategoryDAO.getResourceCategoryID(connection, category));
            statement.setString(6, visibility);
            statement.setBlob(7, resourceValue);

            statement.execute();
        }
    }

    static void updateUniqueBinaryResourceForCategory(Connection connection, String apiID, ResourceCategory category,
                                                      InputStream resourceValue) throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_BINARY_VALUE = ? WHERE " +
                "API_ID = ? AND RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBlob(1, resourceValue);
            statement.setString(2, apiID);
            statement.setInt(3, ResourceCategoryDAO.getResourceCategoryID(connection, category));

            statement.execute();
        }
    }

    static void updateBinaryResource(Connection connection, String resourceID, InputStream resourceValue)
                                                                                            throws SQLException {
        final String query = "UPDATE AM_API_RESOURCES SET RESOURCE_BINARY_VALUE = ? WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBlob(1, resourceValue);
            statement.setString(2, resourceID);

            statement.execute();
        }
    }

    private static boolean isUniqueResourceForCategoryExists(Connection connection, String apiID,
                                                             ResourceCategory resourceCategory) throws SQLException {
        final String query = "SELECT 1 FROM AM_API_RESOURCES WHERE API_ID = ? AND RESOURCE_CATEGORY_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, resourceCategory));
            statement.execute();

            try (ResultSet rs =  statement.getResultSet()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    static void deleteUniqueResourceForCategory(Connection connection, String apiID, ResourceCategory resourceCategory)
            throws SQLException {
        final String query = "DELETE FROM AM_API_RESOURCES WHERE API_ID = ? AND RESOURCE_CATEGORY_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.setInt(2, ResourceCategoryDAO.getResourceCategoryID(connection, resourceCategory));

            statement.execute();
        }
    }

    static void deleteResource(Connection connection, String resourceID)
            throws SQLException {
        final String query = "DELETE FROM AM_API_RESOURCES WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceID);

            statement.execute();
        }
    }
}
