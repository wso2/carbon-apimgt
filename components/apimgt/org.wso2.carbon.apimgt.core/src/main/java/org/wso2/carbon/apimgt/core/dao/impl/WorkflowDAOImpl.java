/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.core.dao.impl;

import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Workflow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Default implementation of the WorkflowDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */

public class WorkflowDAOImpl implements WorkflowDAO {
    
    /**
     * Persists WorkflowDTO to Database
     *
     * @param workflow The {@link Workflow} object to be added
     * @throws APIMgtDAOException
     */
    public void addWorkflowEntry(Workflow workflow) throws APIMgtDAOException {

        final String query = " INSERT INTO AM_WORKFLOWS (WF_REFERENCE,WF_TYPE,WF_STATUS,WF_CREATED_TIME,"
                + "WF_EXTERNAL_REFERENCE) VALUES (?,?,?,?,?)";

        try (Connection connection = DAOUtil.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement prepStmt = connection.prepareStatement(query)) {
                prepStmt.setString(1, workflow.getWorkflowReference());
                prepStmt.setString(2, workflow.getWorkflowType());
                prepStmt.setString(3, workflow.getStatus().toString());
                prepStmt.setTimestamp(4, Timestamp.valueOf(workflow.getCreatedTime()));
                prepStmt.setString(5, workflow.getExternalWorkflowReference());

                prepStmt.execute();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
    }

    /**
     * Update workflow
     * 
     * @param workflow The {@link Workflow} object to be added
     * @throws APIMgtDAOException
     */

    public void updateWorkflowStatus(Workflow workflow) throws APIMgtDAOException {

        final String query = "UPDATE AM_WORKFLOWS SET WF_STATUS = ?, WF_STATUS_DESC = ?, "
                + "WF_UPDATED_TIME = ? WHERE WF_EXTERNAL_REFERENCE = ?";

        try (Connection connection = DAOUtil.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement prepStmt = connection.prepareStatement(query)) {
                prepStmt.setString(1, workflow.getStatus().toString());
                prepStmt.setString(2, workflow.getWorkflowDescription());
                prepStmt.setTimestamp(3, Timestamp.valueOf(workflow.getUpdatedTime()));
                prepStmt.setString(4, workflow.getExternalWorkflowReference());

                prepStmt.execute();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
    }
}
