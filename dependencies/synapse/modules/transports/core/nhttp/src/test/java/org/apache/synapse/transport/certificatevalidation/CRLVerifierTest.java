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

import junit.framework.TestCase;
import org.apache.synapse.transport.certificatevalidation.crl.CRLCache;
import org.apache.synapse.transport.certificatevalidation.crl.CRLVerifier;import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.x509.X509V2CRLGenerator;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

public class CRLVerifierTest extends TestCase {

    /**
     * To test CRLVerifier behaviour when a revoked certificate is given, a fake certificate will be created, signed
     * by a fake root certificate. To make our life easy, the CrlDistributionPoint extension will be extracted from
     * the real peer certificate in resources directory and copied to the fake certificate as a certificate extension.
     * So the criDistributionPointURL in the fake certificate will be the same as in the real certificate.
     * The created X509CRL object will be put to CRLCache against the criDistributionPointURL. Since the crl is in the
     * cache, there will NOT be a remote call to the CRL server at criDistributionPointURL.
     * @throws Exception
     */
    public void testRevokedCertificate() throws Exception {

        //Add BouncyCastle as Security Provider.
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Utils utils = new Utils();
        //Create X509Certificate from the real certificate file in resource folder.
        X509Certificate realPeerCertificate = utils.getRealPeerCertificate();
        //Extract crlDistributionPointUrl from the real peer certificate.
        String crlDistributionPointUrl = getCRLDistributionPointUrl(realPeerCertificate);

        //Create fake CA certificate.
        KeyPair caKeyPair = utils.generateRSAKeyPair();
        X509Certificate fakeCACert = utils.generateFakeRootCert(caKeyPair);

        //Create fake peer certificate signed by the fake CA private key. This will be a revoked certificate.
        KeyPair peerKeyPair = utils.generateRSAKeyPair();
        BigInteger revokedSerialNumber = BigInteger.valueOf(111);
        X509Certificate fakeRevokedCertificate = generateFakePeerCert(revokedSerialNumber, peerKeyPair.getPublic(),
                caKeyPair.getPrivate(), fakeCACert, realPeerCertificate);

        //Create a crl with fakeRevokedCertificate marked as revoked.
        X509CRL x509CRL = createCRL(fakeCACert, caKeyPair.getPrivate(), revokedSerialNumber);

        CRLCache cache = CRLCache.getCache();
        cache.init(5, 5);
        cache.setCacheValue(crlDistributionPointUrl, x509CRL);

        CRLVerifier crlVerifier  = new CRLVerifier(cache);
        RevocationStatus status = crlVerifier.checkRevocationStatus(fakeRevokedCertificate, null);

        //the fake crl we created will be checked if the fake certificate is revoked. So the status should be REVOKED.
        assertTrue(status == RevocationStatus.REVOKED);
    }

    /**
     * This will use Reflection to call getCrlDistributionPoints() private method in CRLVerifier.
     * @param certificate is a certificate with a proper CRLDistributionPoints extension.
     * @return the extracted cRLDistributionPointUrl.
     * @throws Exception
     */
    private String getCRLDistributionPointUrl(X509Certificate certificate) throws Exception {

        CRLVerifier crlVerifier = new CRLVerifier(null);
        // use reflection since getCrlDistributionPoints() is private.
        Class<? extends CRLVerifier> crlVerifierClass = crlVerifier.getClass();
        Method getCrlDistributionPoints = crlVerifierClass.getDeclaredMethod("getCrlDistributionPoints", X509Certificate.class);
        getCrlDistributionPoints.setAccessible(true);

        //getCrlDistributionPoints(..) returns a list of urls. Get the first one.
        List<String> distPoints = (List<String>) getCrlDistributionPoints.invoke(crlVerifier, certificate);
        return distPoints.get(0);
    }

    /**
     * Creates a fake CRL for the fake CA. The fake certificate with the given revokedSerialNumber will be marked
     * as Revoked in the returned CRL.
     * @param caCert the fake CA certificate.
     * @param caPrivateKey private key of the fake CA.
     * @param revokedSerialNumber the serial number of the fake peer certificate made to be marked as revoked.
     * @return the created fake CRL
     * @throws Exception
     */
    public static X509CRL createCRL(X509Certificate caCert, PrivateKey caPrivateKey, BigInteger revokedSerialNumber)
            throws Exception {

        X509V2CRLGenerator crlGen = new X509V2CRLGenerator();
        Date now = new Date();
        crlGen.setIssuerDN(caCert.getSubjectX500Principal());
        crlGen.setThisUpdate(now);
        crlGen.setNextUpdate(new Date(now.getTime() + TestConstants.NEXT_UPDATE_PERIOD));
        crlGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        crlGen.addCRLEntry(revokedSerialNumber, now, CRLReason.privilegeWithdrawn);
        crlGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false, new AuthorityKeyIdentifierStructure(caCert));
        crlGen.addExtension(X509Extensions.CRLNumber, false, new CRLNumber(BigInteger.valueOf(1)));

        return crlGen.generateX509CRL(caPrivateKey, "BC");
    }

    public X509Certificate generateFakePeerCert(BigInteger serialNumber, PublicKey entityKey,
                                                PrivateKey caKey, X509Certificate caCert, X509Certificate firstCertificate)
            throws Exception {

        Utils utils = new Utils();
        X509V3CertificateGenerator certGen = utils.getUsableCertificateGenerator(caCert, entityKey, serialNumber);
        certGen.copyAndAddExtension(new DERObjectIdentifier(X509Extensions.CRLDistributionPoints.getId()), false, firstCertificate);

        return certGen.generateX509Certificate(caKey, "BC");
    }

}
