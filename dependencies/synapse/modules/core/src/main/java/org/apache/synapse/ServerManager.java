/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.apache.synapse.config.SynapsePropertiesLoader;
import org.wso2.securevault.PasswordManager;
import org.wso2.securevault.SecurityConstants;

import javax.management.NotCompliantMBeanException;
import java.util.Date;

/**
 * This is the core class that starts up a Synapse instance.
 * <p/>
 * From the command line scripts synapse.sh and synapse-daemon.sh (though the wrapper.conf)
 * the SynapseServer is invoked which in turn calls on this to start the instance
 * <p/>
 * When the WAR deployment is used, the SynapseStartUpServlet servlet calls on this class to
 * initialize Synapse.
 * <p/>
 * This is the entry point for starting an Synapse instance. All the synapse related management
 * operations are exposed through this class.
 */
@SuppressWarnings({"UnusedDeclaration"})
public class ServerManager {

    private static final Log log = LogFactory.getLog(ServerManager.class);

    /**
     * The controller for synapse create and Destroy synapse artifacts in a particular environment
     * Only for internal usage - DON"T PUT GETTER ,SETTER
     */
    private SynapseController synapseController;

    /* Server Configuration  */
    private ServerConfigurationInformation serverConfigurationInformation;

    /* Server context */
    private ServerContextInformation serverContextInformation;

    /**
     * Only represents whether server manager has been initialized by given required
     * configuration information - not server state or internal usage - DON"T PUT SETTER
     */
    private boolean initialized = false;

    /**
     * Save the TCCL of the initial thread that starts the ESB for future use. When JMX calls are
     * received via RMI connections, re-start etc may otherwise fail due to class loading issues.
     */
    private ClassLoader classLoader;

    /**
     * Construct a server manager.
     */
    public ServerManager() {
    }

    /**
     * Initializes the server, if we need to create a new axis2 instance, calling this will create
     * the new axis2 environment, but this won't start the transport listeners
     *
     * @param serverConfigurationInformation ServerConfigurationInformation instance
     * @param serverContextInformation       ServerContextInformation instance
     * @return ServerState - State of the server which is
     *          {@link org.apache.synapse.ServerState#INITIALIZED}, if successful
     */
    public synchronized ServerState init(
            ServerConfigurationInformation serverConfigurationInformation,
            ServerContextInformation serverContextInformation) {

        classLoader = Thread.currentThread().getContextClassLoader();
        
        // sets the initializations parameters
        this.serverConfigurationInformation = serverConfigurationInformation;

        if (serverContextInformation == null) {
            this.serverContextInformation =
                    new ServerContextInformation(serverConfigurationInformation);
        } else {
            this.serverContextInformation = serverContextInformation;
        }
        synapseController = SynapseControllerFactory
                .createSynapseController(serverConfigurationInformation);

        // does the initialization of the controller
        doInit();
        initialized = true;

        return this.serverContextInformation.getServerState();
    }

    /**
     * Shuts down the Server instance. If the Server is stopped this will shutdown the
     * ServerManager, and if it is running (i.e. in the STARTED state) this will first stop the
     * ServerManager and shutdown it in turn.
     * 
     * @return the state after the shutdown, {@link org.apache.synapse.ServerState#UNDETERMINED}
     */
    public synchronized ServerState shutdown() {

        ServerState serverState = ServerStateDetectionStrategy.currentState(
                serverContextInformation, serverConfigurationInformation);

        switch (serverState) {
            // if the current state is INITIALIZED, then just destroy
            case INITIALIZED: {
                doShutdown();
                break;
            }
            // if the current state is STOPPED, then again just destroy
            case STOPPED: {
                doShutdown();
                break;
            }
            // if the current state is STARTED, then stop and destroy
            case STARTED: {
                stop();
                doShutdown();
                break;
            }
            // if the current state is MAINTENANCE, then stop and destroy
            case MAINTENANCE: {
                stop();
                doShutdown();
                break;
            }
        }

        // clear the instance parameters
        this.synapseController = null;
        this.serverContextInformation = null;
        this.serverConfigurationInformation = null;

        this.initialized = false;
        return ServerState.UNDETERMINED;
    }

