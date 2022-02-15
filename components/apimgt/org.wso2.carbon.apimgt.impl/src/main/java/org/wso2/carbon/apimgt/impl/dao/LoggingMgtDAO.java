/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.ApiLoggingMgtException;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.APILogInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access library for per API logging management feature.
 */
public class LoggingMgtDAO {
    private static final Log log = LogFactory.getLog(LoggingMgtDAO.class);
    private static final LoggingMgtDAO loggingMgtDAO = new LoggingMgtDAO();
    private static final String API_UUID = "API_UUID";
    private static final String CONTEXT = "CONTEXT";

    private LoggingMgtDAO() {

    }

    public static LoggingMgtDAO getInstance() {
        return loggingMgtDAO;
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new ApiLoggingMgtException(msg, t);
    }

    public void addAPILogger(String organization, String apiId, String logLevel) throws APIManagementException {
        try (Connection addLoggingCon = APIMgtDBUtil.getConnection()) {
            addLoggingCon.setAutoCommit(false);
            try (PreparedStatement preparedStatement = addLoggingCon.prepareStatement(
                    SQLConstants.ADD_PER_API_LOGGING_SQL)) {
                preparedStatement.setString(1, logLevel);
                preparedStatement.setString(2, apiId);
                preparedStatement.setString(3, organization);

                preparedStatement.executeUpdate();
                addLoggingCon.commit();
            } catch (SQLException e) {
                addLoggingCon.rollback();
                throw new ApiLoggingMgtException("Error while adding new per API logger", e);
            }
        } catch (SQLException e) {
            handleException("Failed to add API logging for " + apiId + " with the log level " + logLevel, e);
        }
    }

    public List<APILogInfoDTO> retrieveAPILoggerList(String organization, String logLevel) throws
            APIManagementException {
        List<APILogInfoDTO> apiLogInfoDTOList = new ArrayList<>();
        String query;
        if (logLevel == null) {
            query = SQLConstants.RETRIEVE_PER_API_LOGGING_ALL_SQL;
        } else {
            switch (logLevel.toUpperCase()) {
                case APIConstants.LOG_LEVEL_OFF:
                    query = SQLConstants.RETRIEVE_PER_API_LOGGING_OFF_SQL;
                    break;
                case APIConstants.LOG_LEVEL_BASIC:
                    query = SQLConstants.RETRIEVE_PER_API_LOGGING_BASIC_SQL;
                    break;
                case APIConstants.LOG_LEVEL_STANDARD:
                    query = SQLConstants.RETRIEVE_PER_API_LOGGING_STANDARD_SQL;
                    break;
                case APIConstants.LOG_LEVEL_FULL:
                    query = SQLConstants.RETRIEVE_PER_API_LOGGING_FULL_SQL;
                    break;
                default:
                    throw new APIManagementException("Invalid log level",
                            ExceptionCodes.from(ExceptionCodes.LOGGING_API_INCORRECT_LOG_LEVEL));
            }
        }
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, organization);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String retrievedLogLevel = APIConstants.LOG_LEVEL_OFF;
                    if (resultSet.getString(APIConstants.LOG_LEVEL) != null) {
                        retrievedLogLevel = resultSet.getString(APIConstants.LOG_LEVEL);
                    }
                    APILogInfoDTO apiLogInfoDTO = new APILogInfoDTO(resultSet.getString(API_UUID),
                            resultSet.getString(CONTEXT), retrievedLogLevel);
                    apiLogInfoDTOList.add(apiLogInfoDTO);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve API logging for organization" + organization, e);
        }
        return apiLogInfoDTOList;
    }

    public List<APILogInfoDTO> retrieveAllAPILoggerList() throws APIManagementException {
        List<APILogInfoDTO> apiLogInfoDTOList = new ArrayList<>();
        String query = SQLConstants.RETRIEVE_ALL_PER_API_LOGGING_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String logLevel = APIConstants.LOG_LEVEL_OFF;
                    if (resultSet.getString(APIConstants.LOG_LEVEL) != null) {
                        logLevel = resultSet.getString(APIConstants.LOG_LEVEL);
                    }
                    APILogInfoDTO apiLogInfoDTO = new APILogInfoDTO(resultSet.getString(API_UUID),
                            resultSet.getString(CONTEXT), logLevel);
                    apiLogInfoDTOList.add(apiLogInfoDTO);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve organization", e);
        }
        return apiLogInfoDTOList;
    }

    public List<APILogInfoDTO> retrieveAPILoggerByAPIID(String tenant, String apiId) throws APIManagementException {
        String query = SQLConstants.RETRIEVE_PER_API_LOGGING_BY_UUID_SQL;
        List<APILogInfoDTO> apiLogInfoDTOList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, apiId);
            preparedStatement.setString(2, tenant);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String logLevel = APIConstants.LOG_LEVEL_OFF;
                    if (resultSet.getString(APIConstants.LOG_LEVEL) != null) {
                        logLevel = resultSet.getString(APIConstants.LOG_LEVEL);
                    }
                    APILogInfoDTO apiLogInfoDTO = new APILogInfoDTO(resultSet.getString(API_UUID),
                            resultSet.getString(CONTEXT), logLevel);
                    apiLogInfoDTOList.add(apiLogInfoDTO);
                }
            }
        } catch (SQLException e) {
            handleException("Failed to retrieve organization", e);
        }
        return apiLogInfoDTOList;
    }
}
