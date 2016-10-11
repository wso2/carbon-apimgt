/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.lifecycle.manager.sql.dao;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LCConfigBean;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LCStateBean;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.constants.Constants;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.constants.SQLConstants;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.exception.LCManagerDatabaseException;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.utils.LCMgtDBUtil;
import org.wso2.carbon.ndatasource.common.DataSourceException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class represent the DAO layer for lifecycle related operations.
 */
public class LCMgtDAO {

    private static final Log log = LogFactory.getLog(LCMgtDAO.class);

    private LCMgtDAO() {

    }

    /**
     * This is an inner class to hold the instance of the LCMgtDAO.
     * The reason for writing it like this is to guarantee that only one instance would be created.
     */
    private static class LCMgtDAOHolder {

        private static final LCMgtDAO INSTANCE = new LCMgtDAO();
    }

    /**
     * Method to get the instance of the LCMgtDAO.
     *
     * @return {@link LCMgtDAO} instance
     */
    public static LCMgtDAO getInstance() {
        return LCMgtDAOHolder.INSTANCE;
    }

    /**
     * Add lifecycle config for a specific tenant.
     *
     * @param lcConfigBean                  Contains information about name and content.
     * @param tenantId
     * @throws LCManagerDatabaseException   If failed to add lifecycle.
     */
    public void addLifecycle(LCConfigBean lcConfigBean, int tenantId) throws LCManagerDatabaseException{
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String query = SQLConstants.ADD_LIFECYCLE_SQL;
        try {
            connection = LCMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, lcConfigBean.getLCName());
            prepStmt.setBinaryStream(2, new ByteArrayInputStream(lcConfigBean.getLCContent().getBytes()));
            prepStmt.setInt(3, tenantId);
            prepStmt.execute();
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                log.error("Error while roll back operation for adding new lifecycle with name :"+ lcConfigBean
                        .getLCName(), e);
            }
            handleException("Error while adding the lifecycle", e);
        } finally {
            LCMgtDBUtil.closeAllConnections(prepStmt,connection,null);
        }
    }

    /**
     * Get lifecycle list of specific tenant.
     *
     * @param tenantId
     * @return                              Lifecycle list containing names;
     * @throws LCManagerDatabaseException   If failed to get lifecycle list.
     */
    public String[] getLifecycleList(int tenantId) throws LCManagerDatabaseException{
        List<String> lifecycleList = new ArrayList<>();
        Connection connection= null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String query = SQLConstants.GET_LIFECYCLE_LIST_SQL;
        try {
            connection = LCMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setInt(1, tenantId);
            rs = prepStmt.executeQuery();
            while (rs.next()){
                lifecycleList.add(rs.getString(Constants.LIFECYCLE_LIST));
            }

        } catch (SQLException e) {
            handleException("Error while adding the lifecycle", e);
        } finally {
            LCMgtDBUtil.closeAllConnections(prepStmt,connection,rs);
        }
        return lifecycleList.toArray(new String[0]);
    }

    /**
     * Get lifecycle configuration.
     *
     * @param lcName                        Name of the lifecycle
     * @param tenantId
     * @return                              Bean with content and name
     * @throws LCManagerDatabaseException   If failed to get lifecycle config.
     */
    public LCConfigBean getLifecycleConfig(String lcName , int tenantId) throws LCManagerDatabaseException{
        LCConfigBean lcConfigBean = new LCConfigBean();
        Connection connection= null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String query = SQLConstants.GET_LIFECYCLE_CONFIG_SQL;
        try {
            connection = LCMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1,lcName);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();
            while (rs.next()){
                lcConfigBean.setLCName(rs.getString(Constants.LIFECYCLE_NAME));
                InputStream rawInputStream = rs.getBinaryStream(Constants.LIFECYCLE_CONTENT);
                lcConfigBean.setLCContent(IOUtils.toString(rawInputStream));
            }

        } catch (SQLException e) {
            handleException("Error while adding the lifecycle", e);
        } catch (IOException e) {
            handleException("Error while converting lifecycle content stream to string", e);
        } finally {
            LCMgtDBUtil.closeAllConnections(prepStmt,connection,rs);
        }
        return lcConfigBean;
    }

    /**
     * Set initial lifecycle state.
     *
     * @param initialState                  Initial state provided in lifecycle config.
     * @param lcName                        Name of the lifecycle
     * @param tenantId
     * @return                              UUID generated by framework which is stored as reference by external
     *                                      systems.
     * @throws LCManagerDatabaseException   If failed to add initial lifecycle state.
     */
    public String addLifecycleState (String initialState, String lcName, int tenantId) throws LCManagerDatabaseException{
        Connection connection= null;
        PreparedStatement prepStmt1 = null;
        PreparedStatement prepStmt2 = null;
        ResultSet rs1 = null;
        String getLCIdQuery = SQLConstants.GET_LIFECYCLE_DEFINITION_ID_FROM_NAME_SQL;
        String addLCStateQuery = SQLConstants.ADD_LIFECYCLE_STATE_SQL;
        String uuid = null;
        int lcDefinitionId = -1;
        try {
            connection = LCMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt1 = connection.prepareStatement(getLCIdQuery);
            prepStmt1.setString(1,lcName);
            prepStmt1.setInt(2, tenantId);
            rs1 = prepStmt1.executeQuery();

            while (rs1.next()){
                lcDefinitionId = rs1.getInt(Constants.LIFECYCLE_DEFINITION_ID);
            }
            if(lcDefinitionId == -1){
                throw new LCManagerDatabaseException("There is no lifecycle configuration with name "+ lcName);
            }
            uuid = generateUUID();
            prepStmt2 = connection.prepareStatement(addLCStateQuery);
            prepStmt2.setString(1, uuid);
            prepStmt2.setInt(2, lcDefinitionId);
            prepStmt2.setString(3, initialState);
            prepStmt2.setInt(4,tenantId);
            prepStmt2.execute();
            connection.commit();

        } catch (SQLException e) {
            uuid = null;
            try {
                connection.rollback();
            } catch (SQLException e1) {
                log.error("Error while roll back operation for setting initial lifecycle state :" + initialState,
                        e);
            }
            handleException("Error while adding the lifecycle", e);
        } finally {
            LCMgtDBUtil.closeAllConnections(prepStmt1,null,rs1);
            LCMgtDBUtil.closeAllConnections(prepStmt2,connection,null);
        }
        return uuid;
    }

    /**
     * Change lifecycle state.
     *
     * @param lcStateBean                   Bean containing lifecycle id and required state.
     * @throws LCManagerDatabaseException   If failed to change lifecycle state.
     */
    public void changeLifecycleState (LCStateBean lcStateBean) throws LCManagerDatabaseException{
        Connection connection= null;
        PreparedStatement prepStmt = null;
        String updateLifecycleStateQuery = SQLConstants.UPDATE_LIFECYCLE_STATE_SQL;
        try {
            connection = LCMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            prepStmt = connection.prepareStatement(updateLifecycleStateQuery);
            prepStmt.setString(1,lcStateBean.getStatus());
            prepStmt.setString(2,lcStateBean.getStateId());
            prepStmt.setInt(3, lcStateBean.getTenantId());
            prepStmt.executeUpdate();
            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                log.error("Error while roll back operation for lifecycle state change :" + lcStateBean.getStatus(),
                        e);
            }
            handleException("Error while changing the lifecycle state to "+ lcStateBean.getStatus(), e);
        } finally {
            LCMgtDBUtil.closeAllConnections(prepStmt,connection,null);
        }
    }

    /**
     * Get lifecycle state data for a particular uuid.
     *
     * @param uuid                          Reference variable that maps lc data with external system.
     * @param tenantId
     * @return                              Life cycle state bean with all the required information
     * @throws LCManagerDatabaseException   If failed to get lifecycle state data.
     */
    public LCStateBean getLifecycleStateDataFromId (String uuid, int tenantId) throws LCManagerDatabaseException{
        LCStateBean lcStateBean = new LCStateBean();
        Connection connection= null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String getLifecycleNameFromIdQuery = SQLConstants.GET_LIFECYCLE_NAME_FROM_ID_SQL;
        try {
            connection = LCMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(getLifecycleNameFromIdQuery);
            prepStmt.setString(1,uuid);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();
            while (rs.next()){
                lcStateBean.setLcName(rs.getString(Constants.LIFECYCLE_NAME));
                lcStateBean.setStatus(rs.getString(Constants.LIFECYCLE_STATUS));
            }
            lcStateBean.setTenantId(tenantId);
            lcStateBean.setStateId(uuid);

        } catch (SQLException e) {
            handleException("Error while adding the lifecycle", e);
        } finally {
            LCMgtDBUtil.closeAllConnections(prepStmt,connection,rs);
        }
        return lcStateBean;
    }

    /**
     * Get all lifecycle configurations from lc database.
     *
     * @return                              List of beans with content and name
     * @throws LCManagerDatabaseException   If failed to get lifecycle config.
     */
    public LCConfigBean[] getAllLifecycleConfigs() throws LCManagerDatabaseException{
        List<LCConfigBean> lcConfigBeanList = new ArrayList<>();
        Connection connection= null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String query = SQLConstants.GET_ALL_LIFECYCLE_CONFIGS_SQL;
        try {
            connection = LCMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            rs = prepStmt.executeQuery();
            while (rs.next()){
                LCConfigBean lcConfigBean = new LCConfigBean();
                lcConfigBean.setLCName(rs.getString(Constants.LIFECYCLE_NAME));
                InputStream rawInputStream = rs.getBinaryStream(Constants.LIFECYCLE_CONTENT);
                lcConfigBean.setLCContent(IOUtils.toString(rawInputStream));
                lcConfigBean.setTenantId(rs.getInt(Constants.TENANT_ID));
                lcConfigBeanList.add(lcConfigBean);
            }

        } catch (SQLException e) {
            handleException("Error while getting the lifecycle list", e);
        } catch (IOException e) {
            handleException("Error while converting lifecycle content stream to string", e);
        } finally {
            LCMgtDBUtil.closeAllConnections(prepStmt,connection,rs);
        }
        return lcConfigBeanList.toArray(new LCConfigBean[0]);
    }

    /**
     * Check lifecycle already exist with same name.
     *
     * @param lcName                        Name of the lifecycle
     * @param tenantId
     * @return                              Bean with content and name
     * @throws LCManagerDatabaseException
     */
    public boolean checkLifecycleExist (String lcName, int tenantId) throws LCManagerDatabaseException {
        Connection connection= null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String query = SQLConstants.CHECK_LIFECYCLE_EXIST_SQL;
        try {
            connection = LCMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1,lcName);
            prepStmt.setInt(2,tenantId);
            rs = prepStmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            handleException("Error while adding the lifecycle", e);
        } finally {
            LCMgtDBUtil.closeAllConnections(prepStmt,connection,rs);
        }
        return false;
    }

    private void handleException(String msg, Throwable t) throws LCManagerDatabaseException {
        log.error(msg, t);
        throw new LCManagerDatabaseException(msg, t);
    }

    private String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
