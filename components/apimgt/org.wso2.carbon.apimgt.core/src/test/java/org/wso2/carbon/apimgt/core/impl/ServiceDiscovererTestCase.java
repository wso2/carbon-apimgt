/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;

public class ServiceDiscovererTestCase {

    @Test
    public void testInit() throws Exception {
        ServiceDiscoverer serviceDiscoverer = Mockito.mock(ServiceDiscoverer.class);
        ServiceDiscovererKubernetes sdk = new ServiceDiscovererKubernetes();
        sdk.
        HashMap<String, String> implParameters = new HashMap<>();
        implParameters.put("namespace", "dev");
        implParameters.put("criteria", "app=web,db=mysql");
        serviceDiscoverer.init(implParameters);

        Assert.assertEquals(serviceDiscoverer.getNamespaceFilter(),"dev");
    }


}
