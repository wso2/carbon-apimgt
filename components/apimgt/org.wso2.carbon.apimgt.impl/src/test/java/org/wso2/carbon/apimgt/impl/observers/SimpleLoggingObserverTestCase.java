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
package org.wso2.carbon.apimgt.impl.observers;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;

public class SimpleLoggingObserverTestCase {

    private final String API_NAME = "pizza-shack-api";
    private final String API_VERSION = "1.0.0";

    @Test
    public void testStatusChanged() throws Exception {

        API api = Mockito.mock(API.class);
        APIIdentifier apiIdentifier = Mockito.mock(APIIdentifier.class);

        APIStatus previousStatus = APIStatus.CREATED;
        APIStatus currentStatus = APIStatus.PUBLISHED;

        Mockito.when(api.getId()).thenReturn(apiIdentifier);
        Mockito.when(apiIdentifier.getApiName()).thenReturn(API_NAME);
        Mockito.when(apiIdentifier.getVersion()).thenReturn(API_VERSION);

        SimpleLoggingObserver simpleLoggingObserver = new SimpleLoggingObserver();
        boolean returnValue = simpleLoggingObserver.statusChanged(previousStatus, currentStatus, api);
        Assert.assertTrue(returnValue);
    }
}
