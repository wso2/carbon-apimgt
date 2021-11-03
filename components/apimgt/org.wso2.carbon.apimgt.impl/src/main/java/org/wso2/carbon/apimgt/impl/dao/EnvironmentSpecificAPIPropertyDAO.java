/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.environmentspecificproperty.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class implements the DAO operations related to environment specific api properties.
 */
public class EnvironmentSpecificAPIPropertyDAO {

    public static final String ADD_ENVIRONMENT_SPECIFIC_API_PROPERTIES =
            "INSERT INTO AM_API_ENVIRONMENT_KEYS(UUID, API_ID, ENVIRONMENT_ID, PROPERTY_CONFIG) VALUES(?,?,?,?)";

    public static final String UPDATE_ENVIRONMENT_SPECIFIC_API_PROPERTIES =
            "UPDATE AM_API_ENVIRONMENT_KEYS SET PROPERTY_CONFIG = ? WHERE API_ID=? AND ENVIRONMENT_ID=?";

    public static final String GET_ENVIRONMENT_SPECIFIC_API_PROPERTIES =
            "SELECT PROPERTY_CONFIG FROM AM_API_ENVIRONMENT_KEYS WHERE API_ID=? AND ENVIRONMENT_ID=?";

    public static final String IS_ENVIRONMENT_SPECIFIC_API_PROPERTIES_EXIST =
            "SELECT 1 FROM AM_API_ENVIRONMENT_KEYS WHERE API_ID=? AND ENVIRONMENT_ID=?";

    private static EnvironmentSpecificAPIPropertyDAO INSTANCE = null;

    private EnvironmentSpecificAPIPropertyDAO() {

    }

    public static EnvironmentSpecificAPIPropertyDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EnvironmentSpecificAPIPropertyDAO();
        }
        return INSTANCE;
    }

    public void addOrUpdateEnvironmentSpecificAPIProperties(int apiId, String envUuid, String content)
            throws APIManagementException {
        boolean isConfigExist = isEnvironmentSpecificAPIPropertiesExist(apiId, envUuid);
        if (isConfigExist) {
            updateEnvironmentSpecificAPIProperties(apiId, envUuid, content);
        } else {
            addEnvironmentSpecificAPIProperties(apiId, envUuid, content);
        }
    }

    private void addEnvironmentSpecificAPIProperties(int apiId, String envUuid, String content)
            throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            try (PreparedStatement preparedStatement = conn.prepareStatement(ADD_ENVIRONMENT_SPECIFIC_API_PROPERTIES)) {
                preparedStatement.setString(1, UUID.randomUUID().toString());
                preparedStatement.setInt(2, apiId);
                preparedStatement.setString(3, envUuid);
                preparedStatement.setBlob(4, new ByteArrayInputStream(content.getBytes()));
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error occurred when adding environment specific api properties", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }

    private void updateEnvironmentSpecificAPIProperties(int apiId, String envUuid, String content)
            throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement(UPDATE_ENVIRONMENT_SPECIFIC_API_PROPERTIES)) {
                preparedStatement.setBlob(1, new ByteArrayInputStream(content.getBytes()));
                preparedStatement.setInt(2, apiId);
                preparedStatement.setString(3, envUuid);
                preparedStatement.execute();
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error occurred when updating environment specific api properties", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }

    public String getEnvironmentSpecificAPIProperties(int apiId, String envUuid) throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            try (PreparedStatement preparedStatement = conn.prepareStatement(GET_ENVIRONMENT_SPECIFIC_API_PROPERTIES)) {
                preparedStatement.setInt(1, apiId);
                preparedStatement.setString(2, envUuid);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    String propertyConfig = null;
                    if (resultSet.next()) {
                        InputStream propertyConfigBlob = resultSet.getBinaryStream(1);
                        if (propertyConfigBlob != null) {
                            propertyConfig = APIMgtDBUtil.getStringFromInputStream(propertyConfigBlob);
                        }
                    }
                    return propertyConfig;
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error occurred when getting environment specific api properties", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
    }

    private boolean isEnvironmentSpecificAPIPropertiesExist(int apiId, String envUuid) throws APIManagementException {
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            try (PreparedStatement preparedStatement = conn
                    .prepareStatement(IS_ENVIRONMENT_SPECIFIC_API_PROPERTIES_EXIST)) {
                preparedStatement.setInt(1, apiId);
                preparedStatement.setString(2, envUuid);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error occurred when checking environment specific api properties", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
        return false;
    }

    public Map<String, Environment> getEnvironmentSpecificAPIPropertiesOfAPIs(List<String> apiUuidS)
            throws APIManagementException {
        String idsAsList = apiUuidS.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(","));
        Map<String, Environment> mgEnvs = getMGEnvironmentSpecificAPIPropertiesOfAPIs(idsAsList);
        Map<String, Environment> defaultEnvs = getDefaultEnvironmentSpecificAPIPropertiesOfAPIs(idsAsList);
        mgEnvs.putAll(defaultEnvs);
        return mgEnvs;
    }

    private Map<String, Environment> getMGEnvironmentSpecificAPIPropertiesOfAPIs(String apiUuidS) throws APIManagementException {
        final String query = getMgEnvironmentSpecificAPIPropertiesOfAPIsQuery(apiUuidS);
        Map<String, Environment> environmentListMap= new HashMap<>();
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            try (PreparedStatement preparedStatement = conn.prepareStatement(query);
                    ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String envId = resultSet.getString(1);
                    String envName = resultSet.getString(2);
                    String apiId = resultSet.getString(3);
                    JsonObject jsonConfig = null;
                    InputStream propertyConfigBlob = resultSet.getBinaryStream(4);
                    if (propertyConfigBlob != null) {
                        String apiJsonConfig = APIMgtDBUtil.getStringFromInputStream(propertyConfigBlob);
                        jsonConfig = new Gson().fromJson(apiJsonConfig, JsonObject.class);
                    }
                    Environment environment;
                    if (environmentListMap.containsKey(envName)) {
                        environment = environmentListMap.get(envName);
                    } else {
                        environment = new Environment();
                        environment.setEnvId(envId);
                        environment.setEnvName(envName);
                        environment.setConfigs(new HashMap<>());
                        environmentListMap.put(envName, environment);
                    }
                    Map<String, JsonObject> apiConfigs = environment.getConfigs();
                    apiConfigs.put(apiId, jsonConfig);
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error occurred when getting MG environment specific api properties", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
        return environmentListMap;
    }

    private Map<String, Environment> getDefaultEnvironmentSpecificAPIPropertiesOfAPIs(String apiUuidS)
            throws APIManagementException {
        Map<String, org.wso2.carbon.apimgt.api.model.Environment> defaultEnvs = APIUtil.getReadOnlyEnvironments();
        String envsAsList = defaultEnvs.values().stream()
                .map(env -> "'" + env.getUuid() + "'")
                .collect(Collectors.joining(","));
        final String query = getDefaultEnvironmentSpecificAPIPropertiesOfAPIsQuery(apiUuidS, envsAsList);
        Map<String, Environment> environmentListMap = new HashMap<>();
        Connection conn = null;
        try {
            conn = APIMgtDBUtil.getConnection();
            try (PreparedStatement preparedStatement = conn.prepareStatement(query);
                    ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String envId = resultSet.getString(1);
                    String apiId = resultSet.getString(2);
                    JsonObject jsonConfig = null;
                    InputStream propertyConfigBlob = resultSet.getBinaryStream(3);
                    if (propertyConfigBlob != null) {
                        String apiJsonConfig = APIMgtDBUtil.getStringFromInputStream(propertyConfigBlob);
                        jsonConfig = new Gson().fromJson(apiJsonConfig, JsonObject.class);
                    }
                    Environment environment;
                    if (environmentListMap.containsKey(envId)) {
                        environment = environmentListMap.get(envId);
                    } else {
                        environment = new Environment();
                        environment.setEnvId(envId);
                        environment.setEnvName(envId); // If env uuid is empty, it take uuid as env name
                        environment.setConfigs(new HashMap<>());
                        environmentListMap.put(envId, environment);
                    }
                    Map<String, JsonObject> apiConfigs = environment.getConfigs();
                    apiConfigs.put(apiId, jsonConfig);
                }
            }
        } catch (SQLException e) {
            throw new APIManagementException("Error occurred when getting default environment specific api properties", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(null, conn, null);
        }
        return environmentListMap;
    }

    private String getMgEnvironmentSpecificAPIPropertiesOfAPIsQuery(String apiIds) {
        return "SELECT AM_GATEWAY_ENVIRONMENT.UUID ENV_ID,"
                        + "       AM_GATEWAY_ENVIRONMENT.NAME ENV_NAME,"
                        + "       AM_API.API_UUID API_ID,"
                        + "       AM_API_ENVIRONMENT_KEYS.PROPERTY_CONFIG CONFIG"
                        + " FROM AM_API_ENVIRONMENT_KEYS,AM_GATEWAY_ENVIRONMENT,AM_API"
                        + " WHERE AM_API_ENVIRONMENT_KEYS.ENVIRONMENT_ID = AM_GATEWAY_ENVIRONMENT.UUID AND"
                        + "        AM_API_ENVIRONMENT_KEYS.API_ID = AM_API.API_ID AND"
                        + "        AM_API.API_UUID IN (" + apiIds + ")"
                        + " ORDER BY ENV_ID, ENV_NAME, API_ID";
    }

    private String getDefaultEnvironmentSpecificAPIPropertiesOfAPIsQuery(String apiIds, String envIds) {
        return "SELECT AM_API_ENVIRONMENT_KEYS.ENVIRONMENT_ID ENV_ID,"
                + "       AM_API.API_UUID API_ID,"
                + "       AM_API_ENVIRONMENT_KEYS.PROPERTY_CONFIG CONFIG"
                + " FROM AM_API_ENVIRONMENT_KEYS,AM_API"
                + " WHERE AM_API_ENVIRONMENT_KEYS.ENVIRONMENT_ID IN (" + envIds + ") AND"
                + "        AM_API_ENVIRONMENT_KEYS.API_ID = AM_API.API_ID AND"
                + "        AM_API.API_UUID IN (" + apiIds + ")"
                + " ORDER BY ENV_ID, API_ID";
    }
}
