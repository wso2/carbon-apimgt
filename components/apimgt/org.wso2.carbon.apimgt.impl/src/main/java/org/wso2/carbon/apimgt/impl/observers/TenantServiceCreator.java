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

package org.wso2.carbon.apimgt.impl.observers;

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
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
    private String sandboxKeyErrorSequenceName = "_sandbox_key_error_";
    private String productionKeyErrorSequenceName = "_production_key_error_";
    private String throttleOutSequenceName = "_throttle_out_handler_";
    private String faultSequenceName = "fault";
    private String mainSequenceName = "main";
    private String synapseConfigRootPath = CarbonBaseUtils.getCarbonHome() + "/repository/resources/apim-synapse-config/";



    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        /*String tenantDomain =
                PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantDomain();
        int tenantId =  PrivilegedCarbonContext.getCurrentContext(configurationContext).getTenantId();*/
    	String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    	int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
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
            //axisConfig.addParameter(SynapseConstants.Axis2Param.SYNAPSE_CONFIG_LOCATION,synapseConfigsDirLocation);

            // init the multiple configuration tracker
            //ConfigurationManager manger = new ConfigurationManager((UserRegistry) registry, configurationContext);
            //manger.init();

            //File synapseConfigDir = new File(synapseConfigsDir,manger.getTracker().getCurrentConfigurationName());
            //File authFailureHandlerSequenceNameFile = new File(synapseConfigsDir + "/" + manger.getTracker().getCurrentConfigurationName() +
                   // "/" + MultiXMLConfigurationBuilder.SEQUENCES_DIR + "/" + authFailureHandlerSequenceName + ".xml");
            //Here we will check authfailurehandler sequence exist in synapse artifact. If it is not available we will create
            //sequence synapse configurations by using resource artifacts
            //if (!authFailureHandlerSequenceNameFile.exists()) {
               // createTenantSynapseConfigHierarchy(synapseConfigDir, tenantDomain);
            //}
        } catch (Exception e) {
            log.error("Failed to create Tenant's synapse sequences.", e);
        }
    }

//    public void terminatingConfigurationContext(ConfigurationContext configurationContext) {
//
//    }