    /**
     * Starts the system, if the system is initialized, and if not a Runtime exception of type
     * {@link org.apache.synapse.SynapseException} will be thrown
     *
     * @return the state of the server after starting, for a successful start
     *          {@link org.apache.synapse.ServerState#STARTED}
     */
    public synchronized ServerState start() {

        // if the system is not initialized we are not happy
        assertInitialized();

        // starts the system
        ServerState serverState = ServerStateDetectionStrategy.currentState(
                serverContextInformation, serverConfigurationInformation);

        if (serverState == ServerState.INITIALIZED || serverState == ServerState.STOPPED) {

            // creates the Synapse Configuration using the SynapseController
            serverContextInformation.setSynapseConfiguration(
                    synapseController.createSynapseConfiguration());

            // creates the Synapse Environment using the SynapseController
            serverContextInformation.setSynapseEnvironment(
                    synapseController.createSynapseEnvironment());

            // starts the SynapseController
            synapseController.start();

            changeState(ServerState.STARTED);
            log.info("Server ready for processing...");
        } else if (serverState == ServerState.STARTED) {
            String message = "The server has already been started.";
            handleException(message);
        } else if (serverState == ServerState.MAINTENANCE) {
            endMaintenance();
        } else {
            // if the server cannot be started just set the current state as the server state
            changeState(serverState);
        }

        return this.serverContextInformation.getServerState();
    }

    /**
     * Put transport listeners and senders into maintenance mode.
     * 
     * @return the state of the server after maintenance request, for a successful execution
     *          {@link org.apache.synapse.ServerState#MAINTENANCE}
     */
    public synchronized ServerState startMaintenance() {

        assertInitialized();

        ServerState serverState = ServerStateDetectionStrategy.currentState(serverContextInformation,
                serverConfigurationInformation);

        // if the system is started we can enter the maintenance mode
        if (serverState == ServerState.STARTED) {
            synapseController.startMaintenance();
            changeState(ServerState.MAINTENANCE);
        } else if (serverState == ServerState.MAINTENANCE) {
            String message = "The server is already in maintenance mode.";
            handleException(message);
        } else {
            String message = "Couldn't enter maintenance mode, the server has not been started.";
            handleException(message);
        }

        return serverContextInformation.getServerState();
    }

    /**
     * Ends server maintenance resuming transport listeners, senders and tasks.
     * 
     * @return the state of the server after maintenance request, for a successful execution
     *          {@link org.apache.synapse.ServerState#MAINTENANCE}
     */
    public synchronized ServerState endMaintenance() {

        assertInitialized();

        ServerState serverState = ServerStateDetectionStrategy.currentState(
                serverContextInformation, serverConfigurationInformation);

        // if the system is started we can enter the maintenance mode
        if (serverState == ServerState.MAINTENANCE) {
            synapseController.endMaintenance();
            changeState(ServerState.STARTED);
        } else {
            String message = "Couldn't leave maintenance mode." 
                    + " The server has not been in maintenance.";
            handleException(message);
        }

        return serverContextInformation.getServerState();
    }

    /**
     * Stops the system, if it is started and if not a Runtime exception of type
     * {@link org.apache.synapse.SynapseException} will be thrown
     *
     * @return the state of the system after stopping, which is
     *          {@link org.apache.synapse.ServerState#STOPPED} for a successful stopping
     */
    public synchronized ServerState stop() {

        assertInitialized();

        ServerState serverState = ServerStateDetectionStrategy.currentState(
                serverContextInformation, serverConfigurationInformation);

        // if the system is started then stop if not we are not happy
        if (serverState == ServerState.STARTED || serverState == ServerState.MAINTENANCE) {

            // stop the SynapseController
            synapseController.stop();

            // destroy the created Synapse Environment
            synapseController.destroySynapseEnvironment();
            serverContextInformation.setSynapseEnvironment(null);

            // destroy the created Synapse Configuration
            synapseController.destroySynapseConfiguration();
            serverContextInformation.setSynapseConfiguration(null);

            changeState(ServerState.STOPPED);
        } else {
            // if the server cannot be stopped just set the current state as the server state
            changeState(serverState);
            String message = "Couldn't stop the ServerManager, it has not been started yet";
            handleException(message);
        }

        return this.serverContextInformation.getServerState();
    }

    /**
     * Perform a graceful stop of Synapse. Before the instance is stopped it will be put
     * to maintenance mode.
     *
     * @param maxWaitMillis the maximum number of ms to wait until a graceful stop is achieved,
     *                      before forcing a stop
     * @return if successful ServerState#STOPPED
     *                      
     * @throws SynapseException 
     */
    public synchronized ServerState stopGracefully(long maxWaitMillis) {

        final long startTime = System.currentTimeMillis();
        final long endTime = startTime + maxWaitMillis;
        final long waitIntervalMillis = 2000;
        log.info(new StringBuilder("Requesting a graceful shutdown at: ").append(new Date())
                .append(" in a maximum of ").append(maxWaitMillis/1000)
                .append(" seconds.").toString());

        startMaintenance();

        // wait until it is safe to to stop the server or the maximum time to wait is over
        if (synapseController.waitUntilSafeToStop(waitIntervalMillis, endTime)) {
            log.info(new StringBuilder("The instance could not be gracefully stopped in: ") 
                    .append(maxWaitMillis / 1000)
                    .append(" seconds. Performing an immediate stop...").toString());
        }

        stop();

        log.info(new StringBuilder("Graceful stop request completed in ")
                .append((System.currentTimeMillis() - startTime))
                .append(" milliseconds.").toString());

        return this.serverContextInformation.getServerState();
    }

