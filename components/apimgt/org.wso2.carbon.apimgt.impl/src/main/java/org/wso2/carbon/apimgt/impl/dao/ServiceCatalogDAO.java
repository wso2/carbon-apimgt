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
import org.wso2.carbon.apimgt.api.model.ServiceCatalogInfo;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.sql.*;

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
     * @param serviceCatalogInfo ServiceCatalogInfo
     * @param tenantID           ID of the owner's tenant
     * @return serviceCatalogId
     * throws APIManagementException if failed to create service catalog
     */
    public String addServiceCatalog(ServiceCatalogInfo serviceCatalogInfo, int tenantID, String uuid) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.ADD_SERVICE)) {
            connection.setAutoCommit(false);

            try {
                initialAutoCommit = connection.getAutoCommit();
                ps.setString(1, uuid);
                ps.setString(2, serviceCatalogInfo.getKey());
                ps.setString(3, serviceCatalogInfo.getMd5());
                ps.setString(4, serviceCatalogInfo.getName());
                ps.setString(5, serviceCatalogInfo.getDisplayName());
                ps.setString(6, serviceCatalogInfo.getVersion());
                ps.setInt(7, tenantID);
                ps.setString(8, serviceCatalogInfo.getServiceUrl());
                ps.setString(9, serviceCatalogInfo.getDefType());
                ps.setString(10, serviceCatalogInfo.getDefUrl());
                ps.setString(11, serviceCatalogInfo.getDescription());
                ps.setString(12, serviceCatalogInfo.getSecurityType());
                ps.setBoolean(13, serviceCatalogInfo.isMutualSSLEnabled());
                ps.setTimestamp(14, new Timestamp(System.currentTimeMillis()));
                ps.setTimestamp(15, new Timestamp(System.currentTimeMillis()));
                ps.setString(16, serviceCatalogInfo.getCreatedBy());
                ps.setString(17, serviceCatalogInfo.getUpdatedBy());
                ps.setBinaryStream(18, serviceCatalogInfo.getEndpointDef());
                ps.setBinaryStream(19, serviceCatalogInfo.getMetadata());

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
     * @param serviceCatalogInfo ServiceCatalogInfo
     * @param tenantID           ID of the owner's tenant
     * @return serviceCatalogId
     * throws APIManagementException if failed to create service catalog
     */
    public String updateServiceCatalog(ServiceCatalogInfo serviceCatalogInfo, int tenantID) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.UPDATE_SERVICE_BY_KEY)) {
            connection.setAutoCommit(false);

            try {
                initialAutoCommit = connection.getAutoCommit();
                ps.setString(1, serviceCatalogInfo.getMd5());
                ps.setString(2, serviceCatalogInfo.getName());
                ps.setString(3, serviceCatalogInfo.getDisplayName());
                ps.setString(4, serviceCatalogInfo.getVersion());
                ps.setInt(5, tenantID);
                ps.setString(6, serviceCatalogInfo.getServiceUrl());
                ps.setString(7, serviceCatalogInfo.getDefType());
                ps.setString(8, serviceCatalogInfo.getDefUrl());
                ps.setString(9, serviceCatalogInfo.getDescription());
                ps.setString(10, serviceCatalogInfo.getSecurityType());
                ps.setBoolean(11, serviceCatalogInfo.isMutualSSLEnabled());
                ps.setTimestamp(12, serviceCatalogInfo.getCreatedTime());
                ps.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
                ps.setString(14, serviceCatalogInfo.getCreatedBy());
                ps.setString(15, serviceCatalogInfo.getUpdatedBy());
                ps.setBinaryStream(16, serviceCatalogInfo.getEndpointDef());
                ps.setBinaryStream(17, serviceCatalogInfo.getMetadata());
                ps.setString(18, serviceCatalogInfo.getKey());
                ps.setInt(19, tenantID);

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
        return serviceCatalogInfo.getKey();
    }

    /**
     * Add a new end-point definition entry
     *
     * @param serviceCatalogInfo EndPoint related information
     * @return uuid
     * throws APIManagementException if failed to create service catalog
     */
    public String addEndPointDefinition(ServiceCatalogInfo serviceCatalogInfo, String uuid) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.ADD_ENDPOINT_RESOURCES)) {
            connection.setAutoCommit(false);

            try {
                initialAutoCommit = connection.getAutoCommit();
                ps.setString(1, uuid);
                ps.setBinaryStream(2, serviceCatalogInfo.getEndpointDef());
                ps.setBinaryStream(3, serviceCatalogInfo.getMetadata());

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

    public ServiceCatalogInfo getMd5Hash(ServiceCatalogInfo serviceInfo, int tenantId) throws APIManagementException {
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

    public ServiceCatalogInfo getCatalogResourcesByKey(String key, int tenantId) throws APIManagementException {
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
                ServiceCatalogInfo serviceCatalogInfo = new ServiceCatalogInfo();
                serviceCatalogInfo.setUuid(rs.getString("UUID"));
                serviceCatalogInfo.setMetadata(rs.getBinaryStream("METADATA"));
                serviceCatalogInfo.setEndpointDef(rs.getBinaryStream("ENDPOINT_DEFINITION"));
                return serviceCatalogInfo;
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

    public ServiceCatalogInfo getServiceByKey(String key, int tenantId) throws APIManagementException {
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
                ServiceCatalogInfo serviceCatalogInfo = new ServiceCatalogInfo();

                serviceCatalogInfo.setUuid(rs.getString("UUID"));
                serviceCatalogInfo.setKey(rs.getString("SERVICE_KEY"));
                serviceCatalogInfo.setMd5(rs.getString("MD5"));
                serviceCatalogInfo.setName(rs.getString("ENTRY_NAME"));
                serviceCatalogInfo.setDisplayName(rs.getString("DISPLAY_NAME"));
                serviceCatalogInfo.setVersion(rs.getString("ENTRY_VERSION"));
                serviceCatalogInfo.setServiceUrl(rs.getString("SERVICE_URL"));
                serviceCatalogInfo.setDescription(rs.getString("DESCRIPTION"));
                serviceCatalogInfo.setDefType(rs.getString("DEFINITION_TYPE"));
                serviceCatalogInfo.setDefUrl(rs.getString("DEFINITION_URL"));
                serviceCatalogInfo.setSecurityType(rs.getString("SECURITY_TYPE"));
                serviceCatalogInfo.setMutualSSLEnabled(rs.getBoolean("MUTUAL_SSL_ENABLED"));
                serviceCatalogInfo.setCreatedTime(rs.getTimestamp("CREATED_TIME"));
                serviceCatalogInfo.setLastUpdatedTime(rs.getTimestamp("LAST_UPDATED_TIME"));
                serviceCatalogInfo.setCreatedBy(rs.getString("CREATED_BY"));
                serviceCatalogInfo.setUpdatedBy(rs.getString("UPDATED_BY"));
                serviceCatalogInfo.setMetadata(rs.getBinaryStream("METADATA"));
                serviceCatalogInfo.setEndpointDef(rs.getBinaryStream("ENDPOINT_DEFINITION"));

                return serviceCatalogInfo;
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

    public ServiceCatalogInfo getCatalogResourcesByNameAndVersion(String name, String version, int tenantId) throws APIManagementException {
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
                ServiceCatalogInfo serviceCatalogInfo = new ServiceCatalogInfo();
                serviceCatalogInfo.setUuid(rs.getString("UUID"));
                serviceCatalogInfo.setMetadata(rs.getBinaryStream("METADATA"));
                serviceCatalogInfo.setEndpointDef(rs.getBinaryStream("ENDPOINT_DEFINITION"));
                return serviceCatalogInfo;
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
