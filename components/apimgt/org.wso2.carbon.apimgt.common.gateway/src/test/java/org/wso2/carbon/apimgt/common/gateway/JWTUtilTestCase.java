/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.common.gateway;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.common.gateway.util.JWTUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Test cases for {@link JWTUtil}
 */
public class JWTUtilTestCase {

    @Test
    public void testGetJWTClaims() {
        String jwt =
                "eyJhbGciOiJSUzI1NiIsIng1dCI6Ik5tSm1PR1V4TXpabFlqTTJaRFJoTlRabFlUQTFZemRoWlRSaU9XRTBOV0kyTTJKbU9UYzF" +
                        "aQSJ9.eyJodHRwOlwvXC93c28yLm9yZ1wvZ2F0ZXdheVwvYXBwbGljYXRpb25uYW1lIjoiT2F1dGg3IiwiZXhwIjoxN" +
                        "DUyNTk0ODkyLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJodHRwOlwvXC93c28yLm9yZ1wvZ2F0ZXdheVwvc3Vi" +
                        "c2NyaWJlciI6ImFkbWluQGNhcmJvbi5zdXBlciIsImlzcyI6Imh0dHA6XC9cL3dzbzIub3JnXC9nYXRld2F5IiwiaHR" +
                        "0cDpcL1wvd3NvMi5vcmdcL2dhdGV3YXlcL2VuZHVzZXIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJodHRwOlwvXC93c2" +
                        "8yLm9yZ1wvY2xhaW1zXC9yb2xlIjoiYWRtaW4sQXBwbGljYXRpb25cL2Rld3ZkZXcsQXBwbGljYXRpb25cL09hdXRoN" +
                        "yxJbnRlcm5hbFwvZXZlcnlvbmUiLCJodHRwOlwvXC93c28yLm9yZ1wvY2xhaW1zXC9lbWFpbGFkZHJlc3MiOiJhZG1p" +
                        "bkB3c28yLmNvbSIsImlhdCI6MTQ1MjU5MzI1NCwiaHR0cDpcL1wvd3NvMi5vcmdcL2NsYWltc1wvb3JnYW5pemF0aW9" +
                        "uIjoiV1NPMiJ9.WRo2p92f-pt1vH9xfLgmrPWNKJfmST2QSPYcth7gXKz64LdP9zAMUtfAk9DVRdHTIQR3gX0jF4Ohb" +
                        "4UbNN4Oo97a35oTL1iRxIRTKUkh8L1dpt3H03Z0Ze7Q2giHGZikMIQv3gavHRYKjNMoU_1MuB90jiK7";
        Assert.assertNotNull(JWTUtil.getJWTClaims(jwt));
    }

    @Test
    public void testGetJWTClaimsWhenJWTNotAvailable() {
        Assert.assertNull(JWTUtil.getJWTClaims(null));
    }

    @Test
    public void testJWTHeader() throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(
                Files.newInputStream(Paths.get("src/test/resources/cnf/certificate.pem"))
        );
        String signatureAlgorithm = "SHA256withRSA";

        //Use SHA-256 as the certificate hashing algorithm
        String jwt = JWTUtil.generateHeader(cert, signatureAlgorithm, true, true, false);
        Assert.assertNotNull(jwt);
        Assert.assertTrue(jwt.contains("kid"));

        String encodedThumbprint = generateCertThumbprint(cert, "SHA-256");
        //Check if the encoded thumbprint matches with the JWT header's x5t#S256
        Assert.assertTrue(jwt.contains(encodedThumbprint));
        Assert.assertTrue(jwt.contains("x5t#S256"));

        //Use SHA-1 as the certificate hashing algorithm
        jwt = JWTUtil.generateHeader(cert, signatureAlgorithm, false, false, false);
        Assert.assertNotNull(jwt);
        Assert.assertFalse(jwt.contains("kid"));

        encodedThumbprint = generateCertThumbprint(cert, "SHA-1");
        //Check if the encoded thumbprint matches with the JWT header's x5t
        Assert.assertTrue(jwt.contains(encodedThumbprint));
        Assert.assertTrue(jwt.contains("x5t"));
    }

    private String generateCertThumbprint(Certificate cert, String hashingAlgorithm) throws Exception {
        //Get the public certificate's thumbprint and base64url encode it
        byte[] der = cert.getEncoded();
        MessageDigest digestValue = MessageDigest.getInstance(hashingAlgorithm);
        digestValue.update(der);
        byte[] digestInBytes = digestValue.digest();
        String publicCertThumbprint = hexify(digestInBytes);
        return java.util.Base64.getUrlEncoder()
                .encodeToString(publicCertThumbprint.getBytes("UTF-8"));
    }

    /**
     * Helper method to hexify a byte array.
     *
     * @param bytes - The input byte array
     * @return hexadecimal representation
     */
    private String hexify(byte bytes[]) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuilder buf = new StringBuilder(bytes.length * 2);

        for (byte aByte : bytes) {
            buf.append(hexDigits[(aByte & 0xf0) >> 4]);
            buf.append(hexDigits[aByte & 0x0f]);
        }

        return buf.toString();
    }
}
