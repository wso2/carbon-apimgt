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

package org.wso2.carbon.apimgt.keymgt.issuers;

import org.junit.Assert;
import org.junit.Test;

public class PermissionBasedScopeIssuerTestCase {

    @Test
    public void testGetPrefix() throws Exception {

        String ISSUER_PREFIX = "perm";
        PermissionBasedScopeIssuer permissionBasedScopeIssuer = new PermissionBasedScopeIssuer();
        Assert.assertEquals(ISSUER_PREFIX, permissionBasedScopeIssuer.getPrefix());
    }

}