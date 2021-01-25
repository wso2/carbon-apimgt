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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class ServiceCatalogDAO {

    private static final Log log = LogFactory.getLog(ServiceCatalogDAO.class);
    private static ServiceCatalogDAO INSTANCE = null;
    private static boolean initialAutoCommit = false;

    /**
     * Method to get the instance of the ServiceCatalogDAO.
     *
     * @return {@link ApiMgtDAO} instance
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
     * Add a new serviceCatalog
     *
     * @param serviceEntry ServiceCatalogInfo
     * @param tenantID     ID of the owner's tenant
     * @param userName     Logged in user name
     * @return serviceCatalogId
     * throws APIManagementException if failed to create service catalog
     */
    public String addServiceCatalog(ServiceEntry serviceEntry, int tenantID, String uuid, String userName) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.ADD_SERVICE)) {
            connection.setAutoCommit(false);

            try {
                initialAutoCommit = connection.getAutoCommit();
                ps.setString(1, uuid);
                ps.setString(2, serviceEntry.getKey());
                ps.setString(3, serviceEntry.getMd5());
                ps.setString(4, serviceEntry.getName());
                ps.setString(5, serviceEntry.getDisplayName());
                ps.setString(6, serviceEntry.getVersion());
                ps.setInt(7, tenantID);
                ps.setString(8, serviceEntry.getServiceUrl());
                ps.setString(9, serviceEntry.getDefType());
                ps.setString(10, serviceEntry.getDefUrl());
                ps.setString(11, serviceEntry.getDescription());
                ps.setString(12, serviceEntry.getSecurityType());
                ps.setBoolean(13, serviceEntry.isMutualSSLEnabled());
                ps.setTimestamp(14, new Timestamp(System.currentTimeMillis()));
                ps.setTimestamp(15, new Timestamp(System.currentTimeMillis()));
                ps.setString(16, userName);
                ps.setString(17, userName);
                ps.setBinaryStream(18, serviceEntry.getEndpointDef());
                ps.setBinaryStream(19, serviceEntry.getMetadata());

                ps.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to rollback adding endpoint information", e);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, initialAutoCommit);
            }
        } catch (SQLException e) {
            handleException("Failed to add service catalog of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantID), e);
        }
        return uuid;
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
    public String updateServiceCatalog(ServiceEntry serviceEntry, int tenantID, String userName) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.UPDATE_SERVICE_BY_KEY)) {
            connection.setAutoCommit(false);

            try {
                initialAutoCommit = connection.getAutoCommit();
                ps.setString(1, serviceEntry.getMd5());
                ps.setString(2, serviceEntry.getName());
                ps.setString(3, serviceEntry.getDisplayName());
                ps.setString(4, serviceEntry.getVersion());
                ps.setInt(5, tenantID);
                ps.setString(6, serviceEntry.getServiceUrl());
                ps.setString(7, serviceEntry.getDefType());
                ps.setString(8, serviceEntry.getDefUrl());
                ps.setString(9, serviceEntry.getDescription());
                ps.setString(10, serviceEntry.getSecurityType());
                ps.setBoolean(11, serviceEntry.isMutualSSLEnabled());
                ps.setTimestamp(12, new Timestamp(System.currentTimeMillis()));
                ps.setString(13, userName);
                ps.setBinaryStream(14, serviceEntry.getEndpointDef());
                ps.setBinaryStream(15, serviceEntry.getMetadata());
                ps.setString(16, serviceEntry.getKey());
                ps.setInt(17, tenantID);

                ps.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleException("Failed to rollback updating endpoint information", e);
            } finally {
                APIMgtDBUtil.setAutoCommit(connection, initialAutoCommit);
            }
        } catch (SQLException e) {
            handleException("Failed to update service catalog of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantID), e);
        }
        return serviceEntry.getKey();
    }

    /**
     * Add a new end-point definition entry
     *
     * @param serviceEntry EndPoint related information
     * @return uuid
     * throws APIManagementException if failed to create service catalog
     */
    public String addEndPointDefinition(ServiceEntry serviceEntry, String uuid) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.ADD_ENDPOINT_RESOURCES)) {
            connection.setAutoCommit(false);

            try {
                initialAutoCommit = connection.getAutoCommit();
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

    public ServiceEntry getMd5Hash(ServiceEntry serviceInfo, int tenantId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.ServiceCatalogConstants.GET_SERVICE_MD5_BY_NAME_AND_VERSION;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, serviceInfo.getName());
            ps.setString(2, serviceInfo.getVersion());
            ps.setInt(3, tenantId);
            rs = ps.executeQuery();
            while (rs.next()) {
                serviceInfo.setMd5(rs.getString("MD5"));
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback getting md5 hash value", ex);
                }
            }
            handleException("Error while executing SQL for getting User MD5 hash : SQL " + sqlQuery, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return serviceInfo;
    }

    public String getMd5HashByKey(String key, int tenantId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String md5 = null;

        String sqlQuery = SQLConstants.ServiceCatalogConstants.GET_SERVICE_MD5_BY_SERVICE_KEY;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, key);
            ps.setInt(2, tenantId);
            rs = ps.executeQuery();
            while (rs.next()) {
                md5 = rs.getString("MD5");
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback getting md5 hash value by service key", ex);
                }
            }
            handleException("Error while executing SQL for getting User MD5 hash : SQL " + sqlQuery, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return md5;
    }

    public ServiceEntry getCatalogResourcesByKey(String key, int tenantId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.ServiceCatalogConstants.GET_ENDPOINT_RESOURCES_BY_KEY;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, key);
            ps.setInt(2, tenantId);
            rs = ps.executeQuery();
            if (rs.next()) {
                ServiceEntry serviceEntry = new ServiceEntry();
                serviceEntry.setUuid(rs.getString("UUID"));
                serviceEntry.setMetadata(rs.getBinaryStream("METADATA"));
                serviceEntry.setEndpointDef(rs.getBinaryStream("ENDPOINT_DEFINITION"));
                return serviceEntry;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback getting service resource by service key", ex);
                }
            }
            handleException("Error while executing SQL for getting catalog entry resources : SQL " + sqlQuery, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return null;
    }

    public ServiceEntry getServiceByKey(String key, int tenantId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.ServiceCatalogConstants.GET_SERVICE_BY_SERVICE_KEY;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, key);
            ps.setInt(2, tenantId);
            rs = ps.executeQuery();
            if (rs.next()) {
                ServiceEntry serviceEntry = new ServiceEntry();

                serviceEntry.setUuid(rs.getString("UUID"));
                serviceEntry.setKey(rs.getString("SERVICE_KEY"));
                serviceEntry.setMd5(rs.getString("MD5"));
                serviceEntry.setName(rs.getString("ENTRY_NAME"));
                serviceEntry.setDisplayName(rs.getString("DISPLAY_NAME"));
                serviceEntry.setVersion(rs.getString("ENTRY_VERSION"));
                serviceEntry.setServiceUrl(rs.getString("SERVICE_URL"));
                serviceEntry.setDescription(rs.getString("DESCRIPTION"));
                serviceEntry.setDefType(rs.getString("DEFINITION_TYPE"));
                serviceEntry.setDefUrl(rs.getString("DEFINITION_URL"));
                serviceEntry.setSecurityType(rs.getString("SECURITY_TYPE"));
                serviceEntry.setMutualSSLEnabled(rs.getBoolean("MUTUAL_SSL_ENABLED"));
                serviceEntry.setCreatedTime(rs.getTimestamp("CREATED_TIME"));
                serviceEntry.setLastUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME"));
                serviceEntry.setCreatedBy(rs.getString("CREATED_BY"));
                serviceEntry.setUpdatedBy(rs.getString("UPDATED_BY"));
                serviceEntry.setMetadata(rs.getBinaryStream("METADATA"));
                serviceEntry.setEndpointDef(rs.getBinaryStream("ENDPOINT_DEFINITION"));

                return serviceEntry;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback getting service by service key", ex);
                }
            }
            handleException("Error while executing SQL for getting User MD5 hash : SQL " + sqlQuery, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return null;
    }

    public ServiceEntry getCatalogResourcesByNameAndVersion(String name, String version, int tenantId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.ServiceCatalogConstants.GET_ENDPOINT_RESOURCES_BY_NAME_AND_VERSION;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, name);
            ps.setString(2, version);
            ps.setInt(3, tenantId);
            rs = ps.executeQuery();
            if (rs.next()) {
                ServiceEntry serviceEntry = new ServiceEntry();
                serviceEntry.setUuid(rs.getString("UUID"));
                serviceEntry.setMetadata(rs.getBinaryStream("METADATA"));
                serviceEntry.setEndpointDef(rs.getBinaryStream("ENDPOINT_DEFINITION"));
                return serviceEntry;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback getting service by service name and version", ex);
                }
            }
            handleException("Error while executing SQL for getting catalog entry resources : SQL " + sqlQuery, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return null;
    }
}
