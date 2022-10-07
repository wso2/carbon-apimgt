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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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


}
