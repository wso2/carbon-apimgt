/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.interceptor.valve.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Holds the some of the data required by the apimgt valve
 */
public class DataHolder {
    private static ConfigurationContext serverConfigContext;
    
    private static RegistryService registryService;
    
    public static void setServerConfigContext(ConfigurationContext serverConfigContext) {
        DataHolder.serverConfigContext = serverConfigContext;
    }

    public static ConfigurationContext getServerConfigContext() {
        CarbonUtils.checkSecurity();
        return serverConfigContext;
    }

	public static void setRegistryService(RegistryService registryService) {
		DataHolder.registryService = registryService;
	}
	
	public static RegistryService getRegistryService() {
		return registryService;
	}
}
