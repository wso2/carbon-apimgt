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

public class DCRClientInfoTestCase {
    DCRClientInfo dcrClientInfo = new DCRClientInfo();

    @Test
    public void registrationClientUriSettergAndGetterTest() {
        final String registrationClientUriValue = "https://localhost:9443/oauth/xxx-xxx-xxx-xxx";
        dcrClientInfo.setRegistrationClientUri(registrationClientUriValue);
        Assert.assertEquals(dcrClientInfo.getRegistrationClientUri(), registrationClientUriValue);
    }

    @Test
    public void registrationAccessTokenSetterAndGetterTest() {
        final String registrationAccessTokenValue = "xxx-new-reg-access-token-xxx";
        dcrClientInfo.setRegistrationAccessToken(registrationAccessTokenValue);
        Assert.assertEquals(dcrClientInfo.getRegistrationAccessToken(), registrationAccessTokenValue);
    }

    @Test
    public void clientIdIssuedAtGetterAndSetterTest() {
        final String clientIdIssuedAtTime = "15-04-2017 03:34:44";
        dcrClientInfo.setClientIdIssuedAt(clientIdIssuedAtTime);
        Assert.assertEquals(dcrClientInfo.getClientIdIssuedAt(), clientIdIssuedAtTime);
    }

    @Test
    public void clientSecretExpiresAtSetterAndGetterTest() {
        final String clientSecretExpiresAtTime = "16-04-2017 03:34:44";
        dcrClientInfo.setClientSecretExpiresAt(clientSecretExpiresAtTime);
        Assert.assertEquals(dcrClientInfo.getClientSecretExpiresAt(), clientSecretExpiresAtTime);
    }

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
    public void tokenEndpointAuthMethodSetterAndGetterTest() {
        final String tokenEndpointAuthMethod = "password";
        dcrClientInfo.setTokenEndpointAuthMethod(tokenEndpointAuthMethod);
        Assert.assertEquals(dcrClientInfo.getTokenEndpointAuthMethod(), tokenEndpointAuthMethod);
    }

    @Test
    public void jwksUriGetterAndSetterTest() {
        final String jwksUri = "https://jwksuri/xxx.xyz";
        dcrClientInfo.setJwksUri(jwksUri);
        Assert.assertEquals(dcrClientInfo.getJwksUri(), jwksUri);
    }

    @Test
    public void userinfoSignedResponseAlgGetterAndSetterTest() {
        final String userinfoSignedResponseAlgValue = "testing-userinfoSignedResponseAlg";
        dcrClientInfo.setUserinfoSignedResponseAlg(userinfoSignedResponseAlgValue);
        Assert.assertEquals(dcrClientInfo.getUserinfoSignedResponseAlg(), userinfoSignedResponseAlgValue);
    }

    @Test
    public void logoUriSetterAndGetterTest() {
        final String logoUri = "https://logouri/xxx.xyz";
        dcrClientInfo.setLogoUri(logoUri);
        Assert.assertEquals(dcrClientInfo.getLogoUri(), logoUri);
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
