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
package org.wso2.carbon.apimgt.hostobjects.oidc.internal;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.*;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;


public class Util {

    private static Log log = LogFactory.getLog(Util.class);

    private static TenantRegistryLoader tenantRegistryLoader = null;


    public static boolean verifySignature(SignedJWT signedIdToken, ServerConfiguration serverConfiguration)
            throws IOException, ParseException, JOSEException {

        boolean isSigValid = false;
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(serverConfiguration.getJwksUri());
        HttpResponse response = httpClient.execute(get);

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        String jsonString = "";
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            jsonString = jsonString + line;
        }

        JWKSet jwkSet = JWKSet.parse(jsonString);

        // map of identifier to verifier
        Map<String, JWSVerifier> verifiers = new HashMap<String, JWSVerifier>();

        List<JWK> jwkList = jwkSet.getKeys();

        for (JWK jwkKey : jwkList) {
            if (jwkKey != null && jwkKey.getKeyID() != null) {
                // use the key ID that's built into the key itself
                // TODO (#641): deal with JWK thumbprints
                //this.keys.put(key.getKeyID(), key);

                if (jwkKey instanceof RSAKey) {
                    // build RSA signers & verifiers

                    //if (jwkKey.isPrivate()) { // only add the signer if there's a private key
                    //RSASSASigner signer = new RSASSASigner(((RSAKey) jwkKey).toRSAPrivateKey());
                    //signers.put(id, signer);
                    //}

                    RSASSAVerifier verifier = new RSASSAVerifier(((RSAKey) jwkKey).toRSAPublicKey());
                    String id = jwkKey.getKeyID();
                    verifiers.put(id, verifier);

                }

            }
        }

        for (JWSVerifier verifier : verifiers.values()) {
            try {
                if (signedIdToken.verify(verifier)) {
                    isSigValid = true;
                }
            } catch (JOSEException e) {
                log.error("Failed to validate signature, error was: ", e);
            }
        }

        return isSigValid;

    }


    public static boolean validateIdClaims(ServerConfiguration serverConfiguration, AuthClient authClient,
                                           JWT idToken, String nonce,
                                           JWTClaimsSet idClaims) throws Exception {

        boolean isValid = true;
        String clientId = authClient.getClientId();
        String clientAlgorithm = authClient.getClientAlgorithm();
        Algorithm tokenAlg = idToken.getHeader().getAlgorithm();

        if (clientAlgorithm != null) {

            Algorithm clientAlg = new Algorithm(clientAlgorithm);
            if (!clientAlg.equals(tokenAlg)) {
                isValid = false;
                log.error("Token algorithm " + tokenAlg + " does not match expected algorithm " + clientAlg);
            }
        }

        // check the issuer
        if (idClaims.getIssuer() == null) {
            isValid = false;
            log.error("Id Token Issuer is null");
        } else if (!idClaims.getIssuer().equals(serverConfiguration.getIssuer())) {
            isValid = false;
            log.error("Issuers do not match, expected " + serverConfiguration.getIssuer() +
                    " got " + idClaims.getIssuer());
        }

        // check expiration
        if (idClaims.getExpirationTime() == null) {
            isValid = false;
            log.error("Id Token does not have required expiration claim");
        } else {
            // it's not null, see if it's expired
            Date now = new Date(System.currentTimeMillis() - (OIDCConstants.
                    TIME_SKEW_ALLOWANCE * 1000));
            if (now.after(idClaims.getExpirationTime())) {
                isValid = false;
                log.error("Id Token is expired: " + idClaims.getExpirationTime());
            }
        }

        // check not before
        if (idClaims.getNotBeforeTime() != null) {
            Date now = new Date(System.currentTimeMillis() + (OIDCConstants.
                    TIME_SKEW_ALLOWANCE * 1000));
            if (now.before(idClaims.getNotBeforeTime())) {
                isValid = false;
                log.error("Id Token not valid untill: " + idClaims.getNotBeforeTime());
            }
        }

        // check issued at
        if (idClaims.getIssueTime() == null) {
            isValid = false;
            log.error("Id Token does not have required issued-at claim");
        } else {
            // since it's not null, see if it was issued in the future
            Date now = new Date(System.currentTimeMillis() + (OIDCConstants.
                    TIME_SKEW_ALLOWANCE * 1000));
            if (now.before(idClaims.getIssueTime())) {
                isValid = false;
                log.error("Id Token was issued in the future: " + idClaims.getIssueTime());
            }
        }

        // check audience
        if (idClaims.getAudience() == null) {
            isValid = false;
            log.error("Id token audience is null");
        } else if (!idClaims.getAudience().contains(clientId)) {
            isValid = false;
            log.error("Audience does not match, expected " + clientId +
                    " got " + idClaims.getAudience());
        }

        // compare the nonce to our stored claim
        String nonceValue = idClaims.getStringClaim("nonce");
        if (nonceValue == null || "".equals(nonceValue)) {
            isValid = false;
            log.error("ID token did not contain a nonce claim.");
        } else if (!nonceValue.equals(nonce)) {
            isValid = false;
            log.error("Possible replay attack detected! The comparison of the nonce in the returned "
                    + "ID Token to the session " + "NONCE" + " failed. Expected "
                    + nonce + " got " + nonceValue + ".");

        }

        return isValid;
    }



    public static String getUserInfo(ServerConfiguration serverConfiguration,
                                     AuthenticationToken token) throws IOException {

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(serverConfiguration.getUserInfoUri());

        get.setHeader("Authorization", String.format("Bearer %s", token.getAccessTokenValue()));
        HttpResponse response = httpClient.execute(get);

        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        String jsonString = "";
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            jsonString = jsonString + line;
        }
        bufferedReader.close();
        return jsonString;
    }


}