    /**
     * Returns the ServerConfigurationInformation, if the system is initialized and if not a
     * Runtime exception of type {@link org.apache.synapse.SynapseException} will be thrown
     *
     * @return the configuration information of the initialized system
     */
    public ServerConfigurationInformation getServerConfigurationInformation() {
        assertInitialized();
        return serverConfigurationInformation;
    }

    /**
     * Returns the ServerContextInformation, if the system is initialized and if not a Runtime
     * Exception of type {@link org.apache.synapse.SynapseException} will be thrown
     *
     * @return the context information of the initialized system
     */
    public ServerContextInformation getServerContextInformation() {
        assertInitialized();
        return serverContextInformation;
    }

    /** 
     * Returns the context class loader of the original thread.
     * 
     * @return the context class loader of the original thread.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Has server manager been initialized ?
     *
     * @return true if the server manager has been initialized by given required
     *         configuration information
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Retrieves the state of the server.
     * 
     * @return the state of the server
     */
    public ServerState getServerState() {
        if (serverContextInformation != null) {
            return serverContextInformation.getServerState();
        }
        return ServerState.UNDETERMINED;
    }

    /**
     * Helper method for initializing the ServerManager
     */
    private void doInit() {

        ServerState serverState = ServerStateDetectionStrategy.currentState(
                serverContextInformation, serverConfigurationInformation);

        // if the server is ready for the initialization, this will make sure that we are not
        // calling the initialization on an already initialized/started system
        if (serverState == ServerState.INITIALIZABLE) {

            // register the ServerManager MBean
            registerMBean();

            // initialize global PasswordManager instance used in synapse
            PasswordManager.getInstance().init(
                    SynapsePropertiesLoader.loadSynapseProperties(), SynapseConstants.SYNAPSE);

            // initializes the SynapseController
            this.synapseController.init(serverConfigurationInformation, serverContextInformation);
            
            // mark as initialized
            changeState(ServerState.INITIALIZED);
        } else {
            // if the server cannot be initialized just set the current state as the server state
            changeState(serverState);
        }
    }

    /**
     * Helper method to shutdown the the ServerManager
     */
    private void doShutdown() {
        ServerState serverState = ServerStateDetectionStrategy.currentState(
                serverContextInformation, serverConfigurationInformation);

        if (serverState == ServerState.INITIALIZED || serverState == ServerState.STOPPED) {

            // Shutdown global PasswordManager instance used in synapse
            PasswordManager passwordManager = PasswordManager.getInstance();
            if (passwordManager.isInitialized()) {
                PasswordManager.getInstance().shutDown();
            }

            // un-register the ServerManager MBean
            unRegisterMBean();

            // destroy the SynapseController
            synapseController.destroy();

            // mark as destroyed
            changeState(ServerState.UNDETERMINED);
        } else {
            // if the server cannot be destroyed just set the current state as the server state
            changeState(serverState);
        }
    }

    /**
     * Changes the server state to the specified state.
     * 
     * @param serverState the new server state
     */
    private void changeState(ServerState serverState) {
        this.serverContextInformation.setServerState(serverState);
    }

    private void assertInitialized() {
        if (!initialized) {
            String msg = "Server manager has not been initialized, it requires to be " +
                    "initialized, with the required configurations before starting";
            handleException(msg);
        }
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private void registerMBean() {
        MBeanRegistrar.getInstance().registerMBean(new ServerManagerView(this),
                SynapseConstants.SERVER_MANAGER_MBEAN, SynapseConstants.SERVER_MANAGER_MBEAN);
        try {
            MBeanRegistrar.getInstance().registerMBean(
                    new SecretManagerAdminMBeanImpl(),
                    SecurityConstants.PROP_SECURITY_ADMIN_SERVICES,
                    SecurityConstants.PROP_SECRET_MANAGER_ADMIN_MBEAN);
        } catch (NotCompliantMBeanException e) {
            handleException("Error registering SecretManagerAdminMBeanImpl", e);
        }
    }

    private void unRegisterMBean() {
        MBeanRegistrar.getInstance().unRegisterMBean(
                SynapseConstants.SERVER_MANAGER_MBEAN, SynapseConstants.SERVER_MANAGER_MBEAN);
        MBeanRegistrar.getInstance().unRegisterMBean(
                SecurityConstants.PROP_SECURITY_ADMIN_SERVICES,
                SecurityConstants.PROP_SECRET_MANAGER_ADMIN_MBEAN);
    }
}
