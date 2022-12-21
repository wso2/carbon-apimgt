/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.common.gateway.jwt;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.common.gateway.jwttransformer.JWTTransformer;

import java.security.KeyStore;

public class JWTValidatorConfigurationTest {

    @Test
    public void jwtValidatorConfigurationBuildTest() {
        TokenIssuerDto tokenIssuerDto = Mockito.mock(TokenIssuerDto.class);
        JWTTransformer jwtTransformer = Mockito.mock(JWTTransformer.class);
        long timeStampSkewInSeconds = 1L;
        boolean enableCertificateBoundAccessToken = true;
        KeyStore trustStore = Mockito.mock(KeyStore.class);

        JWTValidatorConfiguration configuration = new JWTValidatorConfiguration.Builder()
                .jwtIssuer(tokenIssuerDto)
                .timeStampSkewInSeconds(timeStampSkewInSeconds)
                .enableCertificateBoundAccessToken(enableCertificateBoundAccessToken)
                .trustStore(trustStore)
                .jwtTransformer(jwtTransformer)
                .build();
        Assert.assertNotNull(configuration);
        Assert.assertEquals("TokenIssuerDTO is not populated", tokenIssuerDto, configuration.getJwtIssuer());
        Assert.assertEquals("jwtTransformer is not populated", jwtTransformer,
                configuration.getJwtTransformer());
        Assert.assertEquals("timeStampSkewInSeconds is not populated", timeStampSkewInSeconds,
                configuration.getTimeStampSkewInSeconds());
        Assert.assertEquals("enableCertificateBoundAccessToken is not populated",
                enableCertificateBoundAccessToken, configuration.isEnableCertificateBoundAccessToken());
        Assert.assertEquals("trustStore is not populated", trustStore, configuration.getTrustStore());
    }
}
