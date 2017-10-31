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

package org.wso2.carbon.apimgt.core.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.wso2.carbon.apimgt.core.api.JWTWithRSASignature;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;

/**
 * RSA signatures require a public and private RSA key pair. Get the private key from keyStore to sign and
 * get the public key from trustStore to verify the validity of the signature.
 */
public class JWTWithRSASignatureImpl implements JWTWithRSASignature {

    /**
     * {@inheritDoc}
     */
    @Override
    public PrivateKey getPrivateKey(String keyStoreFilePath, String keyStorePassword, String alias,
                                    String aliasPassword) throws APIManagementException {
        if (keyStoreFilePath == null) {
            throw new IllegalArgumentException("Path to key store file must not be null");
        }
        if (keyStorePassword == null) {
            throw new IllegalArgumentException("The key store password must not be null");
        }
        if (alias == null) {
            throw new IllegalArgumentException("The Alias must not be null");
        }
        if (aliasPassword == null) {
            throw new IllegalArgumentException("The Alias password not be null");
        }
        Key key;
        try (InputStream inputStream = new FileInputStream(keyStoreFilePath)) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(inputStream, keyStorePassword.toCharArray());
            key = keyStore.getKey(alias, aliasPassword.toCharArray());
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | CertificateException |
                IOException e) {
            throw new APIManagementException("Error getting requested key: Private key not found ", e);
        }
        if (!(key instanceof PrivateKey)) {
            throw new APIManagementException("Error getting requested key: Private key not found ");
        }
        return (PrivateKey) key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String rsaSignAndSerialize(RSAPrivateKey rsaPrivateKey, JWTClaimsSet claimsSet)
            throws APIManagementException {
        if (rsaPrivateKey == null) {
            throw new IllegalArgumentException("The private key must not be null");
        }
        if (claimsSet == null) {
            throw new IllegalArgumentException("The JWTClaimsSet must not be null");
        }
        JWSSigner signer = new RSASSASigner(rsaPrivateKey);
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);
        try {
            jwt.sign(signer);
        } catch (JOSEException e) {
            throw new APIManagementException("Error signing JWT ", e);
        }
        return jwt.serialize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublicKey getPublicKey(String keyStoreFilePath, String keyStorePassword, String alias)
            throws APIManagementException {
        if (keyStoreFilePath == null) {
            throw new IllegalArgumentException("Path to key store file must not be null");
        }
        if (keyStorePassword == null) {
            throw new IllegalArgumentException("The key store password must not be null");
        }
        if (alias == null) {
            throw new IllegalArgumentException("The Alias must not be null");
        }

        Certificate cert;
        try (FileInputStream inputStream = new FileInputStream(keyStoreFilePath)) {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(inputStream, keyStorePassword.toCharArray());
            cert = keyStore.getCertificate(alias);
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException e) {
            throw new APIManagementException("Error getting requested key: Public key not found ", e);
        }
        return cert.getPublicKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean verifyRSASignature(String token, RSAPublicKey rsaPublicKey) throws APIManagementException {
        if (token == null) {
            throw new IllegalArgumentException("The SignedJWT must not be null");
        }
        if (rsaPublicKey == null) {
            throw new IllegalArgumentException("The public key must not be null");
        }
        boolean isSignatureVerified;
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(rsaPublicKey);
            isSignatureVerified = signedJWT.verify(verifier);
        } catch (ParseException e) {
            throw new APIManagementException("Error parsing signed JWT string ", e);
        } catch (JOSEException e) {
            throw new APIManagementException("Failed to verify signature ", e);
        }
        return isSignatureVerified;
    }
}
