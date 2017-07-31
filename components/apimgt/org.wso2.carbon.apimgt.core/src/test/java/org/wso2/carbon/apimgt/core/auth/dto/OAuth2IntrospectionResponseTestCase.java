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

public class OAuth2IntrospectionResponseTestCase {
    OAuth2IntrospectionResponse oAuth2IntrospectionResponse = new OAuth2IntrospectionResponse();

    @Test
    public void isActiveSetterAndGetterTest() throws Exception {
        oAuth2IntrospectionResponse.setActive(true);
        Assert.assertTrue(oAuth2IntrospectionResponse.isActive());
    }

    @Test
    public void clientIdSetterAndGetterTest() throws Exception {
        final String testClientId = "id_0001";
        oAuth2IntrospectionResponse.setClientId(testClientId);
        Assert.assertEquals(oAuth2IntrospectionResponse.getClientId(), testClientId);
    }

    @Test
    public void usernameSetterAndGetterTest() throws Exception {
        final String testUserName = "user_0001";
        oAuth2IntrospectionResponse.setUsername(testUserName);
        Assert.assertEquals(oAuth2IntrospectionResponse.getUsername(), testUserName);
    }

    @Test
    public void scopeSetterAndGetterTest() throws Exception {
        final String testScope = "test_scope_1";
        oAuth2IntrospectionResponse.setScope(testScope);
        Assert.assertEquals(oAuth2IntrospectionResponse.getScope(), testScope);
    }

    @Test
    public void subSetterAndGetterTest() throws Exception {
        final String testSub = "test_sub_0001";
        oAuth2IntrospectionResponse.setSub(testSub);
        Assert.assertEquals(oAuth2IntrospectionResponse.getSub(), testSub);
    }

    @Test
    public void audSetterAndGetterTest() throws Exception {
        final String testAud = "test_aud_0001";
        oAuth2IntrospectionResponse.setAud(testAud);
        Assert.assertEquals(oAuth2IntrospectionResponse.getAud(), testAud);
    }

    @Test
    public void issSetterAndGetterTest() throws Exception {
        final String testIss = "test_iss_0001";
        oAuth2IntrospectionResponse.setIss(testIss);
        Assert.assertEquals(oAuth2IntrospectionResponse.getIss(), testIss);
    }

    @Test
    public void expSetterAndGetterTest() throws Exception {
        final long testExp = 22000;
        oAuth2IntrospectionResponse.setExp(testExp);
        Assert.assertEquals(oAuth2IntrospectionResponse.getExp(), testExp);
    }

    @Test
    public void iatSetterAndGetterTest() throws Exception {
        oAuth2IntrospectionResponse.setIat(33000);
        Assert.assertEquals(oAuth2IntrospectionResponse.getIat(), 33000);
    }
}
