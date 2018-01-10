package org.wso2.carbon.apimgt.core.internal;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.broker.amqp.Server;
import org.wso2.broker.common.BrokerConfigProvider;
import org.wso2.broker.common.StartupContext;
import org.wso2.broker.core.Broker;
import org.wso2.broker.core.configuration.BrokerConfiguration;
import org.wso2.broker.core.security.authentication.user.User;
import org.wso2.broker.core.security.authentication.user.UserStoreManager;
import org.wso2.broker.core.security.authentication.user.UsersFile;
import org.wso2.broker.core.security.authentication.util.BrokerSecurityConstants;
import org.wso2.broker.rest.BrokerRestServer;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.sql.DataSource;

/**
 * Implementation layer for broker starting and stop
 */
public class BrokerManager {

    private static final Logger log = LoggerFactory.getLogger(BrokerManager.class);
    private static Server amqpServer;
    private static Broker broker;
    private static BrokerRestServer restServer;
    /**
     * Starting the broker
     */
    public static void start() {
        try {
            StartupContext startupContext = new StartupContext();

            initConfigProvider(startupContext);
            BrokerConfigProvider service = startupContext.getService(BrokerConfigProvider.class);
            BrokerConfiguration brokerConfiguration =
                    service.getConfigurationObject(BrokerConfiguration.NAMESPACE, BrokerConfiguration.class);
            DataSource dataSource = getDataSource(brokerConfiguration.getDataSource());
            startupContext.registerService(DataSource.class, dataSource);
            restServer = new BrokerRestServer(startupContext);

            broker = new Broker(startupContext);
            broker.startMessageDelivery();
            amqpServer = new Server(startupContext);
            amqpServer.start();
            restServer.start();
            loadUsers();
        } catch (Exception e) {
            log.error("Error while starting broker", e);
        }
    }

    public static void stop() {
        try {
            restServer.stop();
            broker.stopMessageDelivery();
            amqpServer.stop();
        } catch (Exception e) {
            log.error("Error while stoping the broker", e);
        }
    }

    private static DataSource getDataSource(BrokerConfiguration.DataSourceConfiguration dataSourceConfiguration) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dataSourceConfiguration.getUrl());
        config.setUsername(dataSourceConfiguration.getUser());
        config.setPassword(dataSourceConfiguration.getPassword());
        config.setAutoCommit(false);

        return new HikariDataSource(config);
    }

    /**
     * Loads configurations during the broker start up.
     * method will try to <br/>
     * (1) Load the configuration file specified in 'broker.file' (e.g. -Dbroker.file=<FilePath>). <br/>
     * (2) If -Dbroker.file is not specified, the broker.yaml file exists in current directory and load it. <br/>
     * <p>
     * <b>Note: </b> if provided configuration file cannot be read broker will not start.
     *
     * @param startupContext startup context of the broker
     */
    private static void initConfigProvider(StartupContext startupContext) throws ConfigurationException {
        Path brokerYamlFile;
        String brokerFilePath = System.getProperty(BrokerConfiguration.SYSTEM_PARAM_BROKER_CONFIG_FILE);
        if (brokerFilePath == null || brokerFilePath.trim().isEmpty()) {
            // use current path.
            brokerYamlFile = Paths.get("", BrokerConfiguration.BROKER_FILE_NAME).toAbsolutePath();
        } else {
            brokerYamlFile = Paths.get(brokerFilePath).toAbsolutePath();
        }

        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(brokerYamlFile, null);
        startupContext.registerService(BrokerConfigProvider.class,
                (BrokerConfigProvider) configProvider::getConfigurationObject);
    }

    /**
     * Loads the users from users.yaml during broker startup
     */
    private static void loadUsers() throws ConfigurationException {
        Path usersYamlFile;
        String usersFilePath = System.getProperty(BrokerSecurityConstants.SYSTEM_PARAM_USERS_CONFIG);
        if (usersFilePath == null || usersFilePath.trim().isEmpty()) {
            // use current path.
            usersYamlFile = Paths.get("", BrokerSecurityConstants.USERS_FILE_NAME).toAbsolutePath();
        } else {
            usersYamlFile = Paths.get(usersFilePath).toAbsolutePath();
        }
        ConfigProvider configProvider = ConfigProviderFactory.getConfigProvider(usersYamlFile, null);
        UsersFile usersFile = configProvider
                .getConfigurationObject(BrokerSecurityConstants.USERS_CONFIG_NAMESPACE, UsersFile.class);
        if (usersFile != null) {
            List<User> users = usersFile.getUsers();
            for (User user : users) {
                UserStoreManager.addUser(user);
            }
        }
    }
}
