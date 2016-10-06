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
        } catch (SQLException | DataSourceException e) {
            handleException("Error while adding the lifecycle", e);
        } finally {
            LCMgtDBUtil.closeAllConnections(prepStmt,connection,null);
        }
    }

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
                lifecycleList.add(rs.getString("LIFECYCLE_LIST"));
            }

        } catch (SQLException | DataSourceException e) {
            handleException("Error while adding the lifecycle", e);
        } finally {
            LCMgtDBUtil.closeAllConnections(prepStmt,connection,null);
        }
        return lifecycleList.toArray(new String[0]);
    }

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
                lcConfigBean.setLCName(rs.getString("LIFECYCLE_NAME"));
                InputStream rawInputStream = rs.getBinaryStream("LIFECYCLE_CONTENT");
                lcConfigBean.setLCContent(IOUtils.toString(rawInputStream));
            }

        } catch (SQLException | DataSourceException e) {
            handleException("Error while adding the lifecycle", e);
        } catch (IOException e) {
            handleException("Error while converting lifecycle content stream to string", e);
        } finally {
            LCMgtDBUtil.closeAllConnections(prepStmt,connection,null);
        }
        return lcConfigBean;
    }

    private void handleException(String msg, Throwable t) throws LCManagerDatabaseException {
        log.error(msg, t);
        throw new LCManagerDatabaseException(msg, t);
    }
}
