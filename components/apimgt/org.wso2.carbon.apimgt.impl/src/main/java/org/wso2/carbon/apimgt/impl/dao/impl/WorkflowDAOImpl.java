package org.wso2.carbon.apimgt.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WorkflowDAOImpl implements WorkflowDAO {

    private static final Log log = LogFactory.getLog(WorkflowDAOImpl.class);
    private static WorkflowDAOImpl INSTANCE = new WorkflowDAOImpl();

    private WorkflowDAOImpl() {

    }

    public static WorkflowDAOImpl getInstance() {
        return INSTANCE;
    }

    private void handleExceptionWithCode(String msg, Throwable t, ErrorHandler code) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, code);
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    @Override
    public WorkflowDTO retrieveWorkflowFromInternalReference(String workflowReference, String workflowType)
            throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        WorkflowDTO workflowDTO = null;

        String query = SQLConstants.GET_ALL_WORKFLOW_ENTRY_FROM_INTERNAL_REF_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowReference);
            prepStmt.setString(2, workflowType);

            rs = prepStmt.executeQuery();
            while (rs.next()) {
                workflowDTO = WorkflowExecutorFactory.getInstance().createWorkflowDTO(rs.getString("WF_TYPE"));
                workflowDTO.setStatus(WorkflowStatus.valueOf(rs.getString("WF_STATUS")));
                workflowDTO.setExternalWorkflowReference(rs.getString("WF_EXTERNAL_REFERENCE"));
                workflowDTO.setCreatedTime(rs.getTimestamp("WF_CREATED_TIME").getTime());
                workflowDTO.setWorkflowReference(rs.getString("WF_REFERENCE"));
                workflowDTO.setTenantDomain(rs.getString("TENANT_DOMAIN"));
                workflowDTO.setTenantId(rs.getInt("TENANT_ID"));
                workflowDTO.setWorkflowDescription(rs.getString("WF_STATUS_DESC"));
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error while retrieving workflow details for " + workflowReference, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return workflowDTO;
    }

    @Override
    public String getExternalWorkflowReferenceForSubscription(int subscriptionId) throws APIManagementException {

        String workflowExtRef = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_EXTERNAL_WORKFLOW_FOR_SUBSCRIPTION_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            // setting subscriptionId as string to prevent error when db finds string type IDs for
            // ApplicationRegistration workflows
            ps.setString(1, String.valueOf(subscriptionId));
            ps.setString(2, WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
            rs = ps.executeQuery();

            // returns only one row
            while (rs.next()) {
                workflowExtRef = rs.getString("WF_EXTERNAL_REFERENCE");
            }
        } catch (SQLException e) {
            handleException("Error occurred while getting workflow entry for " +
                    "Subscription : " + subscriptionId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return workflowExtRef;
    }

}