/*    private void initPersistence(String configName, ConfigurationContext configurationContext, ServerContextInformation contextInfo)
            throws RegistryException, AxisFault {

    }

    private ServerContextInformation initESB(String configurationName, ConfigurationContext configurationContext)
            throws AxisFault {
        return null;
    }*/

    /**
     * Create the file system for holding the synapse configuration for a new tanent.
     *
     * @param synapseConfigDir configuration directory where synapse configuration is created
     * @param tenantDomain     name of the tenent
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    /*private void createTenantSynapseConfigHierarchy(File synapseConfigDir, String tenantDomain) {
        synapseConfigDir.mkdir();
        File sequencesDir = new File(
                synapseConfigDir, MultiXMLConfigurationBuilder.SEQUENCES_DIR);

        if (!sequencesDir.mkdir()) {
            log.warn("Could not create " + sequencesDir);
        }

        SynapseConfiguration initialSynCfg = SynapseConfigurationBuilder.getDefaultConfiguration();
        
        //SequenceMediator mainSequence = (SequenceMediator) initialSynCfg.getMainSequence();
        //SequenceMediator faultSequence = (SequenceMediator) initialSynCfg.getFaultSequence();
        InputStream in = null;
        StAXOMBuilder builder = null;
        SequenceMediatorFactory factory = new SequenceMediatorFactory();
        try {
            if (authFailureHandlerSequence == null) {
                in = FileUtils.openInputStream(new File(synapseConfigRootPath + authFailureHandlerSequenceName + ".xml"));
                builder = new StAXOMBuilder(in);
                authFailureHandlerSequence = (SequenceMediator) factory.createMediator(builder.getDocumentElement(), new Properties());
                authFailureHandlerSequence.setFileName(authFailureHandlerSequenceName + ".xml");
            }
            if (resourceMisMatchSequence == null) {
                in = FileUtils.openInputStream(new File(synapseConfigRootPath + resourceMisMatchSequenceName + ".xml"));
                builder = new StAXOMBuilder(in);
                resourceMisMatchSequence = (SequenceMediator) factory.createMediator(builder.getDocumentElement(), new Properties());
                resourceMisMatchSequence.setFileName(resourceMisMatchSequenceName + ".xml");
            }
            if (throttleOutSequence == null) {
                in = FileUtils.openInputStream(new File(synapseConfigRootPath + throttleOutSequenceName + ".xml"));
                builder = new StAXOMBuilder(in);
                throttleOutSequence = (SequenceMediator) factory.createMediator(builder.getDocumentElement(), new Properties());
                throttleOutSequence.setFileName(throttleOutSequenceName + ".xml");
            }
            if (sandboxKeyErrorSequence == null) {
                in = FileUtils.openInputStream(new File(synapseConfigRootPath + sandboxKeyErrorSequenceName + ".xml"));
                builder = new StAXOMBuilder(in);
                sandboxKeyErrorSequence = (SequenceMediator) factory.createMediator(builder.getDocumentElement(), new Properties());
                sandboxKeyErrorSequence.setFileName(sandboxKeyErrorSequenceName + ".xml");
            }
            if (productionKeyErrorSequence == null) {
                in = FileUtils.openInputStream(new File(synapseConfigRootPath + productionKeyErrorSequenceName + ".xml"));
                builder = new StAXOMBuilder(in);
                productionKeyErrorSequence = (SequenceMediator) factory.createMediator(builder.getDocumentElement(), new Properties());
                productionKeyErrorSequence.setFileName(productionKeyErrorSequenceName + ".xml");
            }
            if (corsSequence == null) {
                in = FileUtils
                        .openInputStream(new File(synapseConfigRootPath + APIConstants.CORS_SEQUENCE_NAME + ".xml"));
                builder = new StAXOMBuilder(in);
                corsSequence = (SequenceMediator) factory.createMediator(builder.getDocumentElement(), new Properties());
                corsSequence.setFileName(APIConstants.CORS_SEQUENCE_NAME + ".xml");
            }
            FileUtils.copyFile(new File(synapseConfigRootPath + mainSequenceName + ".xml"),
                    new File(synapseConfigDir.getAbsolutePath() + File.separator + "sequences" + File.separator + mainSequenceName + ".xml"));

            FileUtils.copyFile(new File(synapseConfigRootPath + faultSequenceName + ".xml"),
                    new File(synapseConfigDir.getAbsolutePath() + File.separator + "sequences" + File.separator + faultSequenceName + ".xml"));

        } catch (IOException e) {                                                             
            log.error("Error while reading API manager specific synapse sequences" + e);

        } catch (XMLStreamException e) {
            log.error("Error while parsing API manager specific synapse sequences" + e);
        } finally {
            IOUtils.closeQuietly(in);
        }

        Registry registry = new WSO2Registry();
        initialSynCfg.setRegistry(registry);
        MultiXMLConfigurationSerializer serializer
                = new MultiXMLConfigurationSerializer(synapseConfigDir.getAbsolutePath());
        try {
            serializer.serializeSequence(authFailureHandlerSequence, initialSynCfg, null);
            serializer.serializeSequence(sandboxKeyErrorSequence, initialSynCfg, null);
            serializer.serializeSequence(productionKeyErrorSequence, initialSynCfg, null);
            serializer.serializeSequence(throttleOutSequence, initialSynCfg, null);
            serializer.serializeSequence(resourceMisMatchSequence, initialSynCfg, null);
            serializer.serializeSequence(corsSequence, initialSynCfg, null);
            serializer.serializeSynapseRegistry(registry, initialSynCfg, null);
        } catch (Exception e) {
            handleException("Couldn't serialise the initial synapse configuration " +
                    "for the domain : " + tenantDomain, e);
        }
    }*/

    /**
     * No need to implement
     */
//    private void addDeployers(ConfigurationContext configurationContext) {
//
//    }

    public static boolean isRunningSamplesMode() {
        return true;
    }

    private void handleFatal(String message) {
        log.fatal(message);
    }

    private void handleFatal(String message, Exception e) {
        log.fatal(message, e);
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
    }
}
