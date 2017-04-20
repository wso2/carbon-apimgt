/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.collections.map.HashedMap;
import org.wso2.carbon.apimgt.core.models.UriTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ApiMetaInfoDAO {
    static Set<String> getLabelNames(Connection connection, String apiID) throws SQLException {
        Set<String> labelNames = new HashSet<>();

        final String query = "SELECT LABEL_ID FROM AM_API_LABEL_MAPPING WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                List<String> labelIDs = new ArrayList<>();

                while (rs.next()) {
                    labelIDs.add(rs.getString("LABEL_ID"));
                }

                if (!labelIDs.isEmpty()) {
                    labelNames = LabelDAOImpl.getLabelNamesByIDs(labelIDs);
                }
            }
        }

        return labelNames;
    }

    static Set<String> getTransports(Connection connection, String apiID) throws SQLException {
        Set<String> transports = new HashSet<>();

        final String query = "SELECT TRANSPORT FROM AM_API_TRANSPORTS WHERE API_ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    transports.add(rs.getString("TRANSPORT"));
                }
            }
        }

        return transports;
    }

    static Map<String, UriTemplate> getUriTemplates(Connection connection, String apiId) throws SQLException {
        final String query = "SELECT OPERATION_ID,API_ID,HTTP_METHOD,URL_PATTERN,AUTH_SCHEME,API_POLICY_ID FROM " +
                "AM_API_OPERATION_MAPPING WHERE API_ID = ?";
        Map<String, UriTemplate> uriTemplateSet = new HashMap();
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiId);
            statement.execute();
            try (ResultSet rs = statement.getResultSet()) {
                while (rs.next()) {
                    UriTemplate uriTemplate = new UriTemplate.UriTemplateBuilder()
                            .uriTemplate(rs.getString("URL_PATTERN")).authType(rs.getString("AUTH_SCHEME"))
                            .httpVerb(rs.getString("HTTP_METHOD"))
                            .policy(getAPIThrottlePolicyName(connection, rs.getString("API_POLICY_ID"))).templateId
                                    (rs.getString("OPERATION_ID")).endpoint(getEndPointsForOperation(connection,
                                    apiId, rs.getString("OPERATION_ID"))).build();
                    uriTemplateSet.put(uriTemplate.getTemplateId(), uriTemplate);
                }
            }
        }
        return uriTemplateSet;
    }


    private static String getAPIThrottlePolicyName(Connection connection, String policyID) throws SQLException {
        final String query = "SELECT NAME FROM AM_API_POLICY WHERE UUID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, policyID);
            statement.execute();

            try (ResultSet rs = statement.getResultSet()) {
                if (rs.next()) {
                    return rs.getString("NAME");
                }
            }
        }

        throw new SQLException("API Policy ID " + policyID + ", does not exist");
    }

    private static Map<String, String> getEndPointsForOperation(Connection connection, String apiId, String operationId)
            throws SQLException {
        Map<String, String> endpointMap = new HashedMap();
        final String query = "SELECT ENDPOINT_ID,TYPE FROM AM_API_RESOURCE_ENDPOINT WHERE API_ID=? AND " +
                "OPERATION_ID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, operationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    endpointMap.put(resultSet.getString("TYPE"), resultSet.getString("ENDPOINT_ID"));
                }
            }
        }
        return endpointMap;
    }
}
