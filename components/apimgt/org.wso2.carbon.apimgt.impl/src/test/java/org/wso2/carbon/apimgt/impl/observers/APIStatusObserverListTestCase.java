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

import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

import java.util.ArrayList;
import java.util.List;

public class APIStatusObserverListTestCase {


    @Test
    public void testInitNonInitializedReadFromConfig() {
        APIStatusObserverList apiStatusObserverList = APIStatusObserverList.getInstance();
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        List<String> observerList = new ArrayList<String>();
        observerList.add("org.wso2.carbon.apimgt.impl.observers.SimpleLoggingObserver");
        Mockito.when(apiManagerConfiguration.getProperty(APIConstants.OBSERVER)).thenReturn(observerList);
        apiStatusObserverList.init(apiManagerConfiguration);
        Mockito.verify(apiManagerConfiguration, Mockito.times(1)).getProperty(APIConstants.OBSERVER);
    }
}
