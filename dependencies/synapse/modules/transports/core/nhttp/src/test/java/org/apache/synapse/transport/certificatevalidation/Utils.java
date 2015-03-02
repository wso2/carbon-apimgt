/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.transport.certificatevalidation;

import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Contains utility methods used by the test classes.
 */
public class Utils {


    public X509Certificate generateFakeRootCert(KeyPair pair) throws Exception {
        
        X509V1CertificateGenerator  certGen = new X509V1CertificateGenerator();
        certGen.setSerialNumber(BigInteger.valueOf(1));
        certGen.setIssuerDN(new X500Principal("CN=Test CA Certificate"));
        certGen.setNotBefore(new Date(System.currentTimeMillis()));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + TestConstants.VALIDITY_PERIOD));
        certGen.setSubjectDN(new X500Principal("CN=Test CA Certificate"));
        certGen.setPublicKey(pair.getPublic());
        certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

        return certGen.generateX509Certificate(pair.getPrivate(), "BC");
    }


    public KeyPair generateRSAKeyPair() throws Exception {

        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", "BC");
        kpGen.initialize(1024, new SecureRandom());
        return kpGen.generateKeyPair();
    }

    /**
     * CRLVerifierTest and OCSPVerifierTest both will use this method. This has common code for both test classes
     * in creating fake peer certificates.
     * @param caCert Certificate of CA which signs the peer certificate which will be generated.
     * @param peerPublicKey public key of the peer certificate which will be generated.
     * @param serialNumber  serial number of the peer certificate.
     * @return
     */
    public X509V3CertificateGenerator getUsableCertificateGenerator(X509Certificate caCert,
                                                                    PublicKey peerPublicKey, BigInteger serialNumber){
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

        certGen.setSerialNumber(serialNumber);
        certGen.setIssuerDN(caCert.getSubjectX500Principal());
        certGen.setNotBefore(new Date(System.currentTimeMillis()));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + TestConstants.VALIDITY_PERIOD));
        certGen.setSubjectDN(new X500Principal("CN=Test End Certificate"));
        certGen.setPublicKey(peerPublicKey);
        certGen.setSignatureAlgorithm("SHA1WithRSAEncryption");

        return certGen;
    }

    /**
     * Generate X509Certificate object from the peer certificate file in resources directory.
     * @return the created certificate object.
     * @throws Exception
     */
    public X509Certificate getRealPeerCertificate()throws Exception {
        return createCertificateFromResourceFile(TestConstants.REAL_PEER_CERT);
    }

    /**
     * Create a certificate chain from the certificates in the resources directory.
     * @return created array of certificates.
     * @throws Exception
     */
    public X509Certificate[] getRealCertificateChain() throws Exception {

        X509Certificate peerCert = createCertificateFromResourceFile(TestConstants.REAL_PEER_CERT);
        X509Certificate intermediateCert = createCertificateFromResourceFile(TestConstants.INTERMEDIATE_CERT);
        X509Certificate rootCert = createCertificateFromResourceFile(TestConstants.ROOT_CERT);

        return new X509Certificate[]{ peerCert,intermediateCert,rootCert  };
    }

    /**
     * Generates a fake certificate chain. The array will contain two certificates, the root and the peer.
     * @return the created array of certificates.
     * @throws Exception
     */
    public X509Certificate[] getFakeCertificateChain() throws Exception{

        KeyPair rootKeyPair = generateRSAKeyPair();
        X509Certificate rootCert = generateFakeRootCert(rootKeyPair);
        KeyPair entityKeyPair = generateRSAKeyPair();
        BigInteger entitySerialNum =BigInteger.valueOf(111);
        X509V3CertificateGenerator certGen = getUsableCertificateGenerator(rootCert,
                entityKeyPair.getPublic(), entitySerialNum);
        X509Certificate entityCert = certGen.generateX509Certificate(rootKeyPair.getPrivate(), "BC");
        return new X509Certificate[]{entityCert, rootCert};
    }

    private X509Certificate createCertificateFromResourceFile(String resourcePath) throws Exception{

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BC");
        File faceBookCertificateFile = new File(this.getClass().getResource(resourcePath).toURI());
        InputStream in = new FileInputStream(faceBookCertificateFile);
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(in);
        return certificate;
    }
}
