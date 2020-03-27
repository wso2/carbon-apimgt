/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.jwt.transformer;

import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.impl.dto.ClaimMappingDto;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.TokenIssuerDto;

import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

public class DefaultJWTTransformerTest {

    @Test
    public void testTransFormJWTFromDefaultClaimMapping() throws ParseException, IOException {

        String jwt = "{\"iss\":\"https://localhost:9443/oauth2/token\",\"sub\":\"admin@carbon.super\",\n" +
                "\"given_name\":\"first\",\n" +
                "\"family_name\":\"abc\",\n" +
                "\"organization\":\"wso2\"}";
        JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jwt);
        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("default-claim-mapping.properties"));
        DefaultJWTTransformer defaultJWTTransformer = new DefaultJWTTransformer(jwtConfigurationDto, properties);
        JWTClaimsSet transformedClaimSet = defaultJWTTransformer.transform(jwtClaimsSet);
        Assert.assertEquals(transformedClaimSet.getSubject(), "admin@carbon.super");
        Assert.assertEquals(transformedClaimSet.getClaim("http://wso2.org/claims/givenname"), "first");
        Assert.assertEquals(transformedClaimSet.getClaim("http://wso2.org/claims/lastname"), "abc");
        Assert.assertEquals(transformedClaimSet.getClaim("organization"), "wso2");
    }

    @Test
    public void testTransFormJWTFromDefaultClaimMappingWithTokenIssuer() throws ParseException, IOException {

        String jwt = "{\"iss\":\"https://localhost:9443/oauth2/token\",\"sub\":\"admin@carbon.super\",\n" +
                "\"given_name\":\"first\",\n" +
                "\"family_name\":\"abc\",\n" +
                "\"organization\":\"wso2\"}";
        JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jwt);
        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        TokenIssuerDto tokenIssuerDto = new TokenIssuerDto("https://localhost:9443/oauth2/token");
        tokenIssuerDto.setDisableDefaultClaimMapping(true);
        jwtConfigurationDto.getTokenIssuerDtoMap().put("https://localhost:9443/oauth2/token", tokenIssuerDto);
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("default-claim-mapping.properties"));
        DefaultJWTTransformer defaultJWTTransformer = new DefaultJWTTransformer(jwtConfigurationDto, properties);
        JWTClaimsSet transformedClaimSet = defaultJWTTransformer.transform(jwtClaimsSet);
        Assert.assertEquals(transformedClaimSet.getSubject(), "admin@carbon.super");
        Assert.assertEquals(transformedClaimSet.getClaim("given_name"), "first");
        Assert.assertEquals(transformedClaimSet.getClaim("family_name"), "abc");
        Assert.assertEquals(transformedClaimSet.getClaim("organization"), "wso2");
    }

    @Test
    public void testTransFormJWTFromDefaultClaimMappingWithTokenIssuerSetToDisable() throws ParseException,
            IOException {

        String jwt = "{\"iss\":\"https://localhost:9443/oauth2/token\",\"sub\":\"admin@carbon.super\",\n" +
                "\"given_name\":\"first\",\n" +
                "\"family_name\":\"abc\",\n" +
                "\"organization\":\"wso2\"}";
        JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jwt);
        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        TokenIssuerDto tokenIssuerDto = new TokenIssuerDto("https://localhost:9443/oauth2/token");
        jwtConfigurationDto.getTokenIssuerDtoMap().put("https://localhost:9443/oauth2/token", tokenIssuerDto);
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("default-claim-mapping.properties"));
        DefaultJWTTransformer defaultJWTTransformer = new DefaultJWTTransformer(jwtConfigurationDto, properties);
        JWTClaimsSet transformedClaimSet = defaultJWTTransformer.transform(jwtClaimsSet);
        Assert.assertEquals(transformedClaimSet.getSubject(), "admin@carbon.super");
        Assert.assertEquals(transformedClaimSet.getClaim("http://wso2.org/claims/givenname"), "first");
        Assert.assertEquals(transformedClaimSet.getClaim("http://wso2.org/claims/lastname"), "abc");
        Assert.assertEquals(transformedClaimSet.getClaim("organization"), "wso2");
    }

    @Test
    public void testTransFormJWTFromDefaultClaimMappingWithTokenIssuerSetToDisablewithClaimMapping()
            throws ParseException,
            IOException {

        String jwt = "{\"iss\":\"https://localhost:9443/oauth2/token\",\"sub\":\"admin@carbon.super\",\n" +
                "\"given_name\":\"first\",\n" +
                "\"family_name\":\"abc\",\n" +
                "\"organization\":\"wso2\"}";
        JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jwt);
        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        TokenIssuerDto tokenIssuerDto = new TokenIssuerDto("https://localhost:9443/oauth2/token");
        ClaimMappingDto claimMappingDto = new ClaimMappingDto("given_name", "http://idp.wso2.org/givenname");
        ClaimMappingDto organization = new ClaimMappingDto("organization", "http://idp.wso2.org/organization");
        tokenIssuerDto.getClaimConfigurations().put("given_name", claimMappingDto);
        tokenIssuerDto.getClaimConfigurations().put("organization", organization);
        jwtConfigurationDto.getTokenIssuerDtoMap().put("https://localhost:9443/oauth2/token", tokenIssuerDto);
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("default-claim-mapping.properties"));
        DefaultJWTTransformer defaultJWTTransformer = new DefaultJWTTransformer(jwtConfigurationDto, properties);
        JWTClaimsSet transformedClaimSet = defaultJWTTransformer.transform(jwtClaimsSet);
        Assert.assertEquals(transformedClaimSet.getSubject(), "admin@carbon.super");
        Assert.assertEquals(transformedClaimSet.getClaim("http://wso2.org/claims/givenname"), "first");
        Assert.assertEquals(transformedClaimSet.getClaim("http://wso2.org/claims/lastname"), "abc");
        Assert.assertEquals(transformedClaimSet.getClaim("http://idp.wso2.org/organization"), "wso2");
        Assert.assertNull(transformedClaimSet.getClaim("http://idp.wso2.org/givenname"));
    }

    @Test
    public void testTransFormJWTFromDefaultClaimMappingWithTokenIssuerEithClaimMapping()
            throws ParseException,
            IOException {

        String jwt = "{\"iss\":\"https://localhost:9443/oauth2/token\",\"sub\":\"admin@carbon.super\",\n" +
                "\"given_name\":\"first\",\n" +
                "\"family_name\":\"abc\",\n" +
                "\"organization\":\"wso2\"}";
        JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jwt);
        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        TokenIssuerDto tokenIssuerDto = new TokenIssuerDto("https://localhost:9443/oauth2/token");
        ClaimMappingDto claimMappingDto = new ClaimMappingDto("given_name", "http://idp.wso2.org/givenname");
        ClaimMappingDto organization = new ClaimMappingDto("organization", "http://idp.wso2.org/organization");
        tokenIssuerDto.getClaimConfigurations().put("given_name", claimMappingDto);
        tokenIssuerDto.getClaimConfigurations().put("organization", organization);
        tokenIssuerDto.setDisableDefaultClaimMapping(true);
        jwtConfigurationDto.getTokenIssuerDtoMap().put("https://localhost:9443/oauth2/token", tokenIssuerDto);
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("default-claim-mapping.properties"));
        DefaultJWTTransformer defaultJWTTransformer = new DefaultJWTTransformer(jwtConfigurationDto, properties);
        JWTClaimsSet transformedClaimSet = defaultJWTTransformer.transform(jwtClaimsSet);
        Assert.assertEquals(transformedClaimSet.getSubject(), "admin@carbon.super");
        Assert.assertEquals(transformedClaimSet.getClaim("http://idp.wso2.org/givenname"), "first");
        Assert.assertEquals(transformedClaimSet.getClaim("family_name"), "abc");
        Assert.assertEquals(transformedClaimSet.getClaim("http://idp.wso2.org/organization"), "wso2");
    }
}