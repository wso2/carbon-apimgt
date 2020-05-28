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
package org.wso2.carbon.apimgt.impl.endpoint.registry.dao;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.endpoint.registry.constants.EndpointRegistryConstants;
import org.wso2.carbon.apimgt.impl.endpoint.registry.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.endpoint.registry.model.EndpointRegistryEntry;
import org.wso2.carbon.apimgt.impl.endpoint.registry.api.EndpointRegistryException;
import org.wso2.carbon.apimgt.impl.endpoint.registry.model.EndpointRegistryInfo;
import org.wso2.carbon.apimgt.impl.factory.SQLConstantManagerFactory;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class represent the EndpointRegistryDAO.
 */
public class EndpointRegistryDAO {

    private static final Log log = LogFactory.getLog(EndpointRegistryDAO.class);
    private static EndpointRegistryDAO INSTANCE = null;

    /**
     * Method to get the instance of the EndpointRegistryDAO.
     *
     * @return {@link EndpointRegistryDAO} instance
     */
    public static EndpointRegistryDAO getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new EndpointRegistryDAO();
        }

        return INSTANCE;
    }

    private void handleException(String msg, Throwable t) throws EndpointRegistryException {

        log.error(msg, t);
        throw new EndpointRegistryException(msg, t);
    }

    /**
     * Add a new endpoint registry
     *
     * @param endpointRegistry EndpointRegistryInfo
     * @param tenantID         ID of the owner's tenant
     * @return registryId
     */
    public String addEndpointRegistry(EndpointRegistryInfo endpointRegistry, int tenantID)
            throws EndpointRegistryException {

        String query = SQLConstants.ADD_ENDPOINT_REGISTRY_SQL;
        String uuid = UUID.randomUUID().toString();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            ps.setString(1, uuid);
            ps.setString(2, endpointRegistry.getName());
            ps.setString(3, endpointRegistry.getType());
            ps.setInt(4, tenantID);
            ps.setString(5, endpointRegistry.getOwner());
            ps.setString(6, endpointRegistry.getOwner());
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(7, timestamp);
            ps.setTimestamp(8, timestamp);

            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding new endpoint registry: " + endpointRegistry.getName(), e);
        }
        return uuid;
    }

    /**
     * Update an existing endpoint registry.
     *
     * @param registryId       uuid of the endpoint registry
     * @param endpointRegistry EndpointRegistryInfo object with updated details
     * @param username         logged in username
     * @throws EndpointRegistryException if unable to update the endpoint registry
     */
    public void updateEndpointRegistry(String registryId, EndpointRegistryInfo endpointRegistry, String username)
            throws EndpointRegistryException {

        String query = SQLConstants.UPDATE_ENDPOINT_REGISTRY_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            ps.setString(1, endpointRegistry.getName());
            ps.setString(2, endpointRegistry.getType());
            ps.setString(3, username);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            ps.setString(5, registryId);
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while updating endpoint registry: " + endpointRegistry.getName(), e);
        }
        return;
    }

    /**
     * Return the details of an Endpoint Registry
     *
     * @param registryId Endpoint Registry Identifier
     * @param tenantID   ID of the owner's tenant
     * @return Endpoint Registry Object
     * @throws EndpointRegistryException
     */
    public EndpointRegistryInfo getEndpointRegistryByUUID(String registryId, int tenantID)
            throws EndpointRegistryException {

        String query = SQLConstants.GET_ENDPOINT_REGISTRY_BY_UUID;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, registryId);
            ps.setInt(2, tenantID);
            ps.executeQuery();
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    EndpointRegistryInfo endpointRegistry = new EndpointRegistryInfo();
                    endpointRegistry.setUuid(rs.getString(EndpointRegistryConstants.COLUMN_UUID));
                    endpointRegistry.setName(rs.getString(EndpointRegistryConstants.COLUMN_REG_NAME));
                    endpointRegistry.setType(rs.getString(EndpointRegistryConstants.COLUMN_REG_TYPE));
                    endpointRegistry.setRegistryId(rs.getInt(EndpointRegistryConstants.COLUMN_ID));
                    endpointRegistry.setOwner(rs.getString(EndpointRegistryConstants.COLUMN_CREATED_BY));
                    endpointRegistry.setUpdatedBy(rs.getString(EndpointRegistryConstants.COLUMN_UPDATED_BY));

                    Timestamp createdTime = rs.getTimestamp(EndpointRegistryConstants.COLUMN_CREATED_TIME);
                    endpointRegistry.setCreatedTime(
                            createdTime == null ? null : String.valueOf(createdTime.getTime()));

                    Timestamp updatedTime = rs.getTimestamp(EndpointRegistryConstants.COLUMN_UPDATED_TIME);
                    endpointRegistry.setLastUpdatedTime(
                            updatedTime == null ? null : String.valueOf(updatedTime.getTime()));

                    return endpointRegistry;
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving details of endpoint registry with Id: "
                    + registryId, e);
        }
        return null;
    }

    /**
     * Deletes an Endpoint Registry
     *
     * @param registryUUID Registry Identifier(UUID)
     * @throws EndpointRegistryException if failed to delete the Endpoint Registry
     */
    public void deleteEndpointRegistry(String registryUUID) throws EndpointRegistryException {

        String deleteRegQuery = SQLConstants.DELETE_ENDPOINT_REGISTRY_SQL;

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statementDeleteRegistry = connection.prepareStatement(deleteRegQuery)
        ) {
            connection.setAutoCommit(false);
            statementDeleteRegistry.setString(1, registryUUID);
            statementDeleteRegistry.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to delete Endpoint Registry with the id: " + registryUUID, e);
        }
    }

    /**
     * Checks whether the given endpoint registry name is already available under given tenant domain
     *
     * @param registryName
     * @param tenantID
     * @return boolean
     * @throws EndpointRegistryException
     */
    public boolean isEndpointRegistryNameExists(String registryName, int tenantID) throws EndpointRegistryException {

        String sql = SQLConstants.IS_ENDPOINT_REGISTRY_NAME_EXISTS;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registryName);
            statement.setInt(2, tenantID);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("ENDPOINT_REGISTRY_COUNT");
                if (count > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check the existence of Endpoint Registry: " + registryName, e);
        }
        return false;
    }

    /**
     * Returns details of all Endpoint Registries belong to a given tenant
     *
     * @param name      Registry name
     * @param sortBy    Name of the sorting field
     * @param sortOrder Order of sorting (asc or desc)
     * @param limit     Limit
     * @param offset    Offset
     * @param tenantID
     * @return A list of EndpointRegistryInfo objects
     * @throws EndpointRegistryException if failed to get details of Endpoint Registries
     */
    public List<EndpointRegistryInfo> getEndpointRegistries(String name, String sortBy, String sortOrder,
                                                            int limit, int offset,
                                                            int tenantID) throws EndpointRegistryException {

        List<EndpointRegistryInfo> endpointRegistryInfoList = new ArrayList<>();

        try {
            boolean nameMatch = !StringUtils.isEmpty(name);
            String query;
            if (nameMatch) {
                query = SQLConstantManagerFactory.getSQlString("GET_ALL_ENDPOINT_REGISTRIES_OF_TENANT_WITH_NAME");
            } else {
                query = SQLConstantManagerFactory.getSQlString("GET_ALL_ENDPOINT_REGISTRIES_OF_TENANT");
            }
            query = query.replace("$1", sortBy);
            query = query.replace("$2", sortOrder);

            try (Connection connection = APIMgtDBUtil.getConnection();
                 PreparedStatement ps = connection.prepareStatement(query)) {
                if (nameMatch) {
                    ps.setString(1, name);
                    ps.setInt(2, tenantID);
                    ps.setInt(3, offset);
                    ps.setInt(4, limit);
                } else {
                    ps.setInt(1, tenantID);
                    ps.setInt(2, offset);
                    ps.setInt(3, limit);
                }
                ps.executeQuery();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        EndpointRegistryInfo endpointRegistry = new EndpointRegistryInfo();
                        endpointRegistry.setUuid(rs.getString(EndpointRegistryConstants.COLUMN_UUID));
                        endpointRegistry.setName(rs.getString(EndpointRegistryConstants.COLUMN_REG_NAME));
                        endpointRegistry.setType(rs.getString(EndpointRegistryConstants.COLUMN_REG_TYPE));
                        endpointRegistry.setOwner(rs.getString(EndpointRegistryConstants.COLUMN_CREATED_BY));
                        endpointRegistry.setUpdatedBy(rs.getString(EndpointRegistryConstants.COLUMN_UPDATED_BY));

                        Timestamp createdTime = rs.getTimestamp(EndpointRegistryConstants.COLUMN_CREATED_TIME);
                        endpointRegistry.setCreatedTime(
                                createdTime == null ? null : String.valueOf(createdTime.getTime()));

                        Timestamp updatedTime = rs.getTimestamp(EndpointRegistryConstants.COLUMN_UPDATED_TIME);
                        endpointRegistry.setLastUpdatedTime(
                                updatedTime == null ? null : String.valueOf(updatedTime.getTime()));

                        endpointRegistryInfoList.add(endpointRegistry);
                    }
                }
            } catch (SQLException e) {
                handleException("Error while retrieving details of endpoint registries", e);
            }
        } catch (APIManagementException e) {
            handleException("Error while retrieving the SQL string", e);
        }
        return endpointRegistryInfoList;
    }

    /**
     * Returns the details of an endpoint registry entry.
     *
     * @param registryEntryUuid endpoint registry entry identifier.
     * @return EndpointRegistryEntry object.
     * @throws EndpointRegistryException
     */
    public EndpointRegistryEntry getEndpointRegistryEntryByUUID(String registryEntryUuid)
            throws EndpointRegistryException {

        String query = SQLConstants.GET_ENDPOINT_REGISTRY_ENTRY_BY_UUID;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, registryEntryUuid);
            ps.executeQuery();
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
                    endpointRegistryEntry.setEntryId(rs.getString(EndpointRegistryConstants.COLUMN_UUID));
                    endpointRegistryEntry.setName(rs.getString(EndpointRegistryConstants.COLUMN_ENTRY_NAME));
                    endpointRegistryEntry.setVersion(rs.getString(EndpointRegistryConstants.COLUMN_ENTRY_VERSION));
                    endpointRegistryEntry.setDefinitionType(
                            rs.getString(EndpointRegistryConstants.COLUMN_DEFINITION_TYPE));
                    endpointRegistryEntry.setDefinitionURL(
                            rs.getString(EndpointRegistryConstants.COLUMN_DEFINITION_URL));
                    endpointRegistryEntry.setServiceType(rs.getString(EndpointRegistryConstants.COLUMN_SERVICE_TYPE));
                    endpointRegistryEntry.setServiceCategory(rs.getString(EndpointRegistryConstants.
                            COLUMN_SERVICE_CATEGORY));
                    endpointRegistryEntry.setProductionServiceURL(rs.getString(EndpointRegistryConstants.
                            COLUMN_PRODUCTION_SERVICE_URL));
                    endpointRegistryEntry.setSandboxServiceUrl(rs.getString(EndpointRegistryConstants.
                            COLUMN_SANDBOX_SERVICE_URL));
                    endpointRegistryEntry.setEndpointDefinition(
                            rs.getBinaryStream(EndpointRegistryConstants.COLUMN_ENDPOINT_DEFINITION));
                    endpointRegistryEntry.setOwner(rs.getString(EndpointRegistryConstants.COLUMN_CREATED_BY));
                    endpointRegistryEntry.setUpdatedBy(rs.getString(EndpointRegistryConstants.COLUMN_UPDATED_BY));

                    Timestamp createdTime = rs.getTimestamp(EndpointRegistryConstants.COLUMN_CREATED_TIME);
                    endpointRegistryEntry.setCreatedTime(
                            createdTime == null ? null : String.valueOf(createdTime.getTime()));

                    Timestamp updatedTime = rs.getTimestamp(EndpointRegistryConstants.COLUMN_UPDATED_TIME);
                    endpointRegistryEntry.setLastUpdatedTime(
                            updatedTime == null ? null : String.valueOf(updatedTime.getTime()));

                    endpointRegistryEntry.setRegistryId(rs.getInt(EndpointRegistryConstants.COLUMN_REG_ID));
                    return endpointRegistryEntry;
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving details of endpoint registry with Id: "
                    + registryEntryUuid, e);
        }
        return null;

    }

    /**
     * Returns all entries belong to a given endpoint registry
     *
     * @param sortBy         Name of the sorting field
     * @param sortOrder      Order of sorting (asc or desc)
     * @param limit          Limit
     * @param offset         Offset
     * @param registryId     UUID of the endpoint registry
     * @param version        The version of registry entry
     * @param exactNameMatch Whether to perform exact search on name
     * @return A list of EndpointRegistryEntry objects
     * @throws EndpointRegistryException if failed to get entries of an Endpoint Registry
     */
    public List<EndpointRegistryEntry> getEndpointRegistryEntries(String sortBy, String sortOrder, int limit,
                                                                  int offset, String registryId, String serviceType,
                                                                  String definitionType, String entryName,
                                                                  String serviceCategory, String version,
                                                                  boolean exactNameMatch)
            throws EndpointRegistryException {

        List<EndpointRegistryEntry> endpointRegistryEntryList = new ArrayList<>();
        String query;
        boolean versionMatch = !StringUtils.isEmpty(version);
        try {
            if (exactNameMatch && versionMatch) {
                query = SQLConstantManagerFactory
                        .getSQlString("GET_ALL_ENTRIES_OF_ENDPOINT_REGISTRY_WITH_EXACT_NAME_WITH_VERSION");
            } else if (exactNameMatch && !versionMatch) {
                query = SQLConstantManagerFactory.getSQlString("GET_ALL_ENTRIES_OF_ENDPOINT_REGISTRY_WITH_EXACT_NAME");
            } else if (!exactNameMatch && versionMatch) {
                query = SQLConstantManagerFactory.getSQlString("GET_ALL_ENTRIES_OF_ENDPOINT_REGISTRY_WITH_VERSION");
            } else {
                query = SQLConstantManagerFactory.getSQlString("GET_ALL_ENTRIES_OF_ENDPOINT_REGISTRY");
            }
            query = query.replace("$1", sortBy);
            query = query.replace("$2", sortOrder);

            try (Connection connection = APIMgtDBUtil.getConnection();
                 PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, registryId);
                if (exactNameMatch) {
                    ps.setString(2, entryName);
                } else {
                    ps.setString(2, "%" + entryName + "%");
                }
                ps.setString(3, "%" + definitionType + "%");
                ps.setString(4, "%" + serviceType + "%");
                ps.setString(5, "%" + serviceCategory + "%");
                if (versionMatch) {
                    ps.setString(6, version);
                    ps.setInt(7, offset);
                    ps.setInt(8, limit);
                } else {
                    ps.setInt(6, offset);
                    ps.setInt(7, limit);
                }
                ps.executeQuery();
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        EndpointRegistryEntry endpointRegistryEntry = new EndpointRegistryEntry();
                        endpointRegistryEntry.setEntryId(rs.getString(EndpointRegistryConstants.COLUMN_UUID));
                        endpointRegistryEntry.setName(rs.getString(EndpointRegistryConstants.COLUMN_ENTRY_NAME));
                        endpointRegistryEntry.setVersion(rs.getString(EndpointRegistryConstants.COLUMN_ENTRY_VERSION));
                        endpointRegistryEntry.setProductionServiceURL(rs.getString(EndpointRegistryConstants.
                                COLUMN_PRODUCTION_SERVICE_URL));
                        endpointRegistryEntry.setSandboxServiceUrl(rs.getString(EndpointRegistryConstants.
                                COLUMN_SANDBOX_SERVICE_URL));
                        endpointRegistryEntry.setDefinitionType(rs.getString(EndpointRegistryConstants.
                                COLUMN_DEFINITION_TYPE));
                        endpointRegistryEntry.setDefinitionURL(rs.getString(EndpointRegistryConstants.
                                COLUMN_DEFINITION_URL));
                        endpointRegistryEntry.setServiceType(rs.getString(EndpointRegistryConstants.COLUMN_SERVICE_TYPE));
                        endpointRegistryEntry.setServiceCategory(rs.getString(EndpointRegistryConstants
                                .COLUMN_SERVICE_CATEGORY));
                        endpointRegistryEntry.setOwner(rs.getString(EndpointRegistryConstants.COLUMN_CREATED_BY));
                        endpointRegistryEntry.setUpdatedBy(rs.getString(EndpointRegistryConstants.COLUMN_UPDATED_BY));

                        Timestamp createdTime = rs.getTimestamp(EndpointRegistryConstants.COLUMN_CREATED_TIME);
                        endpointRegistryEntry.setCreatedTime(
                                createdTime == null ? null : String.valueOf(createdTime.getTime()));

                        Timestamp updatedTime = rs.getTimestamp(EndpointRegistryConstants.COLUMN_UPDATED_TIME);
                        endpointRegistryEntry.setLastUpdatedTime(
                                updatedTime == null ? null : String.valueOf(updatedTime.getTime()));
                        endpointRegistryEntryList.add(endpointRegistryEntry);
                    }
                }
            } catch (SQLException e) {
                handleException("Error while retrieving entries of endpoint registry", e);
            }
        } catch (APIManagementException e) {
            handleException("Error while retrieving the SQL string", e);
        }
        return endpointRegistryEntryList;
    }

    /**
     * Add a new endpoint registry entry
     *
     * @param registryEntry EndpointRegistryEntry
     * @param username      logged in username
     * @return registryId
     */
    public String addEndpointRegistryEntry(EndpointRegistryEntry registryEntry, String username)
            throws EndpointRegistryException {

        String query = SQLConstants.ADD_ENDPOINT_REGISTRY_ENTRY_SQL;
        String uuid = UUID.randomUUID().toString();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            ps.setString(1, uuid);
            ps.setString(2, registryEntry.getName());
            ps.setString(3, registryEntry.getVersion());
            ps.setString(4, registryEntry.getProductionServiceURL());
            ps.setString(5, registryEntry.getSandboxServiceUrl());
            ps.setString(6, registryEntry.getDefinitionType());
            ps.setString(7, registryEntry.getDefinitionURL());
            ps.setString(8, registryEntry.getDescription());
            ps.setString(9, registryEntry.getServiceType());
            ps.setString(10, registryEntry.getServiceCategory());
            ps.setBlob(11, registryEntry.getEndpointDefinition());
            ps.setInt(12, registryEntry.getRegistryId());
            ps.setString(13, username);
            ps.setString(14, username);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            ps.setTimestamp(15, timestamp);
            ps.setTimestamp(16, timestamp);
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while adding new endpoint registry entry: " + registryEntry.getName(), e);
        }
        return uuid;
    }

    /**
     * Updates Registry Entry
     *
     * @param registryEntry EndpointRegistryEntry
     * @param username      logged in username
     * @throws EndpointRegistryException if failed to update EndpointRegistryEntry
     */
    public void updateEndpointRegistryEntry(EndpointRegistryEntry registryEntry, String username)
            throws EndpointRegistryException {

        String query = SQLConstants.UPDATE_ENDPOINT_REGISTRY_ENTRY_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            connection.setAutoCommit(false);
            ps.setString(1, registryEntry.getName());
            ps.setString(2, registryEntry.getVersion());
            ps.setString(3, registryEntry.getProductionServiceURL());
            ps.setString(4, registryEntry.getSandboxServiceUrl());
            ps.setString(5, registryEntry.getDefinitionType());
            ps.setString(6, registryEntry.getDefinitionURL());
            ps.setString(7, registryEntry.getDescription());
            ps.setString(8, registryEntry.getServiceType());
            ps.setString(9, registryEntry.getServiceCategory());
            ps.setBlob(10, registryEntry.getEndpointDefinition());
            ps.setString(11, username);
            ps.setTimestamp(12, new Timestamp(System.currentTimeMillis()));
            ps.setString(13, registryEntry.getEntryId());
            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Error while updating endpoint registry entry with id: " + registryEntry.getEntryId(), e);
        }
    }

    /**
     * Deletes an Endpoint Registry Entry
     *
     * @param entryId Registry Entry Identifier(UUID)
     * @throws EndpointRegistryException if failed to delete the Endpoint Registry Entry
     */
    public void deleteEndpointRegistryEntry(String entryId) throws EndpointRegistryException {

        String query = SQLConstants.DELETE_ENDPOINT_REGISTRY_ENTRY_SQL;

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)
        ) {
            connection.setAutoCommit(false);
            statement.setString(1, entryId);
            statement.execute();
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to delete Endpoint Registry Entry with the id: " + entryId, e);
        }
    }

    /**
     * Checks whether the given endpoint registry entry name is already available under given registry
     *
     * @param registryEntry
     * @return boolean
     */
    public boolean isRegistryEntryNameExists(EndpointRegistryEntry registryEntry) throws EndpointRegistryException {

        String sql = SQLConstants.IS_ENDPOINT_REGISTRY_ENTRY_NAME_EXISTS;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registryEntry.getName());
            statement.setInt(2, registryEntry.getRegistryId());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("REGISTRY_ENTRY_COUNT");
                if (count > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check the existence of Registry Entry: " + registryEntry.getName(), e);
        }
        return false;
    }

    /**
     * Checks whether the given endpoint registry entry name and version is already available under given registry
     *
     * @param registryEntry EndpointRegistryEntry
     * @return boolean
     */
    public boolean isRegistryEntryNameAndVersionExists(EndpointRegistryEntry registryEntry)
            throws EndpointRegistryException {

        String sql = SQLConstants.IS_ENDPOINT_REGISTRY_ENTRY_NAME_AND_VERSION_EXISTS;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, registryEntry.getName());
            statement.setString(2, registryEntry.getVersion());
            statement.setInt(3, registryEntry.getRegistryId());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("REGISTRY_ENTRY_COUNT");
                if (count > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            handleException("Failed to check the existence of Registry Entry: " + registryEntry.getName(), e);
        }
        return false;
    }
}
