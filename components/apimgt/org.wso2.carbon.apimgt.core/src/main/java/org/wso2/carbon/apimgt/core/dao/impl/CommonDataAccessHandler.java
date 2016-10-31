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

import org.wso2.carbon.apimgt.core.dao.APIManagementDAOException;
import org.wso2.carbon.apimgt.core.dao.ErrorCode;
import org.wso2.carbon.apimgt.core.models.API;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;


class CommonDataAccessHandler {

    static API getAPI(String apiID, final String query) throws APIManagementDAOException {
        API api = null;
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    api = new API(rs.getString("PROVIDER"), rs.getString("VERSION"), rs.getString("NAME"));
                    api.setID(rs.getString("UUID"));
                }
            }
        } catch (SQLException e) {
            DAOUtil.handleException(ErrorCode.SQL_EXCEPTION, e.getMessage(), e);
        }

        return api;
    }


    static void addAPI(API api, final String query) throws APIManagementDAOException {
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, api.getProvider());
            statement.setString(2, api.getName());
            statement.setString(3, api.getContext());
            statement.setString(4, api.getVersion());
            statement.setBoolean(5, api.isDefaultVersion());
            statement.setString(6, api.getDescription());
            statement.setString(7, api.getVisibility().toString());
            statement.setBoolean(8, api.isResponseCachingEnabled());
            statement.setInt(9, api.getCacheTimeout());
            api.setID(UUID.randomUUID().toString());
            statement.setString(10, api.getID());
            statement.setString(11, api.getTechnicalOwner());
            statement.setString(12, api.getTechnicalOwnerEmail());
            statement.setString(13, api.getBusinessOwner());
            statement.setString(14, api.getBusinessOwnerEmail());
            statement.setString(15, api.getCreatedBy());
            Date date = new Date();
            api.setCreatedTime(date);
            api.setLastUpdatedTime(date);
            statement.setDate(16, new java.sql.Date(date.getTime()));
            statement.setDate(17, new java.sql.Date(date.getTime()));

            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            DAOUtil.handleException(ErrorCode.SQL_EXCEPTION, e.getMessage(), e);
        }
    }

    static boolean isAPIExists(API api, final String query) throws APIManagementDAOException {
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, api.getProvider());
            statement.setString(2, api.getName());
            statement.setString(3, api.getVersion());

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            DAOUtil.handleException(ErrorCode.SQL_EXCEPTION, e.getMessage(), e);
        }

        return false;
    }

    static void deleteAPI(String apiID, final String query) throws APIManagementDAOException {
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, apiID);
            statement.execute();
        } catch (SQLException e) {
            DAOUtil.handleException(ErrorCode.SQL_EXCEPTION, e.getMessage(), e);
        }
    }

}
