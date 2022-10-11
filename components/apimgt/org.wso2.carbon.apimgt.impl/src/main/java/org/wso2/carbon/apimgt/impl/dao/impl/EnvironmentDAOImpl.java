package org.wso2.carbon.apimgt.impl.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.impl.dao.EnvironmentDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.utils.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EnvironmentDAOImpl implements EnvironmentDAO {
    private static final Log log = LogFactory.getLog(EnvironmentDAOImpl.class);
    private static EnvironmentDAOImpl INSTANCE = new EnvironmentDAOImpl();

    private EnvironmentDAOImpl() {

    }

    public static EnvironmentDAOImpl getInstance() {
        return INSTANCE;
    }

    private void handleExceptionWithCode(String msg, Throwable t, ErrorHandler code) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, code);
    }

    @Override
    public Environment getEnvironment(String tenantDomain, String uuid) throws APIManagementException {

        Environment env = null;
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt =
                     connection.prepareStatement(SQLConstants.GET_ENVIRONMENT_BY_ORGANIZATION_AND_UUID_SQL)) {
            prepStmt.setString(1, tenantDomain);
            prepStmt.setString(2, uuid);
            try (ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    Integer id = rs.getInt("ID");
                    String name = rs.getString("NAME");
                    String displayName = rs.getString("DISPLAY_NAME");
                    String description = rs.getString("DESCRIPTION");
                    String provider = rs.getString("PROVIDER");

                    env = new Environment();
                    env.setId(id);
                    env.setUuid(uuid);
                    env.setName(name);
                    env.setDisplayName(displayName);
                    env.setDescription(description);
                    env.setProvider(provider);
                    env.setVhosts(getVhostGatewayEnvironments(connection, id));
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get Environment in tenant domain:" + tenantDomain, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return env;
    }

    /**
     * Returns a list of vhosts belongs to the gateway environments
     *
     * @param connection DB connection
     * @param envId      Environment id.
     * @return list of vhosts belongs to the gateway environments.
     */
    private List<VHost> getVhostGatewayEnvironments(Connection connection, Integer envId) throws APIManagementException {

        List<VHost> vhosts = new ArrayList<>();
        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_ENVIRONMENT_VHOSTS_BY_ID_SQL)) {
            prepStmt.setInt(1, envId);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String host = rs.getString("HOST");
                    String httpContext = rs.getString("HTTP_CONTEXT");
                    Integer httpPort = rs.getInt("HTTP_PORT");
                    Integer httpsPort = rs.getInt("HTTPS_PORT");
                    Integer wsPort = rs.getInt("WS_PORT");
                    Integer wssPort = rs.getInt("WSS_PORT");

                    VHost vhost = new VHost();
                    vhost.setHost(host);
                    vhost.setHttpContext(httpContext == null ? "" : httpContext);
                    vhost.setHttpPort(httpPort);
                    vhost.setHttpsPort(httpsPort);
                    vhost.setWsPort(wsPort);
                    vhost.setWssPort(wssPort);
                    vhosts.add(vhost);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get gateway environments list of VHost: ", e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return vhosts;
    }

    @Override
    public List<Environment> getAllEnvironments(String tenantDomain) throws APIManagementException {

        List<Environment> envList = new ArrayList<>();
        try (Connection connection = APIMgtDBUtil.getConnection();
             PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.GET_ENVIRONMENT_BY_ORGANIZATION_SQL)) {
            prepStmt.setString(1, tenantDomain);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    Integer id = rs.getInt("ID");
                    String uuid = rs.getString("UUID");
                    String name = rs.getString("NAME");
                    String displayName = rs.getString("DISPLAY_NAME");
                    String description = rs.getString("DESCRIPTION");
                    String provider = rs.getString("PROVIDER");

                    Environment env = new Environment();
                    env.setId(id);
                    env.setUuid(uuid);
                    env.setName(name);
                    env.setDisplayName(displayName);
                    env.setDescription(description);
                    env.setProvider(provider);
                    env.setVhosts(getVhostGatewayEnvironments(connection, id));
                    envList.add(env);
                }
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to get Environments in tenant domain: " + tenantDomain, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return envList;
    }

    @Override
    public Environment addEnvironment(String tenantDomain, Environment environment) throws APIManagementException {

        String uuid = UUID.randomUUID().toString();
        environment.setUuid(uuid);

        try (Connection conn = APIMgtDBUtil.getConnection()) {
            conn.setAutoCommit(false);
            String dbProductName = conn.getMetaData().getDatabaseProductName();
            try (PreparedStatement prepStmt = conn.prepareStatement(SQLConstants.INSERT_ENVIRONMENT_SQL,
                    new String[]{DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "ID")})) {
                prepStmt.setString(1, uuid);
                prepStmt.setString(2, environment.getName());
                prepStmt.setString(3, environment.getDisplayName());
                prepStmt.setString(4, environment.getDescription());
                prepStmt.setString(5, environment.getProvider());
                prepStmt.setString(6, tenantDomain);
                prepStmt.executeUpdate();

                ResultSet rs = prepStmt.getGeneratedKeys();
                int id = -1;
                if (rs.next()) {
                    id = rs.getInt(1);
                }
                addGatewayVhosts(conn, id, environment.getVhosts());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                handleExceptionWithCode("Failed to add VHost: " + uuid, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to add VHost: " + uuid, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return environment;
    }

    /**
     * Add VHost assigned to gateway environment
     *
     * @param connection connection
     * @param id         Environment ID in the databse
     * @param vhosts     list of VHosts assigned to the environment
     * @throws APIManagementException if falied to add VHosts
     */
    private void addGatewayVhosts(Connection connection, int id, List<VHost> vhosts) throws
            APIManagementException {

        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.INSERT_GATEWAY_VHOSTS_SQL)) {
            for (VHost vhost : vhosts) {
                prepStmt.setInt(1, id);
                prepStmt.setString(2, vhost.getHost());
                prepStmt.setString(3, vhost.getHttpContext());
                prepStmt.setString(4, vhost.getHttpPort().toString());
                prepStmt.setString(5, vhost.getHttpsPort().toString());
                prepStmt.setString(6, vhost.getWsPort().toString());
                prepStmt.setString(7, vhost.getWssPort().toString());
                prepStmt.addBatch();
            }
            prepStmt.executeBatch();
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to add VHosts for environment ID: " + id, e,
                    ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public void deleteEnvironment(String uuid) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_ENVIRONMENT_SQL)) {
                prepStmt.setString(1, uuid);
                prepStmt.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleExceptionWithCode("Failed to delete Environment", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to delete Environment", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    @Override
    public Environment updateEnvironment(Environment environment) throws APIManagementException {

        try (Connection connection = APIMgtDBUtil.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.UPDATE_ENVIRONMENT_SQL)) {
                prepStmt.setString(1, environment.getDisplayName());
                prepStmt.setString(2, environment.getDescription());
                prepStmt.setString(3, environment.getUuid());
                prepStmt.executeUpdate();
                deleteGatewayVhosts(connection, environment.getId());
                addGatewayVhosts(connection, environment.getId(), environment.getVhosts());
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                handleExceptionWithCode("Failed to update Environment", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
            }
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to update Environment", e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
        return environment;
    }

    /**
     * Delete all VHosts assigned to gateway environment
     *
     * @param connection connection
     * @param id         Environment ID in the databse
     * @throws APIManagementException if falied to delete VHosts
     */
    private void deleteGatewayVhosts(Connection connection, int id) throws
            APIManagementException {

        try (PreparedStatement prepStmt = connection.prepareStatement(SQLConstants.DELETE_GATEWAY_VHOSTS_SQL)) {
            prepStmt.setInt(1, id);
            prepStmt.executeUpdate();
        } catch (SQLException e) {
            handleExceptionWithCode("Failed to delete VHosts for environment ID: " + id, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }




}
