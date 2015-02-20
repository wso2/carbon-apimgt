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
import org.apache.synapse.transport.certificatevalidation.ocsp.OCSPCache;
import org.apache.synapse.transport.certificatevalidation.ocsp.OCSPVerifier;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.ocsp.*;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Vector;

public class OCSPVerifierTest extends TestCase {

    /**
     * A fake certificate signed by a fake CA is made as the revoked certificate. The created OCSP response to the
     * OCSP request will say that that the fake peer certificate is revoked. the SingleResp derived from the OCSP
     * response will be put the the cache against the serial number of the fake peer certificate. Since the SingleResp
     * which corresponds to the revokedSerialNumber is in the cache, there will NOT be a call to a remote OCSP server.
     * Note that the serviceUrl passed to cache.setCacheValue(..) is null since it is not needed.
     *
     * @throws Exception
     */
    public void testOCSPVerifier() throws Exception{

        //Add BouncyCastle as Security Provider.
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        Utils utils = new Utils();
        //Create fake CA certificate.
        KeyPair caKeyPair = utils.generateRSAKeyPair();
        X509Certificate caCert = utils.generateFakeRootCert(caKeyPair);

        //Create fake peer certificate signed by the fake CA private key. This will be a revoked certificate.
        KeyPair peerKeyPair = utils.generateRSAKeyPair();
        BigInteger revokedSerialNumber = BigInteger.valueOf(111);
        X509Certificate revokedCertificate = generateFakePeerCert(revokedSerialNumber, peerKeyPair.getPublic(),
                caKeyPair.getPrivate(), caCert);

        //Create OCSP request to check if certificate with "serialNumber == revokedSerialNumber" is revoked.
        OCSPReq request = getOCSPRequest(caCert,revokedSerialNumber);

        //Create OCSP response saying that certificate with given serialNumber is revoked.
        CertificateID revokedID = new CertificateID(CertificateID.HASH_SHA1, caCert, revokedSerialNumber);
        OCSPResp response = generateOCSPResponse(request, caKeyPair.getPrivate(), caKeyPair.getPublic(), revokedID);
        SingleResp singleResp = ((BasicOCSPResp)response.getResponseObject()).getResponses()[0];

        OCSPCache cache = OCSPCache.getCache();
        cache.init(5,5);
        cache.setCacheValue(revokedSerialNumber,singleResp, request, null);

        OCSPVerifier ocspVerifier= new OCSPVerifier(cache);
        RevocationStatus status = ocspVerifier.checkRevocationStatus(revokedCertificate, caCert);

        //the cache will have the SingleResponse derived from the OCSP response and it will be checked to see if the
        //fake certificate is revoked. So the status should be REVOKED.
        assertTrue(status == RevocationStatus.REVOKED);
    }

    /**
     * An OCSP request is made to be given to the fake CA. Reflection is used to call generateOCSPRequest(..) private
     * method in OCSPVerifier.
     *
     * @param caCert the fake CA certificate.
     * @param revokedSerialNumber the serial number of the certificate which needs to be checked if revoked.
     * @return the created OCSP request.
     * @throws Exception
     */
    private OCSPReq getOCSPRequest(X509Certificate caCert, BigInteger revokedSerialNumber) throws Exception{
        OCSPVerifier ocspVerifier = new OCSPVerifier(null);
        Class ocspVerifierClass = ocspVerifier.getClass();
        Method generateOCSPRequest = ocspVerifierClass.getDeclaredMethod("generateOCSPRequest", X509Certificate.class,
                BigInteger.class);
        generateOCSPRequest.setAccessible(true);

        OCSPReq request =  (OCSPReq)generateOCSPRequest.invoke(ocspVerifier, caCert, revokedSerialNumber);
        return request;
    }

    /**
     * This makes the corresponding OCSP response to the OCSP request which is sent to the fake CA. If the request
     * has a certificateID which is marked as revoked by the CA, the OCSP response will say that the certificate
     * which is referred to by the request, is revoked.
     *
     * @param request the OCSP request which asks if the certificate is revoked.
     * @param caPrivateKey privateKey of the fake CA.
     * @param caPublicKey  publicKey of the fake CA
     * @param revokedID the ID at fake CA which is checked against the certificateId in the request.
     * @return the created OCSP response by the fake CA.
     * @throws NoSuchProviderException
     * @throws OCSPException
     */
    private OCSPResp generateOCSPResponse(OCSPReq request, PrivateKey caPrivateKey, PublicKey caPublicKey,
                                          CertificateID revokedID) throws NoSuchProviderException, OCSPException {

        BasicOCSPRespGenerator basicOCSPRespGenerator = new BasicOCSPRespGenerator(caPublicKey);
        X509Extensions requestExtensions = request.getRequestExtensions();

        if (requestExtensions != null) {

            X509Extension extension = requestExtensions.getExtension(OCSPObjectIdentifiers.id_pkix_ocsp_nonce);

            if (extension != null) {

                Vector<ASN1ObjectIdentifier> oids = new Vector<ASN1ObjectIdentifier>();
                Vector<X509Extension> values = new Vector<X509Extension>();

                oids.add(OCSPObjectIdentifiers.id_pkix_ocsp_nonce);
                values.add(extension);

                basicOCSPRespGenerator.setResponseExtensions(new X509Extensions(oids, values));
            }
        }

        Req[] requests = request.getRequestList();

        for (Req req : requests) {

            CertificateID certID = req.getCertID();

            if (certID.equals(revokedID)) {

                RevokedStatus revokedStatus = new RevokedStatus(new Date(), CRLReason.privilegeWithdrawn);
                Date nextUpdate = new Date(new Date().getTime() + TestConstants.NEXT_UPDATE_PERIOD);
                basicOCSPRespGenerator.addResponse(certID, revokedStatus , nextUpdate , null);
            }
            else {
                basicOCSPRespGenerator.addResponse(certID, CertificateStatus.GOOD);
            }
        }

        BasicOCSPResp basicResp = basicOCSPRespGenerator.generate("SHA256WithRSA", caPrivateKey, null, new Date(), "BC");
        OCSPRespGenerator respGen = new OCSPRespGenerator();

        return respGen.generate(OCSPRespGenerator.SUCCESSFUL, basicResp);
    }

    private X509Certificate generateFakePeerCert(BigInteger serialNumber, PublicKey entityKey,
                                                PrivateKey caKey, X509Certificate caCert)
            throws Exception {
        Utils utils = new Utils();
        X509V3CertificateGenerator certGen = utils.getUsableCertificateGenerator(caCert,entityKey, serialNumber);
        return certGen.generateX509Certificate(caKey, "BC");
    }
}
