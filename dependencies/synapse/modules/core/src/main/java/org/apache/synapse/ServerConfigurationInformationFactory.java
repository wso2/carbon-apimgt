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

import org.apache.axis2.Constants;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import java.io.File;

/**
 * Contains factory method for creating ServerConfigurationInformation based on various
 */
public class ServerConfigurationInformationFactory {

    private static final Log log = LogFactory.getLog(ServerConfigurationInformationFactory.class);

    /**
     * Factory method for create a ServerConfigurationInformation
     * based on information from AxisConfiguration
     *
     * @param axisConfiguration AxisConfiguration instance
     * @return ServerConfigurationInformation instance
     */
    public static ServerConfigurationInformation createServerConfigurationInformation(
            AxisConfiguration axisConfiguration) {

        ServerConfigurationInformation information = new ServerConfigurationInformation();

        information.setSynapseHome(getAxis2ParameterValue(axisConfiguration,
                SynapseConstants.Axis2Param.SYNAPSE_HOME));
        information.setSynapseXMLLocation(getAxis2ParameterValue(axisConfiguration,
                SynapseConstants.Axis2Param.SYNAPSE_CONFIG_LOCATION));
        information.setServerName(getAxis2ParameterValue(axisConfiguration,
                SynapseConstants.Axis2Param.SYNAPSE_SERVER_NAME));
        information.setResolveRoot(getAxis2ParameterValue(axisConfiguration,
                SynapseConstants.Axis2Param.SYNAPSE_RESOLVE_ROOT));

        return information;

    }

    /**
     * Factory method for create a ServerConfigurationInformation based on command line arguments
     *
     * @param cmd CommandLine instance
     * @return ServerConfigurationInformation instance
     */
    public static ServerConfigurationInformation createServerConfigurationInformation(
            CommandLine cmd) {

        ServerConfigurationInformation information = new ServerConfigurationInformation();

        information.setAxis2RepoLocation(getArgument(cmd, Constants.AXIS2_CONF));
        information.setAxis2Xml(getArgument(cmd, SynapseConstants.SYNAPSE_XML));
        information.setSynapseHome(getArgument(cmd, SynapseConstants.SYNAPSE_HOME));
        information.setSynapseXMLLocation(getArgument(cmd, SynapseConstants.SYNAPSE_XML));
        information.setResolveRoot(getArgument(cmd, SynapseConstants.RESOLVE_ROOT));
        information.setDeploymentMode(getArgument(cmd, SynapseConstants.DEPLOYMENT_MODE));
        information.setServerName(getArgument(cmd, SynapseConstants.SERVER_NAME));

        return information;
    }

    /**
     * Creates a ServerConfigurationInformation based on command line arguments
     *
     * @param args Command line arguments
     * @return ServerConfigurationInformation instance
     */
    public static ServerConfigurationInformation createServerConfigurationInformation(
            String[] args) {

        ServerConfigurationInformation information = new ServerConfigurationInformation();
        information.setAxis2RepoLocation(args[0]);
        if (args.length == 1) {
            log.warn("Configuring server manager using deprecated " +
                    "system properties; please update your configuration");
            information.setAxis2Xml(System.getProperty(Constants.AXIS2_CONF));
            information.setSynapseHome(System.getProperty(SynapseConstants.SYNAPSE_HOME));
            information.setSynapseXMLLocation(System.getProperty(SynapseConstants.SYNAPSE_XML));
            information.setResolveRoot(System.getProperty(SynapseConstants.RESOLVE_ROOT));
            information.setServerName(System.getProperty(SynapseConstants.SERVER_NAME));
            information.setDeploymentMode(System.getProperty(SynapseConstants.DEPLOYMENT_MODE));
        } else if (args.length == 4) {
            information.setAxis2Xml(args[1]);
            information.setSynapseHome(args[2]);
            information.setSynapseXMLLocation(args[3]);
            information.setResolveRoot(args[2] + File.separator + "repository");
        } else if (args.length == 5) {
            information.setAxis2Xml(args[1]);
            information.setSynapseHome(args[2]);
            information.setSynapseXMLLocation(args[3]);
            information.setResolveRoot(args[4]);
        } else if (args.length == 6) {
            information.setAxis2Xml(args[1]);
            information.setSynapseHome(args[2]);
            information.setSynapseXMLLocation(args[3]);
            information.setResolveRoot(args[4]);
            information.setDeploymentMode(args[5]);
        } else if (args.length == 7) {
            information.setAxis2Xml(args[1]);
            information.setSynapseHome(args[2]);
            information.setSynapseXMLLocation(args[3]);
            information.setResolveRoot(args[4]);
            information.setDeploymentMode(args[5]);
            information.setServerName(args[6]);
        }

        return information;
    }

