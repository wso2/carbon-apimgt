/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.common.gateway.util;

import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;
import org.wso2.carbon.apimgt.common.gateway.jwtgenerator.JWTSignatureAlg;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

/**
 * Helper class for util related to jwt generation.
 */
public final class JWTUtil {
    private static final String NONE = "NONE";
    private static final String SHA256_WITH_RSA = "SHA256withRSA";

    /**
     * Get the JWS compliant signature algorithm code of the algorithm used to sign the JWT.
     *
     * @param signatureAlgorithm - The algorithm used to sign the JWT. If signing is disabled, the value will be NONE.
     * @return - The JWS Compliant algorithm code of the signature algorithm.
     */
    public static String getJWSCompliantAlgorithmCode(String signatureAlgorithm) {

        if (signatureAlgorithm == null || NONE.equals(signatureAlgorithm)) {
            return JWTSignatureAlg.NONE.getJwsCompliantCode();
        } else if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            return JWTSignatureAlg.SHA256_WITH_RSA.getJwsCompliantCode();
        } else {
            return signatureAlgorithm;
        }
    }

    /**
     * Utility method to generate JWT header with public certificate thumbprint for signature verification.
     *
     * @param publicCert         - The public certificate which needs to include in the header as thumbprint
     * @param signatureAlgorithm signature algorithm which needs to include in the header
     * @throws JWTGeneratorException
     */
    public static String generateHeader(Certificate publicCert, String signatureAlgorithm) throws
            JWTGeneratorException {

        try {
            //generate the SHA-1 thumbprint of the certificate
            MessageDigest digestValue = MessageDigest.getInstance("SHA-1");
            byte[] der = publicCert.getEncoded();
            digestValue.update(der);
            byte[] digestInBytes = digestValue.digest();
            String publicCertThumbprint = hexify(digestInBytes);
            String base64UrlEncodedThumbPrint;
            base64UrlEncodedThumbPrint = java.util.Base64.getUrlEncoder()
                    .encodeToString(publicCertThumbprint.getBytes("UTF-8"));
            StringBuilder jwtHeader = new StringBuilder();
            /*
             * Sample header
             * {"typ":"JWT", "alg":"SHA256withRSA", "x5t":"a_jhNus21KVuoFx65LmkW2O_l10",
             * "kid":"a_jhNus21KVuoFx65LmkW2O_l10_RS256"}
             * {"typ":"JWT", "alg":"[2]", "x5t":"[1]", "x5t":"[1]"}
             * */
            jwtHeader.append("{\"typ\":\"JWT\",");
            jwtHeader.append("\"alg\":\"");
            jwtHeader.append(getJWSCompliantAlgorithmCode(signatureAlgorithm));
            jwtHeader.append("\",");

            jwtHeader.append("\"x5t\":\"");
            jwtHeader.append(base64UrlEncodedThumbPrint);
            jwtHeader.append("\",");

            jwtHeader.append("\"kid\":\"");
            jwtHeader.append(getKID(base64UrlEncodedThumbPrint, getJWSCompliantAlgorithmCode(signatureAlgorithm)));
            jwtHeader.append("\"");

            jwtHeader.append("}");
            return jwtHeader.toString();

        } catch (NoSuchAlgorithmException | CertificateEncodingException | UnsupportedEncodingException e) {
            throw new JWTGeneratorException("Error in generating public certificate thumbprint", e);
        }
    }

    /**
     * Helper method to hexify a byte array.
     *
     * @param bytes - The input byte array
     * @return hexadecimal representation
     */
    public static String hexify(byte bytes[]) {

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7',
                '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            buf.append(hexDigits[(aByte & 0xf0) >> 4]);
            buf.append(hexDigits[aByte & 0x0f]);
        }
        return buf.toString();
    }

    /**
     * Helper method to add kid claim into to JWT_HEADER.
     *
     * @param certThumbprint     thumbPrint generated for certificate
     * @param signatureAlgorithm relevant signature algorithm
     * @return KID
     */
    private static String getKID(String certThumbprint, String signatureAlgorithm) {

        return certThumbprint + "_" + signatureAlgorithm;
    }

    /**
     * Utility method to sign a JWT assertion with a particular signature algorithm.
     *
     * @param assertion          valid JWT assertion
     * @param privateKey         private key which use to sign the JWT assertion
     * @param signatureAlgorithm signature algorithm which use to sign the JWT assertion
     * @return byte array of the JWT signature
     * @throws JWTGeneratorException
     */
    public static byte[] signJwt(String assertion, PrivateKey privateKey, String signatureAlgorithm) throws
            JWTGeneratorException {

        try {
            //initialize signature with private key and algorithm
            Signature signature = Signature.getInstance(signatureAlgorithm);
            signature.initSign(privateKey);

            //update signature with data to be signed
            byte[] dataInBytes = assertion.getBytes(Charset.defaultCharset());
            signature.update(dataInBytes);

            //sign the assertion and return the signature
            return signature.sign();
        } catch (NoSuchAlgorithmException e) {
            //do not log
            throw new JWTGeneratorException("Signature algorithm not found", e);
        } catch (InvalidKeyException e) {
            //do not log
            throw new JWTGeneratorException("Invalid private key provided for signing", e);
        } catch (SignatureException e) {
            //do not log
            throw new JWTGeneratorException("Error while signing JWT", e);
        }
    }
}
