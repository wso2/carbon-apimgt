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

import org.wso2.carbon.apimgt.core.models.ResourceCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides access to Resource Categories which maybe shared across multiple entities
 */
class ResourceCategoryDAO {

    static boolean isResourceCategoryExists(Connection connection, ResourceCategory resourceCategory)
                                                                                            throws SQLException {
        final String query = "SELECT 1 FROM AM_RESOURCE_CATEGORIES WHERE RESOURCE_CATEGORY = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceCategory.toString());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    static boolean isResourceCategoriesExist(Connection connection) throws SQLException {
        final String query = "SELECT 1 FROM AM_RESOURCE_CATEGORIES";

        try (PreparedStatement statement = connection.prepareStatement(query)) {

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        }

        return false;
    }

    static int getResourceCategoryID(Connection connection, ResourceCategory resourceCategory) throws SQLException {
        final String query = "SELECT RESOURCE_CATEGORY_ID FROM AM_RESOURCE_CATEGORIES WHERE RESOURCE_CATEGORY = ?";
        int resourceTypeID;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceCategory.toString());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    resourceTypeID = rs.getInt("RESOURCE_CATEGORY_ID");
                } else {
                    throw new SQLException("Resource category does not exist");
                }
            }
        }

        return resourceTypeID;
    }

    static void addResourceCategory(Connection connection, ResourceCategory resourceCategory) throws SQLException {
        final String query = "INSERT INTO AM_RESOURCE_CATEGORIES (RESOURCE_CATEGORY) VALUES (?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceCategory.toString());
            statement.execute();
        }
    }

    static void addResourceCategories(Connection connection) throws SQLException {
        final String query = "INSERT INTO AM_RESOURCE_CATEGORIES (RESOURCE_CATEGORY) VALUES (?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, ResourceCategory.SWAGGER.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.WSDL_URI.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.IMAGE.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_HOW_TO_FILE.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_HOW_TO_INLINE.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_HOW_TO_URL.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_OTHER_FILE.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_OTHER_INLINE.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_OTHER_URL.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_PUBLIC_FORUM_FILE.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_PUBLIC_FORUM_INLINE.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_PUBLIC_FORUM_URL.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_SAMPLE_AND_SDK_FILE.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_SAMPLE_AND_SDK_INLINE.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_SAMPLE_AND_SDK_URL.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_SUPPORT_FORUM_FILE.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_SUPPORT_FORUM_INLINE.toString());
            statement.addBatch();

            statement.setString(1, ResourceCategory.DOC_SUPPORT_FORUM_URL.toString());
            statement.addBatch();

            statement.executeBatch();
        }
    }
}
