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

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.WorkflowUtils;
import org.wso2.carbon.apimgt.core.workflow.Workflow;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExecutorFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the WorkflowDAO interface. Uses SQL syntax that is common to H2 and MySQL DBs.
 * Hence is considered as the default due to its re-usability.
 */

public class WorkflowDAOImpl implements WorkflowDAO {
    
    private static final Logger log = LoggerFactory.getLogger(WorkflowDAO.class);
    /**
     * Persists WorkflowDTO to Database
     *
     * @param workflow The {@link Workflow} object to be added
     * @throws APIMgtDAOException if API Manager core level exception occurred
     */
    public void addWorkflowEntry(Workflow workflow) throws APIMgtDAOException {

        final String query = " INSERT INTO AM_WORKFLOWS (WF_REFERENCE,WF_TYPE,WF_STATUS,WF_CREATED_TIME,"
                + "WF_EXTERNAL_REFERENCE, WF_ATTRIBUTES, WF_STATUS_DESC) VALUES (?,?,?,?,?,?,?)";

        try (Connection connection = DAOUtil.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement prepStmt = connection.prepareStatement(query)) {
                prepStmt.setString(1, workflow.getWorkflowReference());
                prepStmt.setString(2, workflow.getWorkflowType());
                prepStmt.setString(3, workflow.getStatus().toString());
                prepStmt.setTimestamp(4, Timestamp.valueOf(workflow.getCreatedTime()));
                prepStmt.setString(5, workflow.getExternalWorkflowReference());
                prepStmt.setString(6, WorkflowUtils.mapTojsonString(workflow.getAttributes()));
                prepStmt.setString(7, workflow.getWorkflowDescription());

                prepStmt.execute();
                connection.commit();
            } catch (SQLException ex) {
                log.error("Error while executing sql query", ex);
                connection.rollback();
                throw new APIMgtDAOException(ex);           
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException ex) {
            log.error("Error while executing sql query", ex);
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
                log.error("Error while executing sql query", ex);
                connection.rollback();
                throw new APIMgtDAOException(ex);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException ex) {
            log.error("Error while executing sql query", ex);
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
        Workflow workflow = null;
        try (Connection conn = DAOUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(getworkflowQuery)) {
            ps.setString(1, workflowReference);
            try (ResultSet rs = ps.executeQuery()) {
                List<Workflow> list = this.createWorkflowFromResultSet(rs);
                if (!list.isEmpty()) {
                    // workflowReference is a unique value.
                    //there is only one entry for that value. Hence get the first      
                    workflow = list.get(0);
                } else {
                    // not found
                    String msg = "Workflow not found for : " + workflowReference;
                    log.warn(msg);
                    throw new APIMgtDAOException(msg, ExceptionCodes.WORKFLOW_NOT_FOUND);
                }

            } catch (ParseException e) {
                log.error("Error while parsing json string", e);
                throw new APIMgtDAOException(e);
            }
        } catch (SQLException ex) {
            log.error("Error while executing sql query", ex);
            throw new APIMgtDAOException(ex);
        }
        return workflow;  
    }

    private List<Workflow> createWorkflowFromResultSet(ResultSet rs)
            throws SQLException, APIMgtDAOException, ParseException {
        Workflow workflow = null;
        List<Workflow> workflowList = new ArrayList<>();
        while (rs.next()) {
            workflow = WorkflowExecutorFactory.getInstance().createWorkflow(rs.getString("WF_TYPE"));
            if (workflow != null) {
                workflow.setStatus(WorkflowStatus.valueOf(rs.getString("WF_STATUS")));
                workflow.setExternalWorkflowReference(rs.getString("WF_EXTERNAL_REFERENCE"));
                workflow.setWorkflowReference(rs.getString("WF_REFERENCE"));
                workflow.setCreatedTime(rs.getTimestamp("WF_CREATED_TIME").toLocalDateTime());
                workflow.setUpdatedTime(rs.getTimestamp("WF_UPDATED_TIME").toLocalDateTime());
                workflow.setWorkflowReference(rs.getString("WF_REFERENCE"));
                workflow.setWorkflowDescription(rs.getString("WF_STATUS_DESC"));
                workflow.setAttributes(WorkflowUtils.jsonStringToMap(rs.getString("WF_ATTRIBUTES")));
                workflowList.add(workflow);
            } else {
                throw new APIMgtDAOException("Invalid workflow type");
            }

        }
        return workflowList;
    }

    /**
     * @see WorkflowDAO#getExternalWorkflowReferenceForPendingTask(String, String)
     */
    @Override
    public String getExternalWorkflowReferenceForPendingTask(String wfReference, String workflowType)
            throws APIMgtDAOException {
        final String getworkflowQuery = "SELECT WF_EXTERNAL_REFERENCE FROM AM_WORKFLOWS WHERE "
                + "WF_REFERENCE=? AND WF_TYPE=? AND WF_STATUS=?";
 
        String externalReferenceId = null;
        try (Connection conn = DAOUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(getworkflowQuery)) {
            ps.setString(1, wfReference);
            ps.setString(2, workflowType);
            ps.setString(3, WorkflowStatus.CREATED.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    externalReferenceId = rs.getString("WF_EXTERNAL_REFERENCE");         
                }
            } 
        } catch (SQLException ex) {
            log.error("Error while executing sql query", ex);
            throw new APIMgtDAOException(ex);
        }
        return externalReferenceId;  
    }
    
