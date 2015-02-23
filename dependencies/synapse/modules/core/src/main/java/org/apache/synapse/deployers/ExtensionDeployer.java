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

package org.apache.synapse.deployers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentClassLoader;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.MediatorFactory;
import org.apache.synapse.config.xml.MediatorFactoryFinder;
import org.apache.synapse.config.xml.MediatorSerializer;
import org.apache.synapse.config.xml.MediatorSerializerFinder;
import org.apache.synapse.config.xml.StartupFactory;
import org.apache.synapse.config.xml.StartupFinder;

/**
 * This will support the hot deployment and hot update of Synapse extensions (mediators
 * and startups) at runtime using the Axis2 concepts of deployers.
 */
public class ExtensionDeployer extends AbstractDeployer {

    /**
     * Holds the log variable for logging purposes
     */
    private static final Log log = LogFactory.getLog(ExtensionDeployer.class);

    /**
     * ConfigurationContext of Axis2
     */
    private ConfigurationContext cfgCtx = null;

    /**
     * Initializes the Deployer
     *
     * @param configurationContext - ConfigurationContext of Axis2 from which
     *  the deployer is initialized
     */
    public void init(ConfigurationContext configurationContext) {
        this.cfgCtx = configurationContext;
    }

    /**
     * This will be called when there is a change in the specified deployment
     * folder (in the axis2.xml) and this will load the relevant classes to the system and
     * register them with the MediatorFactoryFinder
     *
     * @param deploymentFileData - describes the updated file
     * @throws DeploymentException - in case an error on the deployment
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        log.info("Loading extensions from: " + deploymentFileData.getAbsolutePath());

        // get the context class loader for the later restore of the context class loader
        ClassLoader prevCl = Thread.currentThread().getContextClassLoader();

        try {
            boolean isDirectory = deploymentFileData.getFile().isDirectory();
            deploymentFileData.setClassLoader(isDirectory, getClass().getClassLoader(),
                    (File) cfgCtx.getAxisConfiguration().getParameterValue(
                            Constants.Configuration.ARTIFACTS_TEMP_DIR),
                    cfgCtx.getAxisConfiguration().isChildFirstClassLoading());

            DeploymentClassLoader urlCl
                = (DeploymentClassLoader)deploymentFileData.getClassLoader();
            Thread.currentThread().setContextClassLoader(urlCl);

            // StartupFactory registration
            for (StartupFactory factory : getProviders(StartupFactory.class, urlCl)) {
                QName tagQName = factory.getTagQName();
                Class<? extends StartupFactory> clazz = factory.getClass();
                StartupFinder finder = StartupFinder.getInstance();
                finder.getFactoryMap().put(tagQName, clazz);
                finder.getSerializerMap().put(tagQName, factory.getSerializerClass());
                log.info("Registered startup factory and serializer for " + tagQName);
            }

            // MediatorFactory registration
            for (MediatorFactory factory : getProviders(MediatorFactory.class, urlCl)) {
                QName tagQName = factory.getTagQName();
                Class<? extends MediatorFactory> clazz = factory.getClass();
                MediatorFactoryFinder.getInstance().getFactoryMap().put(tagQName, clazz);
                log.info("Registered mediator factory " + clazz.getName() + " for " + tagQName);
            }

            // MediatorSerializer registration
            for (MediatorSerializer serializer : getProviders(MediatorSerializer.class, urlCl)) {
                String mediatorClassName = serializer.getMediatorClassName();
                MediatorSerializerFinder.getInstance().getSerializerMap().put(
                        mediatorClassName, serializer);
                log.info("Registered mediator serializer " + serializer.getClass().getName()
                        + " for " + mediatorClassName);
            }
            
        } catch (IOException e) {
            handleException("I/O error in reading the mediator jar file", e);
        } catch (Exception e) {
            handleException("Error occurred while trying to deploy mediator jar file", e);
        } catch (Throwable t) {
            handleException("Error occurred while trying to deploy the mediator jar file", t);
        } finally {
            // restore the class loader back
            if (log.isDebugEnabled()) {
                log.debug("Restoring the context class loader to the original");
            }
            Thread.currentThread().setContextClassLoader(prevCl);
        }
    }
    
    private <T> List<T> getProviders(Class<T> providerClass, URLClassLoader loader)
            throws IOException {
        
        List<T> providers = new LinkedList<T>();
        String providerClassName = providerClass.getName();
        providerClassName = providerClassName.substring(providerClassName.indexOf('.')+1);
        URL servicesURL = loader.findResource("META-INF/services/" + providerClass.getName());
        if (servicesURL != null) {
            BufferedReader in
                = new BufferedReader(new InputStreamReader(servicesURL.openStream()));
            try {
                String className;
                while ((className = in.readLine()) != null && (!className.trim().equals(""))) {
                    log.info("Loading the " + providerClassName + " implementation: " + className);
                    try {
                        Class<? extends T> clazz
                            = loader.loadClass(className).asSubclass(providerClass);
                        providers.add(clazz.newInstance());
                    } catch (ClassNotFoundException e) {
                        handleException("Unable to find the specified class on the path or " +
                        		"in the jar file", e);
                    } catch (IllegalAccessException e) {
                        handleException("Unable to load the class from the jar", e);
                    } catch (InstantiationException e) {
                        handleException("Unable to instantiate the class specified", e);
                    }
                }
            } finally {
                in.close();
            }
        }
        return providers;
    }

    /**
     * This will not be implemented because we do not support changing the directory at runtime
     *
     * @param string -
     */
    public void setDirectory(String string) {
        // we do not support changing the directory
    }

    /**
     * This will not be implemented because we do not support changing the extension at runtime
     *
     * @param string -
     */
    public void setExtension(String string) {
        // we do not support changing the extension
    }

    /**
     * This will be called when a particular jar file is deleted from the specified folder.
     *
     * @param string - filename of the deleted file
     * @throws DeploymentException - incase of an error in undeployment
     */
    public void undeploy(String string) throws DeploymentException {
        // todo: implement the undeployement
    }

    private void handleException(String message, Exception e) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug(message, e);
        }
        throw new DeploymentException(message, e);
    }

    private void handleException(String message, Throwable t) throws DeploymentException {
        if (log.isDebugEnabled()) {
            log.debug(message, t);
        }
        throw new DeploymentException(message, t);
    }
}
