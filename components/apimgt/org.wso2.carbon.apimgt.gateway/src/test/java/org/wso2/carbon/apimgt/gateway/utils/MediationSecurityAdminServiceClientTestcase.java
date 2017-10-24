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
import org.wso2.carbon.apimgt.api.APIManagementException;

import static junit.framework.Assert.fail;

/**
 * Test class for MediationSecurityAdminServiceClient
 */
public class MediationSecurityAdminServiceClientTestcase {

    @Test
    public void testDoEncryption() {
        MediationSecurityAdminServiceClient mediationSecurityAdminServiceClient = null;
        try {
            mediationSecurityAdminServiceClient = new MediationSecurityAdminServiceClient();
            try {
                mediationSecurityAdminServiceClient.doEncryption("abc");
            } catch (APIManagementException e) {
                Assert.assertTrue(e.getMessage().startsWith("Failed to encrypt the secured endpoint password,"));
            }

        } catch (AxisFault axisFault) {
            fail(axisFault.getMessage());
        }
    }
}
