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
 *
 */

package org.wso2.carbon.apimgt.core.impl;

import com.nimbusds.jwt.JWTClaimsSet;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;

import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Test class for JWTWithRSASignatureImpl
 */
public class JWTWithRSASignatureImplTestCase {
    private static final String KEYSTORE_FILE_PATH =
            "src" + File.separator + "test" + File.separator + "resources" + File.separator + "security"
                    + File.separator + "wso2carbon.jks";
    private static final String KEYSTORE_PASSOWRD = "wso2carbon";
    private static final String KEY_ALIAS = "wso2carbon";
    private static final String ALIAS_PASSWORD = "wso2carbon";

    @Test(description = "Test get private Key")
    public void testGetPrivateKey() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        PrivateKey privateKey = jwtWithRSASignature
                .getPrivateKey(KEYSTORE_FILE_PATH, KEYSTORE_PASSOWRD, KEY_ALIAS, ALIAS_PASSWORD);
        Assert.assertNotNull(privateKey);
    }

    @Test(description = "Test get private Key when keyStoreFilePath is null", expectedExceptions =
            IllegalArgumentException.class)
    public void testGetPrivateKeyWithNullKeyStoreFilePath() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        jwtWithRSASignature.getPrivateKey(null, KEYSTORE_PASSOWRD, KEY_ALIAS, ALIAS_PASSWORD);
    }

    @Test(description = "Test get private Key when key Store password is null", expectedExceptions =
            IllegalArgumentException.class)
    public void testGetPrivateKeyWithNullKeyStoreFilePassword() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        jwtWithRSASignature.getPrivateKey(KEYSTORE_FILE_PATH, null, KEY_ALIAS, ALIAS_PASSWORD);
    }

    @Test(description = "Test get private Key when key alias is null", expectedExceptions = IllegalArgumentException
            .class)
    public void testGetPrivateKeyWithNullKeyAlias() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        jwtWithRSASignature.getPrivateKey(KEYSTORE_FILE_PATH, KEYSTORE_PASSOWRD, null, ALIAS_PASSWORD);
    }

    @Test(description = "Test get private Key when key alias password is null", expectedExceptions =
            IllegalArgumentException.class)
    public void testGetPrivateKeyWithNullKeyAliasPassword() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        jwtWithRSASignature.getPrivateKey(KEYSTORE_FILE_PATH, KEYSTORE_PASSOWRD, KEY_ALIAS, null);
    }

    @Test(description = "Test get private Key when key alias is not available in the keystore", expectedExceptions =
            APIManagementException.class)
    public void testGetPrivateKeyWithNonExistingKeyAlias() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        jwtWithRSASignature.getPrivateKey(KEYSTORE_FILE_PATH, KEYSTORE_PASSOWRD, "wrong_alias", ALIAS_PASSWORD);
    }

    @Test(description = "Test get private key when the keystore password is wrong", expectedExceptions =
            APIManagementException.class)
    public void testGetPrivateKeyWithWrongKeystorePassword() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        jwtWithRSASignature.getPrivateKey(KEYSTORE_FILE_PATH, "wrong_password", ALIAS_PASSWORD, KEY_ALIAS);
    }

   @Test(description = "Test RSA sign and serialize when RSA Private key is null", expectedExceptions =
           IllegalArgumentException.class)
   public void testRSASignAndSerializeWithNullRSAPrivateKey()
            throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        JWTClaimsSet jwtClaimsSet =  Mockito.mock(JWTClaimsSet.class);
        jwtWithRSASignature.rsaSignAndSerialize(null, jwtClaimsSet);
    }

    @Test(description = "Test RSA sign and serialize when JWT Claims Set is null", expectedExceptions =
            IllegalArgumentException.class)
    public void testRSASignAndSerializeWithNullJWTClaimsSet()
            throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        RSAPrivateKey rsaPrivateKey = Mockito.mock(RSAPrivateKey.class);
        jwtWithRSASignature.rsaSignAndSerialize(rsaPrivateKey, null);
    }

    @Test(description = "Test get public key")
    public void testGetPublicKey() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        PublicKey publicKey = jwtWithRSASignature.getPublicKey(KEYSTORE_FILE_PATH, KEYSTORE_PASSOWRD, KEY_ALIAS);
        Assert.assertNotNull(publicKey);
    }

    @Test(description = "Test get public key when key store file path is null", expectedExceptions =
            IllegalArgumentException.class)
    public void testGetPublicKeyWithNullKeyStoreFilePath() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        jwtWithRSASignature.getPublicKey(null, KEYSTORE_PASSOWRD, KEY_ALIAS);
    }

    @Test(description = "Test get public key when key store password is null", expectedExceptions =
            IllegalArgumentException.class)
    public void testGetPublicKeyWithNullKeyStorePassword() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        jwtWithRSASignature.getPublicKey(KEYSTORE_FILE_PATH, null, KEY_ALIAS);
    }

    @Test(description = "Test get public key when key alias is null", expectedExceptions =
            IllegalArgumentException.class)
    public void testGetPublicKeyWithNullKeyAlias() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        jwtWithRSASignature.getPublicKey(KEYSTORE_FILE_PATH, KEYSTORE_PASSOWRD, null);
    }

    @Test(description = "Test get public key when the keystore password is wrong", expectedExceptions =
            APIManagementException.class)
    public void testGetPublicKeyWithWrongKeystorePassword() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        jwtWithRSASignature.getPublicKey(KEYSTORE_FILE_PATH, "wrong_password", KEY_ALIAS);
    }

    @Test(description = "Test get public Key when key alias is not available in the keystore", expectedExceptions =
            NullPointerException.class)
    public void testGetPublicKeyWithNonExistingKeyAlias() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        jwtWithRSASignature.getPublicKey(KEYSTORE_FILE_PATH, KEYSTORE_PASSOWRD, "wrong_alias");
    }

    @Test(description = "Test verify RSA Signature when RSA public key is null", expectedExceptions =
            IllegalArgumentException.class)
    public void testVerifyRSASignatureWithNullRSAPublicKey() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        jwtWithRSASignature.verifyRSASignature("sampletoken", null);
    }

    @Test(description = "Test verify RSA Signature when JWT token is null", expectedExceptions =
            IllegalArgumentException.class)
    public void testVerifyRSASignatureWithNullToken() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        RSAPublicKey rsaPublicKey = Mockito.mock(RSAPublicKey.class);
        jwtWithRSASignature.verifyRSASignature(null , rsaPublicKey);
    }

    @Test(description = "Test verify RSA Signature when JWT token is invalid", expectedExceptions =
            APIManagementException.class)
    public void testVerifyRSASignatureWithInvalidToken() throws APIManagementException {
        JWTWithRSASignatureImpl jwtWithRSASignature = new JWTWithRSASignatureImpl();
        String token = "invalidtoken";
        RSAPublicKey rsaPublicKey = Mockito.mock(RSAPublicKey.class);
        jwtWithRSASignature.verifyRSASignature(token, rsaPublicKey);
    }
}
