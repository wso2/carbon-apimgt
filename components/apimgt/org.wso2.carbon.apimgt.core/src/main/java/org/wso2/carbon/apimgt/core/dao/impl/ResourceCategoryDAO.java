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

import org.wso2.carbon.apimgt.core.models.ArtifactResource;
import org.wso2.carbon.apimgt.core.models.ResourceCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Provides access to Resource Categories which maybe shared across multiple entities
 */
class ResourceCategoryDAO {

    private static boolean isResourceCategoryExists(Connection connection, ResourceCategory resourceCategory)
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

    static boolean isStandardResourceCategoriesExist(Connection connection) throws SQLException {
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

    static int findResourceCategory(Connection connection, ArtifactResource resource) throws SQLException {
        ResourceCategory category = resource.getCategory();
        int categoryID;

        if (!isResourceCategoryExists(connection, category)) {
            if (category == ResourceCategory.OTHER) {
                String customCategory = resource.getCustomCategory();
                ResourceCategoryDAO.addResourceCategory(connection, customCategory);
                categoryID = ResourceCategoryDAO.getResourceCategoryID(connection, customCategory);
            } else {
                throw new IllegalStateException("Resource Category is not type OTHER, " +
                        "but has not been added as a standard category type to table AM_RESOURCE_CATEGORIES");
            }
        } else {
            categoryID = ResourceCategoryDAO.getResourceCategoryID(connection, category.toString());
        }

        return categoryID;
    }

    static int getResourceCategoryID(Connection connection, String resourceCategory) throws SQLException {
        final String query = "SELECT RESOURCE_CATEGORY_ID FROM AM_RESOURCE_CATEGORIES WHERE RESOURCE_CATEGORY = ?";
        int resourceTypeID;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceCategory);

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

    static void addResourceCategory(Connection connection, String resourceCategory) throws SQLException {
        final String query = "INSERT INTO AM_RESOURCE_CATEGORIES (RESOURCE_CATEGORY) VALUES (?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, resourceCategory);
            statement.execute();
        }
    }

    static void addResourceCategories(Connection connection) throws SQLException {
        final String query = "INSERT INTO AM_RESOURCE_CATEGORIES (RESOURCE_CATEGORY) VALUES (?)";

        final Map<String, ResourceCategory> standardCategories = ResourceCategory.getStandardCategories();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (ResourceCategory category : standardCategories.values()) {
                statement.setString(1, category.toString());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }
}
