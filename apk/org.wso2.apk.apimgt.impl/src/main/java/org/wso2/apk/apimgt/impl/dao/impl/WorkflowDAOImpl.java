package org.wso2.apk.apimgt.impl.dao.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.api.model.APIProductIdentifier;
import org.wso2.apk.apimgt.api.model.Identifier;
import org.wso2.apk.apimgt.api.model.Workflow;
import org.wso2.apk.apimgt.impl.dao.WorkflowDAO;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.apk.apimgt.impl.dto.WorkflowDTO;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.apk.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.apk.apimgt.impl.workflow.WorkflowStatus;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

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
                //TODO: APK
//                workflowDTO = WorkflowExecutorFactory.getInstance().createWorkflowDTO(rs.getString("WF_TYPE"));
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

    @Override
    public Workflow[] getWorkflows(String workflowType, String status, String tenantDomain) throws APIManagementException {

        ResultSet rs = null;
        Workflow[] workflows = null;
        String sqlQuery;
        if (workflowType != null) {
            sqlQuery = SQLConstants.GET_ALL_WORKFLOW_DETAILS_BY_WORKFLOW_TYPE;
        } else {
            sqlQuery = SQLConstants.GET_ALL_WORKFLOW_DETAILS;
        }
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            try {
                if (workflowType != null) {
                    prepStmt.setString(1, workflowType);
                    prepStmt.setString(2, status);
                    prepStmt.setString(3, tenantDomain);
                } else {
                    prepStmt.setString(1, status);
                    prepStmt.setString(2, tenantDomain);
                }
                rs = prepStmt.executeQuery();

                ArrayList<Workflow> workflowsList = new ArrayList<Workflow>();
                Workflow workflow;
                while (rs.next()) {
                    workflow = new Workflow();
                    workflow.setWorkflowId(rs.getInt("WF_ID"));
                    workflow.setWorkflowReference(rs.getString("WF_REFERENCE"));
                    workflow.setWorkflowType(rs.getString("WF_TYPE"));
                    String workflowstatus = rs.getString("WF_STATUS");
                    workflow.setStatus(org.wso2.apk.apimgt.api.WorkflowStatus.valueOf(workflowstatus));
                    workflow.setCreatedTime(rs.getTimestamp("WF_CREATED_TIME").toString());
                    workflow.setUpdatedTime(rs.getTimestamp("WF_UPDATED_TIME").toString());
                    workflow.setWorkflowStatusDesc(rs.getString("WF_STATUS_DESC"));
                    workflow.setTenantId(rs.getInt("TENANT_ID"));
                    workflow.setTenantDomain(rs.getString("TENANT_DOMAIN"));
                    workflow.setExternalWorkflowReference(rs.getString("WF_EXTERNAL_REFERENCE"));
                    workflow.setWorkflowDescription(rs.getString("WF_STATUS_DESC"));
                    InputStream metadataBlob = rs.getBinaryStream("WF_METADATA");
                    InputStream propertiesBlob = rs.getBinaryStream("WF_PROPERTIES");

                    if (metadataBlob != null) {
                        String metadata = APIMgtDBUtil.getStringFromInputStream(metadataBlob);
                        Gson metadataGson = new Gson();
                        JSONObject metadataJson = metadataGson.fromJson(metadata, JSONObject.class);
                        workflow.setMetadata(metadataJson);
                    } else {
                        JSONObject metadataJson = new JSONObject();
                        workflow.setMetadata(metadataJson);
                    }

                    if (propertiesBlob != null) {
                        String properties = APIMgtDBUtil.getStringFromInputStream(propertiesBlob);
                        Gson propertiesGson = new Gson();
                        JSONObject propertiesJson = propertiesGson.fromJson(properties, JSONObject.class);
                        workflow.setProperties(propertiesJson);
                    } else {
                        JSONObject propertiesJson = new JSONObject();
                        workflow.setProperties(propertiesJson);
                    }
                    workflowsList.add(workflow);
                }
                workflows = workflowsList.toArray(new Workflow[workflowsList.size()]);
            } catch (SQLException e) {
                handleExceptionWithCode("Error when retrieve all the workflow details. ", e,
                        ExceptionCodes.APIMGT_DAO_EXCEPTION);
            } finally {
                APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error when retrieve all the workflow details. ", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return workflows;
    }

    @Override
    public Workflow getWorkflowReferenceByExternalWorkflowReferenceID(String externalWorkflowRef, String status,
                                                                      String tenantDomain) throws APIManagementException {

        ResultSet rs = null;
        Workflow workflow = new Workflow();
        String sqlQuery = SQLConstants.GET_ALL_WORKFLOW_DETAILS_BY_EXTERNAL_WORKFLOW_REFERENCE;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(sqlQuery)) {
            try {
                prepStmt.setString(1, externalWorkflowRef);
                prepStmt.setString(2, status);
                prepStmt.setString(3, tenantDomain);
                rs = prepStmt.executeQuery();

                while (rs.next()) {
                    workflow.setWorkflowId(rs.getInt("WF_ID"));
                    workflow.setWorkflowReference(rs.getString("WF_REFERENCE"));
                    workflow.setWorkflowType(rs.getString("WF_TYPE"));
                    String workflowstatus = rs.getString("WF_STATUS");
                    workflow.setStatus(org.wso2.apk.apimgt.api.WorkflowStatus.valueOf(workflowstatus));
                    workflow.setCreatedTime(rs.getTimestamp("WF_CREATED_TIME").toString());
                    workflow.setUpdatedTime(rs.getTimestamp("WF_UPDATED_TIME").toString());
                    workflow.setWorkflowDescription(rs.getString("WF_STATUS_DESC"));
                    workflow.setTenantId(rs.getInt("TENANT_ID"));
                    workflow.setTenantDomain(rs.getString("TENANT_DOMAIN"));
                    workflow.setExternalWorkflowReference(rs.getString("WF_EXTERNAL_REFERENCE"));
                    InputStream targetStream = rs.getBinaryStream("WF_METADATA");
                    InputStream propertiesTargetStream = rs.getBinaryStream("WF_PROPERTIES");

                    if (targetStream != null) {
                        String metadata = APIMgtDBUtil.getStringFromInputStream(targetStream);
                        Gson metadataGson = new Gson();
                        JSONObject metadataJson = metadataGson.fromJson(metadata, JSONObject.class);
                        workflow.setMetadata(metadataJson);
                    } else {
                        JSONObject metadataJson = new JSONObject();
                        workflow.setMetadata(metadataJson);
                    }

                    if (propertiesTargetStream != null) {
                        String properties = APIMgtDBUtil.getStringFromInputStream(propertiesTargetStream);
                        Gson propertiesGson = new Gson();
                        JSONObject propertiesJson = propertiesGson.fromJson(properties, JSONObject.class);
                        workflow.setProperties(propertiesJson);
                    } else {
                        JSONObject propertiesJson = new JSONObject();
                        workflow.setProperties(propertiesJson);
                    }
                }
            } catch (SQLException e) {
                handleExceptionWithCode("Error when retriving the workflow details. ", e,
                        ExceptionCodes.APIMGT_DAO_EXCEPTION);
            } finally {
                APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error when retriving the workflow details. ", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return workflow;
    }

    @Override
    public String getExternalWorkflowReferenceForSubscription(Identifier identifier, int appID, String organization)
            throws APIManagementException {

        String workflowExtRef = null;
        int id = -1;
        int subscriptionID = -1;

        String sqlQuery = SQLConstants.GET_EXTERNAL_WORKFLOW_REFERENCE_FOR_SUBSCRIPTION_SQL;
        String postgreSQL = SQLConstants.GET_EXTERNAL_WORKFLOW_REFERENCE_FOR_SUBSCRIPTION_POSTGRE_SQL;
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            if (identifier instanceof APIIdentifier) {
                String apiUuid;
                if (identifier.getUUID() != null) {
                    apiUuid = identifier.getUUID();
                } else {
                    apiUuid = getUUIDFromIdentifier((APIIdentifier) identifier, organization);
                }
                id = getAPIID(apiUuid, conn);

            } else if (identifier instanceof APIProductIdentifier) {
                id = ((APIProductIdentifier) identifier).getProductId();
            }
            if (conn.getMetaData().getDriverName().contains("PostgreSQL")) {
                sqlQuery = postgreSQL;
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                ps.setInt(1, id);
                ps.setInt(2, appID);
                ps.setString(3, WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
                try (ResultSet rs = ps.executeQuery()) {
                    // returns only one row
                    if (rs.next()) {
                        workflowExtRef = rs.getString("WF_EXTERNAL_REFERENCE");
                    }
                }

            }
        } catch (SQLException e) {
            handleException("Error occurred while getting workflow entry for " +
                    "Subscription : " + subscriptionID, e);
        }
        return workflowExtRef;
    }

    /**
     * Get API UUID by the API Identifier.
     *
     * @param identifier API Identifier
     * @param organization identifier of the organization
     * @return String UUID
     * @throws APIManagementException if an error occurs
     */
    private String getUUIDFromIdentifier(APIIdentifier identifier, String organization) throws APIManagementException {

        String uuid = null;
        String sql = SQLConstants.GET_UUID_BY_IDENTIFIER_AND_ORGANIZATION_SQL;
        try (Connection connection = APIMgtDBUtil.getConnection();
                PreparedStatement prepStmt = connection.prepareStatement(sql)) {
            prepStmt.setString(1, identifier.getApiName());
            prepStmt.setString(2, identifier.getVersion());
            prepStmt.setString(3, organization);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    uuid = resultSet.getString(1);
                }
            }
        } catch (SQLException e) {
            handleException(
                    "Failed to get the UUID for API : " + identifier.getApiName() + '-' + identifier.getVersion(), e);
        }
        return uuid;
    }

    private int getAPIID(String uuid, Connection connection) throws APIManagementException, SQLException {

        int id = -1;
        String getAPIQuery = SQLConstants.GET_API_ID_SQL_BY_UUID;

        try (PreparedStatement prepStmt = connection.prepareStatement(getAPIQuery)) {
            prepStmt.setString(1, uuid);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("API_ID");
                }
                if (id == -1) {
                    String msg = "Unable to find the API with UUID : " + uuid + " in the database";
                    log.error(msg);
                    throw new APIManagementException(msg, ExceptionCodes.API_NOT_FOUND);
                }
            }
        }
        return id;
    }

    @Override
    public WorkflowDTO retrieveWorkflow(String workflowReference) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        WorkflowDTO workflowDTO = null;

        String query = SQLConstants.GET_ALL_WORKFLOW_ENTRY_SQL;
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, workflowReference);

            rs = prepStmt.executeQuery();
            while (rs.next()) {
                //TODO: APK
//                workflowDTO = WorkflowExecutorFactory.getInstance().createWorkflowDTO(rs.getString("WF_TYPE"));
                workflowDTO.setStatus(WorkflowStatus.valueOf(rs.getString("WF_STATUS")));
                workflowDTO.setExternalWorkflowReference(rs.getString("WF_EXTERNAL_REFERENCE"));
                workflowDTO.setCreatedTime(rs.getTimestamp("WF_CREATED_TIME").getTime());
                workflowDTO.setWorkflowReference(rs.getString("WF_REFERENCE"));
                workflowDTO.setTenantDomain(rs.getString("TENANT_DOMAIN"));
                workflowDTO.setTenantId(rs.getInt("TENANT_ID"));
                workflowDTO.setWorkflowDescription(rs.getString("WF_STATUS_DESC"));
                InputStream metadataBlob = rs.getBinaryStream("WF_METADATA");

                if (metadataBlob != null) {
                    String metadata = APIMgtDBUtil.getStringFromInputStream(metadataBlob);
                    Gson metadataGson = new Gson();
                    JSONObject metadataJson = metadataGson.fromJson(metadata, JSONObject.class);
                    workflowDTO.setMetadata(metadataJson);
                } else {
                    JSONObject metadataJson = new JSONObject();
                    workflowDTO.setMetadata(metadataJson);
                }
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
    public String getExternalWorkflowRefByInternalRefWorkflowType(int internalRef, String workflowType) throws APIManagementException {

        String workflowExtRef = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String sqlQuery = SQLConstants.GET_EXTERNAL_WORKFLOW_REFERENCE_SQL;
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, workflowType);
            ps.setString(2, String.valueOf(internalRef));
            rs = ps.executeQuery();

            // returns only one row
            while (rs.next()) {
                workflowExtRef = rs.getString("WF_EXTERNAL_REFERENCE");
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Error occurred while getting workflow entry for " +
                    "Internal Ref : " + internalRef, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }

        return workflowExtRef;
    }

    @Override
    public String getExternalWorkflowReferenceForSubscriptionAndWFType(int subscriptionId, String wfType) throws APIManagementException {

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
            ps.setString(2, wfType);
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

    @Override
    public String getRegistrationWFReference(int applicationId, String keyType, String keyManagerName)
            throws APIManagementException {

        String reference = null;

        String sqlQuery = SQLConstants.GET_REGISTRATION_WORKFLOW_SQL;
        try (Connection conn = APIMgtDBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
            ps.setInt(1, applicationId);
            ps.setString(2, keyType);
            ps.setString(3, keyManagerName);

            try (ResultSet rs = ps.executeQuery()) {
                // returns only one row
                if (rs.next()) {
                    reference = rs.getString("WF_REF");
                }
            }

        } catch (SQLException e) {
            handleException("Error occurred while getting registration entry for " + "Application : " + applicationId,
                    e);
        }
        return reference;
    }


}
