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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.common.gateway.constants.JWTConstants;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;
import org.wso2.carbon.apimgt.common.gateway.jwtgenerator.JWTSignatureAlg;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Helper class for util related to jwt generation.
 */
public final class JWTUtil {

    private static final Log log = LogFactory.getLog(JWTUtil.class);
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

    public static String generateHeader(Certificate publicCert, String signatureAlgorithm)
            throws JWTGeneratorException {
        return generateHeader(publicCert, signatureAlgorithm, false, false, false);
    }

    /**
     * Utility method to generate JWT header with public certificate thumbprint for signature verification.
     *
     * @param publicCert         The public certificate which needs to include in the header as thumbprint
     * @param signatureAlgorithm Signature algorithm which needs to include in the header
     * @param useKid             Specifies whether the header should include the kid property
     * @param useSHA256Hash      Specifies whether to use SHA-256 algorithm to generate the certificate thumbprint
     * @throws JWTGeneratorException
     */

    public static String generateHeader(Certificate publicCert, String signatureAlgorithm, boolean useKid,
                                        boolean useSHA256Hash, boolean encodeX5tWithoutPadding)
            throws JWTGeneratorException {

        /*
         * Sample header
         * {"typ":"JWT", "alg":"SHA256withRSA", "x5t":"a_jhNus21KVuoFx65LmkW2O_l10",
         * "kid":"a_jhNus21KVuoFx65LmkW2O_l10"}
         * {"typ":"JWT", "alg":"[2]", "x5t":"[1]"}
         * */
        try {
            X509Certificate x509Certificate = (X509Certificate) publicCert;

            String hashingAlgorithm = useSHA256Hash ? JWTConstants.SHA_256 : JWTConstants.SHA_1;
            //generate the thumbprint of the certificate
            MessageDigest digestValue = MessageDigest.getInstance(hashingAlgorithm);
            byte[] der = publicCert.getEncoded();
            digestValue.update(der);
            byte[] digestInBytes = digestValue.digest();
            String publicCertThumbprint = hexify(digestInBytes);
            String base64UrlEncodedThumbPrint;
            if (encodeX5tWithoutPadding) {
                base64UrlEncodedThumbPrint = java.util.Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(publicCertThumbprint.getBytes("UTF-8"));
            } else {
                base64UrlEncodedThumbPrint = java.util.Base64.getUrlEncoder()
                        .encodeToString(publicCertThumbprint.getBytes("UTF-8"));
            }

            JSONObject jwtHeader = new JSONObject();
            jwtHeader.put("typ", "JWT");
            jwtHeader.put("alg", getJWSCompliantAlgorithmCode(signatureAlgorithm));
            if (useSHA256Hash) {
                jwtHeader.put(JWTConstants.X5T256_PARAMETER, base64UrlEncodedThumbPrint);
            } else {
                jwtHeader.put(JWTConstants.X5T_PARAMETER, base64UrlEncodedThumbPrint);
            }

            if (useKid) {
                jwtHeader.put("kid", getKID(x509Certificate));
            }
            return jwtHeader.toString();

        } catch (NoSuchAlgorithmException | CertificateEncodingException | UnsupportedEncodingException e) {
            throw new JWTGeneratorException("Error in generating public certificate thumbprint", e);
        } catch (JSONException e) {
            throw new JWTGeneratorException("Encountered an error while generating JWT header json object", e);
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
     * @param cert X509 certificate
     * @return KID
     */
    public static String getKID(X509Certificate cert) {
        String serialNumber = cert.getSerialNumber().toString();
        String issuerName = cert.getIssuerDN().getName();
        String kid = issuerName + "#" + serialNumber;
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(kid.getBytes(StandardCharsets.UTF_8));
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

    /**
     * Parse a jwt assertion provided in string format and returns set of claims
     * defined in the assertion.
     *
     * @param jwt jwt assertion
     * @return claims as a {@link Map}. if jwt is not parse-able null will be returned.
     */
    public static Map<String, String> getJWTClaims(String jwt) {

        if (StringUtils.isNotEmpty(jwt)) {
            Map<String, String> jwtClaims = new HashMap<>();
            String[] jwtTokenArray = jwt.split(Pattern.quote("."));
            // decoding JWT
            try {
                byte[] jwtByteArray = Base64.decodeBase64(jwtTokenArray[1].getBytes(StandardCharsets.UTF_8));
                String jwtAssertion = new String(jwtByteArray, StandardCharsets.UTF_8);
                JsonElement parsedJson = new JsonParser().parse(jwtAssertion);
                if (parsedJson.isJsonObject()) {
                    JsonObject rootObject = parsedJson.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> rootElement : rootObject.entrySet()) {
                        if (rootElement.getValue().isJsonPrimitive()) {
                            jwtClaims.put(rootElement.getKey(), rootElement.getValue().getAsString());
                        } else if (rootElement.getValue().isJsonArray()) {
                            JsonArray arrayElement = rootElement.getValue().getAsJsonArray();
                            List<String> element = new ArrayList<>();
                            for (JsonElement jsonElement : arrayElement) {
                                element.add(jsonElement.getAsString());
                            }
                            jwtClaims.put(rootElement.getKey(), String.join("|", element));
                        } else if (rootElement.getValue().isJsonObject()) {
                            getJWTClaimsArray(jwtClaims, (JsonObject) rootElement.getValue(), rootElement.getKey());
                        }
                    }
                }
            } catch (JsonParseException e) {
                // gson throws runtime exceptions for parsing errors. We don't want to throw
                // errors and break the flow from this util method. Therefore logging and
                // returning null for error case
                log.error("Error occurred while parsing jwt claims");
            }
            return jwtClaims;
        } else {
            return null;
        }

    }

    private static void getJWTClaimsArray(Map<String, String> jwtClaims, JsonObject jsonObject, String parent) {

        if (jsonObject.isJsonObject()) {
            JsonObject rootObject = jsonObject.getAsJsonObject();
            for (Map.Entry<String, JsonElement> rootElement : rootObject.entrySet()) {
                String claimKey = parent.concat(".").concat(rootElement.getKey());
                if (rootElement.getValue().isJsonPrimitive()) {
                    jwtClaims.put(claimKey, rootElement.getValue().getAsString());
                } else if (rootElement.getValue().isJsonArray()) {
                    JsonArray arrayElement = rootElement.getValue().getAsJsonArray();
                    List<String> element = new ArrayList<>();
                    for (JsonElement jsonElement : arrayElement) {
                        element.add(jsonElement.getAsString());
                    }
                    jwtClaims.put(claimKey, String.join("|", element));
                } else if (rootElement.getValue().isJsonObject()) {
                    getJWTClaimsArray(jwtClaims, (JsonObject) rootElement.getValue(), claimKey);
                }
            }
        }
    }
}