    /**
     * Factory method for create a ServerConfigurationInformation based on information
     * from ServletConfig
     *
     * @param servletConfig ServletConfig instance
     * @return ServerConfigurationInformation instance
     */
    public static ServerConfigurationInformation createServerConfigurationInformation(
            ServletConfig servletConfig) {

        ServerConfigurationInformation information = new ServerConfigurationInformation();

        String synapseHome = loadParameter(servletConfig, SynapseConstants.SYNAPSE_HOME, false);

        if (synapseHome == null) {
            log.info("synapse.home not set; using web application root as default value");
            String webInfPath = servletConfig.getServletContext().getRealPath("WEB-INF");
            if (webInfPath == null || !webInfPath.endsWith("WEB-INF")) {
                handleFatal("Unable to currentState web application root directory");
            } else {
                synapseHome = webInfPath.substring(0, webInfPath.length() - 7);
                log.info("Setting synapse.home to : " + synapseHome);
            }
        }

        information.setSynapseHome(synapseHome);
        information.setSynapseXMLLocation(loadParameter(servletConfig,
                SynapseConstants.SYNAPSE_XML, true));
        information.setResolveRoot(loadParameter(servletConfig,
                SynapseConstants.RESOLVE_ROOT, false));
        information.setAxis2RepoLocation(loadParameter(servletConfig,
                org.apache.axis2.Constants.AXIS2_REPO, true));
        information.setAxis2Xml(loadParameter(servletConfig,
                org.apache.axis2.Constants.AXIS2_CONF, true));
        information.setServerName(loadParameter(servletConfig,
                SynapseConstants.SERVER_NAME, false));
        information.setDeploymentMode(loadParameter(servletConfig,
                SynapseConstants.DEPLOYMENT_MODE, false));

        return information;
    }

    /**
     * Helper method to get a value of a parameters in the AxisConfiguration
     *
     * @param axisConfiguration AxisConfiguration instance
     * @param paramKey The name / key of the parameter
     * @return The value of the parameter
     */
    private static String getAxis2ParameterValue(AxisConfiguration axisConfiguration,
                                                 String paramKey) {

        Parameter parameter = axisConfiguration.getParameter(paramKey);
        if (parameter == null) {
            return null;
        }
        Object value = parameter.getValue();
        if (value != null && value instanceof String) {
            return (String) parameter.getValue();
        } else {
            return null;
        }
    }


    /**
     * Utility method to extract command line arguments
     *
     * @param cmd Command line which capture all command line arguments
     * @param argName Name of the argument to be extracted
     * @return value of the argument if there is , o.w null
     */
    private static String getArgument(CommandLine cmd, String argName) {

        if (cmd == null) {
            handleFatal("CommandLine is null");
        } else {

            if (argName == null || "".equals(argName)) {
                if (log.isDebugEnabled()) {
                    log.debug("Provided argument name is null. Returning null as value");
                }
                return null;
            }

            if (cmd.hasOption(argName)) {
                return cmd.getOptionValue(argName);
            }
        }
        return null;
    }

    /**
     * Load synapse initialization parameters from servlet configuration
     *
     * @param servletConfig Servlet configuration with the init parameters
     * @param name name of the init parameter to be loaded
     * @param required whether this parameter is a required one or not
     * @return value of the loaded parameter
     */
    private static String loadParameter(ServletConfig servletConfig, String name,
                                        boolean required) {

        if (System.getProperty(name) == null) {

            String value = servletConfig.getInitParameter(name);
            if (log.isDebugEnabled()) {
                log.debug("Init parameter '" + name + "' : " + value);
            }
                
            if ((value == null || value.trim().length() == 0) && required) {
                handleFatal("A valid system property or init parameter '" + name + "' is required");
            } else {
                return value;
            }
        } else {
            return System.getProperty(name);
        }
        return null;
    }

    private static void handleFatal(String msg) {
        log.fatal(msg);
        throw new SynapseException(msg);
    }
}
