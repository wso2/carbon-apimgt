package org.wso2.carbon.apimgt.persistence.dao;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.persistence.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.persistence.utils.PersistanceDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PersistenceDAO {
    private static final Log log = LogFactory.getLog(PersistenceDAO.class);
    private static PersistenceDAO INSTANCE = null;

    private PersistenceDAO() {

    }

    public static PersistenceDAO getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PersistenceDAO();
        }

        return INSTANCE;
    }

    public int addAPISchema(String uuid, String jsonString) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int apiSchemaId = -1;

        String query = SQLConstants.ADD_API_SCHEMA_SQL;

        try {
            connection = PersistanceDBUtil.getConnection();
            connection.setAutoCommit(false);

            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, jsonString);
            prepStmt.setString(2, uuid);

            prepStmt.execute();

            rs = prepStmt.getGeneratedKeys();
            if (rs.next()) {
                apiSchemaId = rs.getInt(1);
            }

            connection.commit();

        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                // Rollback failed. Exception will be thrown later for upper exception
                log.error("Failed to rollback the add API schema: " + apiSchemaId, ex);
            }
            handleException("Error while adding the API schema: " + apiSchemaId + " to the database", e);
        } finally {
            PersistanceDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return apiSchemaId;
    }

    private void handleException(String msg, Throwable t) throws APIManagementException {

        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }
}
