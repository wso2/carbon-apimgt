package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIManagerDatabaseException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GatewayArtifactsMgtDBUtil {

    private static final Log log = LogFactory.getLog(GatewayArtifactsMgtDBUtil.class);
    private static volatile DataSource artifactSynchronizerDataSource = null;
    private static final String DATA_SOURCE_NAME = "DataSourceName";

    /**
     * Initializes the data source
     *
     * @throws APIManagementException if an error occurs while loading DB configuration
     */
    public static void initialize() throws APIManagerDatabaseException {
        if (artifactSynchronizerDataSource != null) {
            return;
        }

        synchronized (GatewayArtifactsMgtDBUtil.class) {
            if (artifactSynchronizerDataSource == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing data source");
                }
                GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties =
                        ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                                .getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();
                String artifactSynchronizerDataSourceName =
                        gatewayArtifactSynchronizerProperties.getArtifactSynchronizerDataSource();

                if (artifactSynchronizerDataSourceName != null) {
                    try {
                        Context ctx = new InitialContext();
                        artifactSynchronizerDataSource = (DataSource) ctx.lookup(artifactSynchronizerDataSourceName);
                    } catch (NamingException e) {
                        throw new APIManagerDatabaseException("Error while looking up the data " +
                                "source: " + artifactSynchronizerDataSourceName, e);
                    }
                } else {
                    log.error("AS" + artifactSynchronizerDataSourceName + " not defined in api-manager.xml.");
                }
            }
        }
    }

    /**
     * Utility method to get a new database connection for gatewayRuntime artifacts
     *
     * @return Connection
     * @throws java.sql.SQLException if failed to get Connection
     */
    public static Connection getArtifactSynchronizerConnection() throws SQLException {
        if (artifactSynchronizerDataSource != null) {
            return artifactSynchronizerDataSource.getConnection();
        }
        throw new SQLException("Data source is not configured properly.");
    }
}
