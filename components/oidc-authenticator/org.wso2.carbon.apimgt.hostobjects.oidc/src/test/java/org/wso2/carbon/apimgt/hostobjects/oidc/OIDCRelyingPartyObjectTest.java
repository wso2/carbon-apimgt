/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.hostobjects.oidc;

import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Test case for @{@link OIDCRelyingPartyObject}.
 */
public class OIDCRelyingPartyObjectTest {

    /**
     * This test class is used to test the getUserName when the "username" property is given and when the property is
     * not given.
     *
     * @throws Exception Exception.
     */
    @Test
    public void testGetUerName() throws Exception {
        String preferredUserName = "test";
        String email = "test@wso2.com";
        OIDCRelyingPartyObject oidcRelyingPartyObject = new OIDCRelyingPartyObject();
        Properties oidcConfigProperties = new Properties();
        Field oidcConfigPropertiesField = OIDCRelyingPartyObject.class.getDeclaredField("oidcConfigProperties");
        oidcConfigPropertiesField.setAccessible(true);
        oidcConfigPropertiesField.set(oidcRelyingPartyObject, oidcConfigProperties);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("preferred_username", preferredUserName);
        jsonObject.addProperty("email", email);

        Assert.assertEquals("When the property for username is not defined, extraction of username failed",
                preferredUserName, OIDCRelyingPartyObject.getUserName(jsonObject.toString(), oidcRelyingPartyObject));

        oidcConfigProperties.setProperty("usernameClaim", "email");
        oidcConfigPropertiesField.set(oidcRelyingPartyObject, oidcConfigProperties);
        Assert.assertEquals("When the property for username is defined, extraction of username does not honour "
                        + "the defined property", email,
                OIDCRelyingPartyObject.getUserName(jsonObject.toString(), oidcRelyingPartyObject));
    }
}
