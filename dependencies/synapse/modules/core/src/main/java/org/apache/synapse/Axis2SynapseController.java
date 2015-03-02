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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.*;
import org.apache.axis2.dispatchers.SOAPMessageBodyBasedDispatcher;
import org.apache.axis2.engine.*;
import org.apache.axis2.format.BinaryBuilder;
import org.apache.axis2.format.PlainTextBuilder;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.beanstalk.enterprise.EnterpriseBeanstalkConstants;
import org.apache.synapse.commons.beanstalk.enterprise.EnterpriseBeanstalkManager;
import org.apache.synapse.commons.datasource.DataSourceRepositoryHolder;
import org.apache.synapse.commons.util.RMIRegistryController;
import org.apache.synapse.config.*;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.wso2.securevault.SecurityConstants;
import org.wso2.securevault.secret.SecretCallbackHandler;
import org.apache.synapse.commons.datasource.DataSourceInformationRepository;
import org.apache.synapse.commons.datasource.DataSourceConstants;
import org.apache.synapse.commons.jmx.JmxInformation;
import org.apache.synapse.commons.jmx.JmxInformationFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.*;
import org.apache.synapse.deployers.ImportDeployer;
import org.apache.synapse.deployers.LibraryArtifactDeployer;
import org.apache.synapse.deployers.SynapseArtifactDeploymentStore;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.libraries.imports.SynapseImport;
import org.apache.synapse.task.*;
import org.wso2.securevault.secret.handler.SharedSecretCallbackHandlerCache;
import org.apache.synapse.util.xpath.ext.SynapseXpathFunctionContextProvider;
import org.apache.synapse.util.xpath.ext.SynapseXpathVariableResolver;
import org.apache.synapse.util.xpath.ext.XpathExtensionUtil;

import java.io.File;
import java.util.*;

/**
 * Axis2 Based Synapse Controller.
 *
 * @see  org.apache.synapse.SynapseController
 */
public class Axis2SynapseController implements SynapseController {

    private static final Log log = LogFactory.getLog(Axis2SynapseController.class);

    private static final String JMX_AGENT_NAME = "jmx.agent.name";

    /** The Axis2 listener Manager */
    private ListenerManager listenerManager;

    /** The Axis2 configuration context used by Synapse */
    private ConfigurationContext configurationContext;

    /** Reference to the Synapse configuration */
    protected SynapseConfiguration synapseConfiguration;

    /** Reference to the Synapse configuration */
    protected SynapseEnvironment synapseEnvironment;

    /** Indicate initialization state */
    private boolean initialized;

    /** ServerConfiguration Information */
    protected ServerConfigurationInformation serverConfigurationInformation;

    /** Runtime information about the server */
    protected ServerContextInformation serverContextInformation;

    /** JMX Adapter */
    private JmxAdapter jmxAdapter;

    private TaskDescriptionRepository taskDescriptionRepository;

    private TaskScheduler taskScheduler;

