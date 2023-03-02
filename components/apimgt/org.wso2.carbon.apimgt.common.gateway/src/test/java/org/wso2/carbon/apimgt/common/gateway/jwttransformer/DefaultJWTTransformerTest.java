/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.common.gateway.jwttransformer;

import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;

import java.util.ArrayList;
import java.util.List;

public class DefaultJWTTransformerTest {

    @Test
    public void getTransformedConsumerKey() {
        DefaultJWTTransformer defaultJWTTransformer = new DefaultJWTTransformer();
        TokenIssuerDto tokenIssuerDto = new TokenIssuerDto("https://localhost:9443/oauth2/token");
        tokenIssuerDto.setConsumerKeyClaim("aud");
        defaultJWTTransformer.loadConfiguration(tokenIssuerDto);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder().claim("aud", "stringConsumerKey").build();
        try {
            Assert.assertEquals("stringConsumerKey", defaultJWTTransformer.getTransformedConsumerKey(jwtClaimsSet));
        } catch (JWTGeneratorException e) {
            Assert.fail("JWTGeneratorException thrown");
        }

        List<String> audList = new ArrayList<>(2);
        audList.add("arrayConsumerKey");
        audList.add("someOtherAudience");
        jwtClaimsSet = new JWTClaimsSet.Builder().claim("aud",  audList).build();
        try {
            Assert.assertEquals("arrayConsumerKey", defaultJWTTransformer.getTransformedConsumerKey(jwtClaimsSet));
        } catch (JWTGeneratorException e) {
            Assert.fail("JWTGeneratorException thrown");
        }
    }
}
