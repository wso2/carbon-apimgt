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
import org.apache.synapse.config.SynapseConfigUtils;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Factory method for create a  SynapseController instance
 */
public class SynapseControllerFactory {

    private static final Log log = LogFactory.getLog(SynapseControllerFactory.class);

    /**
     * Create a SynapseController instance based on information in the ServerConfigurationInformation
     *
     * @param information ServerConfigurationInformation instance
     * @return SynapseController instance
     */
    public static SynapseController createSynapseController(
            ServerConfigurationInformation information) {
        validate(information);
        return loadSynapseController(information);
    }

    private static SynapseController loadSynapseController(
            ServerConfigurationInformation information) {

        String provider = information.getServerControllerProvider();
        try {
            Class aClass = SynapseControllerFactory.class.getClassLoader().loadClass(provider);
            Object instance = aClass.newInstance();

            if (instance != null && instance instanceof SynapseController) {
                return (SynapseController) instance;
            } else {
                handleFatal("Invalid class as SynapseController : Class Name : " + provider);
            }

        } catch (ClassNotFoundException e) {
            handleFatal("A SynapseController cannot be found for class name : " + provider, e);
        } catch (IllegalAccessException e) {
            handleFatal("Error creating a instance from class : " + provider, e);
        } catch (InstantiationException e) {
            handleFatal("Error creating a instance from class : " + provider, e);
        }
        return null;
    }

    /**
     * Validate core settings for startup
     *
     * @param information ServerConfigurationInformation to be validated
     */
    private static void validate(ServerConfigurationInformation information) {

        if (information == null) {
            handleFatal("Server Configuration Information is null");
        } else {

            validatePath("Synapse home", information.getSynapseHome());
            if (information.isCreateNewInstance()) {
                validatePath("Axis2 repository", information.getAxis2RepoLocation());
                validatePath("axis2.xml location", information.getAxis2Xml());
            }
            validatePath("synapse.xml location", information.getSynapseXMLLocation());

            String serverName = information.getServerName();
            if (serverName == null) {
                try {
                    serverName = InetAddress.getLocalHost().getHostName();
                } catch (UnknownHostException ignore) {}
                log.info("The server name was not specified, defaulting to : " + serverName);
            } else {
                log.info("Using server name : " + serverName);
            }

            if (log.isDebugEnabled()) {
                log.debug("Using Server Configuration As : " + information);
            }

            log.info("The timeout handler will run every : " +
                    (SynapseConfigUtils.getTimeoutHandlerInterval() / 1000) + "s");
        }
    }

    private static void validatePath(String msgPre, String path) {
        if (path == null) {
            handleFatal("The " + msgPre + " must be set as a system property or init-parameter");
        } else if (!new File(path).exists()) {
            handleFatal("The " + msgPre + " " + path + " doesn't exist");
        } else {
            log.info("Using " + msgPre + " : " + new File(path).getAbsolutePath());
        }
    }

    private static void handleFatal(String msg) {
        log.fatal(msg);
        throw new SynapseException(msg);
    }

    private static void handleFatal(String msg, Exception e) {
        log.fatal(msg, e);
        throw new SynapseException(msg, e);
    }
}
