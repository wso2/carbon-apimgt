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
package org.wso2.carbon.apimgt.impl.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APIManagerFactory.class)
public class APIConsumerAdminTestCase {

    @Test
    public void testResumeWorkflow() throws Exception {
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        Mockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);

        APIConsumer consumer = Mockito.mock(APIConsumer.class);
        Mockito.when(apiManagerFactory.getAPIConsumer("")).thenReturn(consumer);
        APIConsumerAdmin apiConsumerAdmin = new APIConsumerAdmin();
        apiConsumerAdmin.resumeWorkflow(null, "");
        Mockito.verify(consumer, Mockito.times(1)).resumeWorkflow(null);
    }
}