    /**
     * {@inheritDoc}
     *
     * @param serverConfigurationInformation ServerConfigurationInformation Instance
     * @param serverContextInformation       Server Context if the Axis2 Based Server
     *                                       Environment has been already set up.
     */
    public void init(ServerConfigurationInformation serverConfigurationInformation,
                     ServerContextInformation serverContextInformation) {

        log.info("Initializing Synapse at : " + new Date());
        if (serverConfigurationInformation == null) {
            throw new IllegalArgumentException("ServerConfigurationInformation cannot be null");
        }

        if (serverContextInformation == null) {
            throw new IllegalArgumentException("ServerContextInformation cannot be null");
        }

        this.serverConfigurationInformation = serverConfigurationInformation;
        this.serverContextInformation = serverContextInformation;
        /* If no system property for the JMX agent is specified from outside, use a default one
           to show all MBeans (including the Axis2-MBeans) within the Synapse tree */
        if (System.getProperty(JMX_AGENT_NAME) == null) {
            System.setProperty(JMX_AGENT_NAME, "org.apache.synapse");
        }

        if (serverContextInformation.getServerContext() == null
                || serverConfigurationInformation.isCreateNewInstance()) {

            if (log.isDebugEnabled()) {
                log.debug("Initializing Synapse in a new axis2 server environment instance");
            }
            createNewInstance(serverConfigurationInformation);
        } else {
            Object context = serverContextInformation.getServerContext();
            if (context instanceof ConfigurationContext) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing Synapse in an already existing " +
                            "axis2 server environment instance");
                }
                configurationContext = (ConfigurationContext) context;
                configurationContext.setProperty(
                        AddressingConstants.ADDR_VALIDATE_ACTION, Boolean.FALSE);
            } else {
                handleFatal("Synapse startup initialization failed : Provided server context is"
                        + " invalid, expected an Axis2 ConfigurationContext instance");
            }
        }
        // set the configuration context
        serverContextInformation.setServerContext(configurationContext);

        // set the ServerContextInformation as a parameter
        Parameter serverContextParameter = new Parameter(
                SynapseConstants.SYNAPSE_SERVER_CTX_INFO, serverContextInformation);
        // set the ServerConfiguration as a parameter
        Parameter serverConfigParameter = new Parameter(
                SynapseConstants.SYNAPSE_SERVER_CONFIG_INFO, serverConfigurationInformation);
        try {
            configurationContext.getAxisConfiguration().addParameter(serverContextParameter);
            configurationContext.getAxisConfiguration().addParameter(serverConfigParameter);
        } catch (AxisFault ignored) {
            log.fatal("Error adding the parameter to the Axis Configuration");
        }

        // we retrieve these properties to initialize the task scheduler in the environment
        Object repo =
                serverContextInformation.getProperty(TaskConstants.TASK_DESCRIPTION_REPOSITORY);
        Object taskScheduler = serverContextInformation.getProperty(TaskConstants.TASK_SCHEDULER);

        if (repo != null && (repo instanceof TaskDescriptionRepository)) {
            this.taskDescriptionRepository = (TaskDescriptionRepository) repo;
        }

        if (taskScheduler != null && (taskScheduler instanceof TaskScheduler)) {
            this.taskScheduler = (TaskScheduler) taskScheduler;
        }

        addDefaultBuildersAndFormatters(configurationContext.getAxisConfiguration());
        initDataSourceHelper(serverContextInformation);
        initSharedSecretCallbackHandlerCache(serverContextInformation);
        initEnterpriseBeanstalkHolder(serverContextInformation);
        initialized = true;
    }


    /**
     * {@inheritDoc}
     */
    public void destroy() {

        try {
            // only if we have created the server
            if (serverConfigurationInformation.isCreateNewInstance()) {

                // destroy listener manager
                if (listenerManager != null) {
                    listenerManager.destroy();
                }

                stopJmxAdapter();
                RMIRegistryController.getInstance().shutDown();

                // we need to call this method to clean the temp files we created.
                if (configurationContext != null) {
                    configurationContext.terminate();
                }
            }
            initialized = false;
        } catch (Exception e) {
            log.error("Error stopping the Axis2 Based Server Environment", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Adds the synapse handlers to the inflow Dispatch phase and starts the listener manager
     * if the axis2 instance is created by the Synapse
     */
    public void start() {

        // add the Synapse handlers
        if (configurationContext != null) {
            List<Phase> inflowPhases
                    = configurationContext.getAxisConfiguration().getInFlowPhases();
            for (Phase inPhase : inflowPhases) {
                // we are interested about the Dispatch phase in the inflow
                if (PhaseMetadata.PHASE_DISPATCH.equals(inPhase.getPhaseName())) {
                    try {
                        inPhase.addHandler(prepareSynapseDispatcher());
                        inPhase.addHandler(prepareMustUnderstandHandler());
                    } catch (PhaseException e) {
                        handleFatal("Couldn't start Synapse, " +
                                "Cannot add the required Synapse handlers", e);
                    }
                }
            }
        } else {
            handleFatal("Couldn't start Synapse, ConfigurationContext not found");
        }

        // if the axis2 instance is created by us, then start the listener manager
        if (serverConfigurationInformation.isCreateNewInstance()) {
            if (listenerManager != null) {
                listenerManager.start();
            } else {
                handleFatal("Couldn't start Synapse, ListenerManager not found");
            }
            /* if JMX Adapter has been configured and started, output usage information rather
               at the end of the startup process to make it more obvious */
            if (jmxAdapter != null && jmxAdapter.isRunning()) {
                log.info("Management using JMX available via: "
                        + jmxAdapter.getJmxInformation().getJmxUrl());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startMaintenance() {
        log.info("Putting transport listeners, senders and tasks into maintenance mode..");

        // pause transport listeners and senders
        Axis2TransportHelper transportHelper = new Axis2TransportHelper(configurationContext);
        transportHelper.pauseListeners();
        transportHelper.pauseSenders();

        // put tasks on hold
        SynapseTaskManager synapseTaskManager = synapseEnvironment.getTaskManager();
        if (synapseTaskManager.isInitialized()) {
            synapseTaskManager.pauseAll();
        }

        log.info("Entered maintenance mode");
    }

    /**
     * {@inheritDoc}
     */
    public void endMaintenance() {
        log.info("Resuming transport listeners, senders and tasks from maintenance mode...");

        // resume transport listeners and senders
        Axis2TransportHelper transportHelper = new Axis2TransportHelper(configurationContext);
        transportHelper.resumeListeners();
        transportHelper.resumeSenders();

        // resume tasks
        SynapseTaskManager synapseTaskManager = synapseEnvironment.getTaskManager();
        if (synapseTaskManager.isInitialized()) {
            synapseTaskManager.resumeAll();
        }

        log.info("Resumed normal operation from maintenance mode");
    }

    /**
     * Cleanup the axis2 environment and stop the synapse environment.
     */
    public void stop() {
        try {
            // stop tasks
            SynapseTaskManager synapseTaskManager = synapseEnvironment.getTaskManager();
            if (synapseTaskManager.isInitialized()) {
                synapseTaskManager.cleanup();
            }

            EnterpriseBeanstalkManager manager = (EnterpriseBeanstalkManager)
                    serverContextInformation.getProperty(EnterpriseBeanstalkConstants.BEANSTALK_MANAGER_PROP_NAME);
            if (manager != null) {
                manager.destroy();
            }

            // stop the listener manager
            if (listenerManager != null) {
                listenerManager.stop();
            }

            // detach the synapse handlers
            if (configurationContext != null) {
                List<Phase> inflowPhases =
                        configurationContext.getAxisConfiguration().getInFlowPhases();
                for (Phase inPhase : inflowPhases) {
                    // we are interested about the Dispatch phase in the inflow
                    if (PhaseMetadata.PHASE_DISPATCH.equals(inPhase.getPhaseName())) {
                        List<HandlerDescription> synapseHandlers
                                = new ArrayList<HandlerDescription>();
                        for (Handler handler : inPhase.getHandlers()) {
                            if (SynapseDispatcher.NAME.equals(handler.getName()) ||
                                    SynapseMustUnderstandHandler.NAME.equals(handler.getName())) {
                                synapseHandlers.add(handler.getHandlerDesc());
                            }
                        }

                        for (HandlerDescription handlerMD : synapseHandlers) {
                            inPhase.removeHandler(handlerMD);
                        }
                    }
                }
            } else {
                handleException("Couldn't detach the Synapse handlers, " +
                        "ConfigurationContext not found.");
            }

            // continue stopping the axis2 environment if we created it
            if (serverConfigurationInformation.isCreateNewInstance() && configurationContext != null
                    && configurationContext.getAxisConfiguration() != null) {
                Map<String, AxisService> serviceMap =
                        configurationContext.getAxisConfiguration().getServices();
                for (AxisService svc : serviceMap.values()) {
                    svc.setActive(false);
                }

                // stop all modules
                Map<String, AxisModule> moduleMap =
                        configurationContext.getAxisConfiguration().getModules();
                for (AxisModule mod : moduleMap.values()) {
                    if (mod.getModule() != null && !"synapse".equals(mod.getName())) {
                        mod.getModule().shutdown(configurationContext);
                    }
                }
            }
        } catch (AxisFault e) {
            log.error("Error stopping the Axis2 Environment");
        }
    }

    /**
     * Setup synapse in axis2 environment and return the created instance.
     *
     * @return SynapseEnvironment instance
     */
    public SynapseEnvironment createSynapseEnvironment() {

        try {
        	deployMediationLibraryArtifacts();
        	deployMediatorExtensions();
            deploySynapseService();
            deployProxyServices();
            deployEventSources();
            //deployMediatorExtensions();
        } catch (AxisFault axisFault) {
            log.fatal("Synapse startup failed...", axisFault);
            throw new SynapseException("Synapse startup failed", axisFault);
        }

        synapseEnvironment = new Axis2SynapseEnvironment(
                configurationContext, synapseConfiguration, serverContextInformation);
        MessageContextCreatorForAxis2.setSynEnv(synapseEnvironment);

        Parameter synapseEnvironmentParameter = new Parameter(
                SynapseConstants.SYNAPSE_ENV, synapseEnvironment);
        try {
            configurationContext.getAxisConfiguration().addParameter(synapseEnvironmentParameter);
        } catch (AxisFault e) {
            handleFatal("Could not set parameter '" + SynapseConstants.SYNAPSE_ENV +
                    "' to the Axis2 configuration : " + e.getMessage(), e);

        }

        synapseEnvironment.getTaskManager().init(taskDescriptionRepository, taskScheduler);
        synapseConfiguration.init(synapseEnvironment);
        synapseEnvironment.setInitialized(true);

        return synapseEnvironment;
    }

	/**
	 * The mediation library deployer will handling the process of deploying the
	 * libararyArtifacts, this is required since the library specific artifacts
	 * has to be initialized priorly for the cases like connectors
	 * 
	 */
	private void deployMediationLibraryArtifacts() {
		if (configurationContext == null || synapseConfiguration == null) {
			return;
		}
		DeploymentEngine deploymentEngine = (DeploymentEngine) configurationContext
				.getAxisConfiguration().getConfigurator();
		String carbonRepoPath = configurationContext.getAxisConfiguration().getRepository()
				.getPath();
		SynapseArtifactDeploymentStore deploymentStore = synapseConfiguration
				.getArtifactDeploymentStore();

		String synapseImportDir = synapseConfiguration.getPathToConfigFile() + File.separator
				+ MultiXMLConfigurationBuilder.SYNAPSE_IMPORTS_DIR;

        /*Registering Import Deployer is not required here.*/
		//deploymentEngine.addDeployer(new ImportDeployer(), synapseImportDir, "xml");

		String libsPath = carbonRepoPath + File.separator + "synapse-libs";
		deploymentEngine.addDeployer(new LibraryArtifactDeployer(), libsPath, "zip");
	}


	/**
     * Destroys the Synapse Environment by undeploying all Axis2 services.
     */
    public void destroySynapseEnvironment() {
        if (synapseEnvironment != null) {
            try {
                undeploySynapseService();
                undeployProxyServices();
                undeployEventSources();
            } catch (AxisFault e) {
                handleFatal("Error while shutting down the Synapse environment", e);
            }
            synapseEnvironment.setInitialized(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    public SynapseConfiguration createSynapseConfiguration() {

        String synapseXMLLocation = serverConfigurationInformation.getSynapseXMLLocation();
        Properties properties = SynapsePropertiesLoader.loadSynapseProperties();
        if (serverConfigurationInformation.getResolveRoot() != null) {
            properties.put(SynapseConstants.RESOLVE_ROOT,
                    serverConfigurationInformation.getResolveRoot());
        }

        if (serverConfigurationInformation.getSynapseHome() != null) {
            properties.put(SynapseConstants.SYNAPSE_HOME,
                    serverConfigurationInformation.getSynapseHome());
        }

        if (synapseXMLLocation != null) {
            synapseConfiguration = SynapseConfigurationBuilder.getConfiguration(
                    synapseXMLLocation, properties);
        } else {
            log.warn("System property or init-parameter '" + SynapseConstants.SYNAPSE_XML +
                    "' is not specified. Using default configuration..");
            synapseConfiguration = SynapseConfigurationBuilder.getDefaultConfiguration();
        }

        Enumeration keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            synapseConfiguration.setProperty(key, properties.getProperty(key));
        }

        // Set the Axis2 ConfigurationContext to the SynapseConfiguration
        synapseConfiguration.setAxisConfiguration(configurationContext.getAxisConfiguration());
        MessageContextCreatorForAxis2.setSynConfig(synapseConfiguration);

        // set the Synapse configuration into the Axis2 configuration
        Parameter synapseConfigurationParameter = new Parameter(
                SynapseConstants.SYNAPSE_CONFIG, synapseConfiguration);
        try {
            configurationContext.getAxisConfiguration().addParameter(synapseConfigurationParameter);
        } catch (AxisFault e) {
            handleFatal("Could not set parameters '" + SynapseConstants.SYNAPSE_CONFIG +
                    "' to the Axis2 configuration : " + e.getMessage(), e);
        }

        addServerIPAndHostEntries();

        return synapseConfiguration;
    }

    /**
     * {@inheritDoc}
     */
    public void destroySynapseConfiguration() {
        if (synapseConfiguration != null) {
            synapseConfiguration.destroy();
            synapseConfiguration = null;
        }
    }


    /**
     * Waits until it is safe to stop or the the specified end time has been reached. A delay
     * of <code>waitIntervalMillis</code> milliseconds is used between each subsequent check.
     * If the state "safeToStop" is reached before the specified <code>endTime</code>,
     * the return value is true.
     *
     * @param waitIntervalMillis the pause time (delay) in milliseconds between subsequent checks
     * @param endTime            the time until which the checks need to finish successfully
     *
     * @return true, if a safe state is reached before the specified <code>endTime</code>,
     *         otherwise false (forceful stop required)
     */
    public boolean waitUntilSafeToStop(long waitIntervalMillis, long endTime) {

        boolean safeToStop = false;
        boolean forcefulStop = false;
        Axis2TransportHelper transportHelper = new Axis2TransportHelper(configurationContext);

        // wait until it is safe to shutdown (listeners and tasks are idle, no callbacks)
        while (!safeToStop && !forcefulStop) {

            int pendingListenerThreads = transportHelper.getPendingListenerThreadCount();
            if (pendingListenerThreads > 0) {
                log.info(new StringBuilder("Waiting for: ").append(pendingListenerThreads)
                        .append(" listener threads to complete").toString());
            }
            int pendingSenderThreads = transportHelper.getPendingSenderThreadCount();
            if (pendingSenderThreads > 0) {
                log.info(new StringBuilder("Waiting for: ").append(pendingSenderThreads)
                        .append(" listener threads to complete").toString());
            }
            int activeConnections = transportHelper.getActiveConnectionsCount();
            if (activeConnections > 0) {
                log.info("Waiting for: " + activeConnections
                        + " active connections to be closed..");
            }
            int pendingTransportThreads = pendingListenerThreads + pendingSenderThreads;

            int pendingCallbacks = serverContextInformation.getCallbackCount();
            if (pendingCallbacks > 0) {
                log.info("Waiting for: " + pendingCallbacks + " callbacks/replies..");
            }

            int runningTasks = 0;
            SynapseTaskManager synapseTaskManager = synapseEnvironment.getTaskManager();
            if (synapseTaskManager.isInitialized()) {
                runningTasks = synapseTaskManager.getTaskScheduler().getRunningTaskCount();
                if (runningTasks > 0) {
                    log.info("Waiting for : " + runningTasks + " tasks to complete..");
                }
            }

            // it is safe to stop if all used listener threads, callbacks and tasks are zero
            safeToStop = ((pendingTransportThreads + pendingCallbacks + runningTasks) == 0);

            if (safeToStop) {
                log.info("All transport threads and tasks are idle and no pending callbacks..");
            } else {
                if (System.currentTimeMillis() < endTime) {
                    log.info(new StringBuilder("Waiting for a maximum of another ")
                            .append((endTime - System.currentTimeMillis()) / 1000)
                            .append(" seconds until transport threads and tasks become idle, ")
                            .append("active connections to get closed,")
                            .append(" and callbacks to be completed..").toString());
                    try {
                        Thread.sleep(waitIntervalMillis);
                    } catch (InterruptedException ignore) {
                        // nothing to do here
                    }
                } else {
                    // maximum time to wait is over, do a forceful stop
                    forcefulStop = true;
                }
            }
        }

        return !forcefulStop;
    }

    public Object getContext() {
        return configurationContext;
    }

    /**
     * Create a Axis2 Based Server Environment
     *
     * @param serverConfigurationInformation ServerConfigurationInformation instance
     */
    private void createNewInstance(ServerConfigurationInformation serverConfigurationInformation) {

        try {
            configurationContext = ConfigurationContextFactory.
                    createConfigurationContextFromFileSystem(
                            serverConfigurationInformation.getAxis2RepoLocation(),
                            serverConfigurationInformation.getAxis2Xml());

            configurationContext.setProperty(
                    AddressingConstants.ADDR_VALIDATE_ACTION, Boolean.FALSE);

            startJmxAdapter();

            listenerManager = configurationContext.getListenerManager();
            if (listenerManager == null) {

                // create and initialize the listener manager but do not start
                listenerManager = new ListenerManager();
                listenerManager.init(configurationContext);
            }

            // do not use the listener manager shutdown hook, because it clashes with the
            // SynapseServer shutdown hook.
            listenerManager.setShutdownHookRequired(false);

        } catch (Throwable t) {
            handleFatal("Failed to create a new Axis2 instance...", t);
        }
    }

    /**
     * Adds Synapse Service to Axis2 configuration which enables the main message mediation.
     *
     * @throws AxisFault if an error occurs during Axis2 service initialization
     */
    private void deploySynapseService() throws AxisFault {

        log.info("Deploying the Synapse service...");
        // Dynamically initialize the Synapse Service and deploy it into Axis2
        AxisConfiguration axisCfg = configurationContext.getAxisConfiguration();
        AxisService synapseService = new AxisService(SynapseConstants.SYNAPSE_SERVICE_NAME);
        AxisOperation mediateOperation = new InOutAxisOperation(
                SynapseConstants.SYNAPSE_OPERATION_NAME);
        mediateOperation.setMessageReceiver(new SynapseMessageReceiver());
        synapseService.addOperation(mediateOperation);
        List<String> transports = new ArrayList<String>();
        transports.add(Constants.TRANSPORT_HTTP);
        transports.add(Constants.TRANSPORT_HTTPS);
        synapseService.setExposedTransports(transports);
        AxisServiceGroup synapseServiceGroup = new AxisServiceGroup(axisCfg);
        synapseServiceGroup.setServiceGroupName(SynapseConstants.SYNAPSE_SERVICE_NAME);
        synapseServiceGroup.addParameter(SynapseConstants.HIDDEN_SERVICE_PARAM, "true");
        synapseServiceGroup.addService(synapseService);
        axisCfg.addServiceGroup(synapseServiceGroup);
    }

    /**
     * Removes the Synapse Service from the Axis2 configuration.
     *
     * @throws AxisFault if an error occurs during Axis2 service removal
     */
    private void undeploySynapseService() throws AxisFault {
        log.info("Undeploying the Synapse service...");
        configurationContext.getAxisConfiguration().removeService(
                SynapseConstants.SYNAPSE_SERVICE_NAME);
    }

    /**
     * Adds all Synapse proxy services to the Axis2 configuration.
     */
    private void deployProxyServices() {

        boolean failSafeProxyEnabled = SynapseConfigUtils.isFailSafeEnabled(
                SynapseConstants.FAIL_SAFE_MODE_PROXY_SERVICES);

        log.info("Deploying Proxy services...");
        String thisServerName = serverConfigurationInformation.getServerName();
        if (thisServerName == null || "".equals(thisServerName)) {
            thisServerName = serverConfigurationInformation.getHostName();
            if (thisServerName == null || "".equals(thisServerName)) {
                thisServerName = "localhost";
            }
        }

        for (ProxyService proxy : synapseConfiguration.getProxyServices()) {

            // start proxy service if either, pinned server name list is empty
            // or pinned server list has this server name
            List pinnedServers = proxy.getPinnedServers();
            if (pinnedServers != null && !pinnedServers.isEmpty()) {
                if (!pinnedServers.contains(thisServerName)) {
                    log.info("Server name not in pinned servers list." +
                            " Not deploying Proxy service : " + proxy.getName());
                    continue;
                }
            }

            try {
                AxisService proxyService = proxy.buildAxisService(synapseConfiguration,
                        configurationContext.getAxisConfiguration());
                if (proxyService != null) {
                    log.info("Deployed Proxy service : " + proxy.getName());
                    if (!proxy.isStartOnLoad()) {
                        proxy.stop(synapseConfiguration);
                    }
                } else {
                    log.warn("The proxy service " + proxy.getName() + " will NOT be available");
                }
            } catch (SynapseException e) {
                if (failSafeProxyEnabled) {
                    log.warn("The proxy service " + proxy.getName() + " cannot be deployed - " +
                            "Continue in Proxy Service fail-safe mode.");
                } else {
                    handleException("The proxy service " + proxy.getName() + " : Deployment Error");
                }
            }
        }
    }
    /**
     * Removes all Synapse proxy services from the Axis2 configuration.
     *
     * @throws AxisFault if an error occurs undeploying proxy services
     */
    private void undeployProxyServices() throws AxisFault {

        log.info("Undeploying Proxy services...");

        for (ProxyService proxy : synapseConfiguration.getProxyServices()) {
            configurationContext.getAxisConfiguration().removeService(
                    proxy.getName());
        }
    }

    /**
     * Deploys the mediators in the mediator extensions folder.
     */
    private void deployMediatorExtensions() {
        log.info("Loading mediator extensions...");
        AxisConfigurator configurator = configurationContext.getAxisConfiguration().getConfigurator();
        if (configurator instanceof DeploymentEngine) {
            ((DeploymentEngine) configurator).getRepoListener().checkServices();
        } else {
            log.warn("Unable to access the repository listener. Custom extensions will " +
                    "not get loaded now!");
        }
    }

    /**
     * Deploys all event sources.
     *
     * @throws AxisFault if an error occurs deploying the event sources.
     */
    private void deployEventSources() throws AxisFault {
        log.info("Deploying EventSources...");
        for (SynapseEventSource eventSource : synapseConfiguration.getEventSources()) {
            eventSource.buildService(configurationContext.getAxisConfiguration());
        }
    }

    /**
     * Undeploys all event sources.
     *
     * @throws AxisFault if an error occurs undeploying the event sources.
     */
    private void undeployEventSources() throws AxisFault {
        log.info("Undeploying EventSources...");
        for (SynapseEventSource eventSource : synapseConfiguration.getEventSources()) {
            configurationContext.getAxisConfiguration().removeService(eventSource.getName());
        }
    }

    /**
     * Initiating DataSourceRepositoryHolder with a new data source information repository or
     * reusing an existing repository.
     *
     * @param serverContextInformation ServerContextInformation instance
     */
    private void initDataSourceHelper(ServerContextInformation serverContextInformation) {
        DataSourceRepositoryHolder repositoryHolder = DataSourceRepositoryHolder.getInstance();
        Properties synapseProperties = SynapsePropertiesLoader.reloadSynapseProperties();
        Object repo =
                serverContextInformation.getProperty(
                        DataSourceConstants.DATA_SOURCE_INFORMATION_REPOSITORY);
        if (repo instanceof DataSourceInformationRepository) {
            repositoryHolder.init((DataSourceInformationRepository) repo, synapseProperties);
        } else {
            repositoryHolder.init(null, synapseProperties);
        }
    }

    /**
     * Initiating SharedSecretCallbackHandlerCache reusing an existing SecretCallbackHandler instance -
     * a SecretCallbackHandler passed when start synapse.
     *
     * @param information ServerContextInformation instance
     */
    private void initSharedSecretCallbackHandlerCache(ServerContextInformation information) {
        SharedSecretCallbackHandlerCache cache = SharedSecretCallbackHandlerCache.getInstance();
        Object handler =
                information.getProperty(
                        SecurityConstants.PROP_SECRET_CALLBACK_HANDLER);
        if (handler instanceof SecretCallbackHandler) {
            cache.setSecretCallbackHandler((SecretCallbackHandler) handler);
        }
    }

    private synchronized void initEnterpriseBeanstalkHolder(ServerContextInformation serverContextInformation) {
        if (serverContextInformation.getProperty(EnterpriseBeanstalkConstants.BEANSTALK_MANAGER_PROP_NAME) == null) {
            EnterpriseBeanstalkManager beanstalkHolder = new EnterpriseBeanstalkManager();
            Properties synapseProperties = SynapsePropertiesLoader.reloadSynapseProperties();
            beanstalkHolder.init(synapseProperties);
            serverContextInformation.addProperty(EnterpriseBeanstalkConstants.BEANSTALK_MANAGER_PROP_NAME,
                    beanstalkHolder);
        }
    }

    private void addDefaultBuildersAndFormatters(AxisConfiguration axisConf) {
        if (axisConf.getMessageBuilder("text/plain") == null) {
            axisConf.addMessageBuilder("text/plain", new PlainTextBuilder());
        }
        if (axisConf.getMessageBuilder("application/octet-stream") == null) {
            axisConf.addMessageBuilder("application/octet-stream", new BinaryBuilder());
        }
    }

    private void addServerIPAndHostEntries() {
        String hostName = serverConfigurationInformation.getHostName();
        String ipAddress = serverConfigurationInformation.getIpAddress();
        if (hostName != null && !"".equals(hostName)) {
            Entry entry = new Entry(SynapseConstants.SERVER_HOST);
            entry.setValue(hostName);
            synapseConfiguration.addEntry(SynapseConstants.SERVER_HOST, entry);
        }

        if (ipAddress != null && !"".equals(ipAddress)) {
            Entry entry = new Entry(SynapseConstants.SERVER_IP);
            entry.setValue(ipAddress);
            if (synapseConfiguration.getAxisConfiguration().getTransportsIn() != null) {
                Map<String, TransportInDescription> transportInConfigMap = synapseConfiguration.getAxisConfiguration().getTransportsIn();
                if (transportInConfigMap != null) {
                    TransportInDescription transportInDescription = transportInConfigMap.get("http");
                    if (transportInDescription != null) {
                        Parameter bindAddressParam = transportInDescription.getParameter("bind-address");
                        if (bindAddressParam != null) {
                            entry.setValue(bindAddressParam.getValue());
                        }
                    }
                }
            }
            synapseConfiguration.addEntry(SynapseConstants.SERVER_IP, entry);
        }
    }

    private HandlerDescription prepareSynapseDispatcher() {
        HandlerDescription handlerMD = new HandlerDescription(SynapseDispatcher.NAME);
        // <order after="SOAPMessageBodyBasedDispatcher" phase="Dispatch"/>
        PhaseRule rule = new PhaseRule(PhaseMetadata.PHASE_DISPATCH);
        rule.setAfter(SOAPMessageBodyBasedDispatcher.NAME);
        handlerMD.setRules(rule);
        SynapseDispatcher synapseDispatcher = new SynapseDispatcher();
        synapseDispatcher.initDispatcher();
        handlerMD.setHandler(synapseDispatcher);
        return handlerMD;
    }

    private HandlerDescription prepareMustUnderstandHandler() {
        HandlerDescription handlerMD
                = new HandlerDescription(SynapseMustUnderstandHandler.NAME);
        // <order after="SynapseDispatcher" phase="Dispatch"/>
        PhaseRule rule = new PhaseRule(PhaseMetadata.PHASE_DISPATCH);
        rule.setAfter(SynapseDispatcher.NAME);
        handlerMD.setRules(rule);
        SynapseMustUnderstandHandler synapseMustUnderstandHandler
                = new SynapseMustUnderstandHandler();
        synapseMustUnderstandHandler.init(handlerMD);
        handlerMD.setHandler(synapseMustUnderstandHandler);
        return handlerMD;
    }

    /**
     * Starts the JMX Adaptor.
     *
     * @throws  SynapseException  if the JMX configuration is erroneous and/or the connector server
     *                            cannot be started
     */
    private void startJmxAdapter() {
        Properties synapseProperties = SynapsePropertiesLoader.loadSynapseProperties();
        JmxInformation jmxInformation = JmxInformationFactory.createJmxInformation(
                synapseProperties, serverConfigurationInformation.getHostName());

        // Start JMX Adapter only if at least a JMX JNDI port is configured
        if (jmxInformation.getJndiPort() != -1) {
            jmxAdapter = new JmxAdapter(jmxInformation);
            jmxAdapter.start();
        }
    }

    /**
     * Stops the JMX Adaptor.
     */
    private void stopJmxAdapter() {
        if (jmxAdapter != null) {
            jmxAdapter.stop();
        }
    }

    private void handleFatal(String msg, Throwable e) {
        log.fatal(msg, e);
        throw new SynapseException(msg, e);
    }

    private void handleFatal(String msg) {
        log.fatal(msg);
        throw new SynapseException(msg);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }
}
