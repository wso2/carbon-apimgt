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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.AxisServlet;
import org.apache.axis2.transport.http.ListingAgent;
import org.apache.synapse.ServerManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * Extends axis2 servlet functionality so that  avoid  starting listeners again
 */

public class SynapseAxisServlet extends AxisServlet {
    
    /**
     * Overrides init method so that avoid  starting listeners again
     *
     * @param config the servlet configuration on which synapse initializes.
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();

        ServerManager serverManager = (ServerManager) config.getServletContext().
                getAttribute(SynapseStartUpServlet.SYNAPSE_SERVER_MANAGER);
        if (serverManager != null) {
            this.configContext = (ConfigurationContext) serverManager.
                    getServerContextInformation().getServerContext();
            this.axisConfiguration = this.configContext.getAxisConfiguration();
            servletContext.setAttribute(this.getClass().getName(), this);
            this.servletConfig = config;
            agent = new ListingAgent(configContext);
            initParams();
        }
    }

    public void initContextRoot(HttpServletRequest req) {
        this.configContext.setContextRoot("/");
    }
}
