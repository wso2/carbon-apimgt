/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dao;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyDTO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.List;
import java.util.TimeZone;

/**
 * This class represents the ApiKeyMgtDAO
 */
public class ApiKeyMgtDAO {

    private static final Log log = LogFactory.getLog(ApiKeyMgtDAO.class);
    private static ApiKeyMgtDAO instance = null;

    private ApiKeyMgtDAO() {}

    /**
     * Returns the single instance of ApiKeyMgtDAO, creating it if necessary.
     *
     * @return the singleton instance of ApiKeyMgtDAO
     */
    public static ApiKeyMgtDAO getInstance() {
        if (instance == null) {
            instance = new ApiKeyMgtDAO();
        }
        return instance;
    }

    /**
     * Add new api key against an API or Application
     *
     * @param apiKeyHash Generated api key value
     * @param keyInfoDTO APIKeyDTO object with required data
     * @throws APIManagementException
     */
    public void addAPIKey(String apiKeyHash, APIKeyDTO keyInfoDTO) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String addApiKeySql = SQLConstants.ADD_API_KEY_SQL;
            String addApiKeyToApiMappingSql = SQLConstants.ADD_API_KEY_TO_API_MAPPING_SQL;
            String addApiKeyToAppMappingSql = SQLConstants.ADD_API_KEY_TO_APP_MAPPING_SQL;
            ObjectMapper mapper = new ObjectMapper();
            try {
                byte[] properties = mapper.writeValueAsBytes(keyInfoDTO.getApiKeyProperties());

                try (PreparedStatement ps = conn.prepareStatement(addApiKeySql)) {
                    ps.setString(1, keyInfoDTO.getKeyId());
                    ps.setString(2, keyInfoDTO.getKeyName());
                    ps.setString(3, apiKeyHash);
                    ps.setString(4, keyInfoDTO.getKeyType());
                    ps.setBinaryStream(5, new ByteArrayInputStream(properties), properties.length);
                    ps.setString(6, keyInfoDTO.getAuthUser());
                    ps.setTimestamp(7, new Timestamp(keyInfoDTO.getCreatedTime()),
                            Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                    ps.setLong(8, keyInfoDTO.getValidityPeriod());
                    if (keyInfoDTO.getLastUsedTime() == null) {
                        ps.setNull(9, Types.TIMESTAMP);
                    } else {
                        ps.setTimestamp(9, new Timestamp(keyInfoDTO.getLastUsedTime()),
                                Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                    }
                    ps.setString(10, "ACTIVE");
                    ps.executeUpdate();
                    conn.commit();
                }
                if (keyInfoDTO.getApiId() != null) {
                    try (PreparedStatement ps = conn.prepareStatement(addApiKeyToApiMappingSql)) {
                        ps.setString(1, keyInfoDTO.getKeyId());
                        ps.setString(2, keyInfoDTO.getApiId());
                        ps.executeUpdate();
                        conn.commit();
                    }
                } if (keyInfoDTO.getApplicationId() != null) {
                    try (PreparedStatement ps = conn.prepareStatement(addApiKeyToAppMappingSql)) {
                        ps.setString(1, keyInfoDTO.getKeyId());
                        ps.setString(2, keyInfoDTO.getApplicationId());
                        ps.executeUpdate();
                        conn.commit();
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                handleException("Failed to add generated API key", e);
            } catch (IOException e) {
                handleException("Failed to add generated API key", e);
            }
        } catch (SQLException e) {
            handleException("Failed to add generated API key", e);
        }
    }

    /**
     * Returns a list of api keys against an Application
     *
     * @param applicationUUID Application UUID
     * @param keyType Key type of the api keys
     * @param tenantDomain Tenant domain
     * @param username Username
     * @return Returns a list of api keys
     * @throws APIManagementException
     */
    public List<APIKeyInfo> getAPIKeys(String applicationUUID, String keyType, String tenantDomain, String username) throws APIManagementException {

        List<APIKeyInfo> apiKeyInfoList = new ArrayList<APIKeyInfo>();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String getApiKeysSql = SQLConstants.GET_API_KEY_SQL;
            try (PreparedStatement ps = conn.prepareStatement(getApiKeysSql)) {
                ps.setString(1, applicationUUID);
                ps.setString(2, keyType);
                ps.setString(3, username);
                ps.setString(4, tenantDomain);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        APIKeyInfo keyInfo = new APIKeyInfo();
                        keyInfo.setKeyUUID(rs.getString("API_KEY_UUID"));
                        keyInfo.setKeyName(rs.getString("NAME"));
                        Timestamp createdTime = rs.getTimestamp("TIME_CREATED");
                        keyInfo.setCreatedTime(createdTime.getTime());
                        keyInfo.setValidityPeriod(rs.getLong("VALIDITY_PERIOD"));
                        Timestamp lastUsedTime = rs.getTimestamp("LAST_USED");
                        keyInfo.setLastUsedTime(lastUsedTime != null ? lastUsedTime.getTime() : null);
                        keyInfo.setApplicationId(applicationUUID);
                        keyInfo.setKeyType(keyType);
                        apiKeyInfoList.add(keyInfo);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get API keys", e);
        }
        return apiKeyInfoList;
    }

    /**
     * Returns a list of api key associations against an Application
     *
     * @param applicationUUID Application UUID
     * @param keyType Key type of the api keys
     * @param tenantDomain Tenant domain
     * @param username Username
     * @return Returns a list of api keys
     * @throws APIManagementException
     */
    public List<APIKeyInfo> getAPIKeyAssociations(String applicationUUID, String keyType, String tenantDomain, String username)
            throws APIManagementException {

        List<APIKeyInfo> apiKeyInfoList = new ArrayList<APIKeyInfo>();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String sqlQuery = SQLConstants.GET_API_KEY_ASSOCIATIONS_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, applicationUUID);
                ps.setString(2, keyType);
                ps.setString(3, username);
                ps.setString(4, tenantDomain);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        APIKeyInfo keyInfo = new APIKeyInfo();
                        keyInfo.setKeyUUID(rs.getString("API_KEY_UUID"));
                        keyInfo.setKeyName(rs.getString("NAME"));
                        keyInfo.setApiName(rs.getString("API_NAME"));
                        Timestamp createdTime = rs.getTimestamp("TIME_CREATED");
                        keyInfo.setCreatedTime(createdTime.getTime());
                        keyInfo.setValidityPeriod(rs.getLong("VALIDITY_PERIOD"));
                        Timestamp lastUsedTime = rs.getTimestamp("LAST_USED");
                        keyInfo.setLastUsedTime(lastUsedTime != null ? lastUsedTime.getTime() : null);
                        keyInfo.setApplicationId(applicationUUID);
                        keyInfo.setKeyType(keyType);
                        keyInfo.setApiUUId(rs.getString("API_UUID"));
                        keyInfo.setAuthUser(rs.getString("AUTHZ_USER"));
                        apiKeyInfoList.add(keyInfo);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get API key associations", e);
        }
        return apiKeyInfoList;
    }

