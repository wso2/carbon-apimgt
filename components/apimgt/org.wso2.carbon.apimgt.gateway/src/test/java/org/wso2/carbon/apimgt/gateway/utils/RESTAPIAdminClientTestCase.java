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
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for RESTAPIAdminClient
 */
public class RESTAPIAdminClientTestCase {

    @Test
    public void testUpdateApi() throws Exception {
        RESTAPIAdminClient restapiAdminClient = new RESTAPIAdminClient("admin", "PhoneVerify",
                "1.0");
        try {
            restapiAdminClient.updateApi("config", "abc.com");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.updateApi("config");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.updateApiForInlineScript("config", "abc.com");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.updateApiForInlineScript("config");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.updateDefaultApi("config", "abc.com");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.updateDefaultApi("config");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

    @Test
    public void testAddApi() throws Exception {
        RESTAPIAdminClient restapiAdminClient = new RESTAPIAdminClient("admin", "PhoneVerify",
                "1.0");
        try {
            restapiAdminClient.addApi("config", "abc.com");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.addApi("config");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.addPrototypeApiScriptImpl("config", "abc.com");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.addPrototypeApiScriptImpl("config");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.addDefaultAPI("config", "abc.com");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.addDefaultAPI("config");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

    @Test
    public void testGetApi() throws Exception {
        RESTAPIAdminClient restapiAdminClient = new RESTAPIAdminClient("admin", "PhoneVerify",
                "1.0");
        try {
            restapiAdminClient.getApi("abc.com");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.getApi();
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.getDefaultApi();
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.getDefaultApi("config");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

    @Test
    public void testDeleteApi() throws Exception {
        RESTAPIAdminClient restapiAdminClient = new RESTAPIAdminClient("admin", "PhoneVerify",
                "1.0");
        try {
            restapiAdminClient.deleteApi("abc.com");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.deleteApi();
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.deleteDefaultApi();
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
        try {
            restapiAdminClient.deleteDefaultApi("config");
        } catch (AxisFault axisFault) {
            // test for axisFault
        }
    }

}
