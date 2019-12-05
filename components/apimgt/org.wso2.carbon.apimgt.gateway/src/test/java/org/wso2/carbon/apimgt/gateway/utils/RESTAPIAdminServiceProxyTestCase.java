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
 */

package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axis2.AxisFault;
import org.junit.Test;

/**
 * Test class for RESTAPIAdminServiceProxy
 */
public class RESTAPIAdminServiceProxyTestCase {

    @Test
    public void testUpdateApi() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = new RESTAPIAdminServiceProxy("abc.com");
        String apiName = "admin--PhoneVerify:v1.0";
        try {
            restapiAdminServiceProxy.updateApi(apiName, "config");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminServiceProxy.updateApi(apiName, "config");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

    @Test
    public void testAddApi() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = new RESTAPIAdminServiceProxy("abc.com");
        String apiName = "admin--PhoneVerify:v1.0";
        try {
            restapiAdminServiceProxy.addApi("config" );
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminServiceProxy.addApi("config");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

    @Test
    public void testGetApi() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = new RESTAPIAdminServiceProxy("abc.com");
        String apiName = "admin--PhoneVerify:v1.0";

        try {
            restapiAdminServiceProxy.getApi(apiName );
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminServiceProxy.getApi(apiName);
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

    @Test
    public void testDeleteApi() throws Exception {
        RESTAPIAdminServiceProxy restapiAdminServiceProxy = new RESTAPIAdminServiceProxy("abc.com");
        String apiName = "admin--PhoneVerify:v1.0";

        try {
            restapiAdminServiceProxy.deleteApi(apiName);
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminServiceProxy.deleteApi(apiName);
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

}
