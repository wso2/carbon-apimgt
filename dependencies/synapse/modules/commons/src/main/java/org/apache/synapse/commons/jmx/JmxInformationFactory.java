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

package org.apache.synapse.commons.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.util.MiscellaneousUtil;
import org.wso2.securevault.secret.SecretInformation;
import org.wso2.securevault.secret.SecretInformationFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Factory to create a JmxInformation based on given properties.
 */

public class JmxInformationFactory {

    private static final Log log = LogFactory.getLog(JmxInformationFactory.class);

    private JmxInformationFactory() {
    }

    /**
     * Factory method to create a JmxInformation instance based on given properties
     *
     * @param properties Properties to create and configure DataSource
     * @param defaultHostName the default host name tobe used in case of a the host name is not set
     * @return DataSourceInformation instance
     */
    public static JmxInformation createJmxInformation(Properties properties, String defaultHostName) {

        // Prefix for getting particular JMX properties
        String prefix = JmxConfigurationConstants.PROP_SYNAPSE_PREFIX_JMX;

        JmxInformation jmxInformation = new JmxInformation();

        SecretInformation secretInformation = SecretInformationFactory.createSecretInformation(
                properties, prefix, null);
        secretInformation.setToken(JmxConfigurationConstants.JMX_PROTECTED_TOKEN);

        jmxInformation.setSecretInformation(secretInformation);

        int jndiPort = MiscellaneousUtil.getProperty(
                properties, prefix + JmxConfigurationConstants.PROP_JNDI_PORT, -1, Integer.class);
        jmxInformation.setJndiPort(jndiPort);

        int rmiPort = MiscellaneousUtil.getProperty(
                properties, prefix + JmxConfigurationConstants.PROP_RMI_PORT, 0, Integer.class);
        jmxInformation.setRmiPort(rmiPort);

        String jmxHostName = MiscellaneousUtil.getProperty(
                properties, prefix + JmxConfigurationConstants.PROP_HOSTNAME, null);
        if (jmxHostName == null || jmxHostName.trim().length() == 0) {
            jmxHostName = defaultHostName;
        }
        jmxInformation.setHostName(jmxHostName);

        // begin of special JMX security options
        Properties managementProperties = readManagementProperties();

        Boolean authenticate;
        String value = getConfigProperty(
                managementProperties, "com.sun.management.jmxremote.authenticate");
        if (value != null) {
            authenticate = Boolean.valueOf(value);
        } else {
            if (secretInformation.getUser() == null) {
                authenticate = Boolean.FALSE;
            } else {
                authenticate = Boolean.TRUE;
            }
        }
        jmxInformation.setAuthenticate(authenticate);

        value = getConfigProperty(managementProperties, "com.sun.management.jmxremote.access.file");
        if (value == null || value.trim().length() == 0) {
            value = MiscellaneousUtil.getProperty(
                    properties, prefix + JmxConfigurationConstants.PROP_REMOTE_ACCESS_FILE, null);
        }
        if (value != null && value.trim().length() > 0) {
            jmxInformation.setRemoteAccessFile(value);
        }

        value = getConfigProperty(managementProperties, "com.sun.management.jmxremote.password.file");
        if (value != null && value.trim().length() > 0) {
            jmxInformation.setRemotePasswordFile(value);
        }

        Boolean remoteSSL;
        value = getConfigProperty(managementProperties, "com.sun.management.jmxremote.ssl");
        if (value != null) {
            remoteSSL = Boolean.valueOf(value);
        } else {
            remoteSSL = MiscellaneousUtil.getProperty( properties,
                prefix + JmxConfigurationConstants.PROP_REMOTE_SSL, Boolean.FALSE, Boolean.class);
        }
        jmxInformation.setRemoteSSL(remoteSSL);

        return jmxInformation;
    }

    /**
     * Retrieves the management properties if a JMX config file has been specified via the system
     * property <code>com.sun.management.config.file</code>.
     *
     * @return JMX management properties
     */
    private static Properties readManagementProperties() {

        Properties managementProperties = new Properties();
        String configFileName = System.getProperty("com.sun.management.config.file");
        if (configFileName != null) {
            FileInputStream configFile = null;
            try {
                configFile = new FileInputStream(configFileName);
                managementProperties.load(configFile);
                if (log.isDebugEnabled()) {
                    log.debug("Initialized management properties from file " + configFileName);
                }
            } catch (FileNotFoundException ex) {
                log.error("Cannot open " + configFileName, ex);
            } catch (IOException ex) {
                log.error("Error while reading " + configFileName, ex);
            } finally {
                if (configFile != null) {
                    try {
                        configFile.close();
                    } catch (IOException ignore) {
                        // nothing to do here
                    }
                }
            }
        }
        return managementProperties;
    }

    /**
     * Retrieves a JMX configuration property (first by looking for a Java system property and if
     * not present by looking for a management property specified in a file specified via<code>
     * com.sun.management.config.file</code> system property.
     *
     * @param managementProperties properties tobe looked up if the system property is not set
     * @param   name  the name of the property to look up
     *
     * @return  the config property value or null if the property is not configured
     */
    private static String getConfigProperty(Properties managementProperties, String name) {
        String result = System.getProperty(name);
        if ((result == null) && (managementProperties != null)) {
            result = managementProperties.getProperty(name);
        }
        return result;
    }

}
