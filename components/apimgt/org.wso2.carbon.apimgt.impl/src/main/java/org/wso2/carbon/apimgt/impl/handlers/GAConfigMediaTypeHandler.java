/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.handlers;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.LocalEntryAdminClient;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.Map;

public class GAConfigMediaTypeHandler extends Handler {
    private static Log log = LogFactory.getLog(GAConfigMediaTypeHandler.class);

    public void put(RequestContext requestContext) throws RegistryException {
        ResourceImpl resource = (ResourceImpl) requestContext.getResource();
        if (! resource.isContentModified()) {
            return;
        }

        // Local entry is updated only if the content of ga-config is updated
        Object content = resource.getContent();
        String resourceContent;
        if (content instanceof String) {
            resourceContent = (String) content;
        } else if (content instanceof byte[]) {
            resourceContent = RegistryUtils.decodeBytes((byte[]) content);
        } else {
            log.warn("The resource content is not of expected type");
            return;
        }

        APIManagerConfiguration config = getAPIManagerConfig();
        if (config == null) {
            return;
        }

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        Map<String, Environment> environments = config.getApiGatewayEnvironments();

        for (String environmentName : environments.keySet()) {
            Environment environment = environments.get(environmentName);

            try {
                LocalEntryAdminClient localEntryAdminClient = new LocalEntryAdminClient(environment, tenantDomain);
                localEntryAdminClient.deleteEntry(APIConstants.GA_CONF_KEY);
                localEntryAdminClient.addLocalEntry("<localEntry key=\"" + APIConstants.GA_CONF_KEY + "\">"
                        + resourceContent + "</localEntry>");
            } catch (AxisFault e) {
                log.error("Error occurred while adding local entry(GA-config) to gateway " + environmentName, e);
            }
        }
    }

    private APIManagerConfiguration getAPIManagerConfig() {
        APIManagerConfigurationService apiManagerConfigurationService = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService();
        if (apiManagerConfigurationService == null) {
            return null;
        }
        return apiManagerConfigurationService.getAPIManagerConfiguration();
    }

}
