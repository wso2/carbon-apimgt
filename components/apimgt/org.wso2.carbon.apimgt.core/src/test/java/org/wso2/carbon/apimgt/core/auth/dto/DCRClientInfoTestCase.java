/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.core.auth.dto;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/*
   Test cases for DCRClientInfo class
 */
public class DCRClientInfoTestCase {
    DCRClientInfo dcrClientInfo = new DCRClientInfo();

    @Test
    public void redirectURIsGetterAndSetterTest() {
        List<String> redirectUris = new ArrayList<>();
        redirectUris.add("http://test.url.1.com");
        redirectUris.add("http://test.url.2.com");
        dcrClientInfo.setRedirectURIs(redirectUris);
        Assert.assertEquals(dcrClientInfo.getRedirectURIs(), redirectUris);
        dcrClientInfo.addCallbackUrl(null);
        Assert.assertTrue(dcrClientInfo.getRedirectURIs().size() == 2);
    }

    @Test
    public void grantTypeSetterAndGetterTest() {
        List<String> grantTypes = new ArrayList<>();
        dcrClientInfo.setGrantTypes(grantTypes);

        dcrClientInfo.addGrantType("password");
        dcrClientInfo.addGrantType("client-credentials");
        dcrClientInfo.addGrantType(null);

        List<String> grantTypesAdded = dcrClientInfo.getGrantTypes();
        Assert.assertEquals(grantTypesAdded, grantTypes);
        Assert.assertTrue(grantTypesAdded.size() == 2);
    }
}