    /**
     * @see WorkflowDAO#deleteWorkflowEntryforExternalReference(String)
     */
    @Override
    public void deleteWorkflowEntryforExternalReference(String externalReferenceId) throws APIMgtDAOException {
        final String query = "DELETE FROM AM_WORKFLOWS WHERE WF_EXTERNAL_REFERENCE = ?";
        try (Connection connection = DAOUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try {
                connection.setAutoCommit(false);
                statement.setString(1, externalReferenceId);
                statement.execute();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error while executing sql query", e);
                throw new APIMgtDAOException(e);
            } finally {
                connection.setAutoCommit(DAOUtil.isAutoCommit());
            }
        } catch (SQLException e) {
            log.error("Error while executing sql query", e);
            throw new APIMgtDAOException(e);
        }
    }

    @Override
    public List<Workflow> retrieveUncompleteWorkflows(String type) throws APIMgtDAOException {
        final String getworkflowQuery = "SELECT * FROM AM_WORKFLOWS WHERE WF_TYPE = ? AND WF_STATUS=?";
        List<Workflow> workflow;
        try (Connection conn = DAOUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(getworkflowQuery)) {
            ps.setString(1, type);
            ps.setString(2, WorkflowStatus.CREATED.toString());
            try (ResultSet rs = ps.executeQuery()) {
                workflow = this.createWorkflowFromResultSet(rs);
            } catch (ParseException e) {
                log.error("Error while parsing json string", e);
                throw new APIMgtDAOException(e);
            }
        } catch (SQLException ex) {
            log.error("Error while executing sql query", ex);
            throw new APIMgtDAOException(ex);
        }
        return workflow;  
    }

    @Override
    public List<Workflow> retrieveUncompleteWorkflows() throws APIMgtDAOException {
        final String getworkflowQuery = "SELECT * FROM AM_WORKFLOWS WHERE WF_STATUS=?";
        List<Workflow> workflow;
        try (Connection conn = DAOUtil.getConnection();
                PreparedStatement ps = conn.prepareStatement(getworkflowQuery)) {  
            ps.setString(1, WorkflowStatus.CREATED.toString());
            try (ResultSet rs = ps.executeQuery()) {
                workflow = this.createWorkflowFromResultSet(rs);
            } catch (ParseException e) {
                log.error("Error while parsing json string", e);
                throw new APIMgtDAOException(e);
            }
        } catch (SQLException ex) {
            log.error("Error while executing sql query", ex);
            throw new APIMgtDAOException(ex);
        }
        return workflow;  
    }   
}
