/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.core.internal;

import com.zaxxer.hikari.HikariDataSource;
import io.ballerina.messaging.broker.amqp.Server;
import io.ballerina.messaging.broker.auth.AuthManager;
import io.ballerina.messaging.broker.common.StartupContext;
import io.ballerina.messaging.broker.common.config.BrokerConfigProvider;
import io.ballerina.messaging.broker.coordination.CoordinationException;
import io.ballerina.messaging.broker.coordination.HaStrategy;
import io.ballerina.messaging.broker.coordination.HaStrategyFactory;
import io.ballerina.messaging.broker.core.Broker;
import io.ballerina.messaging.broker.core.BrokerImpl;
import io.ballerina.messaging.broker.metrics.BrokerMetricService;
import io.ballerina.messaging.broker.rest.BrokerRestServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;

import javax.naming.Context;
import javax.sql.DataSource;

/**
 * Implementation layer for broker starting and stop
 */
public class BrokerManager {

    private static final Logger log = LoggerFactory.getLogger(BrokerManager.class);
    private static Server amqpServer;
    private static Broker broker;
    private static BrokerRestServer restServer;
    private static AuthManager authManager;
    private static BrokerMetricService metricService;

    /**
     * Starting the broker
     */
    public static void start(Context ctx, ConfigProvider configProvider) throws Exception {
        try {
            StartupContext startupContext = new StartupContext();

            initConfigProvider(configProvider, startupContext);
            DataSource dataSource = (HikariDataSource) ctx.lookup("java:comp/env/jdbc/WSO2MB");

            startupContext.registerService(DataSource.class, dataSource);
            HaStrategy haStrategy;
            //Initializing an HaStrategy implementation only if HA is enabled
            try {
                haStrategy = HaStrategyFactory.getHaStrategy(startupContext);
            } catch (Exception e) {
                throw new CoordinationException("Error initializing HA Strategy: ", e);
            }

            authManager = new AuthManager(startupContext);
            metricService = new BrokerMetricService(startupContext);
            restServer = new BrokerRestServer(startupContext);
            broker = new BrokerImpl(startupContext);
            amqpServer = new Server(startupContext);

            if (haStrategy != null) {
                //Start the HA strategy after all listeners have been registered, and before the listeners are started
                haStrategy.start();
            }

            metricService.start();
            authManager.start();
            broker.startMessageDelivery();
            amqpServer.start();
            restServer.start();
        } catch (Throwable e) {
            log.error("Error while starting broker", e);
            throw e;
        }
    }

    public static void stop() {
        try {
            restServer.shutdown();
            authManager.stop();
            amqpServer.shutdown();
            amqpServer.awaitServerClose();
            broker.shutdown();
            metricService.stop();
        } catch (Exception e) {
            log.error("Error while stoping the broker", e);
        }
    }

    /**
     * Loads configurations during the broker start up.
     * method will try to <br/>
     * (1) Load the configuration file specified in 'broker.file' (e.g. -Dbroker.file={FilePath}). <br/>
     * (2) If -Dbroker.file is not specified, the broker.yaml file exists in current directory and load it. <br/>
     * <p>
     * <b>Note: </b> if provided configuration file cannot be read broker will not start.
     *
     * @param startupContext startup context of the broker
     */
    private static void initConfigProvider(ConfigProvider configProvider, StartupContext startupContext) {
        startupContext.registerService(BrokerConfigProvider.class,
                (BrokerConfigProvider) configProvider::getConfigurationObject);
    }
}
