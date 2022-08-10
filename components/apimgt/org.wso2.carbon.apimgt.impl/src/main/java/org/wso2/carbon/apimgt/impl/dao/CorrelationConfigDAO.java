/*
 *
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  n compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigPropertyDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Database Access Library for the Correlation Configs Feature.
 */
public class CorrelationConfigDAO {
    private static final Log log = LogFactory.getLog(CorrelationConfigDAO.class);
    private static final CorrelationConfigDAO correlationConfigDAO = new CorrelationConfigDAO();

    private static final String COMPONENT_NAME = "COMPONENT_NAME";
    private static final String ENABLED = "ENABLED";
    private static final String PROPERTY_NAME = "PROPERTY_NAME";
    private static final String PROPERTY_VALUE = "PROPERTY_VALUE";
    private static final String DENIED_THREADS = "deniedThreads";
    private List<String> correlationComponents;
    private static final String DEFAULT_DENIED_THREADS = "MessageDeliveryTaskThreadPool,HumanTaskServer," +
            "BPELServer,CarbonDeploymentSchedulerThread";

    private CorrelationConfigDAO() {
        correlationComponents = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration().getProperty(APIConstants.CORRELATION_LOG_COMPONENTS);
        correlationComponents.replaceAll(String::trim);
        log.info("Correlation Components " + correlationComponents.toString());
    }

    public static CorrelationConfigDAO getInstance() {
        return correlationConfigDAO;
    }

    public boolean updateCorrelationConfigs(List<CorrelationConfigDTO> correlationConfigDTOList)
            throws APIManagementException {

        for (CorrelationConfigDTO correlationConfigDTO : correlationConfigDTOList) {
            String componentName = correlationConfigDTO.getName().trim();
            if (!correlationComponents.contains(componentName)) {
                throw new APIManagementException("Invalid Component Name : " + componentName,
                        ExceptionCodes.from(ExceptionCodes.CORRELATION_CONFIG_BAD_REQUEST_INVALID_NAME));
            }
        }
        String queryConfigs = SQLConstants.UPDATE_CORRELATION_CONFIGS;
        String queryProps = SQLConstants.UPDATE_CORRELATION_CONFIG_PROPERTIES;
        try (Connection connection = APIMgtDBUtil.getConnection()) {

            connection.setAutoCommit(false);
            log.debug("Updating Correlation Configs");
            try (PreparedStatement preparedStatementConfigs = connection.prepareStatement(queryConfigs)) {
                for (CorrelationConfigDTO correlationConfigDTO : correlationConfigDTOList) {
                    String componentName = correlationConfigDTO.getName().trim();
                    String enabled = correlationConfigDTO.getEnabled();

                    preparedStatementConfigs.setString(1, enabled);
                    preparedStatementConfigs.setString(2, componentName);
                    preparedStatementConfigs.addBatch();
                }
                preparedStatementConfigs.executeBatch();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Failed to update correlation configs");
                throw new APIManagementException("Failed to update correlation configs", e);
            }

            try (PreparedStatement preparedStatementProps = connection.prepareStatement(queryProps)) {
                for (CorrelationConfigDTO correlationConfigDTO : correlationConfigDTOList) {
                    String componentName = correlationConfigDTO.getName();
                    List<CorrelationConfigPropertyDTO> correlationConfigPropertyDTOList =
                            correlationConfigDTO.getProperties();
                    if (correlationConfigPropertyDTOList == null) {
                        continue;
                    }

                    for (CorrelationConfigPropertyDTO correlationConfigPropertyDTO : correlationConfigPropertyDTOList) {
                        String propertyName = correlationConfigPropertyDTO.getName();
                        String propertyValue = String.join(",", correlationConfigPropertyDTO.getValue());

                        preparedStatementProps.setString(1, propertyValue);
                        preparedStatementProps.setString(2, componentName);
                        preparedStatementProps.setString(3, propertyName);
                        preparedStatementProps.addBatch();
                    }
                    preparedStatementProps.executeBatch();
                }
            } catch (SQLException e) {
                connection.rollback();
                log.error("Failed to update correlation configs");
                throw new APIManagementException("Failed to update correlation configs", e);
            }
            connection.commit();
            return true;

        } catch (SQLException e) {

            log.error("Failed to update correlation configs");
            throw new APIManagementException("Failed to update correlation configs", e);

        }
    }

