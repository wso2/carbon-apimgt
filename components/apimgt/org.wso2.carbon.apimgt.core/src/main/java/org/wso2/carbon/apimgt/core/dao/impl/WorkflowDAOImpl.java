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
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExecutorFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
     * @throws APIMgtDAOException if API Manager core level exception occurred
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
     * @throws APIMgtDAOException if API Manager core level exception occurred
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
    

    /**
     * Returns a workflow object for a given external workflow reference.
     *
     * @param workflowReference workflow related external reference id
     * @return Workflow The {@link Workflow} object 
     * @throws APIMgtDAOException if API Manager core level exception occurred
     */
    public Workflow retrieveWorkflow(String workflowReference) throws APIMgtDAOException {
        
        final String getworkflowQuery = "SELECT * FROM AM_WORKFLOWS WHERE WF_EXTERNAL_REFERENCE=?";
        Workflow workflow;
        try (Connection conn = DAOUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(getworkflowQuery)) {
            ps.setString(1, workflowReference);
            try (ResultSet rs = ps.executeQuery()) {
                workflow = this.createWorkflowFromResultSet(rs);
            }
        } catch (SQLException ex) {
            throw new APIMgtDAOException(ex);
        }
        return workflow;  
    }

    private Workflow createWorkflowFromResultSet(ResultSet rs) throws SQLException, APIMgtDAOException {
        Workflow workflow = null;

        if (rs.next()) {
            workflow = WorkflowExecutorFactory.getInstance().createWorkflow(rs.getString("WF_TYPE"));
            if (workflow != null) {
                workflow.setStatus(WorkflowStatus.valueOf(rs.getString("WF_STATUS")));
                workflow.setExternalWorkflowReference(rs.getString("WF_EXTERNAL_REFERENCE"));
                workflow.setWorkflowReference(rs.getString("WF_REFERENCE"));
                workflow.setCreatedTime(rs.getTimestamp("WF_CREATED_TIME").toLocalDateTime());
                workflow.setUpdatedTime(rs.getTimestamp("WF_UPDATED_TIME").toLocalDateTime());
                workflow.setWorkflowReference(rs.getString("WF_REFERENCE"));
                workflow.setWorkflowDescription(rs.getString("WF_STATUS_DESC"));
            } else {
                throw new APIMgtDAOException("Invalid workflow type");
            }
           

        }
        return workflow;
    }

}
