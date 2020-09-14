/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *  This class used to manage Scopes related dao operations.
 */
public class ScopesDAO {

    private static final Log log = LogFactory.getLog(ScopesDAO.class);
    private static final ScopesDAO INSTANCE = new ScopesDAO();

    public static ScopesDAO getInstance() {

        return INSTANCE;
    }

    private ScopesDAO() {

    }

    public boolean addScopes(Set<Scope> scopeSet, int tenantId) throws APIManagementException {

        if (scopeSet == null || scopeSet.isEmpty()) {
            return false;
        }
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement addScopeStatement;
            PreparedStatement addScopeBindingStatement;
            try {
                addScopeStatement = connection.prepareStatement(SQLConstants.INSERT_SCOPE_SQL);
                addScopeBindingStatement = connection.prepareStatement(SQLConstants.ADD_SCOPE_MAPPING);
                for (Scope scope : scopeSet) {
                    if (!isScopeExist(connection, scope.getKey(), tenantId)) {
                        addScopeStatement.setString(1, scope.getKey());
                        addScopeStatement.setString(2, scope.getName());
                        addScopeStatement.setString(3, scope.getDescription());
                        addScopeStatement.setInt(4, tenantId);
                        addScopeStatement.setString(5, APIConstants.DEFAULT_SCOPE_TYPE);
                        addScopeStatement.addBatch();
                        addScopeBindingsToBatch(addScopeBindingStatement, scope, tenantId);
                    }
                }
                addScopeStatement.executeBatch();
                addScopeBindingStatement.executeBatch();
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error while saving scopes into db", e, ExceptionCodes.INTERNAL_ERROR);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
    }

    private void addScopeBindingsToBatch(PreparedStatement addScopeBindingStatement, Scope scope, int tenantId)
            throws SQLException {

        for (String role : scope.getRoles().split(",")) {
            addScopeBindingStatement.setString(1, scope.getKey());
            addScopeBindingStatement.setInt(2, tenantId);
            addScopeBindingStatement.setString(3, role);
            addScopeBindingStatement.setString(4, APIConstants.DEFAULT_BINDING_TYPE);
            addScopeBindingStatement.addBatch();
        }
    }

    public boolean updateScope(Scope scope, int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.UPDATE_SCOPE_SQL)) {
                preparedStatement.setString(1, scope.getName());
                preparedStatement.setString(2, scope.getDescription());
                preparedStatement.setString(3, scope.getKey());
                preparedStatement.setInt(4, tenantId);
                preparedStatement.executeUpdate();
                deleteScopeBindings(connection, scope.getKey(), tenantId);
                addScopeBindings(connection, scope, tenantId);
                connection.commit();
                return true;
            } catch (SQLException e) {
                log.error("Error while updating scopes into db", e);
                connection.rollback();
                throw new APIManagementException("Error while updating scopes into db", e,
                        ExceptionCodes.INTERNAL_ERROR);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
    }

    public boolean deleteScope(String scopeName, int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.DELETE_SCOPE_SQL)) {
                preparedStatement.setString(1, scopeName);
                preparedStatement.setInt(2, tenantId);
                preparedStatement.executeUpdate();
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error while deleting scopes from db", e,
                        ExceptionCodes.INTERNAL_ERROR);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
    }

    public boolean deleteScopes(Set<String> scopes, int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.DELETE_SCOPE_SQL)) {
                for (String scope : scopes) {
                    preparedStatement.setString(1, scope);
                    preparedStatement.setInt(2, tenantId);
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw new APIManagementException("Error while deleting scopes from db", e,
                        ExceptionCodes.INTERNAL_ERROR);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
    }

    public Scope getScope(String name, int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.GET_SCOPE_SQL)) {
                preparedStatement.setString(1, name);
                preparedStatement.setInt(2, tenantId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String displayName = resultSet.getString("DISPLAY_NAME");
                        String description = resultSet.getString("DESCRIPTION");
                        Scope scope = new Scope();
                        scope.setName(displayName);
                        scope.setDescription(description);
                        scope.setKey(name);
                        scope.setRoles(String.join(",", getScopeBindings(connection, name, tenantId)));
                        return scope;
                    }
                }
            } catch (SQLException e) {
                String msg = String.format("Error while retrieving scope %s from db", name);
                log.error(msg, e);
                throw new APIManagementException(msg, e, ExceptionCodes.INTERNAL_ERROR);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
    }

    private boolean isScopeExist(Connection connection, String scopeKey, int tenantId) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.SCOPE_EXIST_SQL)) {
            preparedStatement.setString(1, scopeKey);
            preparedStatement.setInt(2, tenantId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public boolean isScopeExist(String scopeKey, int tenantId) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return isScopeExist(connection, scopeKey, tenantId);
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }
    }

    public List<Scope> getScopes(int tenantId) throws APIManagementException {

        List<Scope> scopeList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.GET_SCOPES_SQL)) {
                preparedStatement.setInt(1, tenantId);
                preparedStatement.setString(2, APIConstants.DEFAULT_SCOPE_TYPE);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String name = resultSet.getString("SCOPE_KEY");
                        String displayName = resultSet.getString("DISPLAY_NAME");
                        String description = resultSet.getString("DESCRIPTION");
                        Scope scope = new Scope();
                        scope.setName(displayName);
                        scope.setDescription(description);
                        scope.setKey(name);
                        scope.setRoles(String.join(",", getScopeBindings(connection, name, tenantId)));
                        scopeList.add(scope);
                    }
                }
            } catch (SQLException e) {
                throw new APIManagementException("Error while retrieving scopes from db", e,
                        ExceptionCodes.INTERNAL_ERROR);
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving database connection", e,
                    ExceptionCodes.INTERNAL_ERROR);
        }

        return scopeList;
    }

    private void addScopeBindings(Connection connection, Scope scope, int tenantId) throws SQLException {

        if (StringUtils.isNotEmpty(scope.getRoles()) && scope.getRoles().split(",").length > 0) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.ADD_SCOPE_MAPPING)) {
                for (String role : scope.getRoles().split(",")) {
                    preparedStatement.setString(1, scope.getKey());
                    preparedStatement.setInt(2, tenantId);
                    preparedStatement.setString(3, role);
                    preparedStatement.setString(4, APIConstants.DEFAULT_BINDING_TYPE);
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        }
    }

    private List<String> getScopeBindings(Connection connection, String scopeKey, int tenantId)
            throws SQLException {

        List<String> bindingList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.RETRIEVE_SCOPE_MAPPING)) {
            preparedStatement.setString(1, scopeKey);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setString(3, APIConstants.DEFAULT_BINDING_TYPE);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String binding = resultSet.getString("SCOPE_BINDING");
                    if (StringUtils.isNotEmpty(binding)) {
                        bindingList.add(binding);
                    }
                }
            }
        }
        return bindingList;
    }

    private void deleteScopeBindings(Connection connection, String scopeKey, int tenantId) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.DELETE_SCOPE_MAPPING)) {
            preparedStatement.setString(1, scopeKey);
            preparedStatement.setInt(2, tenantId);
            preparedStatement.executeUpdate();
        }
    }
}
