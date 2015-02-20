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
import org.wso2.securevault.PasswordManager;
import org.wso2.securevault.secret.SecretInformation;
import org.apache.synapse.commons.util.RMIRegistryController;
import org.apache.synapse.commons.jmx.JmxInformation;
import org.apache.synapse.commons.jmx.JmxSecretAuthenticator;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * JMX Adaptor class providing a JMX server connector to be able to query MBeans via JConsole or any
 * other JMX-compatible management solution.<br>
 * The provided JNDI port will be used to create a local RMI registry. If no RMI port will be
 * provided dynamic RMI ports will be used for remote MBean queries.<br>
 * If the JMXAdaptor shall be used in a firewalled environment, additionally a fixed RMI port should
 * be provided and both ports should be opened in the firewall.<br>
 * JMX URL used if only JNDI port is provided:<br>
 * <code>service:jmx:rmi:///jndi/rmi://<hostname>:<jndiPort>/synapse</code><br>
 * JMX URL used if JNDI port and RMI port are provided:<br>
 * <code>service:jmx:rmi://<hostname>:<rmiPort>/jndi/rmi://<hostname>:<jndiPort>/synapse</code><br>
 */
public class JmxAdapter {

    /**
     * Logger of this class.
     */
    private static Log log = LogFactory.getLog(JmxAdapter.class);

    /**
     * Base port to start with if automatic free port detection is used (default). Configurable in
     * synapse.properties via synapse.jmx.jndiPort=0.
     */
    private static final int JNDI_AUTO_PORT_OFFSET = 1099;

    /**
     * Encapsulates all information needed to configure the JMX Adapter.
     */
    private JmxInformation jmxInformation;

    /**
     * @see  JMXConnectorServer
     */
    private JMXConnectorServer connectorServer;

    /**
     * Creates a new instance of a JMX Adaptor using the provided JMX information.
     *
     * @param  jmxInformation  any JMX related information
     */
    public JmxAdapter(JmxInformation jmxInformation) {
        this.jmxInformation = jmxInformation;
    }

    /**
     * Lazily creates the RMI registry and starts the JMX connector server based on the 
     *
     * @throws  SynapseException  if the JMX configuration is erroneous and/or the connector server
     *                            cannot be started
     */
    public void start() {
        initConfiguration();

        try {
            boolean registryCreated = false;
            int jndiPort = jmxInformation.getJndiPort();
            
            // automatic detection starting at base port
            if (jndiPort == 0) {
                jndiPort = JNDI_AUTO_PORT_OFFSET;
                for (int retries = 0; !registryCreated && (retries < 100); retries++) {
                    try {
                        RMIRegistryController.getInstance().createLocalRegistry(jndiPort);
                        registryCreated = true;
                    } catch (Exception ignored) {
                        jndiPort++;
                        log.warn("Trying alternate port " + jndiPort);
                    }
                }
                jmxInformation.setJndiPort(jndiPort);
            } else {
                RMIRegistryController.getInstance().createLocalRegistry(jndiPort);
                registryCreated = true;
            }
            
            if (registryCreated) {
                jmxInformation.updateJMXUrl();
                JMXServiceURL url = new JMXServiceURL(jmxInformation.getJmxUrl());
                Map<String, Object> env = createContextMap();
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
                try {
                    connectorServer.start();
                } catch (IOException ex) {
                    log.warn("Cannot start JMXConnectorServer on " + jmxInformation.getJmxUrl(), ex);
                }
            }
        } catch (Exception ex) {
            log.error("Error while setting up remote JMX connector", ex);
        }
    }

    /**
     * Stops the JMX connector server.
     */
    public void stop() {
        if (connectorServer != null) {
            log.info("JMXConnectorServer stopping on " + jmxInformation.getJmxUrl());
            try {
                connectorServer.stop();
                RMIRegistryController.getInstance().removeLocalRegistry(jmxInformation.getJndiPort());
                jmxInformation = null;
            } catch (IOException ex) {
                log.error("Error while stopping remote JMX connector", ex);
            }
            connectorServer = null;
        }
    }

    /**
     * Initialized the JMX configuration.
     *
     * @throws  SynapseException  if the port or host configuration is erroneous
     */
    private void initConfiguration() {
        int jndiPort = jmxInformation.getJndiPort();
        if ((jndiPort < 0) || (65535 < jndiPort)) {
            throw new SynapseException("JNDI Port for Remote Registry not properly configured");
        }

        int rmiPort = jmxInformation.getRmiPort();
        if ((rmiPort < 0) || (65535 < rmiPort)) {
            rmiPort = 0;
            log.info("No or invalid value specified for JMX RMI port - using dynamic port");
        }

        String hostname = jmxInformation.getHostName();
        if ((hostname == null) || (hostname.trim().length() == 0)) {
            try {
                InetAddress address = InetAddress.getLocalHost();
                jmxInformation.setHostName(address.getHostName());
            } catch (UnknownHostException ex) {
                throw new SynapseException("Hostname of loopback could not be determined", ex);
            }
        }
    }

    /**
     * Determines whether the JMX Connector server has been started and is running.
     * 
     * @return true, if the connector server is running, otherwise false
     */
    public boolean isRunning() {
        return connectorServer != null && connectorServer.isActive();
    }

    public JmxInformation getJmxInformation() {
        return jmxInformation;
    }

    public void setJmxInformation(JmxInformation jmxInformation) {
        this.jmxInformation = jmxInformation;
    }

    /**
     * Creates an environment context map containing the configuration used to start the
     * server connector.
     * 
     * @return an environment context map containing the configuration used to start the server 
     *         connector
     */
    private Map<String, Object> createContextMap() {
        Map<String, Object> env = new HashMap<String, Object>();

        if (jmxInformation.isAuthenticate()) {

            if (jmxInformation.getRemotePasswordFile() != null) {
                env.put("jmx.remote.x.password.file", jmxInformation.getRemotePasswordFile());
            } else {
                SecretInformation secretInformation = jmxInformation.getSecretInformation();
                // Get the global secret resolver
                //TODO This should be properly implemented if JMX adapter is going to use out side synapse
                PasswordManager pwManager = PasswordManager.getInstance();
                if (pwManager.isInitialized()) {
                    secretInformation.setGlobalSecretResolver(pwManager.getSecretResolver());
                }
                env.put(JMXConnectorServer.AUTHENTICATOR,
                        new JmxSecretAuthenticator(jmxInformation.getSecretInformation()));
            }

            if (jmxInformation.getRemoteAccessFile() != null) {
                env.put("jmx.remote.x.access.file", jmxInformation.getRemoteAccessFile());
            }
        } else {
            log.warn("Using unsecured JMX remote access!");
        }

        if (jmxInformation.isRemoteSSL()) {
            log.info("Activated SSL communication");
            env.put("jmx.remote.rmi.client.socket.factory", new SslRMIClientSocketFactory());
            env.put("jmx.remote.rmi.server.socket.factory", new SslRMIServerSocketFactory());
        }
        
        return env;
    }
}
