/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represent the ChoreoApiMgtDAO.
 */
public class ChoreoApiMgtDAO {

    private static final Log log = LogFactory.getLog(ChoreoApiMgtDAO.class);
    private static ChoreoApiMgtDAO INSTANCE = null;

    /**
     * Method to get the instance of the ChoreoApiMgtDAO.
     *
     * @return {@link ChoreoApiMgtDAO} instance
     */
    public static ChoreoApiMgtDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChoreoApiMgtDAO();
        }
        return INSTANCE;
    }

    /**
     * Add Environment to Data-Plane mapping.
     *
     * @param connection     connection
     * @param envUUID        UUID of the Environment
     * @param dataplaneId    ID of the Dataplane
     * @throws APIManagementException if failed to add mapping
     */
    public void addEnvToDataPlaneMapping(Connection connection, String envUUID, String dataplaneId)
            throws APIManagementException {

        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.INSERT_ENV_TO_DATA_PLANE_MAPPING_SQL)) {
            prepStmt.setString(1, envUUID);
            prepStmt.setString(2, dataplaneId);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Failed to add Environment to Data-Plane mapping for environment UUID: " + envUUID, e);
        }
    }

    /**
     * Update Environment to Data-Plane mapping.
     *
     * @param connection     connection
     * @param envUUID        UUID of the Environment
     * @throws APIManagementException if failed to update mapping
     */
    public void updateEnvToDataPlaneMapping(Connection connection, String envUUID)
            throws APIManagementException {

        String dataplaneId = getDataPlaneIdForEnvironment(connection, envUUID);
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.UPDATE_ENV_TO_DATA_PLANE_MAPPING_SQL)) {
            prepStmt.setString(1, dataplaneId);
            prepStmt.setString(2, envUUID);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Failed to update Environment to Data-Plane mapping for environment UUID: " + envUUID, e);
        }
    }

    /**
     * Delete Environment to Data-Plane mapping.
     *
     * @param connection     connection
     * @param envUUID        UUID of the Environment
     * @throws APIManagementException if failed to delete mapping
     */
    public void deleteEnvToDataPlaneMapping(Connection connection, String envUUID)
            throws APIManagementException {

        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.DELETE_ENV_TO_DATA_PLANE_MAPPING_SQL)) {
            prepStmt.setString(1, envUUID);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            throw new APIManagementException(
                    "Failed to delete Environment to Data-Plane mapping for environment UUID: " + envUUID, e);
        }
    }

    /**
     * get Data-Plane ID for the given Environment UUID.
     *
     * @param connection     connection
     * @param envUUID        UUID of the Environment
     * @throws APIManagementException if failed to get the dataPlaneId
     */
    public String getDataPlaneIdForEnvironment(Connection connection, String envUUID) throws APIManagementException {
        String dataPlaneId = null;
        try (PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_DATA_PLANE_ID_FOR_ENV_SQL)) {
            prepStmt.setString(1, envUUID);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    dataPlaneId = rs.getString("DATA_PLANE_ID");
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to get the Data-Plane ID for environment UUID: " + envUUID, e);
        }
        return dataPlaneId;
    }

    /**
     * Returns the Environments List for the Organization.
     *
     * @param organization The organization.
     * @return List of Environments.
     */
    public List<Environment> getAllEnvironments(String organization) throws APIManagementException {

        List<Environment> envList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_ENVIRONMENTS_BY_ORGANIZATION_WITH_DATA_PLANE_ID_SQL))
        {
            prepStmt.setString(1, organization);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    Integer id = rs.getInt("ID");
                    String uuid = rs.getString("UUID");
                    String name = rs.getString("NAME");
                    String displayName = rs.getString("DISPLAY_NAME");
                    String description = rs.getString("DESCRIPTION");
                    String provider = rs.getString("PROVIDER");
                    String dataPlaneId = rs.getString("DATA_PLANE_ID");
                    String gwAccessibilityType = rs.getString("ACCESSIBILITY_TYPE");

                    Environment env = new Environment();
                    env.setId(id);
                    env.setUuid(uuid);
                    env.setName(name);
                    env.setDisplayName(displayName);
                    env.setDescription(description);
                    env.setProvider(provider);
                    env.setDataPlaneId(dataPlaneId);
                    env.setAccessibilityType(gwAccessibilityType);
                    env.setVhosts(ApiMgtDAO.getInstance().getVhostGatewayEnvironments(connection, id));
                    envList.add(env);
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to get Environments of Organization: " + organization, e);
        }
        return envList;
    }

    /**
     * Retrieve dataPlaneId to Environment mapping.
     *
     * @param environmentsSet   Environment name set
     * @throws APIManagementException if failed to retrieve mapping
     */
    public Map<String, String> getEnvironmentToDataPlaneMapping(Set<String> environmentsSet, String organization)
            throws APIManagementException {
        Map<String, String> envToDataPlaneIdMap = new HashMap<>();
        if (environmentsSet == null || environmentsSet.size() == 0) {
            return envToDataPlaneIdMap;
        }
        Map<String, Environment> readOnlyEnvironments = APIUtil.getReadOnlyEnvironments();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            for (String envName: environmentsSet) {
                try (PreparedStatement prepStmt =
                             connection.prepareStatement(SQLConstants.GET_ENVIRONMENTS_TO_DATA_PLANE_ID_MAPPING_SQL)) {
                    prepStmt.setString(1, envName);
                    prepStmt.setString(2, organization);
                    try (ResultSet rs = prepStmt.executeQuery()) {
                        if (rs.next()) {
                            envToDataPlaneIdMap.put(envName, rs.getString("DATA_PLANE_ID"));
                        }
                    }
                }
                // check if the environment is in the read-only environments list
                if (readOnlyEnvironments.containsKey(envName)) {
                    envToDataPlaneIdMap.put(envName,
                            readOnlyEnvironments.get(envName).getDataPlaneId());
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to get Environment to DataPlaneId Mapping for EnvironmentsSet: "
                    + environmentsSet + " of Organization: " + organization, e);
        }
        return envToDataPlaneIdMap;
    }

    /**
     *
     * @param environmentsSet
     * @param organization
     * @return
     * @throws APIManagementException
     */
    public Map<String, String> getEnvironmentToGatewayAccessibilityTypeMapping(Set<String> environmentsSet,
                                                                               String organization)
            throws APIManagementException {
        Map<String, String> envToGatewayAccessibilityTypeMap = new HashMap<>();
        if (environmentsSet == null || environmentsSet.size() == 0) {
            return envToGatewayAccessibilityTypeMap;
        }
        Map<String, Environment> readOnlyEnvironments = APIUtil.getReadOnlyEnvironments();
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            for (String envName: environmentsSet) {
                try (PreparedStatement prepStmt =
                             connection.prepareStatement(
                                     SQLConstants.GET_ENVIRONMENT_TO_GATEWAY_ACCESSIBILITY_TYPE_MAPPING_SQL)) {
                    prepStmt.setString(1, envName);
                    prepStmt.setString(2, organization);
                    try (ResultSet rs = prepStmt.executeQuery()) {
                        if (rs.next()) {
                            envToGatewayAccessibilityTypeMap.put(envName, rs.getString("ACCESSIBILITY_TYPE"));
                        }
                    }
                }
                // check if the environment is in the read-only environments list
                if (readOnlyEnvironments.containsKey(envName)) {
                    envToGatewayAccessibilityTypeMap.put(envName,
                            readOnlyEnvironments.get(envName).getAccessibilityType());
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException(String.format("Failed to get Environment to gateway accessibility mapping " +
                    "for EnvironmentsSet: %s of Organization: %s", environmentsSet, organization), e);
        }
        return envToGatewayAccessibilityTypeMap;
    }

    /**
     * Retrieves dataPlaneID & GatewayAccessibilityType for a given environment(name)
     *
     * @param environment  name of the environment
     * @param organization organization ID of the environment
     * @return a map containing dataPlaneID & GatewayAccessibilityType for a given environment
     * @throws APIManagementException
     */
    public Map<String, String> getEnvDetailsForDeployEvent(String environment, String organization)
        throws APIManagementException {
        Map<String, String> envDetailsMap = new HashMap<>();
        if (environment == null || environment.equals("")) {
            return envDetailsMap;
        }
        Map<String, Environment> readOnlyEnvironments = APIUtil.getReadOnlyEnvironments();

        // check if the environment is in the read-only environments list
        if (readOnlyEnvironments.containsKey(environment)) {
            Environment readOnlyEnv = readOnlyEnvironments.get(environment);
            envDetailsMap.put("dataPlaneID", readOnlyEnv.getDataPlaneId());
            envDetailsMap.put("gatewayAccessibilityType", readOnlyEnv.getAccessibilityType());
            return envDetailsMap;
        }

        // check if the environment is in the DB
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement prepStmt =
                         connection.prepareStatement(SQLConstants.GET_ENVIRONMENT_DETAILS_MAPPING_SQL)) {
                prepStmt.setString(1, environment);
                prepStmt.setString(2, organization);
                try (ResultSet rs = prepStmt.executeQuery()) {
                    if (rs.next()) {
                        envDetailsMap.put("dataPlaneID", rs.getString("DATA_PLANE_ID"));
                        envDetailsMap.put("gatewayAccessibilityType", rs.getString("ACCESSIBILITY_TYPE"));
                        return envDetailsMap;
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to get Environment details for Environment: "
                    + environment + " of Organization: " + organization, e);
        }
        return envDetailsMap;
    }

    /**
     * Checks whether a given environment name in given organization is already exists in the DB
     *
     * @param name         String represents the name of the environment
     * @param organization string represents organizationId
     * @return true if the environment is exists and false otherwise
     * @throws APIManagementException if failed to check the environment name
     */
    public boolean isEnvNameExists(String name, String organization) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_ENVIRONMENT_BY_NAME_SQL)) {
            prepStmt.setString(1, organization);
            prepStmt.setString(2, name);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to check the availability of the provided environment name " +
                    name + " of Organization: " + organization, e);
        }
        return false;
    }

    /**
     * Checks whether the given vHost exists.
     *
     * @param vHost        vHost name
     * @throws APIManagementException if failed to check the vHost name
     */
    public boolean isVHostExists(String vHost) throws APIManagementException {
        boolean isValidVHost = false;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_ENVIRONMENT_VHOSTS_BY_NAME_SQL))
        {
            prepStmt.setString(1, vHost);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    isValidVHost = true;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Failed to check the availability of the provided vHost" + vHost, e);
        }
        return isValidVHost;
    }
}
