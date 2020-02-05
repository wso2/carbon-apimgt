/*
*  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.carbon.apimgt.gateway.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.MultiXMLConfigurationBuilder;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.mediation.initializer.configurations.ConfigurationManager;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * This creates the {@link org.apache.synapse.config.SynapseConfiguration}
 * for the respective tenants. This class specifically add to deploy API Manager
 * related synapse sequences. This class used to deploy resource mismatch handler, auth failure handler,
 * sandbox error handler, throttle out handler, build sequence, main sequence and fault sequence into tenant
 * synapse artifact space.
 */
public class TenantServiceCreator extends AbstractAxis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(TenantServiceCreator.class);
    private String resourceMisMatchSequenceName = "_resource_mismatch_handler_";
    private String authFailureHandlerSequenceName = "_auth_failure_handler_";
    private String graphqlAuthFailureHandlerSequenceName = "_graphql_failure_handler_";
    private String sandboxKeyErrorSequenceName = "_sandbox_key_error_";
    private String productionKeyErrorSequenceName = "_production_key_error_";
    private String throttleOutSequenceName = "_throttle_out_handler_";
    private String faultSequenceName = "fault";
    private String mainSequenceName = "main";
    private String corsSequenceName = "_cors_request_handler_";
    private String threatFaultSequenceName = "_threat_fault_";
    private String synapseConfigRootPath = CarbonBaseUtils.getCarbonHome() + "/repository/resources/apim-synapse-config/";

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        log.info("Initializing APIM TenantServiceCreator for the tenant domain : " + tenantDomain);
        try {

            // first check which configuration should be active
            org.wso2.carbon.registry.core.Registry registry =
                    (org.wso2.carbon.registry.core.Registry) PrivilegedCarbonContext
                            .getThreadLocalCarbonContext().
                                    getRegistry(RegistryType.SYSTEM_CONFIGURATION);

            AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();

            // initialize the lock
            Lock lock = new ReentrantLock();
            axisConfig.addParameter("synapse.config.lock", lock);

            // creates the synapse configuration directory hierarchy if not exists
            // useful at the initial tenant creation
            File tenantAxis2Repo = new File(
                    configurationContext.getAxisConfiguration().getRepository().getFile());
            File synapseConfigsDir = new File(tenantAxis2Repo, "synapse-configs");
            if (!synapseConfigsDir.exists()) {
                if (!synapseConfigsDir.mkdir()) {
                    log.fatal("Couldn't create the synapse-config root on the file system " +
                            "for the tenant domain : " + tenantDomain);
                    return;
                }
            }

            String synapseConfigsDirLocation = synapseConfigsDir.getAbsolutePath();
            // set the required configuration parameters to initialize the ESB
            axisConfig.addParameter(SynapseConstants.Axis2Param.SYNAPSE_CONFIG_LOCATION,
                    synapseConfigsDirLocation);

            // init the multiple configuration tracker
            ConfigurationManager manger = new ConfigurationManager((UserRegistry) registry,
                    configurationContext);
            manger.init();

            File synapseConfigDir = new File(synapseConfigsDir, manger.getTracker().getCurrentConfigurationName());
            StringBuilder filepath = new StringBuilder();
            filepath.append(synapseConfigsDir).append('/').append(manger.getTracker().getCurrentConfigurationName()).append('/').
                    append(MultiXMLConfigurationBuilder.SEQUENCES_DIR).append('/').append(authFailureHandlerSequenceName).
                    append(".xml");
             File authFailureHandlerSequenceNameFile = new File(filepath.toString());
            //Here we will check authfailurehandler sequence exist in synapse artifact. If it is not available we will create
            //sequence synapse configurations by using resource artifacts
            if (!authFailureHandlerSequenceNameFile.exists()) {
                createTenantSynapseConfigHierarchy(synapseConfigDir, tenantDomain);
            }

            String graphqlFilepath = String.valueOf(synapseConfigsDir) + '/' +
                    manger.getTracker().getCurrentConfigurationName() + '/' + MultiXMLConfigurationBuilder.SEQUENCES_DIR
                    + '/' + graphqlAuthFailureHandlerSequenceName + ".xml";
            File graphqlFailureHandlerSequenceNameFile = new File(graphqlFilepath);
            if (!graphqlFailureHandlerSequenceNameFile.exists()) {
                createTenantSynapseConfigHierarchy(synapseConfigDir, tenantDomain);
            }

            String threatFaultConfigLocation = synapseConfigsDir.getAbsolutePath() + File.separator +
                    manger.getTracker().getCurrentConfigurationName() + File.separator +
                    MultiXMLConfigurationBuilder.SEQUENCES_DIR + File.separator + threatFaultSequenceName + ".xml";
            File threatFaultXml = new File(threatFaultConfigLocation);
            if (!threatFaultXml.exists()) {
                FileUtils.copyFile(new File(synapseConfigRootPath + threatFaultSequenceName + ".xml"),
                        new File(synapseConfigDir.getAbsolutePath() + File.separator +
                                MultiXMLConfigurationBuilder.SEQUENCES_DIR + File.separator +
                                threatFaultSequenceName + ".xml"));
            }
        } catch (RemoteException e) {
            log.error("Failed to create Tenant's synapse sequences.", e);
        } catch (Exception e) {
            log.error("Failed to create Tenant's synapse sequences.", e);
        }

        //Create caches for tenants
        CacheProvider.removeAllCaches();
        CacheProvider.createGatewayKeyCache();
        CacheProvider.createResourceCache();
        CacheProvider.createGatewayTokenCache();
        CacheProvider.createInvalidTokenCache();
        CacheProvider.createGatewayBasicAuthResourceCache();
        CacheProvider.createGatewayUsernameCache();
        CacheProvider.createInvalidUsernameCache();

        //Initialize product REST API token caches
        CacheProvider.createRESTAPITokenCache();
        CacheProvider.createRESTAPIInvalidTokenCache();
        CacheProvider.createGatewayJWTTokenCache();
    }

    /**
     * Create the file system for holding the synapse configuration for a new tanent.
     *
     * @param synapseConfigDir configuration directory where synapse configuration is created
     * @param tenantDomain     name of the tenent
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private void createTenantSynapseConfigHierarchy(File synapseConfigDir, String tenantDomain) {
        Thread tenantSynapseConfigHierarchyCreator
                = new Thread(new TenantSynapseConfigHierarchyCreator(synapseConfigDir, tenantDomain));
        tenantSynapseConfigHierarchyCreator.start();
    }

    public static boolean isRunningSamplesMode() {
        return true;
    }

    private class TenantSynapseConfigHierarchyCreator implements Runnable{

        private File synapseConfigDir;
        private String tenantDomain;
        final private int timeoutInSeconds = 60;

        public TenantSynapseConfigHierarchyCreator(File synapseConfigDir, String tenantDomain) {
            this.synapseConfigDir = synapseConfigDir;
            this.tenantDomain = tenantDomain;
        }

        @Override
        public void run() {
            final long startTime = System.currentTimeMillis();

            //wait until the synpase config directory structure is created by carbon-mediation
            while(!synapseConfigDir.exists()){
                if((System.currentTimeMillis() - startTime) / 1000 > timeoutInSeconds ){
                    log.error("Waiting for Synapse Configuration hierarchy of tenant " + tenantDomain +
                              " timed out. Copying custom sequence files failed!");
                    return;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error("Error occurred while waiting for Synapse Configuration hierarchy of tenant "
                              + tenantDomain, e);
                }
            }

            try {
                FileUtils.copyFile(new File(synapseConfigRootPath + mainSequenceName + ".xml"),
                                   new File(synapseConfigDir.getAbsolutePath() + File.separator + "sequences"
                                            + File.separator + mainSequenceName + ".xml"));
                FileUtils.copyFile(new File(synapseConfigRootPath + faultSequenceName + ".xml"),
                                   new File(synapseConfigDir.getAbsolutePath() + File.separator + "sequences"
                                            + File.separator + faultSequenceName + ".xml"));
                FileUtils.copyFile(new File(synapseConfigRootPath + authFailureHandlerSequenceName + ".xml"),
                                   new File(synapseConfigDir.getAbsolutePath() + File.separator + "sequences"
                                            + File.separator + authFailureHandlerSequenceName + ".xml"));
                FileUtils.copyFile(new File(synapseConfigRootPath + graphqlAuthFailureHandlerSequenceName + ".xml"),
                        new File(synapseConfigDir.getAbsolutePath() + File.separator + "sequences"
                                + File.separator + graphqlAuthFailureHandlerSequenceName + ".xml"));
                FileUtils.copyFile(new File(synapseConfigRootPath + resourceMisMatchSequenceName + ".xml"),
                                   new File(synapseConfigDir.getAbsolutePath() + File.separator + "sequences"
                                            + File.separator + resourceMisMatchSequenceName + ".xml"));
                FileUtils.copyFile(new File(synapseConfigRootPath + throttleOutSequenceName + ".xml"),
                                   new File(synapseConfigDir.getAbsolutePath() + File.separator + "sequences"
                                            + File.separator + throttleOutSequenceName + ".xml"));
                FileUtils.copyFile(new File(synapseConfigRootPath + sandboxKeyErrorSequenceName + ".xml"),
                                   new File(synapseConfigDir.getAbsolutePath() + File.separator + "sequences"
                                            + File.separator + sandboxKeyErrorSequenceName + ".xml"));
                FileUtils.copyFile(new File(synapseConfigRootPath + productionKeyErrorSequenceName + ".xml"),
                                   new File(synapseConfigDir.getAbsolutePath() + File.separator + "sequences"
                                            + File.separator + productionKeyErrorSequenceName + ".xml"));
                FileUtils.copyFile(new File(synapseConfigRootPath + corsSequenceName + ".xml"),
                                   new File(synapseConfigDir.getAbsolutePath() + File.separator + "sequences"
                                            + File.separator + corsSequenceName + ".xml"));
                FileUtils.copyFile(new File(synapseConfigRootPath + threatFaultSequenceName + ".xml"),
                        new File(synapseConfigDir.getAbsolutePath() + File.separator + "sequences"
                                + File.separator + threatFaultSequenceName + ".xml"));
            } catch (IOException e) {
                log.error("Error while copying API manager specific synapse sequences" + e);
            }
        }
    }
}
