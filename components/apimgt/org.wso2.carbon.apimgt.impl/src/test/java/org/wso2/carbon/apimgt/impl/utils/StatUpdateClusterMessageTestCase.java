/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.context.ConfigurationContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.utils.StatUpdateClusterMessage;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIManagerAnalyticsConfiguration.class)
public class StatUpdateClusterMessageTestCase {

    @Before
    public void setup() {
        PowerMockito.mockStatic(APIManagerAnalyticsConfiguration.class);
        APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration = Mockito
                .mock(APIManagerAnalyticsConfiguration.class);
        PowerMockito.when(APIManagerAnalyticsConfiguration.getInstance()).thenReturn(apiManagerAnalyticsConfiguration);
    }

    @Test
    public void testExecute() throws Exception {
        StatUpdateClusterMessage statUpdateClusterMessage = new StatUpdateClusterMessage(true, "tcp://10.100.1.7/9711", "john", "johnpassword");
        ConfigurationContext configurationContext = Mockito.mock(ConfigurationContext.class);
        statUpdateClusterMessage.execute(configurationContext);
    }
}
