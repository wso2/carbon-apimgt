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

import org.wso2.carbon.apimgt.core.dao.LambdaFunctionDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.LambdaFunction;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * DAO implementation for Lambda functions
 */
public class LambdaFunctionDAOImpl implements LambdaFunctionDAO {

    @Override
    public List<LambdaFunction> getUserDeployedFunctions(String userName) throws APIMgtDAOException {
        if (userName == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        List<LambdaFunction> functions = new ArrayList<>();
        final String sqlQuery = "SELECT FUNCTION_NAME, FUNCTION_URI " +
                "FROM AM_LAMBDA_FUNCTION " +
                "WHERE USER_NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, userName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String functionName = resultSet.getString("FUNCTION_NAME");
                    URI endpointURI = new URI(resultSet.getString("FUNCTION_URI"));
                    functions.add(new LambdaFunction(functionName, endpointURI));
                }
            } catch (URISyntaxException e) {
                throw new APIMgtDAOException("Not a URI", e);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Error retrieving functions of user: " + userName, e);
        }
        return functions;
    }

    @Override
    public List<LambdaFunction> getUserFunctionsForEvent(String userName, Event event) throws APIMgtDAOException {
        if (userName == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        List<LambdaFunction> functions = new ArrayList<>();
        final String sqlQuery = "SELECT FUNCTION_NAME, FUNCTION_URI " +
                "FROM AM_LAMBDA_FUNCTION JOIN AM_EVENT_FUNCTION_MAPPING " +
                "ON AM_LAMBDA_FUNCTION.FUNCTION_ID = AM_EVENT_FUNCTION_MAPPING.FUNCTION_ID " +
                "WHERE USER_NAME = ? AND EVENT = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, event.getEventAsString());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String functionName = resultSet.getString("FUNCTION_NAME");
                    URI endpointURI = new URI(resultSet.getString("FUNCTION_URI"));
                    functions.add(new LambdaFunction(functionName, endpointURI));
                }
            } catch (URISyntaxException e) {
                throw new APIMgtDAOException("Not a URI", e);
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Error retrieving functions for event: " + event + " of user: " + userName, e);
        }
        return functions;
    }

    @Override
    public List<Event> getTriggersForUserFunction(String userName, String functionName) throws APIMgtDAOException {
        if (userName == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        if (functionName == null) {
            throw new IllegalArgumentException("Function name must not be null");
        }
        List<Event> events = new ArrayList<>();
        final String sqlQuery = "SELECT EVENT " +
                "FROM AM_LAMBDA_FUNCTION JOIN AM_EVENT_FUNCTION_MAPPING " +
                "ON AM_LAMBDA_FUNCTION.FUNCTION_ID = AM_EVENT_FUNCTION_MAPPING.FUNCTION_ID " +
                "WHERE USER_NAME = ? AND FUNCTION_NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, functionName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    events.add(Event.valueOf(resultSet.getString("EVENT")));
                }
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Error retrieving triggers for function: " + functionName +
                    " of user: " + userName, e);
        }
        return events;
    }

    @Override
    public void addEventFunctionMapping(String userName, Event event, String functionName) throws APIMgtDAOException {
        if (userName == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (functionName == null) {
            throw new IllegalArgumentException("Function name must not be null");
        }
        final String sqlQuery = "INSERT INTO AM_EVENT_FUNCTION_MAPPING(EVENT, FUNCTION_ID) " +
                "SELECT ?, FUNCTION_ID " +
                "FROM AM_LAMBDA_FUNCTION " +
                "WHERE USER_NAME = ? AND FUNCTION_NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, event.getEventAsString());
            preparedStatement.setString(2, userName);
            preparedStatement.setString(3, functionName);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new APIMgtDAOException("Error adding new event function mapping for user: " + userName, e);
        }
    }

    @Override
    public void deleteEventFunctionMapping(String userName, Event event, String functionName) throws
            APIMgtDAOException {
        if (userName == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (functionName == null) {
            throw new IllegalArgumentException("Function name must not be null");
        }
        final String sqlQuery = "DELETE FROM AM_EVENT_FUNCTION_MAPPING " +
                "WHERE EVENT = ? AND FUNCTION_ID = " +
                "(SELECT FUNCTION_ID " +
                "FROM AM_LAMBDA_FUNCTION " +
                "WHERE USER_NAME = ? AND FUNCTION_NAME = ?)";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, event.getEventAsString());
            preparedStatement.setString(2, userName);
            preparedStatement.setString(3, functionName);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new APIMgtDAOException("Error deleting event function mapping -event: " + event +
                    " -function: " + functionName + " -user: " + userName, e);
        }
    }

    @Override
    public void updateUserDeployedFunctions(String userName, List<LambdaFunction> functions) throws
            APIMgtDAOException {
        if (userName == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        if (functions == null) {
            throw new IllegalArgumentException("Functions(List) must not be null");
        }
        try {
            List<LambdaFunction> userFunctionsInDB = getUserDeployedFunctions(userName);
            for (LambdaFunction function : functions) {
                boolean isFunctionExist = false;
                for (Iterator<LambdaFunction> iterator = userFunctionsInDB.iterator(); iterator.hasNext(); ) {
                    LambdaFunction localStoredFunction = iterator.next();
                    if (function.equals(localStoredFunction)) {
                        isFunctionExist = true;
                        iterator.remove();
                        break;
                    }
                }
                if (!isFunctionExist) {
                    addNewFunction(userName, function);
                }
            }
            for (LambdaFunction missingFunction : userFunctionsInDB) {
                deleteFunction(userName, missingFunction.getName());
            }
        } catch (SQLException e) {
            throw new APIMgtDAOException("Error updating user deployed functions of user: " + userName, e);
        }
    }

    private void addNewFunction(String userName, LambdaFunction function) throws SQLException {
        final String sqlQuery = "INSERT INTO AM_LAMBDA_FUNCTION (FUNCTION_NAME, FUNCTION_URI, USER_NAME) " +
                "VALUES(?, ?, ?)";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, function.getName());
            preparedStatement.setString(2, function.getEndpointURI().toString());
            preparedStatement.setString(3, userName);
            preparedStatement.executeUpdate();
        }
    }

    private void deleteFunction(String userName, String functionName) throws SQLException {
        final String sqlQuery = "DELETE FROM AM_LAMBDA_FUNCTION " +
                "WHERE USER_NAME = ? AND FUNCTION_NAME = ?";

        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, functionName);
            preparedStatement.executeUpdate();
        }
    }

}
