/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;

public class JWTUtil {

    private static final Log log = LogFactory.getLog(JWTUtil.class);

    /**
     * This method used to retrieve JWKS keys from endpoint
     *
     * @param jwksEndpoint
     * @return
     * @throws IOException
     */
    public static String retrieveJWKSConfiguration(String jwksEndpoint) throws IOException {

        URL url = new URL(jwksEndpoint);
        try (CloseableHttpClient httpClient = (CloseableHttpClient) APIUtil
                .getHttpClient(url.getPort(), url.getProtocol())) {
            HttpGet httpGet = new HttpGet(jwksEndpoint);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    try (InputStream content = entity.getContent()) {
                        return IOUtils.toString(content);
                    }
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * Verify the JWT token signature.
     *
     * @param jwt SignedJwt Token
     * @param publicKey      public certificate
     * @return whether the signature is verified or or not
     */
    public static boolean verifyTokenSignature(SignedJWT jwt, RSAPublicKey publicKey) {

        JWSAlgorithm algorithm = jwt.getHeader().getAlgorithm();
        if ((JWSAlgorithm.RS256.equals(algorithm) || JWSAlgorithm.RS512.equals(algorithm) ||
                JWSAlgorithm.RS384.equals(algorithm))) {
            try {
                JWSVerifier jwsVerifier = new RSASSAVerifier(publicKey);
                return jwt.verify(jwsVerifier);
            } catch (JOSEException e) {
                log.error("Error while verifying JWT signature", e);
                return false;
            }
        } else {
            log.error("Public key is not a RSA");
            return false;
        }
    }

    /**
     * Verify the JWT token signature.
     *
     * @param jwt SignedJwt Token
     * @param alias      public certificate keystore alias
     * @return whether the signature is verified or or not
     * @throws APIManagementException in case of signature verification failure
     */
    public static boolean verifyTokenSignature(SignedJWT jwt, String alias) throws APIManagementException {

        Certificate publicCert = null;
        //Read the client-truststore.jks into a KeyStore
        try {
            publicCert = APIUtil.getCertificateFromTrustStore(alias);
        } catch (APIManagementException e) {
            throw new APIManagementException("Error retrieving certificate from truststore ",e);
        }

        if (publicCert != null) {
            JWSAlgorithm algorithm = jwt.getHeader().getAlgorithm();
            if ((JWSAlgorithm.RS256.equals(algorithm) || JWSAlgorithm.RS512.equals(algorithm) ||
                    JWSAlgorithm.RS384.equals(algorithm))) {
                return verifyTokenSignature(jwt, (RSAPublicKey) publicCert.getPublicKey());
            } else {
                log.error("Public key is not RSA");
                throw new APIManagementException("Public key is not RSA");
            }
        } else {
            log.error("Couldn't find a public certificate with alias " + alias + " to verify the signature");
            throw new APIManagementException(
                    "Couldn't find a public certificate with alias " + alias + " to verify the signature");
        }
    }

}