    public List<CorrelationConfigDTO> getCorrelationConfigsList() throws APIManagementException {
        List<CorrelationConfigDTO> correlationConfigDTOList = new ArrayList<>();
        String queryConfigs = SQLConstants.RETRIEVE_CORRELATION_CONFIGS;
        String queryProps = SQLConstants.RETRIEVE_CORRELATION_CONFIG_PROPERTIES;

        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement preparedStatementConfigs = connection.prepareStatement(queryConfigs);
                PreparedStatement preparedStatementProps = connection.prepareStatement(queryProps)) {
            try (ResultSet resultSetConfigs = preparedStatementConfigs.executeQuery()) {
                while (resultSetConfigs.next()) {
                    String componentName = resultSetConfigs.getString(COMPONENT_NAME);
                    String enabled = resultSetConfigs.getString(ENABLED);

                    preparedStatementProps.setString(1, componentName);
                    try (ResultSet resultSetProps = preparedStatementProps.executeQuery()) {

                        List<CorrelationConfigPropertyDTO> correlationConfigPropertyDTOList = new ArrayList<>();
                        while (resultSetProps.next()) {
                            String propertyName = resultSetProps.getString(PROPERTY_NAME);
                            String propertyValue = resultSetProps.getString(PROPERTY_VALUE);
                            CorrelationConfigPropertyDTO correlationConfigPropertyDTO =
                                    new CorrelationConfigPropertyDTO(propertyName, propertyValue.split(","));
                            correlationConfigPropertyDTOList.add(correlationConfigPropertyDTO);
                        }

                        CorrelationConfigDTO correlationConfigDTO = new CorrelationConfigDTO(componentName, enabled,
                                correlationConfigPropertyDTOList);
                        correlationConfigDTOList.add(correlationConfigDTO);
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to retrieve correlation component configs", e);
        }
        return correlationConfigDTOList;
    }

    public boolean isConfigExist() throws APIManagementException {
        String queryComponentNames = SQLConstants.RETRIEVE_CORRELATION_COMPONENT_NAMES;
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement preparedStatementComponentNames = connection.prepareStatement(queryComponentNames);
                ResultSet resultSetComponentNames = preparedStatementComponentNames.executeQuery()) {
            List<String> persistedCorrelationComponents = new ArrayList<>();
            if (resultSetComponentNames != null) {
                while (resultSetComponentNames.next()) {
                    persistedCorrelationComponents.add(resultSetComponentNames.getString(COMPONENT_NAME));
                }
            }
            // Checking whether any changes needed to the persisted component list
            if (correlationComponents.size() != persistedCorrelationComponents.size()) {
                return false;
            }
            for (String component: correlationComponents) {
                if (!persistedCorrelationComponents.contains(component)) {
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            throw new APIManagementException("Error while retrieving correlation configs" , e);
        }
    }

    public void addDefaultCorrelationConfigs() throws APIManagementException {
        String queryConfigs = SQLConstants.INSERT_CORRELATION_CONFIGS;
        String queryProps = SQLConstants.INSERT_CORRELATION_CONFIG_PROPERTIES;
        String queryComponentNames = SQLConstants.RETRIEVE_CORRELATION_COMPONENT_NAMES;
        String queryDelete = SQLConstants.DELETE_CORRELATION_CONFIGS;

        List<String> correlationComponentsInsertList = new ArrayList<>();
        List<String> correlationComponentsDeleteList = new ArrayList<>();

        try (Connection connection = APIMgtDBUtil.getConnection()) {

            try (PreparedStatement preparedStatementComponentNames = connection.prepareStatement(queryComponentNames);
                    ResultSet resultSetComponentNames = preparedStatementComponentNames.executeQuery()) {
                List<String> persistedCorrelationComponents = new ArrayList<>();
                if (resultSetComponentNames != null) {
                    while (resultSetComponentNames.next()) {
                        persistedCorrelationComponents.add(resultSetComponentNames.getString(COMPONENT_NAME));
                    }
                }
                //Checking for the components to be deleted from the DB
                for (String persistedComponent: persistedCorrelationComponents) {
                    if (!correlationComponents.contains(persistedComponent)) {
                        correlationComponentsDeleteList.add(persistedComponent);
                    }
                }
                //Checking for the components to be added to the DB
                for (String component: correlationComponents) {
                    if (!persistedCorrelationComponents.contains(component)) {
                        correlationComponentsInsertList.add(component);
                    }
                }
            }

            try (PreparedStatement preparedStatementConfigs = connection.prepareStatement(queryConfigs);
                    PreparedStatement preparedStatementProps = connection.prepareStatement(queryProps)) {

                connection.setAutoCommit(false);
                log.debug("Inserting into Correlation Configs");
                for (String componentName : correlationComponentsInsertList) {
                    String enabled = "false";

                    preparedStatementConfigs.setString(1, componentName);
                    preparedStatementConfigs.setString(2, enabled);
                    preparedStatementConfigs.addBatch();
                }
                preparedStatementConfigs.executeBatch();

                if (correlationComponentsInsertList.contains("jdbc")) {
                    preparedStatementProps.setString(1, DENIED_THREADS);
                    preparedStatementProps.setString(2, "jdbc");
                    preparedStatementProps.setString(3, DEFAULT_DENIED_THREADS);
                    preparedStatementProps.executeUpdate();
                }
            } catch (SQLException e) {
                connection.rollback();
                boolean isExist = false;
                log.debug("Caught SQLException : " + e);
                // If two concurrent calls to same method have been called, need not throw an error.
                if (e.getMessage().contains("Unique index or primary key violation")) {
                    isExist = isConfigExist();
                }
                if (isExist) {
                    log.warn("Correlation configs are already persisted");
                } else {
                    throw new APIManagementException("Error while updating the correlation configs", e);
                }
            }


            try (PreparedStatement preparedStatementDelete = connection.prepareStatement(queryDelete)) {
                for (String componentName : correlationComponentsDeleteList) {
                    preparedStatementDelete.setString(1, componentName);
                    preparedStatementDelete.addBatch();
                }
                preparedStatementDelete.executeBatch();
            }

            connection.commit();
        } catch (SQLException e) {
            throw new APIManagementException("Error while updating the correlation configs", e);
        }
    }
}
