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


import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.utils.ConfigurationContextService;


public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private ConfigurationContextService cfgCtxService;
    private APIManagerConfigurationService amConfigService;
    public ThrottleDataHolder throttleDataHolder;
    private ThrottleProperties throttleProperties;
    private ConfigurationContext axis2ConfigurationContext;
    private ServerConfigurationService serverConfigurationService;

    public ThrottleDataHolder getThrottleDataHolder() {
        return throttleDataHolder;
    }

    public void setThrottleDataHolder(ThrottleDataHolder throttleDataHolder) {
        this.throttleDataHolder = throttleDataHolder;
    }

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        this.cfgCtxService = cfgCtxService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return cfgCtxService;
    }

    public ConfigurationContext getServerConfigurationContext() {
        return cfgCtxService.getServerConfigContext();
    }

    public APIManagerConfiguration getAPIManagerConfiguration() {
        return amConfigService.getAPIManagerConfiguration();
    }

    public APIManagerConfigurationService getApiManagerConfigurationService() {
        return amConfigService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigService) {
        this.amConfigService = amConfigService;
    }

    public ThrottleProperties getThrottleProperties() {
        return throttleProperties;
    }

    public void setThrottleProperties(ThrottleProperties throttleProperties) {
        this.throttleProperties = throttleProperties;
    }

    public void setAxis2ConfigurationContext(ConfigurationContext axis2ConfigurationContext) {
        this.axis2ConfigurationContext = axis2ConfigurationContext;
    }

    public ConfigurationContext getAxis2ConfigurationContext() {
        return axis2ConfigurationContext;
    }

    /**
     * To get the server configuration service.
     *
     * @return an instance of {@link ServerConfigurationService}
     */
    public ServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    /**
     * To set the server configuration service.
     *
     * @param serverConfigurationService {@link ServerConfigurationService}
     */
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }
}
