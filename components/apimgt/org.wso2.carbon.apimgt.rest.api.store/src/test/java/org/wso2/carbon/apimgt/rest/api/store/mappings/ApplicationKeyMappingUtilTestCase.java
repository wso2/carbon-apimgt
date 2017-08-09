/*
 *
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
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.core.models.ApplicationToken;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationTokenDTO;

import java.util.ArrayList;
import java.util.List;

public class ApplicationKeyMappingUtilTestCase {

    @Test
    public void testFromApplicationKeysToDTO() {

        String keyType = "PRODUCTION";

        List<String> grantTypes = new ArrayList<>();
        grantTypes.add("password");
        grantTypes.add("jwt");

        OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
        oAuthApplicationInfo.setKeyType(keyType);
        oAuthApplicationInfo.setClientId("clientID");
        oAuthApplicationInfo.setClientSecret("clientSecret");
        oAuthApplicationInfo.setGrantTypes(grantTypes);

        ApplicationKeysDTO applicationKeysDTO =
                ApplicationKeyMappingUtil.fromApplicationKeysToDTO(oAuthApplicationInfo);

        Assert.assertEquals(applicationKeysDTO.getKeyType().toString(), keyType);
    }

    @Test
    public void testFromApplicationTokenToDTO() {

        String accessToken = "123123123123123123132";

        ApplicationToken applicationToken = new ApplicationToken();
        applicationToken.setAccessToken(accessToken);
        applicationToken.setScopes("Scope1");
        applicationToken.setValidityPeriod(100000);

        ApplicationTokenDTO applicationTokenDTO =
                ApplicationKeyMappingUtil.fromApplicationTokenToDTO(applicationToken);

        Assert.assertEquals(applicationTokenDTO.getAccessToken(), accessToken);
    }
}
