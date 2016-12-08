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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Tag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to Tags which maybe shared across multiple entities
 */
public class TagDAOImpl implements TagDAO {
    TagDAOImpl() {
    }

    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    static List<Integer> addTagsIfNotExist(
            Connection connection, List<String> tags) throws SQLException {
        List<Integer> tagIDs = new ArrayList<>();

        if (!tags.isEmpty()) {
            final String query = "SELECT TAG_ID, NAME FROM AM_TAGS WHERE NAME IN (" +
                    DAOUtil.getParameterString(tags.size()) + ")";

            List<String> existingTags = new ArrayList<>();

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (int i = 0; i < tags.size(); ++i) {
                    statement.setString(i + 1, tags.get(i));
                }

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) { // If Tag already exists get respective tag ID
                        int tagID = rs.getInt("TAG_ID");
                        tagIDs.add(tagID);
                        incrementTagCount(connection, tagID);
                        existingTags.add(rs.getString("NAME"));
                    }
                }
            }

            tags.removeAll(existingTags); // Remove already existing tags from list so we wont try to add them again

            if (!tags.isEmpty()) { // Add tags that don't already exist
                insertNewTags(connection, tags, tagIDs);
            }
        }

        return tagIDs;
    }

    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
    static List<String> getTagsByIDs(Connection connection, List<Integer> tagIDs) throws SQLException {
        List<String> tags = new ArrayList<>();

        if (!tagIDs.isEmpty()) {
            final String query = "SELECT NAME FROM AM_TAGS WHERE TAG_ID IN (" +
                    DAOUtil.getParameterString(tagIDs.size()) + ")";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (int i = 0; i < tagIDs.size(); ++i) {
                    statement.setInt(i + 1, tagIDs.get(i));
                }

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        tags.add(rs.getString("NAME"));
                    }
                }
            }
        }

        return tags;
    }

    private static void incrementTagCount(Connection connection, int tagID) throws SQLException {
        final String query = "UPDATE AM_TAGS SET COUNT = COUNT + 1 WHERE TAG_ID = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, tagID);
            statement.execute();
        }
    }

    private static void insertNewTags(Connection connection, List<String> tags, List<Integer> tagIDs)
            throws SQLException {
        final String maxTagIDQuery = "SELECT MAX(TAG_ID) AS TAG_ID FROM AM_TAGS";
        int maxTagID = -1;
        try (PreparedStatement statement = connection.prepareStatement(maxTagIDQuery)) {
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    maxTagID = rs.getInt("TAG_ID");
                }
            }
        }

        final String query = "INSERT INTO AM_TAGS (NAME, COUNT) VALUES (?,?)";

        try (PreparedStatement statement = connection.prepareStatement(query, new String[] { "tag_id" })) {
            for (String tag : tags) {
                statement.setString(1, tag);
                statement.setInt(2, 1); // The count should always be 1 initially
                statement.addBatch();
            }

            statement.executeBatch();
        }

        final String newTagIDsQuery = "SELECT TAG_ID FROM AM_TAGS WHERE TAG_ID > ?";

        try (PreparedStatement statement = connection.prepareStatement(newTagIDsQuery)) {
            statement.setInt(1, maxTagID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    tagIDs.add(rs.getInt("TAG_ID"));
                }
            }
        }

    }

    @Override
    public List<Tag> getTags() throws APIMgtDAOException {
        final String query = "SELECT NAME FROM AM_TAGS";

        List<Tag> tags = new ArrayList<>();

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Tag tag = new Tag.Builder().
                            name(rs.getString("NAME")).
                            count(rs.getInt("COUNT")).build();

                    tags.add(tag);
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException(e);
        }

        return tags;
    }
}
