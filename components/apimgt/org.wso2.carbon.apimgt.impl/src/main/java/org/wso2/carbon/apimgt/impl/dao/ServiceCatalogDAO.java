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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.api.model.ServiceFilterParams;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class represent the ServiceCatalogDAO.
 */
public class ServiceCatalogDAO {

    private static final Log log = LogFactory.getLog(ServiceCatalogDAO.class);
    private static ServiceCatalogDAO INSTANCE = null;

    /**
     * Method to get the instance of the ServiceCatalogDAO.
     *
     * @return {@link ServiceCatalogDAO} instance
     */
    public static ServiceCatalogDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServiceCatalogDAO();
        }

        return INSTANCE;
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    /**
     * Add a new service to Service Catalog
     *
     * @param serviceEntry Service
     * @param tenantID     ID of the owner's tenant
     * @param username     Logged in user name
     * @return UUID of the added service
     * throws APIManagementException if failed to create service catalog
     */
    public String addService(ServiceEntry serviceEntry, int tenantID, String username)
            throws APIManagementException {
        String uuid = StringUtils.EMPTY;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.ADD_SERVICE)) {
            try {
                connection.setAutoCommit(false);
                uuid = setServiceParams(ps, serviceEntry, tenantID, username);
                ps.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to rollback adding endpoint information", e);
            }
        } catch (SQLException e) {
            handleException("Failed to add service catalog of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantID), e);
        }
        return uuid;
    }

    /**
     * Add list of services to Service Catalog
     * @param services List of Services that needs to be added
     * @param tenantId Tenant ID of the logged-in user
     * @param username Logged-in username
     * @param connection DB Connection
     *
     */
    private void addServices(List<ServiceEntry> services, int tenantId, String username, Connection connection)
            throws SQLException {
        try (PreparedStatement preparedStatement = connection
                .prepareStatement(SQLConstants.ServiceCatalogConstants.ADD_SERVICE)) {
            for (ServiceEntry service : services) {
                setServiceParams(preparedStatement, service, tenantId, username);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        }
    }

    /**
     * Update list of services available in Service Catalog
     * @param services List of Services that needs to be updated
     * @param tenantId Tenant ID of the logged-in user
     * @param username Logged-in username
     * @param connection DB Connection
     *
     */
    private void updateServices(List<ServiceEntry> services, int tenantId, String username, Connection connection)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SQLConstants.ServiceCatalogConstants
                .UPDATE_SERVICE_BY_KEY)) {
            for (ServiceEntry service: services) {
                setUpdateServiceParams(ps, service, tenantId, username);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<ServiceEntry> importServices(List<ServiceEntry> services, int tenantId, String username,
                                             boolean overwrite) throws APIManagementException {
        List<ServiceEntry> serviceListToAdd = new ArrayList<>();
        List<ServiceEntry> serviceListToUpdate = new ArrayList<>();
        boolean isValid = true;
        for (int i = 0; i < services.size(); i++) {
            ServiceEntry service = services.get(i);
            ServiceEntry existingService = getServiceByKey(service.getKey(), tenantId);
            if (existingService != null && StringUtils.isNotEmpty(existingService.getMd5())) {
                if (!existingService.getVersion().equals(service.getVersion())) {
                    isValid = false;
                    break;
                }
                if (!existingService.getDefinitionType().equals(service.getDefinitionType())) {
                    isValid = false;
                    break;
                }
                if (!existingService.getKey().equals(service.getKey())) {
                    isValid = false;
                    break;
                }
                if (!existingService.getName().equals(service.getName())) {
                    isValid = false;
                    break;
                }
                if (!existingService.getMd5().equals(service.getMd5())) {
                    serviceListToUpdate.add(service);
                }
            } else {
                serviceListToAdd.add(service);
            }
        }
        if (isValid && !overwrite && serviceListToUpdate.size() > 0) {
            throw new APIManagementException("Cannot update the existing services", ExceptionCodes
                    .from(ExceptionCodes.SERVICE_IMPORT_FAILED_WITHOUT_OVERWRITE));
        }
        if (isValid) {
            try (Connection connection = APIMgtDBUtil.getConnection()) {
                try {
                    connection.setAutoCommit(false);
                    addServices(serviceListToAdd, tenantId, username, connection);
                    updateServices(serviceListToUpdate, tenantId, username, connection);
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    handleException("Failed to import services to service catalog of tenant " + tenantId, e);
                }
            } catch (SQLException e) {
                handleException("Failed to import services to service catalog of tenant "
                        + APIUtil.getTenantDomainFromTenantId(tenantId), e);
            }
            List<ServiceEntry> importedServiceList = new ArrayList<>();
            importedServiceList.addAll(serviceListToAdd);
            importedServiceList.addAll(serviceListToUpdate);
            return importedServiceList;
        } else {
            return null;
        }
    }

    /**
     * Update an existing serviceCatalog
     *
     * @param serviceEntry ServiceCatalogInfo
     * @param tenantID     ID of the owner's tenant
     * @param userName     Logged in user name
     * @return serviceCatalogId
     * throws APIManagementException if failed to create service catalog
     */
    public void updateService(ServiceEntry serviceEntry, int tenantID, String userName)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.UPDATE_SERVICE_BY_KEY)) {
            try {
                connection.setAutoCommit(false);
                setUpdateServiceParams(ps, serviceEntry, tenantID, userName);
                ps.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to rollback updating endpoint information", e);
            }
        } catch (SQLException e) {
            handleException("Failed to update service catalog of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantID), e);
        }
    }

    /**
     * Add a new end-point definition entry
     *
     * @param serviceEntry EndPoint related information
     * @return uuid
     * throws APIManagementException if failed to update service catalog
     */
    public String addEndPointDefinition(ServiceEntry serviceEntry, String uuid) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.ADD_ENDPOINT_RESOURCES)) {
            boolean initialAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                ps.setString(1, uuid);
                ps.setBinaryStream(2, serviceEntry.getEndpointDef());
                ps.setBinaryStream(3, serviceEntry.getMetadata());

                ps.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to rollback adding endpoint definitions", e);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, initialAutoCommit);
            }
        } catch (SQLException e) {
            handleException("Failed to add end point definition for service catalog entry ID "
                    + uuid, e);
        }
        return uuid;
    }

    /**
     * Update MD5 hash value of existing ServiceEntry object
     *
     * @param serviceInfo  ServiceEntry object
     * @param tenantId     ID of the owner's tenant
     * @return ServiceEntry
     * throws APIManagementException if failed
     */
    public ServiceEntry getMd5Hash(ServiceEntry serviceInfo, int tenantId) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(SQLConstants.ServiceCatalogConstants.GET_SERVICE_MD5_BY_NAME_AND_VERSION)) {
            ps.setString(1, serviceInfo.getName());
            ps.setString(2, serviceInfo.getVersion());
            ps.setInt(3, tenantId);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    serviceInfo.setMd5(resultSet.getString("MD5"));
                }
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL for getting User MD5 hash", e);
        }
        return serviceInfo;
    }

    /**
     * Get MD5 hash value of a service
     *
     * @param key          Service key of service
     * @param tenantId     ID of the owner's tenant
     * @return String key
     * throws APIManagementException if failed
     */
    public String getMd5HashByKey(String key, int tenantId) throws APIManagementException {
        String md5 = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(SQLConstants.ServiceCatalogConstants.GET_SERVICE_MD5_BY_SERVICE_KEY)) {
            ps.setString(1, key);
            ps.setInt(2, tenantId);
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    md5 = resultSet.getString("MD5");
                }
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL for getting User MD5 hash", e);
        }
        return md5;
    }

    /**
     * Get service information by service key
     *
     * @param key          Service key of service
     * @param tenantId     ID of the owner's tenant
     * @return ServiceEntry
     * throws APIManagementException if failed to retrieve
     */
    public ServiceEntry getServiceByKey(String key, int tenantId) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(SQLConstants.ServiceCatalogConstants.GET_SERVICE_BY_SERVICE_KEY)) {
            ps.setString(1, key);
            ps.setInt(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ServiceEntry serviceEntry = getServiceParams(rs, false);
                    return serviceEntry;
                }
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL for getting service information", e);
        }
        return null;
    }

    /**
     * Get service information by name and version
     *
     * @param name          Service name
     * @param version       Service version
     * @param tenantId     ID of the owner's tenant
     * @return ServiceEntry
     * throws APIManagementException if failed to retrieve
     */
    public ServiceEntry getServiceByNameAndVersion(String name, String version, int tenantId)
            throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(SQLConstants.ServiceCatalogConstants
                             .GET_SERVICE_BY_NAME_AND_VERSION)) {
            ps.setString(1, name);
            ps.setString(2, version);
            ps.setInt(3, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ServiceEntry serviceEntry = getServiceParams(rs, false);
                    return serviceEntry;
                }
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL for getting catalog entry resources", e);
        }
        return null;
    }

    /**
     * Delete service by service ID
     *
     * @param serviceId   Service ID
     * @param tenantId     ID of the owner's tenant
     * throws APIManagementException if failed to delete
     */
    public void deleteService(String serviceId, int tenantId) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQLConstants
                     .ServiceCatalogConstants.DELETE_SERVICE_BY_SERVICE_ID)) {
            boolean initialAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                statement.setString(1, serviceId);
                statement.setInt(2, tenantId);
                statement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to delete service : " + serviceId + " from service catalog: " + tenantId, e);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, initialAutoCommit);
            }
        } catch (SQLException e) {
            handleException("Failed to delete service : " + serviceId + " from service catalog: " + tenantId, e);
        }
    }

    /**
     * Get services
     * @param filterParams Service Filter parameters
     * @param tenantId Tenant ID of the logged in user
     * @param shrink Whether to shrink the response or not
     * @return List of Services
     * @throws APIManagementException
     */
    public List<ServiceEntry> getServices(ServiceFilterParams filterParams, int tenantId, boolean shrink)
            throws APIManagementException {
        List<ServiceEntry> serviceEntryList = new ArrayList<>();
        String query;
        boolean searchByKey = false;
        boolean searchByDefinitionType = false;
        boolean exactNameSearch = false;
        boolean exactVersionSearch = false;
        StringBuilder querySb = new StringBuilder();
        querySb.append("SELECT UUID, SERVICE_KEY, MD5, SERVICE_NAME, SERVICE_VERSION," +
                "   SERVICE_URL, DEFINITION_TYPE, DEFINITION_URL, DESCRIPTION, SECURITY_TYPE, MUTUAL_SSL_ENABLED," +
                "   CREATED_TIME, LAST_UPDATED_TIME, CREATED_BY, UPDATED_BY, SERVICE_DEFINITION FROM " +
                "   AM_SERVICE_CATALOG WHERE TENANT_ID = ? ");
        String whereClauseForExactNameSearch = "AND SERVICE_NAME = ? ";
        String whereClauseForNameSearch = "AND SERVICE_NAME LIKE ? ";
        String whereClauseForExactVersionSearch = "AND SERVICE_VERSION = ? ";
        String whereClauseForVersionSearch = " AND SERVICE_VERSION LIKE ? ";
        String whereClauseWithDefinitionType = " AND DEFINITION_TYPE = ? ";
        String whereClauseWithServiceKey = " AND SERVICE_KEY = ? ";
        if (filterParams.getName().startsWith("\"") && filterParams.getName().endsWith("\"")) {
            exactNameSearch = true;
            filterParams.setName(filterParams.getName().replace("\"", "").trim());
            querySb.append(whereClauseForExactNameSearch);
        } else {
            querySb.append(whereClauseForNameSearch);
        }
        if (filterParams.getVersion().startsWith("\"") && filterParams.getVersion().endsWith("\"")) {
            exactVersionSearch = true;
            filterParams.setVersion(filterParams.getVersion().replace("\"", "").trim());
            querySb.append(whereClauseForExactVersionSearch);
        } else {
            querySb.append(whereClauseForVersionSearch);
        }
        if (StringUtils.isNotEmpty(filterParams.getDefinitionType()) && StringUtils.isEmpty(filterParams.getKey())) {
            searchByDefinitionType = true;
            querySb.append(whereClauseWithDefinitionType);
        } else if (StringUtils.isNotEmpty(filterParams.getKey()) &&
                StringUtils.isEmpty(filterParams.getDefinitionType())) {
            searchByKey = true;
            querySb.append(whereClauseWithServiceKey);
        } else if (StringUtils.isNotEmpty(filterParams.getDefinitionType()) &&
                StringUtils.isNotEmpty(filterParams.getKey())) {
            searchByKey = true;
            searchByDefinitionType = true;
            querySb.append(whereClauseWithDefinitionType)
                    .append(whereClauseWithServiceKey);
        }
        querySb.append("ORDER BY ")
                .append(filterParams.getSortBy())
                .append(" " + filterParams.getSortOrder());
        String[] keyArray = null;
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            String driverName = connection.getMetaData().getDriverName();
            if (driverName.contains("Oracle") || driverName.contains("MS SQL") || driverName.contains("Microsoft")) {
                querySb.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
            } else if (driverName.contains("PostgreSQL")) {
                querySb.append(" OFFSET ? LIMIT ? ");
            } else {
                querySb.append(" LIMIT ?, ?");
            }
            query = querySb.toString();
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                keyArray = filterParams.getKey().split(",");
                for (String key : keyArray) {
                    ps.setInt(1, tenantId);
                    if (exactNameSearch) {
                        ps.setString(2, filterParams.getName());
                    } else {
                        ps.setString(2, "%" + filterParams.getName() + "%");
                    }
                    if (exactVersionSearch) {
                        ps.setString(3, filterParams.getVersion());
                    } else {
                        ps.setString(3, "%" + filterParams.getVersion() + "%");
                    }
                    if (searchByKey && searchByDefinitionType) {
                        ps.setString(4, filterParams.getDefinitionType());
                        ps.setString(5, key);
                        ps.setInt(6, filterParams.getOffset());
                        ps.setInt(7, filterParams.getLimit());
                    } else if (searchByKey) {
                        ps.setString(4, key);
                        ps.setInt(5, filterParams.getOffset());
                        ps.setInt(6, filterParams.getLimit());
                    } else if (searchByDefinitionType) {
                        ps.setString(4, filterParams.getDefinitionType());
                        ps.setInt(5, filterParams.getOffset());
                        ps.setInt(6, filterParams.getLimit());
                    } else {
                        ps.setInt(4, filterParams.getOffset());
                        ps.setInt(5, filterParams.getLimit());
                    }
                    try (ResultSet resultSet = ps.executeQuery()) {
                        while (resultSet.next()) {
                            ServiceEntry service = getServiceParams(resultSet, shrink);
                            List<API> usedAPIs = getServiceUsage(service.getUuid(), tenantId, connection);
                            int usage = usedAPIs != null ? usedAPIs.size() : 0;
                            service.setUsage(usage);
                            serviceEntryList.add(service);
                        }
                    }
                }
            } catch (SQLException e) {
                handleException("Error while retrieving the Services", e);
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the Services", e);
        }
        return serviceEntryList;
    }

    public ServiceEntry getServiceByUUID(String serviceId, int tenantId) throws APIManagementException {
        String query = SQLConstants.ServiceCatalogConstants.GET_SERVICE_BY_SERVICE_ID;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, serviceId);
            ps.setInt(2, tenantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ServiceEntry service = getServiceParams(rs, false);
                    int usage = getServiceUsage(serviceId, tenantId, connection) != null ? getServiceUsage(serviceId,
                            tenantId, connection).size() : 0;
                    service.setUsage(usage);
                    return service;
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving details of Service with Id: " + serviceId, e);
        }
        return null;
    }

    public List<API> getServiceUsage(String serviceId, int tenantId) throws APIManagementException {
        try (Connection connection = APIMgtDBUtil.getConnection()) {
            return getServiceUsage(serviceId, tenantId, connection);
        } catch (SQLException e) {
            handleException("Error while retrieving the usage of Service with Id: " + serviceId, e);
            return null;
        }
    }

    public int getServicesCount(int tenantId, ServiceFilterParams filterParams) throws APIManagementException {
        int noOfServices = 0;
        boolean searchByKey = false;
        boolean searchByDefinitionType = false;
        boolean exactNameSearch = false;
        boolean exactVersionSearch = false;
        String whereClauseForExactNameSearch = "AND SERVICE_NAME = ? ";
        String whereClauseForNameSearch = "AND SERVICE_NAME LIKE ? ";
        String whereClauseForExactVersionSearch = "AND SERVICE_VERSION = ? ";
        String whereClauseForVersionSearch = " AND SERVICE_VERSION LIKE ? ";
        String whereClauseWithDefinitionType = " AND DEFINITION_TYPE = ? ";
        String whereClauseWithServiceKey = " AND SERVICE_KEY = ? ";
        StringBuilder querySb = new StringBuilder();
        querySb.append("SELECT count(*) count FROM AM_SERVICE_CATALOG WHERE TENANT_ID = ? ");
        if (filterParams.getName().startsWith("\"") && filterParams.getName().endsWith("\"")) {
            exactNameSearch = true;
            filterParams.setName(filterParams.getName().replace("\"", "").trim());
            querySb.append(whereClauseForExactNameSearch);
        } else {
            querySb.append(whereClauseForNameSearch);
        }
        if (filterParams.getVersion().startsWith("\"") && filterParams.getVersion().endsWith("\"")) {
            exactVersionSearch = true;
            filterParams.setVersion(filterParams.getVersion().replace("\"", "").trim());
            querySb.append(whereClauseForExactVersionSearch);
        } else {
            querySb.append(whereClauseForVersionSearch);
        }
        if (StringUtils.isNotEmpty(filterParams.getDefinitionType()) && StringUtils.isEmpty(filterParams.getKey())) {
            searchByDefinitionType = true;
            querySb.append(whereClauseWithDefinitionType);
        } else if (StringUtils.isNotEmpty(filterParams.getKey()) &&
                StringUtils.isEmpty(filterParams.getDefinitionType())) {
            searchByKey = true;
            querySb.append(whereClauseWithServiceKey);
        } else if (StringUtils.isNotEmpty(filterParams.getDefinitionType()) &&
                StringUtils.isNotEmpty(filterParams.getKey())) {
            searchByKey = true;
            searchByDefinitionType = true;
            querySb.append(whereClauseWithDefinitionType)
                    .append(whereClauseWithServiceKey);
        }
        String[] keyArray = null;
        String query = querySb.toString();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            keyArray = filterParams.getKey().split(",");
            for (String key : keyArray) {
                ps.setInt(1, tenantId);
                if (exactNameSearch) {
                    ps.setString(2, filterParams.getName());
                } else {
                    ps.setString(2, "%" + filterParams.getName() + "%");
                }
                if (exactVersionSearch) {
                    ps.setString(3, filterParams.getVersion());
                } else {
                    ps.setString(3, "%" + filterParams.getVersion() + "%");
                }
                if (searchByKey && searchByDefinitionType) {
                    ps.setString(4, filterParams.getDefinitionType());
                    ps.setString(5, key);
                } else if (searchByKey) {
                    ps.setString(4, key);
                } else if (searchByDefinitionType) {
                    ps.setString(4, filterParams.getDefinitionType());
                }
                try (ResultSet resultSet = ps.executeQuery()) {
                    if (resultSet != null) {
                        while (resultSet.next()) {
                            noOfServices = resultSet.getInt("count");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            handleException("Error while retrieving the services count", e);
        }
        return noOfServices;
    }

    private List<API> getServiceUsage(String serviceId, int tenantId, Connection connection) throws SQLException {
        String query = SQLConstants.ServiceCatalogConstants.GET_USAGE_OF_SERVICES_BY_SERVICE_ID;
        List<API> apis = new ArrayList<>();
        try(PreparedStatement ps = connection.prepareStatement(query)) {
            String serviceKey = getServiceKeyByUUID(serviceId, tenantId, connection);
            if (StringUtils.isNotEmpty(serviceKey)) {
                ps.setString(1, serviceKey);
                ps.setInt(2, tenantId);
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        String provider = resultSet.getString(APIConstants.FIELD_API_PUBLISHER);
                        String apiName = resultSet.getString(APIConstants.FIELD_API_NAME);
                        String version = resultSet.getString(APIConstants.FIELD_API_VERSION);
                        APIIdentifier apiIdentifier = new APIIdentifier(provider, apiName, version);
                        API api = new API(apiIdentifier);
                        api.setContext(resultSet.getString("CONTEXT"));
                        api.setUuid(resultSet.getString("API_UUID"));
                        apis.add(api);
                    }
                }
            } else {
                return null;
            }
        }
        return apis;
    }

    private String getServiceKeyByUUID(String serviceId, int tenantId, Connection connection) throws SQLException {
        String query = SQLConstants.ServiceCatalogConstants.GET_SERVICE_KEY_BY_SERVICE_UUID;
        String serviceKey = StringUtils.EMPTY;
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, serviceId);
            ps.setInt(2, tenantId);
            try (ResultSet resultSet = ps.executeQuery()) {
                if(resultSet.next()) {
                    serviceKey = resultSet.getString(APIConstants.ServiceCatalogConstants.SERVICE_KEY);
                }
            }
        }
        return serviceKey;
    }

    private void setUpdateServiceParams(PreparedStatement ps, ServiceEntry service, int tenantId, String username)
            throws SQLException {
        ps.setString(1, service.getMd5());
        ps.setString(2, service.getName());
        ps.setInt(3, tenantId);
        ps.setString(4, service.getServiceUrl());
        ps.setString(5, service.getDefUrl());
        ps.setString(6, service.getDescription());
        ps.setString(7, service.getSecurityType().toString());
        ps.setBoolean(8, service.isMutualSSLEnabled());
        ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
        ps.setString(10, username);
        ps.setBinaryStream(11, service.getEndpointDef());
        ps.setString(12, service.getKey());
        ps.setInt(13, tenantId);
    }

    private String setServiceParams(PreparedStatement ps, ServiceEntry service, int tenantId, String username)
            throws SQLException {
        String uuid = UUID.randomUUID().toString();
        ps.setString(1, uuid);
        ps.setString(2, service.getKey());
        ps.setString(3, service.getMd5());
        ps.setString(4, service.getName());
        ps.setString(5, service.getVersion());
        ps.setInt(6, tenantId);
        ps.setString(7, service.getServiceUrl());
        ps.setString(8, service.getDefinitionType().name());
        ps.setString(9, service.getDefUrl());
        ps.setString(10, service.getDescription());
        ps.setString(11, service.getSecurityType().toString());
        ps.setBoolean(12, service.isMutualSSLEnabled());
        ps.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
        ps.setTimestamp(14, new Timestamp(System.currentTimeMillis()));
        ps.setString(15, username);
        ps.setString(16, username);
        ps.setBinaryStream(17, service.getEndpointDef());
        return uuid;
    }

    private ServiceEntry getServiceParams(ResultSet resultSet, boolean shrink) throws APIManagementException {
        ServiceEntry service = new ServiceEntry();
        try {
            service.setUuid(resultSet.getString(APIConstants.ServiceCatalogConstants.SERVICE_UUID));
            service.setName(resultSet.getString(APIConstants.ServiceCatalogConstants.SERVICE_NAME));
            service.setKey(resultSet.getString(APIConstants.ServiceCatalogConstants.SERVICE_KEY));
            service.setMd5(resultSet.getString(APIConstants.ServiceCatalogConstants.MD5));
            service.setVersion(resultSet.getString(APIConstants.ServiceCatalogConstants.SERVICE_VERSION));
            if (!shrink) {
                service.setServiceUrl(resultSet.getString(APIConstants.ServiceCatalogConstants.SERVICE_URL));
                service.setDefinitionType(ServiceEntry.DefinitionType.valueOf(resultSet.getString(APIConstants
                        .ServiceCatalogConstants.DEFINITION_TYPE)));
                service.setDefUrl(resultSet.getString(APIConstants.ServiceCatalogConstants.DEFINITION_URL));
                service.setDescription(resultSet.getString(APIConstants.ServiceCatalogConstants.DESCRIPTION));
                service.setSecurityType(ServiceEntry.SecurityType.valueOf(resultSet
                        .getString(APIConstants.ServiceCatalogConstants.SECURITY_TYPE)));
                service.setMutualSSLEnabled(resultSet.getBoolean(APIConstants.ServiceCatalogConstants
                        .MUTUAL_SSL_ENABLED));
                service.setCreatedTime(resultSet.getTimestamp(APIConstants.ServiceCatalogConstants
                        .CREATED_TIME));
                service.setLastUpdatedTime(resultSet.getTimestamp(APIConstants.ServiceCatalogConstants
                        .LAST_UPDATED_TIME));
                service.setCreatedBy(resultSet.getString(APIConstants.ServiceCatalogConstants.CREATED_BY));
                service.setUpdatedBy(resultSet.getString(APIConstants.ServiceCatalogConstants.UPDATED_BY));
                InputStream serviceDefinition = resultSet.getBinaryStream(APIConstants.ServiceCatalogConstants
                        .SERVICE_DEFINITION);
                ByteArrayOutputStream serviceDefinitionByteArray = new ByteArrayOutputStream();
                IOUtils.copy(serviceDefinition, serviceDefinitionByteArray);
                service.setEndpointDef(new ByteArrayInputStream(serviceDefinitionByteArray.toByteArray()));
            }
            return service;
        } catch (SQLException e) {
            handleException("Error while setting service parameters", e);
            return null;
        } catch (IOException e) {
            handleException("Error when retrieving the service definition", e);
            return null;
        }
    }
}
