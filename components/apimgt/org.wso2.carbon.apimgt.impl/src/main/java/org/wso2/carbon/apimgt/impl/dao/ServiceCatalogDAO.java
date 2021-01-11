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
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
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

        String uuid = UUID.randomUUID().toString();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.ADD_SERVICE)) {
            connection.setAutoCommit(false);

            ps.setString(1, uuid);
            ps.setString(2, serviceCatalogInfo.getMd5());
            ps.setString(3, serviceCatalogInfo.getName());
            ps.setString(4, serviceCatalogInfo.getDisplayName());
            ps.setString(5, serviceCatalogInfo.getVersion());
            ps.setInt(6, tenantID);
            ps.setString(7, serviceCatalogInfo.getServiceUrl());
            ps.setString(8, serviceCatalogInfo.getDefType());
            ps.setString(9, serviceCatalogInfo.getDefUrl());
            ps.setString(10, serviceCatalogInfo.getDescription());
            ps.setString(11, serviceCatalogInfo.getSecurityType());
            ps.setBoolean(12, serviceCatalogInfo.getIsMutualSSLEnabled());
            ps.setTimestamp(13, serviceCatalogInfo.getCreatedTime());
            ps.setTimestamp(14, serviceCatalogInfo.getLastUpdatedTime());
            ps.setString(15, serviceCatalogInfo.getCreatedBy());
            ps.setString(16, serviceCatalogInfo.getUpdatedBy());

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
    public String addEndPointDefinition(EndPointInfo endPointInfo) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement ps = connection
                     .prepareStatement(SQLConstants.ServiceCatalogConstants.ADD_ENDPOINT_DEFINITION_ENTRY)) {
            connection.setAutoCommit(false);

            ps.setString(1, endPointInfo.getUuid());
            ps.setBlob(2, endPointInfo.getEndPointDef());

            ps.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to add end point definition for service catalog entry ID "
                    + endPointInfo.getUuid(), e);
        }
        return endPointInfo.getUuid();
    }
}
