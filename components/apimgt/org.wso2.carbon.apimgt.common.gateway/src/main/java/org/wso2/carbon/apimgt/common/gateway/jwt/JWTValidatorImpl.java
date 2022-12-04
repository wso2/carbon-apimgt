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

package org.wso2.carbon.apimgt.common.gateway.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.util.DateUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.wso2.carbon.apimgt.common.gateway.constants.APIConstants;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.common.gateway.exception.CommonGatewayException;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;
import org.wso2.carbon.apimgt.common.gateway.jwttransformer.DefaultJWTTransformer;
import org.wso2.carbon.apimgt.common.gateway.jwttransformer.JWTTransformer;
import org.wso2.carbon.apimgt.common.gateway.util.JWTUtil;

import java.io.IOException;
import java.io.InputStream;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Default JWTValidator Implementation.
 */
public class JWTValidatorImpl implements JWTValidator {

    TokenIssuerDto tokenIssuer;
    private final Log log = LogFactory.getLog(JWTValidatorImpl.class);
    JWTTransformer jwtTransformer;
    private JWKSet jwkSet;

    // TODO: (VirajSalaka)
    private JWTValidatorConfiguration jwtValidatorConfiguration;

    @Override
    public JWTValidationInfo validateToken(SignedJWTInfo signedJWTInfo) throws CommonGatewayException {

        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        boolean state;
        try {
            state = validateSignature(signedJWTInfo.getSignedJWT());
            if (state) {
                JWTClaimsSet jwtClaimsSet = signedJWTInfo.getJwtClaimsSet();
                state = isValidCertificateBoundAccessToken(signedJWTInfo);
                if (state) {
                    state = validateTokenExpiry(jwtClaimsSet);
                    if (state) {
                        jwtValidationInfo.setConsumerKey(getConsumerKey(jwtClaimsSet));
                        jwtValidationInfo.setScopes(getScopes(jwtClaimsSet));
                        jwtValidationInfo.setAppToken(getIsAppToken(jwtClaimsSet));
                        JWTClaimsSet transformedJWTClaimSet = transformJWTClaims(jwtClaimsSet);
                        createJWTValidationInfoFromJWT(jwtValidationInfo, transformedJWTClaimSet);
                        jwtValidationInfo.setRawPayload(signedJWTInfo.getToken());
                        return jwtValidationInfo;
                    } else {
                        jwtValidationInfo.setValid(false);
                        jwtValidationInfo.setValidationCode(
                                APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                        return jwtValidationInfo;
                    }
                } else {
                    jwtValidationInfo.setValid(false);
                    jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                    return jwtValidationInfo;

                }
            } else {
                jwtValidationInfo.setValid(false);
                jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                return jwtValidationInfo;
            }
        } catch (ParseException | JWTGeneratorException e) {
            throw new CommonGatewayException("Error while parsing JWT", e);
        }
    }
    private boolean isValidCertificateBoundAccessToken(SignedJWTInfo signedJWTInfo) { //Holder of Key token

        if (isCertificateBoundAccessTokenEnabled()) {
            if (signedJWTInfo.getClientCertificate() == null ||
                    StringUtils.isEmpty(signedJWTInfo.getClientCertificateHash())) {
                return true; // If cnf is not available - 200 success
            }
            return signedJWTInfo.getClientCertificateHash().equals(signedJWTInfo.getCertificateThumbprint());
        }
        return true; /// if config is not enabled - 200 success
    }

    private boolean isCertificateBoundAccessTokenEnabled() {
        return jwtValidatorConfiguration.isEnableCertificateBoundAccessToken();
    }
    @Override
    public void loadTokenIssuerConfiguration(TokenIssuerDto tokenIssuerConfigurations) {

        // TODO: (VirajSalaka) read this from config

    }

    @Override
    public void loadValidatorConfiguration(JWTValidatorConfiguration jwtValidatorConfiguration) {
        this.jwtValidatorConfiguration = jwtValidatorConfiguration;
        this.tokenIssuer = jwtValidatorConfiguration.getJwtIssuer();
        JWTTransformer jwtTransformer = jwtValidatorConfiguration.getJwtTransformer();
        if (jwtTransformer != null) {
            this.jwtTransformer = jwtTransformer;
        } else {
            this.jwtTransformer = new DefaultJWTTransformer();
        }
        this.jwtTransformer.loadConfiguration(tokenIssuer);
    }

    protected boolean validateSignature(SignedJWT signedJWT) throws CommonGatewayException {

        String certificateAlias = APIConstants.GATEWAY_PUBLIC_CERTIFICATE_ALIAS;
        try {
            String keyID = signedJWT.getHeader().getKeyID();
            if (StringUtils.isNotEmpty(keyID)) {
                if (tokenIssuer.getJwksConfigurationDTO().isEnabled() &&
                        StringUtils.isNotEmpty(tokenIssuer.getJwksConfigurationDTO().getUrl())) {
                    // Check JWKSet Available in Cache
                    if (jwkSet == null) {
                        jwkSet = retrieveJWKSet();
                    }
                    if (jwkSet.getKeyByKeyId(keyID) == null) {
                        jwkSet = retrieveJWKSet();
                    }
                    if (jwkSet.getKeyByKeyId(keyID) instanceof RSAKey) {
                        RSAKey keyByKeyId = (RSAKey) jwkSet.getKeyByKeyId(keyID);
                        RSAPublicKey rsaPublicKey = keyByKeyId.toRSAPublicKey();
                        if (rsaPublicKey != null) {
                            return JWTUtil.verifyTokenSignature(signedJWT, rsaPublicKey);
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Key Algorithm not supported");
                        }
                        return false; // return false to produce 401 unauthenticated response
                    }
                } else if (tokenIssuer.getCertificate() != null) {
                    log.debug("Retrieve certificate from Token issuer and validating");
                    RSAPublicKey rsaPublicKey = (RSAPublicKey) tokenIssuer.getCertificate().getPublicKey();
                    return JWTUtil.verifyTokenSignature(signedJWT, rsaPublicKey);
                } else {
                    return JWTUtil.verifyTokenSignature(signedJWT, keyID, jwtValidatorConfiguration.getTrustStore());
                }
            }
            return JWTUtil.verifyTokenSignature(signedJWT, certificateAlias, jwtValidatorConfiguration.getTrustStore());
        } catch (ParseException | JOSEException | IOException e) {
            log.error("Error while parsing JWT", e);
        }

        return true;
    }

    protected boolean validateTokenExpiry(JWTClaimsSet jwtClaimsSet) {

        long timestampSkew = jwtValidatorConfiguration.getTimeStampSkewInSeconds();
        Date now = new Date();
        Date exp = jwtClaimsSet.getExpirationTime();
        return exp == null || DateUtils.isAfter(exp, now, timestampSkew);
    }

    protected JWTClaimsSet transformJWTClaims(JWTClaimsSet jwtClaimsSet) throws JWTGeneratorException {

        return jwtTransformer.transform(jwtClaimsSet);
    }

    protected String getConsumerKey(JWTClaimsSet jwtClaimsSet) throws JWTGeneratorException {

        return jwtTransformer.getTransformedConsumerKey(jwtClaimsSet);
    }

    protected List<String> getScopes(JWTClaimsSet jwtClaimsSet) throws JWTGeneratorException {

        return jwtTransformer.getTransformedScopes(jwtClaimsSet);
    }

    protected Boolean getIsAppToken(JWTClaimsSet jwtClaimsSet) throws JWTGeneratorException {

        return jwtTransformer.getTransformedIsAppTokenType(jwtClaimsSet);
    }

    private void createJWTValidationInfoFromJWT(JWTValidationInfo jwtValidationInfo,
                                                JWTClaimsSet jwtClaimsSet)
            throws ParseException {

        jwtValidationInfo.setIssuer(jwtClaimsSet.getIssuer());
        jwtValidationInfo.setValid(true);
        jwtValidationInfo.setClaims(new HashMap<>(jwtClaimsSet.getClaims()));
        if (jwtClaimsSet.getExpirationTime() != null) {
            jwtValidationInfo.setExpiryTime(jwtClaimsSet.getExpirationTime().getTime());
        }
        if (jwtClaimsSet.getIssueTime() != null) {
            jwtValidationInfo.setIssuedTime(jwtClaimsSet.getIssueTime().getTime());
        }
        jwtValidationInfo.setUser(jwtClaimsSet.getSubject());
        jwtValidationInfo.setJti(jwtClaimsSet.getJWTID());
    }

    private JWKSet retrieveJWKSet() throws IOException, ParseException {

        String jwksInfo = retrieveJWKSConfiguration(tokenIssuer.getJwksConfigurationDTO().getUrl());
        jwkSet = JWKSet.parse(jwksInfo);
        return jwkSet;
    }

    /**
     * This method used to retrieve JWKS keys from endpoint
     *
     * @param jwksEndpoint JWKS endpoint URL
     * @return JWK payload
     * @throws IOException If an exception occurs when calling the JWKS endpoint
     */
    public String retrieveJWKSConfiguration(String jwksEndpoint) throws IOException {
        // TODO: (VirajSalaka) moved from JWTUtil
        try (CloseableHttpClient httpClient = (CloseableHttpClient) jwtValidatorConfiguration.getHttpClient()) {
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
}
