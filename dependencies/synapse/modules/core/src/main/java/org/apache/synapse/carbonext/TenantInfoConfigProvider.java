/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.carbonext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapsePropertiesLoader;

import java.util.Properties;

public final class TenantInfoConfigProvider {
    /** */
    public static final String CARBON_TENANT_INFO_CONFIGURATOR = "synapse.carbon.ext.tenant.info";

    private static final Log logger = LogFactory.getLog(TenantInfoConfigProvider.class.getName());

    public static TenantInfoConfigurator getConfigurator() {
        Properties properties = SynapsePropertiesLoader.loadSynapseProperties();
        String property = properties.getProperty(CARBON_TENANT_INFO_CONFIGURATOR);
        if (property != null) {
                try {
                    Class clazz = TenantInfoConfigProvider.class.getClassLoader().
                            loadClass(property.trim());
                    TenantInfoConfigurator obj = (TenantInfoConfigurator) clazz.newInstance();
                    return obj;
                } catch (Exception e) {
                    logger.error("Error while initializing tenant info configuration provider. Error:"
                                 + e.getLocalizedMessage());
                }
        }
        return null;
    }
}
