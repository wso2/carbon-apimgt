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

import java.util.concurrent.CountDownLatch;

/**
 * This is the class invoked by the command line scripts synapse.sh and synapse-daemon.sh to
 * start an instance of Synapse. This class calls on the ServerManager to start up the instance
 *
 * TODO Switch to using commons-cli and move all command line parameter processing etc from the
 * .sh and .bat into this.. for 1.3 release :)
 */
public class SynapseServer {

    private static final Log log = LogFactory.getLog(SynapseServer.class);

    private static final String USAGE_TXT =
            "Usage: SynapseServer <axis2_repository> <axis2_xml> <synapse_home> <synapse_xml> " +
                    "<resolve_root> <deployment mode>" +
                    "\n Opts: -? this message";

    /** This is the class to control the synapse server */
    private static ServerManager serverManager;

    public static void printUsage() {
        System.out.println(USAGE_TXT);
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {

        // first check if we should print usage
        if (args.length <= 0 || args.length == 2 || args.length == 3 || args.length >= 8) {
            printUsage();
        }

        log.info("Starting Apache Synapse...");

        // create the server configuration using the commandline arguments
        ServerConfigurationInformation configurationInformation =
                ServerConfigurationInformationFactory.createServerConfigurationInformation(args);

        serverManager = new ServerManager();
        serverManager.init(configurationInformation, null);

        try {
            serverManager.start();
            addShutdownHook();
            log.info("Apache Synapse started successfully");

            // Put the main thread into wait state. This makes sure that the Synapse server
            // doesn't stop immediately if ServerManager#start doesn't create any non daemon
            // threads (see also SYNAPSE-425).
            new CountDownLatch(1).await();

        } catch (SynapseException e) {
            log.error("Error starting Apache Synapse, trying a clean shutdown...", e);
            serverManager.shutdown();
        }
    }

    private static void addShutdownHook() {
        Thread shutdownHook = new Thread() {
            public void run() {
                log.info("Shutting down Apache Synapse...");
                try {
                    serverManager.shutdown();
                    log.info("Apache Synapse shutdown complete");
                    log.info("Halting JVM");
                } catch (Exception e) {
                    log.error("Error occurred while shutting down Apache Synapse, " +
                            "it may not be a clean shutdown", e);
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }
}
