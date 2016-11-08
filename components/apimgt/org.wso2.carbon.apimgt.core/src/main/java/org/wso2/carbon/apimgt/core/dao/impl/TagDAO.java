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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides access to Tags which maybe shared across multiple entities
 */
public class TagDAO {
    @SuppressFBWarnings("SQL_PREPARED_STATEMENT_GENERATED_FROM_NONCONSTANT_STRING")
     static List<Integer> addTagsIfNotExist(Connection connection, List<String> tags) throws SQLException {
        List<Integer> tagIDs =  new ArrayList<>();

        if (!tags.isEmpty()) {
            final String query = "SELECT TAG_ID, TAG_NAME FROM AM_TAGS WHERE TAG_NAME IN (" +
                    DAOUtil.getParameterString(tags.size()) + ")";

            List<String> existingTags = new ArrayList<>();

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (int i = 0; i < tags.size(); ++i) {
                    statement.setString(i + 1, tags.get(i));
                }

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) { // If Tag already exists get respective tag ID
                        tagIDs.add(rs.getInt("TAG_ID"));
                        existingTags.add(rs.getString("TAG_NAME"));
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
            List<String> questionMarks = new ArrayList<>(Collections.nCopies(tagIDs.size(), "?"));

            final String query = "SELECT TAG_NAME FROM AM_TAGS WHERE TAG_ID IN (" + String.join(",", questionMarks) + ")";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (int i = 0; i < tagIDs.size(); ++i) {
                    statement.setInt(i + 1, tagIDs.get(i));
                }

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        tags.add(rs.getString("TAG_NAME"));
                    }
                }
            }
        }

        return tags;
    }

    private static void insertNewTags(Connection connection, List<String> tags, List<Integer> tagIDs) throws SQLException {
        final String query = "INSERT INTO AM_TAGS (TAG_NAME) VALUES (?)";

        try (PreparedStatement statement = connection.prepareStatement(query, new String[]{"tag_id"})) {
            for (String tag : tags) {
                statement.setString(1, tag);
                statement.addBatch();
            }

            statement.execute();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                while (rs.next()) { // Get tag ID of newly added tags
                    tagIDs.add(rs.getInt(1));
                }
            }

        }
    }
}
