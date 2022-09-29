package org.wso2.carbon.apimgt.impl.dao;

import org.wso2.carbon.apimgt.impl.APIConstants.ResourceCategory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides access to Resource Categories which maybe shared across multiple entities
 */
public class ResourceCategoryDAO {

    public static boolean isStandardResourceCategoriesExist(Connection connection) throws SQLException {
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

    public static int getResourceCategoryID(Connection connection, ResourceCategory category) throws SQLException {
        final String query = "SELECT RESOURCE_CATEGORY_ID FROM AM_RESOURCE_CATEGORIES WHERE RESOURCE_CATEGORY = ?";
        int resourceTypeID;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, category.toString());

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

    public static void addResourceCategories(Connection connection) throws SQLException {
        final String query = "INSERT INTO AM_RESOURCE_CATEGORIES (RESOURCE_CATEGORY) VALUES (?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            for (ResourceCategory category : ResourceCategory.values()) {
                statement.setString(1, category.toString());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

}
