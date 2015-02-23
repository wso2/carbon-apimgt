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
package org.apache.synapse.core.axis2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ServerConfigurationInformation;
import org.apache.synapse.ServerConfigurationInformationFactory;
import org.apache.synapse.ServerManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * When Synapse is deployed on a WAR container, this is the init servlet that kicks off the
 * Synapse instance, calling on the ServerManager
 */

public class SynapseStartUpServlet extends HttpServlet {

    private static Log log = LogFactory.getLog(SynapseStartUpServlet.class);
    private static final String ALREADY_INITED = "synapseAlreadyInited";

    public static final String SYNAPSE_SERVER_MANAGER = "synapse.server.manager";

    public void init() throws ServletException {
        ServletConfig servletConfig = getServletConfig();
        ServletContext servletContext = servletConfig.getServletContext();
        if (Boolean.TRUE.equals(servletContext.getAttribute(ALREADY_INITED))) {
            return;
        }
        ServerManager serverManager = new ServerManager();
        ServerConfigurationInformation information =
                ServerConfigurationInformationFactory.
                        createServerConfigurationInformation(servletConfig);
        serverManager.init(information, null);
        serverManager.start();
        servletContext.setAttribute(ALREADY_INITED, Boolean.TRUE);

        servletContext.setAttribute(SYNAPSE_SERVER_MANAGER, serverManager);
    }


    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
    }

    public void destroy() {
        try {
            Object o = getServletConfig().getServletContext().getAttribute(SYNAPSE_SERVER_MANAGER);
            if (o != null && o instanceof ServerManager) {
                ServerManager serverManager = (ServerManager) o;
                serverManager.stop();
                getServletContext().removeAttribute(ALREADY_INITED);
            }
        } catch (Exception e) {
            log.error("Error stopping the Synapse listener manager", e);
        }
    }
}
