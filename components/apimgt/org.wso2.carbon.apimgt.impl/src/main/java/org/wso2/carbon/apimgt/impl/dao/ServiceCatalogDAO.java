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
import org.wso2.carbon.apimgt.api.model.EndPointInfo;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogInfo;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServiceCatalogDAO {

    private static final Log log = LogFactory.getLog(ServiceCatalogDAO.class);
    private static ServiceCatalogDAO INSTANCE = null;

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
    public String addServiceCatalog(ServiceCatalogInfo serviceCatalogInfo, int tenantID) throws APIManagementException {


        // Check createApplicationRegistrationEntry in ApiMgtDAO

        String uuid = UUID.randomUUID().toString();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.ADD_SERVICE)) {
            connection.setAutoCommit(false);

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
            ps.setTimestamp(14, serviceCatalogInfo.getCreatedTime());
            ps.setTimestamp(15, serviceCatalogInfo.getLastUpdatedTime());
            ps.setString(16, serviceCatalogInfo.getCreatedBy());
            ps.setString(17, serviceCatalogInfo.getUpdatedBy());

            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to add service catalog of tenant "
                    + APIUtil.getTenantDomainFromTenantId(tenantID), e);
        }
        return uuid;
    }

    /**
     * Add a new end-point definition entry
     *
     * @param endPointInfo EndPoint related information
     * @return uuid
     * throws APIManagementException if failed to create service catalog
     */
    public String addEndPointDefinition(EndPointInfo endPointInfo, String uuid) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.ADD_ENDPOINT_DEFINITION_ENTRY)) {
            connection.setAutoCommit(false);

            ps.setString(1, uuid);
            ps.setBlob(2, endPointInfo.getEndPointDef());
            ps.setBlob(3, endPointInfo.getMetadata());

            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to add end point definition for service catalog entry ID "
                    + endPointInfo.getUuid(), e);
        }
        return endPointInfo.getUuid();
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
            handleException("Error while executing SQL for getting User MD5 hash : SQL " + sqlQuery, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return md5;
    }

    public EndPointInfo getCatalogResourcesByKey(String key, int tenantId) throws APIManagementException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.ServiceCatalogConstants.GET_ENDPOINT_DEFINITION_ENTRY_BY_KEY;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, key);
            ps.setInt(2, tenantId);
            rs = ps.executeQuery();
            if (rs.next()) {
                EndPointInfo endPointInfo = new EndPointInfo();
                endPointInfo.setUuid(rs.getString("UUID"));
                endPointInfo.setMetadata(rs.getBlob("METADATA").getBinaryStream());
                endPointInfo.setEndPointDef(rs.getBlob("ENDPOINT_DEFINITION").getBinaryStream());
                return endPointInfo;
            }
        } catch (SQLException e) {
            handleException("Error while executing SQL for getting catalog entry resources : SQL " + sqlQuery, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return null;
    }
}
