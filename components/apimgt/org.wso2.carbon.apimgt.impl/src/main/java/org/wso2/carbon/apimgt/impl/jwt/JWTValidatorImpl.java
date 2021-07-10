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

package org.wso2.carbon.apimgt.impl.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.util.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.jwt.transformer.DefaultJWTTransformer;
import org.wso2.carbon.apimgt.impl.jwt.transformer.JWTTransformer;
import org.wso2.carbon.apimgt.impl.utils.JWTUtil;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class JWTValidatorImpl implements JWTValidator {

    TokenIssuerDto tokenIssuer;
    private Log log = LogFactory.getLog(JWTValidatorImpl.class);
    JWTTransformer jwtTransformer;
    private JWKSet jwkSet;
    @Override
    public JWTValidationInfo validateToken(SignedJWTInfo signedJWTInfo) throws APIManagementException {

        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        boolean state;
        try {
            state = validateSignature(signedJWTInfo.getSignedJWT());
            if (state) {
                JWTClaimsSet jwtClaimsSet = signedJWTInfo.getJwtClaimsSet();
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
                    jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                    return jwtValidationInfo;
                }
            } else {
                jwtValidationInfo.setValid(false);
                jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                return jwtValidationInfo;
            }
        } catch (ParseException e) {
            throw new APIManagementException("Error while parsing JWT", e);
        }
    }

    @Override
    public void loadTokenIssuerConfiguration(TokenIssuerDto tokenIssuerConfigurations) {

        this.tokenIssuer = tokenIssuerConfigurations;
        JWTTransformer jwtTransformer = ServiceReferenceHolder.getInstance().getJWTTransformer(tokenIssuer.getIssuer());
        if (jwtTransformer != null) {
            this.jwtTransformer = jwtTransformer;
        } else {
            this.jwtTransformer = new DefaultJWTTransformer();
        }
        this.jwtTransformer.loadConfiguration(tokenIssuer);
    }

    protected boolean validateSignature(SignedJWT signedJWT) throws APIManagementException {

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
                        throw new APIManagementException("Key Algorithm not supported");
                    }
                } else if (tokenIssuer.getCertificate() != null) {
                    log.debug("Retrieve certificate from Token issuer and validating");
                    RSAPublicKey rsaPublicKey = (RSAPublicKey) tokenIssuer.getCertificate().getPublicKey();
                    return JWTUtil.verifyTokenSignature(signedJWT, rsaPublicKey);
                } else {
                    return JWTUtil.verifyTokenSignature(signedJWT, keyID);
                }
            }
            return JWTUtil.verifyTokenSignature(signedJWT, certificateAlias);
        } catch (ParseException | JOSEException | IOException e) {
            log.error("Error while parsing JWT", e);
        }

        return true;
    }

    protected boolean validateTokenExpiry(JWTClaimsSet jwtClaimsSet) {

        long timestampSkew =
                ServiceReferenceHolder.getInstance().getOauthServerConfiguration().getTimeStampSkewInSeconds();
        Date now = new Date();
        Date exp = jwtClaimsSet.getExpirationTime();
        return exp == null || DateUtils.isAfter(exp, now, timestampSkew);
    }

    protected JWTClaimsSet transformJWTClaims(JWTClaimsSet jwtClaimsSet) throws APIManagementException {

        return jwtTransformer.transform(jwtClaimsSet);
    }
    protected String getConsumerKey(JWTClaimsSet jwtClaimsSet) throws APIManagementException {

        return jwtTransformer.getTransformedConsumerKey(jwtClaimsSet);
    }
    protected List<String> getScopes(JWTClaimsSet jwtClaimsSet) throws APIManagementException {

        return jwtTransformer.getTransformedScopes(jwtClaimsSet);
    }

    protected Boolean getIsAppToken(JWTClaimsSet jwtClaimsSet) throws APIManagementException {

        return jwtTransformer.getTransformedIsAppTokenType(jwtClaimsSet);
    }

    private void createJWTValidationInfoFromJWT(JWTValidationInfo jwtValidationInfo,
                                                JWTClaimsSet jwtClaimsSet)
            throws ParseException {

        jwtValidationInfo.setIssuer(jwtClaimsSet.getIssuer());
        jwtValidationInfo.setValid(true);
        jwtValidationInfo.setClaims(new HashMap<>(jwtClaimsSet.getClaims()));
        if (jwtClaimsSet.getExpirationTime() != null){
            jwtValidationInfo.setExpiryTime(jwtClaimsSet.getExpirationTime().getTime());
        }
        if (jwtClaimsSet.getIssueTime() != null){
            jwtValidationInfo.setIssuedTime(jwtClaimsSet.getIssueTime().getTime());
        }
        jwtValidationInfo.setUser(jwtClaimsSet.getSubject());
        jwtValidationInfo.setJti(jwtClaimsSet.getJWTID());
    }
    private JWKSet retrieveJWKSet() throws IOException, ParseException {
        String jwksInfo = JWTUtil
                .retrieveJWKSConfiguration(tokenIssuer.getJwksConfigurationDTO().getUrl());
        jwkSet = JWKSet.parse(jwksInfo);
        return jwkSet;
    }
}
