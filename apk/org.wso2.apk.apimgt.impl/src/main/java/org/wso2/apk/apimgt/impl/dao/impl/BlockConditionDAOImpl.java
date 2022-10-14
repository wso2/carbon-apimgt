package org.wso2.apk.apimgt.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.impl.dao.BlockConditionDAO;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.BlockConditionAlreadyExistsException;
import org.wso2.apk.apimgt.api.ErrorHandler;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.BlockConditionsDTO;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.apk.apimgt.impl.dao.util.DBUtils;
import org.wso2.apk.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BlockConditionDAOImpl implements BlockConditionDAO {
    private static final Log log = LogFactory.getLog(BlockConditionDAOImpl.class);
    private static BlockConditionDAOImpl INSTANCE = new BlockConditionDAOImpl();

    private BlockConditionDAOImpl() {

    }

    public static BlockConditionDAOImpl getInstance() {
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
    public List<BlockConditionsDTO> getBlockConditions(String tenantDomain) throws APIManagementException {

        Connection connection = null;
        PreparedStatement selectPreparedStatement = null;
        ResultSet resultSet = null;
        List<BlockConditionsDTO> blockConditionsDTOList = new ArrayList<BlockConditionsDTO>();
        try {
            String query = SQLConstants.ThrottleSQLConstants.GET_BLOCK_CONDITIONS_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(true);
            selectPreparedStatement = connection.prepareStatement(query);
            selectPreparedStatement.setString(1, tenantDomain);
            resultSet = selectPreparedStatement.executeQuery();
            while (resultSet.next()) {
                BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
                blockConditionsDTO.setEnabled(resultSet.getBoolean("ENABLED"));
                blockConditionsDTO.setConditionType(resultSet.getString("TYPE"));
                blockConditionsDTO.setConditionValue(resultSet.getString("BLOCK_CONDITION"));
                blockConditionsDTO.setConditionId(resultSet.getInt("CONDITION_ID"));
                blockConditionsDTO.setUUID(resultSet.getString("UUID"));
                blockConditionsDTO.setTenantDomain(resultSet.getString("DOMAIN"));
                blockConditionsDTOList.add(blockConditionsDTO);
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleExceptionWithCode("Failed to rollback getting Block conditions ", ex,
                            ExceptionCodes.APIMGT_DAO_EXCEPTION);
                }
            }
            handleExceptionWithCode("Failed to get Block conditions", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectPreparedStatement, connection, resultSet);
        }
        return blockConditionsDTOList;
    }

    @Override
    public BlockConditionsDTO getBlockCondition(int conditionId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement selectPreparedStatement = null;
        ResultSet resultSet = null;
        BlockConditionsDTO blockCondition = null;
        try {
            String query = SQLConstants.ThrottleSQLConstants.GET_BLOCK_CONDITION_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(true);
            selectPreparedStatement = connection.prepareStatement(query);
            selectPreparedStatement.setInt(1, conditionId);
            resultSet = selectPreparedStatement.executeQuery();
            if (resultSet.next()) {
                blockCondition = new BlockConditionsDTO();
                blockCondition.setEnabled(resultSet.getBoolean("ENABLED"));
                blockCondition.setConditionType(resultSet.getString("TYPE"));
                blockCondition.setConditionValue(resultSet.getString("BLOCK_CONDITION"));
                blockCondition.setConditionId(conditionId);
                blockCondition.setTenantDomain(resultSet.getString("DOMAIN"));
                blockCondition.setUUID(resultSet.getString("UUID"));
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback getting Block condition with id " + conditionId, ex);
                }
            }
            handleException("Failed to get Block condition with id " + conditionId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectPreparedStatement, connection, resultSet);
        }
        return blockCondition;
    }

    @Override
    public BlockConditionsDTO getBlockConditionByUUID(String uuid) throws APIManagementException {

        Connection connection = null;
        PreparedStatement selectPreparedStatement = null;
        ResultSet resultSet = null;
        BlockConditionsDTO blockCondition = null;
        try {
            String query = SQLConstants.ThrottleSQLConstants.GET_BLOCK_CONDITION_BY_UUID_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(true);
            selectPreparedStatement = connection.prepareStatement(query);
            selectPreparedStatement.setString(1, uuid);
            resultSet = selectPreparedStatement.executeQuery();
            if (resultSet.next()) {
                blockCondition = new BlockConditionsDTO();
                blockCondition.setEnabled(resultSet.getBoolean("ENABLED"));
                blockCondition.setConditionType(resultSet.getString("TYPE"));
                blockCondition.setConditionValue(resultSet.getString("BLOCK_CONDITION"));
                blockCondition.setConditionId(resultSet.getInt("CONDITION_ID"));
                blockCondition.setTenantDomain(resultSet.getString("DOMAIN"));
                blockCondition.setUUID(resultSet.getString("UUID"));
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleExceptionWithCode("Failed to rollback getting Block condition by uuid " + uuid, ex,
                            ExceptionCodes.APIMGT_DAO_EXCEPTION);
                }
            }
            handleExceptionWithCode("Failed to get Block condition by uuid " + uuid, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(selectPreparedStatement, connection, resultSet);
        }
        return blockCondition;
    }

    @Override
    public boolean updateBlockConditionState(int conditionId, String state) throws APIManagementException {

        Connection connection = null;
        PreparedStatement updateBlockConditionPreparedStatement = null;
        boolean status = false;
        try {
            String query = SQLConstants.ThrottleSQLConstants.UPDATE_BLOCK_CONDITION_STATE_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            updateBlockConditionPreparedStatement = connection.prepareStatement(query);
            updateBlockConditionPreparedStatement.setString(1, state.toUpperCase());
            updateBlockConditionPreparedStatement.setInt(2, conditionId);
            updateBlockConditionPreparedStatement.executeUpdate();
            connection.commit();
            status = true;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleException("Failed to rollback updating Block condition with condition id " + conditionId, ex);
                }
            }
            handleException("Failed to update Block condition with condition id " + conditionId, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(updateBlockConditionPreparedStatement, connection, null);
        }
        return status;
    }

    @Override
    public boolean updateBlockConditionStateByUUID(String uuid, String state) throws APIManagementException {

        Connection connection = null;
        PreparedStatement updateBlockConditionPreparedStatement = null;
        boolean status = false;
        try {
            String query = SQLConstants.ThrottleSQLConstants.UPDATE_BLOCK_CONDITION_STATE_BY_UUID_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            updateBlockConditionPreparedStatement = connection.prepareStatement(query);
            updateBlockConditionPreparedStatement.setString(1, state.toUpperCase());
            updateBlockConditionPreparedStatement.setString(2, uuid);
            updateBlockConditionPreparedStatement.executeUpdate();
            connection.commit();
            status = true;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleExceptionWithCode("Failed to rollback updating Block condition with condition UUID " + uuid,
                            ex, ExceptionCodes.APIMGT_DAO_EXCEPTION);
                }
            }
            handleExceptionWithCode("Failed to update Block condition with condition UUID " + uuid, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(updateBlockConditionPreparedStatement, connection, null);
        }
        return status;
    }

    @Override
    public BlockConditionsDTO addBlockConditions(BlockConditionsDTO blockConditionsDTO) throws
            APIManagementException {

        Connection connection = null;
        PreparedStatement insertPreparedStatement = null;
        boolean status = false;
        boolean valid = false;
        ResultSet rs = null;
        String uuid = blockConditionsDTO.getUUID();
        String conditionType = blockConditionsDTO.getConditionType();
        String conditionValue = blockConditionsDTO.getConditionValue();
        String tenantDomain = blockConditionsDTO.getTenantDomain();
        String conditionStatus = String.valueOf(blockConditionsDTO.isEnabled());
        try {
            String query = SQLConstants.ThrottleSQLConstants.ADD_BLOCK_CONDITIONS_SQL;
            if (APIConstants.BLOCKING_CONDITIONS_API.equals(conditionType)) {
                String extractedTenantDomain = getTenantDomainFromRequestURL(conditionValue);
                if (extractedTenantDomain == null) {
                    extractedTenantDomain = APIConstants.SUPER_TENANT_DOMAIN;
                }
                if (tenantDomain.equals(extractedTenantDomain) && isValidContext(conditionValue)) {
                    valid = true;
                } else {
                    throw new APIManagementException("Couldn't Save Block Condition Due to Invalid API Context " +
                            conditionValue, ExceptionCodes.BLOCK_CONDITION_UNSUPPORTED_API_CONTEXT);
                }
            } else if (APIConstants.BLOCKING_CONDITIONS_APPLICATION.equals(conditionType)) {
                String appArray[] = conditionValue.split(":");
                if (appArray.length > 1) {
                    String appOwner = appArray[0];
                    String appName = appArray[1];

                    if ((MultitenantUtils.getTenantDomain(appOwner).equals(tenantDomain)) &&
                            isValidApplication(appOwner, appName)) {
                        valid = true;
                    } else {
                        throw new APIManagementException("Couldn't Save Block Condition Due to Invalid Application " +
                                "name " + appName + " from Application " +
                                "Owner " + appOwner, ExceptionCodes.BLOCK_CONDITION_UNSUPPORTED_APP_ID_NAME);
                    }
                }
            } else if (APIConstants.BLOCKING_CONDITIONS_USER.equals(conditionType)) {
                if (MultitenantUtils.getTenantDomain(conditionValue).equals(tenantDomain)) {
                    valid = true;
                } else {
                    throw new APIManagementException("Invalid User in Tenant Domain " + tenantDomain,
                            ExceptionCodes.INTERNAL_ERROR);
                }
            } else if (APIConstants.BLOCKING_CONDITIONS_IP.equals(conditionType) ||
                    APIConstants.BLOCK_CONDITION_IP_RANGE.equals(conditionType)) {
                valid = true;
            } else if (APIConstants.BLOCKING_CONDITIONS_SUBSCRIPTION.equals(conditionType)) {
                /* ATM this condition type will be used internally to handle subscription blockings for JWT type access
                   tokens.
                */
                String[] conditionsArray = conditionValue.split(":");
                if (conditionsArray.length > 0) {
                    String apiContext = conditionsArray[0];
                    String applicationIdentifier = conditionsArray[2];

                    String[] app = applicationIdentifier.split("-", 2);
                    String appOwner = app[0];
                    String appName = app[1];

                    // TODO :// MultitenantUtils Check whether the given api context exists in tenant
                    String extractedTenantDomain = getTenantDomainFromRequestURL(apiContext);
                    if (extractedTenantDomain == null) {
                        extractedTenantDomain = APIConstants.SUPER_TENANT_DOMAIN;
                    }
                    if (tenantDomain.equals(extractedTenantDomain) && isValidContext(apiContext)) {
                        valid = true;
                    } else {
                        throw new APIManagementException(
                                "Couldn't Save Subscription Block Condition Due to Invalid API Context "
                                        + apiContext, ExceptionCodes.BLOCK_CONDITION_UNSUPPORTED_API_CONTEXT);
                    }

                    // Check whether the given application is valid
                    if ((MultitenantUtils.getTenantDomain(appOwner).equals(tenantDomain)) &&
                            isValidApplication(appOwner, appName)) {
                        valid = true;
                    } else {
                        throw new APIManagementException(
                                "Couldn't Save Subscription Block Condition Due to Invalid Application " + "name "
                                        + appName + " from Application " + "Owner " + appOwner,
                                ExceptionCodes.BLOCK_CONDITION_UNSUPPORTED_APP_ID_NAME);
                    }
                } else {
                    throw new APIManagementException(
                            "Invalid subscription block condition with insufficient data : " + conditionValue,
                            ExceptionCodes.INTERNAL_ERROR);
                }
            }
            if (valid) {
                connection = APIMgtDBUtil.getConnection();
                connection.setAutoCommit(false);
                if (!isBlockConditionExist(conditionType, conditionValue, tenantDomain, connection)) {
                    String dbProductName = connection.getMetaData().getDatabaseProductName();
                    insertPreparedStatement = connection.prepareStatement(query,
                            new String[]{ DBUtils.getConvertedAutoGeneratedColumnName(dbProductName,
                                    "CONDITION_ID")});
                    insertPreparedStatement.setString(1, conditionType);
                    insertPreparedStatement.setString(2, conditionValue);
                    insertPreparedStatement.setString(3, conditionStatus);
                    insertPreparedStatement.setString(4, tenantDomain);
                    insertPreparedStatement.setString(5, uuid);
                    insertPreparedStatement.execute();
                    ResultSet generatedKeys = insertPreparedStatement.getGeneratedKeys();
                    if (generatedKeys != null && generatedKeys.next()) {
                        blockConditionsDTO.setConditionId(generatedKeys.getInt(1));
                    }
                    connection.commit();
                    status = true;
                } else {
                    throw new BlockConditionAlreadyExistsException(
                            "Condition with type: " + conditionType + ", value: " + conditionValue + " already exists");
                }
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleExceptionWithCode(
                            "Failed to rollback adding Block condition : " + conditionType + " and " + conditionValue,
                            ex, ExceptionCodes.APIMGT_DAO_EXCEPTION);
                }
            }
            handleExceptionWithCode("Failed to add Block condition : " + conditionType + " and " + conditionValue, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(insertPreparedStatement, connection, null);
        }
        if (status) {
            return blockConditionsDTO;
        } else {
            return null;
        }
    }

    private static String getTenantDomainFromRequestURL(String requestURI) {
        String domain = null;
        if (requestURI.contains("/t/")) {
            int index = requestURI.indexOf("/t/");
            int endIndex = requestURI.indexOf("/", index + 3);
            domain = endIndex != -1 ? requestURI.substring(index + 3, endIndex) : requestURI.substring(index + 3);
        }

        return domain;
    }

    private boolean isValidContext(String context) throws APIManagementException {

        Connection connection = null;
        PreparedStatement validateContextPreparedStatement = null;
        ResultSet resultSet = null;
        boolean status = false;
        try {
            String query = "select count(*) COUNT from AM_API where CONTEXT=?";
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            validateContextPreparedStatement = connection.prepareStatement(query);
            validateContextPreparedStatement.setString(1, context);
            resultSet = validateContextPreparedStatement.executeQuery();
            connection.commit();
            if (resultSet.next() && resultSet.getInt("COUNT") > 0) {
                status = true;
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleExceptionWithCode("Failed to rollback checking Block condition with context " + context, ex,
                            ExceptionCodes.APIMGT_DAO_EXCEPTION);
                }
            }
            handleExceptionWithCode("Failed to check Block condition with context " + context, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(validateContextPreparedStatement, connection, resultSet);
        }
        return status;
    }

    private boolean isValidApplication(String appOwner, String appName) throws APIManagementException {

        Connection connection = null;
        PreparedStatement validateContextPreparedStatement = null;
        ResultSet resultSet = null;
        boolean status = false;
        try {
            String query = "SELECT * FROM AM_APPLICATION App,AM_SUBSCRIBER SUB  WHERE App.NAME=? AND App" +
                    ".SUBSCRIBER_ID=SUB.SUBSCRIBER_ID AND SUB.USER_ID=?";
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            validateContextPreparedStatement = connection.prepareStatement(query);
            validateContextPreparedStatement.setString(1, appName);
            validateContextPreparedStatement.setString(2, appOwner);
            resultSet = validateContextPreparedStatement.executeQuery();
            connection.commit();
            if (resultSet.next()) {
                status = true;
            }
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleExceptionWithCode(
                            "Failed to rollback checking Block condition with Application Name " + appName + " with "
                                    + "Application Owner" + appOwner, ex, ExceptionCodes.APIMGT_DAO_EXCEPTION);
                }
            }
            handleExceptionWithCode("Failed to check Block condition with Application Name " + appName + " with " +
                    "Application Owner" + appOwner, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(validateContextPreparedStatement, connection, resultSet);
        }
        return status;
    }

    private boolean isBlockConditionExist(String conditionType, String conditionValue, String tenantDomain, Connection
            connection) throws APIManagementException {

        PreparedStatement checkIsExistPreparedStatement = null;
        ResultSet checkIsResultSet = null;
        boolean status = false;
        try {
            String isExistQuery = SQLConstants.ThrottleSQLConstants.BLOCK_CONDITION_EXIST_SQL;
            checkIsExistPreparedStatement = connection.prepareStatement(isExistQuery);
            checkIsExistPreparedStatement.setString(1, tenantDomain);
            checkIsExistPreparedStatement.setString(2, conditionType);
            checkIsExistPreparedStatement.setString(3, conditionValue);
            checkIsResultSet = checkIsExistPreparedStatement.executeQuery();
            connection.commit();
            if (checkIsResultSet.next()) {
                status = true;
            }
        } catch (SQLException e) {
            String msg = "Couldn't check the Block Condition Exist";
            log.error(msg, e);
            handleExceptionWithCode(msg, e,ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(checkIsExistPreparedStatement, null, checkIsResultSet);
        }
        return status;
    }

    @Override
    public boolean deleteBlockCondition(int conditionId) throws APIManagementException {

        Connection connection = null;
        PreparedStatement deleteBlockConditionPreparedStatement = null;
        boolean status = false;
        try {
            String query = SQLConstants.ThrottleSQLConstants.DELETE_BLOCK_CONDITION_SQL;
            connection = APIMgtDBUtil.getConnection();
            connection.setAutoCommit(false);
            deleteBlockConditionPreparedStatement = connection.prepareStatement(query);
            deleteBlockConditionPreparedStatement.setInt(1, conditionId);
            status = deleteBlockConditionPreparedStatement.execute();
            connection.commit();
            status = true;
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    handleExceptionWithCode("Failed to rollback deleting Block condition with condition id "
                            + conditionId, ex, ExceptionCodes.APIMGT_DAO_EXCEPTION);
                }
            }
            handleExceptionWithCode("Failed to delete Block condition with condition id " + conditionId, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        } finally {
            APIMgtDBUtil.closeAllConnections(deleteBlockConditionPreparedStatement, connection, null);
        }
        return status;
    }


}
