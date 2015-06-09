/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.hostobjects.sso.internal.util;

import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.credential.CredentialContextSet;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.x509.X509Credential;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Collection;

/**
 * X509Credential implementation for signature verification of self issued tokens. The key is
 * constructed from modulus and exponent
 */
public class X509CredentialImpl implements X509Credential {

    private PublicKey publicKey = null;
    private X509Certificate signingCert = null;

    /**
     * The key is constructed form modulus and exponent.
     *
     * @param modulus
     * @param publicExponent
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.spec.InvalidKeySpecException
     */
    public X509CredentialImpl(BigInteger modulus, BigInteger publicExponent)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        publicKey = keyFactory.generatePublic(spec);
    }

    public X509CredentialImpl(X509Certificate cert) {
        publicKey = cert.getPublicKey();
        signingCert = cert;
    }

    /**
     * Retrieves the publicKey
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    public X509Certificate getSigningCert() {
        return signingCert;
    }

    // ********** Not implemented **************************************************************

    public X509Certificate getEntityCertificate() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<X509CRL> getCRLs() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<X509Certificate> getEntityCertificateChain() {
        // TODO Auto-generated method stub
        return null;
    }

    public CredentialContextSet getCredentalContextSet() {
        // TODO Auto-generated method stub
        return null;
    }

    public Class<? extends Credential> getCredentialType() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getEntityId() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<String> getKeyNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public PrivateKey getPrivateKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public SecretKey getSecretKey() {
        // TODO Auto-generated method stub
        return null;
    }

    public UsageType getUsageType() {
        // TODO Auto-generated method stub
		return null;
	}
}
