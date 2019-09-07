/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.tenant.initializer.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.junit.Test;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * ServiceDataHolderTest Class
 */
public class ServiceDataHolderTest {
    @Test
    public void getInstance() throws Exception {
        ServiceDataHolder.getInstance();
    }

    @Test
    public void getConfigurationContextService() throws Exception {
        ServiceDataHolder.getInstance().getConfigurationContextService();
    }

    @Test
    public void setConfigurationContextService() throws Exception {
        ConfigurationContextService service = new ConfigurationContextService(new ConfigurationContext
                (new AxisConfiguration()), new ConfigurationContext(new AxisConfiguration()));
        ServiceDataHolder.getInstance().setConfigurationContextService(service);
    }

}