    /**
     * Returns a list of api keys against an API
     *
     * @param apiUUID API UUID
     * @param username Username
     * @return Returns a list of api keys
     * @throws APIManagementException
     */
    public List<APIKeyInfo> getAPIKeys(String apiUUID, String username) throws APIManagementException {

        List<APIKeyInfo> apiKeyInfoList = new ArrayList<APIKeyInfo>();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String sqlQuery = SQLConstants.GET_API_API_KEY_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, apiUUID);
                ps.setString(2, username);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        APIKeyInfo keyInfo = new APIKeyInfo();
                        keyInfo.setKeyUUID(rs.getString("API_KEY_UUID"));
                        keyInfo.setKeyName(rs.getString("NAME"));
                        Timestamp createdTime = rs.getTimestamp("TIME_CREATED");
                        keyInfo.setCreatedTime(createdTime.getTime());
                        keyInfo.setValidityPeriod(rs.getLong("VALIDITY_PERIOD"));
                        Timestamp lastUsedTime = rs.getTimestamp("LAST_USED");
                        keyInfo.setLastUsedTime(lastUsedTime != null ? lastUsedTime.getTime() : null);
                        keyInfo.setApplicationId(rs.getString("APPLICATION_UUID"));
                        if (keyInfo.getApplicationId() == null) {
                            keyInfo.setApplicationName(APIConstants.NO_ASSOCIATION);
                        } else {
                            keyInfo.setApplicationName(rs.getString("APPLICATION_NAME"));
                        }
                        keyInfo.setKeyType(rs.getString("KEY_TYPE"));
                        apiKeyInfoList.add(keyInfo);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get API keys for the API: " + apiUUID, e);
        }
        return apiKeyInfoList;
    }

    /**
     * Returns a list of APIs with api keys against an Application
     *
     * @param applicationUUID Application UUID
     * @param keyType Key type of the api keys
     * @param tenantDomain Tenant domain
     * @param username Username
     * @return Returns a list of APIs with api keys
     * @throws APIManagementException
     */
    public List<APIKeyInfo> getSubscribedAPIsWithAPIKeys(String applicationUUID, String keyType, String tenantDomain, String username) throws APIManagementException {

        List<APIKeyInfo> apiKeyInfoList = new ArrayList<APIKeyInfo>();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String sqlQuery = SQLConstants.GET_SUBSCRIBED_API_WITH_API_KEY_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, applicationUUID);
                ps.setString(2, keyType);
                ps.setString(3, username);
                ps.setString(4, tenantDomain);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        APIKeyInfo keyInfo = new APIKeyInfo();
                        keyInfo.setKeyUUID(rs.getString("API_KEY_UUID"));
                        keyInfo.setKeyName(rs.getString("NAME"));
                        keyInfo.setApiUUId(rs.getString("API_UUID"));
                        keyInfo.setApiName(rs.getString("API_NAME"));
                        keyInfo.setAuthUser(rs.getString("AUTHZ_USER"));
                        keyInfo.setApplicationId(applicationUUID);
                        keyInfo.setKeyType(keyType);
                        apiKeyInfoList.add(keyInfo);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get APIs with API keys", e);
        }
        return apiKeyInfoList;
    }

    /**
     * Returns a list of all api keys
     *
     * @param tenantDomain Tenant domain
     * @throws APIManagementException
     */
    public List<APIKeyInfo> getAllAPIKeys(String tenantDomain) throws APIManagementException {

        List<APIKeyInfo> apiKeyInfoList = new ArrayList<APIKeyInfo>();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            String sqlQuery = SQLConstants.GET_ALL_API_KEYS_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, tenantDomain);
                ps.setString(2, tenantDomain);
                ps.setString(3, tenantDomain);
                ps.setString(4, tenantDomain);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        APIKeyInfo keyInfo = new APIKeyInfo();
                        keyInfo.setKeyUUID(rs.getString("API_KEY_UUID"));
                        keyInfo.setKeyName(rs.getString("KEY_NAME"));
                        Timestamp createdTime = rs.getTimestamp("TIME_CREATED");
                        keyInfo.setCreatedTime(createdTime.getTime());
                        Timestamp lastUsedTime = rs.getTimestamp("LAST_USED");
                        keyInfo.setLastUsedTime(lastUsedTime != null ? lastUsedTime.getTime() : null);
                        keyInfo.setKeyType(rs.getString("KEY_TYPE"));
                        try (InputStream apiKeyPropertiesInputStream = rs.getBinaryStream("ADDITIONAL_PROPERTIES")) {
                            if (apiKeyPropertiesInputStream != null) {
                                ObjectMapper mapper = new ObjectMapper();
                                Map<String, String> propertiesMap = mapper.readValue(apiKeyPropertiesInputStream,
                                        Map.class);
                                keyInfo.setProperties(propertiesMap);
                            }
                        } catch (IOException e) {
                            handleException("Failed to convert apiKeyProperties", e);
                        }
                        keyInfo.setApiKeyHash(rs.getString("API_KEY_HASH"));
                        keyInfo.setAuthUser(rs.getString("AUTHZ_USER"));
                        keyInfo.setStatus(rs.getString("STATUS"));
                        keyInfo.setAppId(rs.getInt("APP_ID"));
                        keyInfo.setApplicationId(rs.getString("APPLICATION_UUID"));
                        keyInfo.setApiId(rs.getInt("API_ID"));
                        keyInfo.setApiUUId(rs.getString("API_UUID"));
                        long validityPeriodInSeconds = rs.getLong("VALIDITY_PERIOD");
                        if (validityPeriodInSeconds < 0) {
                            keyInfo.setExpiresAt(Long.MAX_VALUE);
                        } else {
                            long validityPeriodInMillis = validityPeriodInSeconds * 1000L;
                            long createdTimeMillis = createdTime.getTime();
                            // Guard against arithmetic overflow before adding
                            if (validityPeriodInMillis < 0
                                    || validityPeriodInMillis > Long.MAX_VALUE - createdTimeMillis) {
                                keyInfo.setExpiresAt(Long.MAX_VALUE);
                            } else {
                                keyInfo.setExpiresAt(createdTimeMillis + validityPeriodInMillis);
                            }
                        }
                        keyInfo.setValidityPeriod(validityPeriodInSeconds);
                        apiKeyInfoList.add(keyInfo);
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get all API keys", e);
        }
        return apiKeyInfoList;
    }

    /**
     * Revoke an api key provided by the key UUID
     *
     * @param keyUUId API key UUID
     * @param tenantDomain Tenant domain
     * @throws APIManagementException
     */
    public void revokeAPIKey(String keyUUId, String tenantDomain) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String sqlQuery = SQLConstants.REVOKE_API_KEY_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, keyUUId);
                ps.setString(2, tenantDomain);
                ps.setString(3, tenantDomain);
                ps.executeUpdate();
                conn.commit();
            }
        } catch (SQLException e) {
            handleException("Failed to revoke the API key", e);
        }
    }

    /**
     * Revoke an api key provided by the key UUID
     *
     * @param keyUUId API key UUID
     * @param username Username
     * @throws APIManagementException
     */
    public void revokeAPIKeyViaUser(String keyUUId, String username) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String sqlQuery = SQLConstants.REVOKE_API_KEY_VIA_USER_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, keyUUId);
                ps.setString(2, username);
                ps.executeUpdate();
                conn.commit();
            }
        } catch (SQLException e) {
            handleException("Failed to revoke the API key", e);
        }
    }

    /**
     * Returns the api key specified by the key UUID
     *
     * @param keyUUId Application UUID
     * @param tenantDomain Tenant domain
     * @return API key info
     * @throws APIManagementException
     */
    public APIKeyInfo getAPIKeyForTenant(String keyUUId, String tenantDomain) throws APIManagementException {

        APIKeyInfo keyInfo = new APIKeyInfo();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            String sqlQuery = SQLConstants.GET_API_KEY_DETAILS_FROM_KEY_UUID_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, keyUUId);
                ps.setString(2, tenantDomain);
                ps.setString(3, tenantDomain);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        keyInfo.setKeyUUID(rs.getString("API_KEY_UUID"));
                        keyInfo.setKeyName(rs.getString("NAME"));
                        keyInfo.setApiKeyHash(rs.getString("API_KEY_HASH"));
                        keyInfo.setKeyType(rs.getString("KEY_TYPE"));
                        keyInfo.setValidityPeriod(rs.getLong("VALIDITY_PERIOD"));
                        Timestamp lastUsedTime = rs.getTimestamp("LAST_USED");
                        keyInfo.setLastUsedTime(lastUsedTime != null ? lastUsedTime.getTime() : null);
                        keyInfo.setAuthUser(rs.getString("AUTHZ_USER"));
                        try (InputStream apiKeyProperties = rs.getBinaryStream("API_KEY_PROPERTIES")) {
                            if (apiKeyProperties != null) {
                                ObjectMapper mapper = new ObjectMapper();
                                Map<String, String> propertiesMap = mapper.readValue(apiKeyProperties,
                                        Map.class);
                                keyInfo.setProperties(propertiesMap);
                            }
                        } catch (IOException e) {
                            handleException("Failed to convert apiKeyProperties", e);
                        }
                        keyInfo.setApiUUId(rs.getString("API_UUID"));
                        keyInfo.setOrigin(rs.getString("ORGANIZATION"));
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get the API key details for " + keyUUId, e);
        }
        return keyInfo;
    }

    /**
     * Returns the api key specified by the key UUID
     *
     * @param keyUUId Application UUID
     * @param username Username
     * @return API key info
     * @throws APIManagementException
     */
    public APIKeyInfo getAPIKey(String keyUUId, String username) throws APIManagementException {

        APIKeyInfo keyInfo = new APIKeyInfo();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            String sqlQuery = SQLConstants.GET_API_KEY_DETAILS_FROM_KEY_UUID_WITHOUT_TENANT_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, keyUUId);
                ps.setString(2, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        keyInfo.setKeyUUID(rs.getString("API_KEY_UUID"));
                        keyInfo.setKeyName(rs.getString("NAME"));
                        keyInfo.setApiKeyHash(rs.getString("API_KEY_HASH"));
                        keyInfo.setKeyType(rs.getString("KEY_TYPE"));
                        keyInfo.setValidityPeriod(rs.getLong("VALIDITY_PERIOD"));
                        Timestamp lastUsedTime = rs.getTimestamp("LAST_USED");
                        keyInfo.setLastUsedTime(lastUsedTime != null ? lastUsedTime.getTime() : null);
                        keyInfo.setAuthUser(rs.getString("AUTHZ_USER"));
                        try (InputStream apiKeyProperties = rs.getBinaryStream("API_KEY_PROPERTIES")) {
                            if (apiKeyProperties != null) {
                                ObjectMapper mapper = new ObjectMapper();
                                Map<String, String> propertiesMap = mapper.readValue(apiKeyProperties,
                                        Map.class);
                                keyInfo.setProperties(propertiesMap);
                            }
                        }
                    }
                }
            }
        } catch (SQLException | IOException e) {
            handleException("Failed to get the API key details for " + keyUUId, e);
        }
        return keyInfo;
    }

    /**
     * Returns the API bound api key specified by the key UUID
     *
     * @param apiUUId API UUID
     * @param keyUUId UUID of the api key
     * @param username Username
     * @return API key info
     * @throws APIManagementException
     */
    public APIKeyInfo getAPIAPIKey(String apiUUId, String keyUUId, String username) throws APIManagementException {

        APIKeyInfo keyInfo = new APIKeyInfo();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            String sqlQuery = SQLConstants.GET_API_API_KEY_DETAILS_FROM_KEY_UUID_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, apiUUId);
                ps.setString(2, keyUUId);
                ps.setString(3, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        keyInfo.setKeyName(rs.getString("NAME"));
                        keyInfo.setKeyUUID(rs.getString("API_KEY_UUID"));
                        keyInfo.setApiKeyHash(rs.getString("API_KEY_HASH"));
                        keyInfo.setValidityPeriod(rs.getLong("VALIDITY_PERIOD"));
                        Timestamp lastUsedTime = rs.getTimestamp("LAST_USED");
                        keyInfo.setLastUsedTime(lastUsedTime != null ? lastUsedTime.getTime() : null);
                        keyInfo.setApiUUId(apiUUId);
                        keyInfo.setKeyType(rs.getString("KEY_TYPE"));
                        keyInfo.setAuthUser(rs.getString("AUTHZ_USER"));
                        try (InputStream apiKeyProperties = rs.getBinaryStream("API_KEY_PROPERTIES")) {
                            if (apiKeyProperties != null) {
                                ObjectMapper mapper = new ObjectMapper();
                                Map<String, String> propertiesMap = mapper.readValue(apiKeyProperties,
                                        Map.class);
                                keyInfo.setProperties(propertiesMap);
                            }
                        } catch (IOException e) {
                            handleException("Failed to convert apiKeyProperties", e);
                        }
                        keyInfo.setApplicationId(rs.getString("APPLICATION_UUID"));
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get the API key details for " + keyUUId, e);
        }
        return keyInfo;
    }

    /**
     * Returns the API UUID for an API bound API key specified by the key name and application Id
     *
     * @param appUUId Application UUID
     * @param keyUUId UUId of the api key
     * @param username Username
     * @return APIKeyInfo
     * @throws APIManagementException
     */
    public APIKeyInfo getAPIKeyDetailsByKeyUUIDAndAppUUID(String appUUId, String keyUUId, String username) throws APIManagementException {

        APIKeyInfo apiKeyInfo = new APIKeyInfo();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            String sqlQuery = SQLConstants.GET_API_UUID_AND_TYPE_FOR_ASSOCIATION_VIA_APP_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, appUUId);
                ps.setString(2, keyUUId);
                ps.setString(3, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        apiKeyInfo.setApiUUId(rs.getString("API_UUID"));
                        apiKeyInfo.setAuthUser(rs.getString("AUTHZ_USER"));
                        apiKeyInfo.setKeyType(rs.getString("KEY_TYPE"));
                        apiKeyInfo.setKeyName(rs.getString("NAME"));
                        apiKeyInfo.setApiKeyHash(rs.getString("API_KEY_HASH"));
                        try (InputStream apiKeyProperties = rs.getBinaryStream("API_KEY_PROPERTIES")) {
                            if (apiKeyProperties != null) {
                                ObjectMapper mapper = new ObjectMapper();
                                Map<String, String> propertiesMap = mapper.readValue(apiKeyProperties,
                                        Map.class);
                                apiKeyInfo.setProperties(propertiesMap);
                            }
                        } catch (IOException e) {
                            handleException("Failed to convert apiKeyProperties", e);
                        }
                        apiKeyInfo.setAppId(rs.getInt("APPLICATION_ID"));
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get the API UUID for key " + keyUUId, e);
        }
        return apiKeyInfo;
    }

    /**
     * Remove association of an api key provided by the key name and app Id
     *
     * @param appUUId UUId of the Application
     * @param keyUUId API key UUId
     * @param tenantDomain Tenant domain
     * @throws APIManagementException
     */
    public void removeAssociationOfAPIKeyViaApp(String appUUId, String keyUUId, String tenantDomain) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String sqlQuery = SQLConstants.REMOVE_API_KEY_ASSOCIATION_VIA_APP_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, appUUId);
                ps.setString(2, keyUUId);
                ps.setString(3, tenantDomain);
                ps.executeUpdate();
                conn.commit();
            }
        } catch (SQLException e) {
            handleException("Failed to removing association of the API key", e);
        }
    }

    /**
     * Returns the key details for association
     *
     * @param keyUUId UUID of the api key
     * @param username Username
     * @return APIKeyInfo
     * @throws APIManagementException
     */
    public APIKeyInfo getKeyDetailsForAssociation(String keyUUId, String username) throws APIManagementException {

        APIKeyInfo apiKeyInfo = new APIKeyInfo();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            String sqlQuery = SQLConstants.GET_KEY_DETAILS_FOR_ASSOCIATION_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, keyUUId);
                ps.setString(2, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        APIKeyInfo keyInfo = new APIKeyInfo();
                        keyInfo.setKeyUUID(rs.getString("API_KEY_UUID"));
                        keyInfo.setKeyName(rs.getString("KEY_NAME"));
                        Timestamp createdTime = rs.getTimestamp("TIME_CREATED");
                        keyInfo.setCreatedTime(createdTime.getTime());
                        Timestamp lastUsedTime = rs.getTimestamp("LAST_USED");
                        keyInfo.setLastUsedTime(lastUsedTime != null ? lastUsedTime.getTime() : null);
                        keyInfo.setKeyType(rs.getString("KEY_TYPE"));
                        try (InputStream apiKeyPropertiesInputStream = rs.getBinaryStream("ADDITIONAL_PROPERTIES")) {
                            if (apiKeyPropertiesInputStream != null) {
                                ObjectMapper mapper = new ObjectMapper();
                                Map<String, String> propertiesMap = mapper.readValue(apiKeyPropertiesInputStream,
                                        Map.class);
                                keyInfo.setProperties(propertiesMap);
                            }
                        } catch (IOException e) {
                            handleException("Failed to convert apiKeyProperties", e);
                        }
                        keyInfo.setApiKeyHash(rs.getString("API_KEY_HASH"));
                        keyInfo.setAuthUser(rs.getString("AUTHZ_USER"));
                        keyInfo.setStatus(rs.getString("STATUS"));
                        keyInfo.setAppId(rs.getInt("APP_ID"));
                        keyInfo.setApplicationId(rs.getString("APPLICATION_UUID"));
                        keyInfo.setApiId(rs.getInt("API_ID"));
                        keyInfo.setApiUUId(rs.getString("API_UUID"));
                        long validityPeriodInSeconds = rs.getLong("VALIDITY_PERIOD");
                        if (validityPeriodInSeconds < 0) {
                            keyInfo.setExpiresAt(Long.MAX_VALUE);
                        } else {
                            long validityPeriodInMillis = validityPeriodInSeconds * 1000L;
                            long createdTimeMillis = createdTime.getTime();
                            // Guard against arithmetic overflow before adding
                            if (validityPeriodInMillis < 0
                                    || validityPeriodInMillis > Long.MAX_VALUE - createdTimeMillis) {
                                keyInfo.setExpiresAt(Long.MAX_VALUE);
                            } else {
                                keyInfo.setExpiresAt(createdTimeMillis + validityPeriodInMillis);
                            }
                        }
                        keyInfo.setValidityPeriod(validityPeriodInSeconds);
                        return keyInfo;
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get the key type for " + keyUUId, e);
        }
        return apiKeyInfo;
    }

    /**
     * Update application association of an API key
     *
     * @param keyUUId UUId of the API key
     * @param appUUId Application UUId
     * @throws APIManagementException
     */
    public void createAssociationToApiKey(String keyUUId, String appUUId) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String sqlQuery = SQLConstants.ADD_API_KEY_TO_APP_MAPPING_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, keyUUId);
                ps.setString(2, appUUId);
                ps.executeUpdate();
                conn.commit();
            }
        } catch (SQLException e) {
            handleException("Failed to update association of " + appUUId + " for the API key " + keyUUId, e);
        }
    }

    /**
     * Returns the key type of an API bound API key specified by the key name and api Id
     *
     * @param apiUUId API UUID
     * @param keyUUId UUID of the api key
     * @param username Username
     * @return APIKeyInfo
     * @throws APIManagementException
     */
    public APIKeyInfo getKeyTypeByAPIUUIDAndKeyName(String apiUUId, String keyUUId, String username) throws APIManagementException {

        APIKeyInfo apiKeyInfo = new APIKeyInfo();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            String sqlQuery = SQLConstants.GET_KEY_TYPE_ONLY_FOR_ASSOCIATION_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setString(1, apiUUId);
                ps.setString(2, keyUUId);
                ps.setString(3, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        apiKeyInfo.setKeyName(rs.getString("NAME"));
                        apiKeyInfo.setKeyType(rs.getString("KEY_TYPE"));
                        apiKeyInfo.setApiKeyHash(rs.getString("API_KEY_HASH"));
                        apiKeyInfo.setAuthUser(rs.getString("AUTHZ_USER"));
                        try (InputStream apiKeyProperties = rs.getBinaryStream("API_KEY_PROPERTIES")) {
                            if (apiKeyProperties != null) {
                                ObjectMapper mapper = new ObjectMapper();
                                Map<String, String> propertiesMap = mapper.readValue(apiKeyProperties,
                                        Map.class);
                                apiKeyInfo.setProperties(propertiesMap);
                            }
                        } catch (IOException e) {
                            handleException("Failed to convert apiKeyProperties", e);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Failed to get the key type for " + keyUUId, e);
        }
        return apiKeyInfo;
    }

    /**
     * Update last used time of an API key
     *
     * @param apiKeyHash Hash value of the API key
     * @param lastUsedTimestamp Last used timestamp
     * @throws APIManagementException
     */
    public void updateAPIKeyUsage(String apiKeyHash, Timestamp lastUsedTimestamp) throws APIManagementException {

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String sqlQuery = SQLConstants.UPDATE_API_KEY_LAST_USED_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setTimestamp(1, lastUsedTimestamp);
                ps.setString(2, apiKeyHash);
                ps.executeUpdate();
                conn.commit();
            }
        } catch (SQLException e) {
            handleException("Failed to update last used time for the API key", e);
        }
    }

    /**
     * Batch update last used time for API keys.
     *
     * @param apiKeyUsageUpdates map of API key hash to last used timestamp
     * @throws APIManagementException if database update fails
     */
    public void updateAPIKeyUsageBatch(Map<String, Timestamp> apiKeyUsageUpdates) throws APIManagementException {

        if (apiKeyUsageUpdates == null || apiKeyUsageUpdates.isEmpty()) {
            return;
        }

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String sqlQuery = SQLConstants.UPDATE_API_KEY_LAST_USED_SQL;
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                for (Map.Entry<String, Timestamp> entry : apiKeyUsageUpdates.entrySet()) {
                    ps.setTimestamp(1, entry.getValue());
                    ps.setString(2, entry.getKey());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                handleException("Failed to batch update last used time for API keys", e);
            }
        } catch (SQLException e) {
            handleException("Failed to batch update last used time for API keys", e);
        }
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }
}
