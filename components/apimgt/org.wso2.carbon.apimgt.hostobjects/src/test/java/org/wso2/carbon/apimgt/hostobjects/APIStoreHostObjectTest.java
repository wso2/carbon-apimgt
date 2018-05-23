/*
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
package org.wso2.carbon.apimgt.hostobjects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * APIStore Test Suite
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CarbonUtils.class, HostObjectUtils.class})
public class APIStoreHostObjectTest {
    private static final Log log = LogFactory.getLog(APIStoreHostObjectTest.class);

    public void testAPIStore() {
        //These are some test 
        /*       Testcode goes in here
        */
    }

    @Test
    public void testGetHTTPsURL() throws APIManagementException {
        Context cx = Mockito.mock(Context.class);
        Scriptable thisObj = Mockito.mock(Scriptable.class);
        Function funObj = Mockito.mock(Function.class);
        Object[] args = new Object[1];
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.mockStatic(HostObjectUtils.class);
        ServerConfiguration serverConfiguration = Mockito.mock(ServerConfiguration.class);
        PowerMockito.when(CarbonUtils.getServerConfiguration()).thenReturn(serverConfiguration);
        PowerMockito.when(HostObjectUtils.getBackendPort("https")).thenReturn("9443");

        //when hostName is not set and when hostName is set in system property
        System.setProperty("carbon.local.ip", "127.0.0.1");
        Assert.assertEquals("https://127.0.0.1:9443", APIStoreHostObject.jsFunction_getHTTPsURL(cx, thisObj, args, funObj));

        //when hostName is not set and when hostName is set in carbon.xml
        Mockito.when(serverConfiguration.getFirstProperty("HostName")).thenReturn("localhost");
        Assert.assertEquals("https://localhost:9443", APIStoreHostObject.jsFunction_getHTTPsURL(cx, thisObj, args, funObj));

        //when hostName is set to a valid host
        args[0] = "https://localhost:9443/store/";
        Assert.assertEquals("https://localhost:9443", APIStoreHostObject.jsFunction_getHTTPsURL(cx, thisObj, args, funObj));

    }
}
