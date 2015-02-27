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

package org.apache.synapse.config;

import org.apache.axiom.om.OMNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.apache.synapse.config.xml.XMLConfigurationBuilder;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.builtin.DropMediator;
import org.apache.synapse.mediators.builtin.LogMediator;
import org.apache.synapse.registry.Registry;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Builds a Synapse Configuration model with a given input
 * (e.g. XML, programmatic creation, default etc)
 */
public class SynapseConfigurationBuilder {

    private static Log log = LogFactory.getLog(SynapseConfigurationBuilder.class);

    /**
     * Return the default Synapse Configuration
     * @return the default configuration to be used
     */
    public static SynapseConfiguration getDefaultConfiguration() {
        // programatically create an empty configuration which just log and drop the messages 
        SynapseConfiguration config = SynapseConfigUtils.newConfiguration();
        SequenceMediator mainMediator = new SequenceMediator();
        mainMediator.addChild(new LogMediator());
        mainMediator.addChild(new DropMediator());
        mainMediator.setName(SynapseConstants.MAIN_SEQUENCE_KEY);
        config.addSequence(SynapseConstants.MAIN_SEQUENCE_KEY, mainMediator);
        SequenceMediator faultMediator = new SequenceMediator();
        LogMediator fault = new LogMediator();
        fault.setLogLevel(LogMediator.FULL);
        faultMediator.addChild(fault);
        faultMediator.setName(SynapseConstants.FAULT_SEQUENCE_KEY);
        config.addSequence(SynapseConstants.FAULT_SEQUENCE_KEY, faultMediator);
        config.setDescription("The default configuration of the ESB, that is created " +
                "programatically at the startup");
        return config;
    }

    /**
     * Build a Synapse configuration from a given XML configuration file
     *
     * @param configFile Path to the Synapse configuration file or directory
     * @param properties bag of properties to be passed into the builder
     * @return the Synapse configuration model
     */
    public static SynapseConfiguration getConfiguration(String configFile, Properties properties) {

        File synapseConfigLocation = new File(configFile);
        if (!synapseConfigLocation.exists()) {
            String message = "Unable to load the Synapse configuration from : "
                    + configFile + ". Specified file not found";
            log.fatal(message);
            throw new SynapseException(message);
        }

        SynapseConfiguration synCfg = null;
        if (synapseConfigLocation.isFile()) {
            // build the Synapse configuration parsing the XML config file
            try {
                synCfg = XMLConfigurationBuilder.getConfiguration(
                        new FileInputStream(configFile), properties);
                log.info("Loaded Synapse configuration from : " + configFile);
            } catch (Exception e) {
                handleException("Could not initialize Synapse : " + e.getMessage(), e);
            }

        } else if (synapseConfigLocation.isDirectory()) {
            // build the Synapse configuration by processing given directory hierarchy
            synCfg = MultiXMLConfigurationBuilder.getConfiguration(configFile, properties);
            log.info("Loaded Synapse configuration from the artifact " +
                    "repository at : " + configFile);
        }

        assert synCfg != null;
        synCfg.setPathToConfigFile(new File(configFile).getAbsolutePath());
        Registry localConfigReg = synCfg.getRegistry();
        if (synCfg.getLocalRegistry().isEmpty() && synCfg.getProxyServices().isEmpty()
                && localConfigReg != null) {
            if (log.isDebugEnabled()) {
                log.debug("Only the registry is defined in the synapse configuration, trying " +
                        "to fetch a configuration from the registry");
            }
            // TODO: support a artifact repository for registry as well instead of just the synapse.xml
            OMNode remoteConfigNode = localConfigReg.lookup("synapse.xml");
            if (remoteConfigNode != null) {
                try {
                    synCfg = XMLConfigurationBuilder.getConfiguration(SynapseConfigUtils
                            .getStreamSource(remoteConfigNode).getInputStream(), properties);
                    // TODO: when you fetch the configuration and serialize the config in any case
                    // TODO: the remote config is serialized to the synapse.xml we should prevent
                    // TODO: that, and should serialize the config to the registry
                    if (synCfg.getRegistry() == null) {
                        synCfg.setRegistry(localConfigReg);
                    } else {
                        log.warn("Registry declaration has been overwritten by the registry " +
                                "declaration found at the remote configuration");
                    }
                } catch (XMLStreamException xse) {
                    throw new SynapseException("Problem loading remote synapse.xml ", xse);
                }
            } else if (log.isDebugEnabled()) {
                log.debug("Couldn't find a synapse configuration on the registry");
            }
        }

        // Check for the main sequence and add a default main sequence if not present
        if (synCfg.getMainSequence() == null) {
            SynapseConfigUtils.setDefaultMainSequence(synCfg);
        }

        // Check for the fault sequence and add a default fault sequence if not present
        if (synCfg.getFaultSequence() == null) {
            SynapseConfigUtils.setDefaultFaultSequence(synCfg);
        }

        return synCfg;
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
