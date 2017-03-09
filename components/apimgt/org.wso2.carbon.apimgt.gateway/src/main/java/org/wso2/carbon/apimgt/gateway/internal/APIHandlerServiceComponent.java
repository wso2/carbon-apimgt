/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.handlers.security.keys.APIKeyValidatorClientPool;
import org.wso2.carbon.apimgt.gateway.handlers.security.thrift.ThriftKeyValidatorClientPool;
import org.wso2.carbon.apimgt.gateway.service.APIThrottleDataService;
import org.wso2.carbon.apimgt.gateway.service.APIThrottleDataServiceImpl;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.util.BlockingConditionRetriever;
import org.wso2.carbon.apimgt.gateway.throttling.util.KeyTemplateRetriever;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;

/**
 * @scr.component name="org.wso2.carbon.apimgt.handlers" immediate="true"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 */
public class APIHandlerServiceComponent {

    private static final Log log = LogFactory.getLog(APIHandlerServiceComponent.class);

    private APIKeyValidatorClientPool clientPool;
    private ThriftKeyValidatorClientPool thriftClientPool;
    private APIManagerConfiguration configuration = new APIManagerConfiguration();
    private ServiceRegistration registration;

    protected void activate(ComponentContext context) {
    	BundleContext bundleContext = context.getBundleContext();
        if (log.isDebugEnabled()) {
            log.debug("API handlers component activated");
        }
        clientPool = APIKeyValidatorClientPool.getInstance();
        thriftClientPool = ThriftKeyValidatorClientPool.getInstance();

        String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "api-manager.xml";
		try {
			configuration.load(filePath);

			String gatewayType = configuration.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
			if ("Synapse".equalsIgnoreCase(gatewayType)) {
			  //Register Tenant service creator to deploy tenant specific common synapse configurations
			  TenantServiceCreator listener = new TenantServiceCreator();
			  bundleContext.registerService(
			          Axis2ConfigurationContextObserver.class.getName(), listener, null);

                if (configuration.getThrottleProperties().isEnabled()) {
                    ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
                    APIThrottleDataServiceImpl throttleDataServiceImpl = new APIThrottleDataServiceImpl();
                    throttleDataServiceImpl.setThrottleDataHolder(throttleDataHolder);


                    // Register APIThrottleDataService so that ThrottleData maps are available to other components.
                    registration = context.getBundleContext().registerService(
                            APIThrottleDataService.class.getName(),
                            throttleDataServiceImpl, null);
                    ServiceReferenceHolder.getInstance().setThrottleDataHolder(throttleDataHolder);

                    log.debug("APIThrottleDataService Registered...");
                    ServiceReferenceHolder.getInstance().setThrottleProperties(configuration
                            .getThrottleProperties());


                    //First do web service call and update map.
                    //Then init JMS listener to listen que and update it.
                    //Following method will initialize JMS listnet and listen all updates and keep throttle data map
                    // up to date
                    //start web service throttle data retriever as separate thread and start it.
                    if (configuration.getThrottleProperties().getBlockCondition().isEnabled()){
                        BlockingConditionRetriever webServiceThrottleDataRetriever = new
                                BlockingConditionRetriever();
                        webServiceThrottleDataRetriever.startWebServiceThrottleDataRetriever();
                        KeyTemplateRetriever webServiceBlockConditionsRetriever = new
                                KeyTemplateRetriever();
                        webServiceBlockConditionsRetriever.startKeyTemplateDataRetriever();
                    }
                }
            }
		} catch (APIManagementException e) {
			log.error("Error while initializing the API Gateway (APIHandlerServiceComponent) component", e);
		}
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("API handlers component deactivated");
        }
        clientPool.cleanup();
        thriftClientPool.cleanup();
        if(registration != null){
            log.debug("Unregistering ThrottleDataService...");
            registration.unregister();
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        if (log.isDebugEnabled()) {
            log.debug("Configuration context service bound to the API handlers");
        }
        ServiceReferenceHolder.getInstance().setConfigurationContextService(cfgCtxService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService cfgCtxService) {
        if (log.isDebugEnabled()) {
            log.debug("Configuration context service unbound from the API handlers");
        }
        ServiceReferenceHolder.getInstance().setConfigurationContextService(null);
    }

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service bound to the API handlers");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from the API handlers");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

}
