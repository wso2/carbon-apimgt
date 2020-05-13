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
        if (algorithm != null && (JWSAlgorithm.RS256.equals(algorithm) || JWSAlgorithm.RS512.equals(algorithm) ||
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
            throw new APIManagementException("Error w", e);
        }

        if (publicCert != null) {
            JWSAlgorithm algorithm = jwt.getHeader().getAlgorithm();
            if (algorithm != null && (JWSAlgorithm.RS256.equals(algorithm) || JWSAlgorithm.RS512.equals(algorithm) ||
                    JWSAlgorithm.RS384.equals(algorithm))) {
                return verifyTokenSignature(jwt, (RSAPublicKey) publicCert.getPublicKey());
            } else {
                log.error("Public key is not a RSA");
                throw new APIManagementException("Public key is not a RSA");
            }
        } else {
            log.error("Couldn't find a public certificate to verify signature with alias " + alias);
            throw new APIManagementException("Couldn't find a public certificate to verify signature with alias ");
        }
    }

}
