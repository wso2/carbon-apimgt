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

package org.wso2.carbon.apimgt.usage.client.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.client.exception.APIMgtUsageQueryServiceClientException;
import org.wso2.carbon.apimgt.usage.client.UsageClient;

/**
 * @scr.component name="org.wso2.apimgt.usage.client" immediate="true"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 */
public class APIUsageClientServiceComponent {

    private static final Log log = LogFactory.getLog(APIUsageClientServiceComponent.class);

    private static APIManagerConfiguration configuration = null;

    private static APIManagerAnalyticsConfiguration analyticsConfiguration = null;

    protected void activate(ComponentContext componentContext)
            throws APIMgtUsageQueryServiceClientException {
        if (log.isDebugEnabled()) {
            log.debug("API usage client component activated");
        }
        if (APIUtil.isAnalyticsEnabled()){
            UsageClient.initializeDataSource();
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        log.debug("API usage client component deactivated");
    }

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        log.debug("API manager configuration service bound to the API usage client component");
        configuration = amcService.getAPIManagerConfiguration();
        analyticsConfiguration = amcService.getAPIAnalyticsConfiguration();
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        log.debug("API manager configuration service unbound from the API usage client component");
    }

    public static APIManagerConfiguration getAPIManagerConfiguration() {
        return configuration;
    }

    public static APIManagerAnalyticsConfiguration getAnalyticsConfiguration() {
        return analyticsConfiguration;
    }
}
