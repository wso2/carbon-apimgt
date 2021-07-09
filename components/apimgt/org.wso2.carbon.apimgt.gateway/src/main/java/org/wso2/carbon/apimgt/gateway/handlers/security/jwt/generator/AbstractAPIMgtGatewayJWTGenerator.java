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
package org.wso2.carbon.apimgt.gateway.handlers.security.jwt.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.dto.JWTInfoDto;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.SigningUtil;
import org.wso2.carbon.base.MultitenantConstants;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractAPIMgtGatewayJWTGenerator {
    private static final Log log = LogFactory.getLog(AbstractAPIMgtGatewayJWTGenerator.class);
    private static final String NONE = "NONE";
    private static final String SHA256_WITH_RSA = "SHA256withRSA";
    public static final String API_GATEWAY_ID = "wso2.org/products/am";
    public static final String FORMAT_JSON_ARRAY_PROPERTY = "formatJWTJsonArray";

    private static volatile long ttl = -1L;
    protected String dialectURI;

    private String signatureAlgorithm;
    private boolean tenantBasedSigningEnabled;

    public AbstractAPIMgtGatewayJWTGenerator() {

        JWTConfigurationDto jwtConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().
                        getAPIManagerConfiguration().getJwtConfigurationDto();
        dialectURI = jwtConfigurationDto.getConsumerDialectUri();
        if (dialectURI == null) {
            dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;
        }
        signatureAlgorithm = jwtConfigurationDto.getSignatureAlgorithm();
        if (signatureAlgorithm == null || !(NONE.equals(signatureAlgorithm)
                || SHA256_WITH_RSA.equals(signatureAlgorithm))) {
            signatureAlgorithm = SHA256_WITH_RSA;
        }
        tenantBasedSigningEnabled = jwtConfigurationDto.isTenantBasedSigningEnabled();
    }

    public String generateToken(JWTInfoDto jwtInfoDto) throws APIManagementException {

        int endUserTenantId = jwtInfoDto.getEndusertenantid();
        String jwtHeader = buildHeader(endUserTenantId);
        String jwtBody = buildBody(jwtInfoDto);
        String base64UrlEncodedHeader = "";
        if (jwtHeader != null) {
            base64UrlEncodedHeader = encode(jwtHeader.getBytes(Charset.defaultCharset()));
        }
        String base64UrlEncodedBody = "";
        if (jwtBody != null) {
            base64UrlEncodedBody = encode(jwtBody.getBytes());
        }
        if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            String assertion = base64UrlEncodedHeader + '.' + base64UrlEncodedBody;

            //get the assertion signed
            byte[] signedAssertion = signJWT(assertion, endUserTenantId);

            if (log.isDebugEnabled()) {
                log.debug("signed assertion value : " + new String(signedAssertion, Charset.defaultCharset()));
            }
            String base64UrlEncodedAssertion = encode(signedAssertion);

            return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.' + base64UrlEncodedAssertion;
        } else {
            return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.';
        }
    }

    /**
     * Helper method build JWT header with public certificate thumbprint if need to sign.
     *
     * @param tenantId tenant id of the certificate if need to sign
     * @return JWT Header
     * @throws APIManagementException if an error occurs while building
     */
    public String buildHeader(int tenantId) throws APIManagementException {

        String jwtHeader = null;

        if (NONE.equals(signatureAlgorithm)) {
            StringBuilder jwtHeaderBuilder = new StringBuilder();
            jwtHeaderBuilder.append("{\"typ\":\"JWT\",");
            jwtHeaderBuilder.append("\"alg\":\"");
            jwtHeaderBuilder.append(APIUtil.getJWSCompliantAlgorithmCode(NONE));
            jwtHeaderBuilder.append('\"');
            jwtHeaderBuilder.append('}');

            jwtHeader = jwtHeaderBuilder.toString();

        } else if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            jwtHeader = addCertToHeader(tenantId);
        }
        return jwtHeader;
    }

    /**
     * Sign JWT assertion using the relevant tenant key if config is enabled if not sign using super tenant key.
     *
     * @param assertion Assertion payload
     * @param tenantId  Tenant Id of the key to sign the assertion
     * @return Signed JWT Assertion
     * @throws APIManagementException if an error occurs while signing
     */
    public byte[] signJWT(String assertion, int tenantId) throws APIManagementException {

        if (!tenantBasedSigningEnabled) {
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
        }
        return APIUtil.signJwt(assertion, SigningUtil.getSigningKey(tenantId), signatureAlgorithm);
    }

    protected long getTTL() {
        if (ttl != -1) {
            return ttl;
        }

        synchronized (AbstractAPIMgtGatewayJWTGenerator.class) {
            if (ttl != -1) {
                return ttl;
            }
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();

            String gwTokenCacheConfig = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
            boolean isGWTokenCacheEnabled = Boolean.parseBoolean(gwTokenCacheConfig);

            if (isGWTokenCacheEnabled) {
                String apimKeyCacheExpiry = config.getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);

                if (apimKeyCacheExpiry != null) {
                    ttl = Long.parseLong(apimKeyCacheExpiry);
                } else {
                    ttl = Long.valueOf(900);
                }
            } else {
                String ttlValue = config.getFirstProperty(APIConstants.JWT_EXPIRY_TIME);
                if (ttlValue != null) {
                    ttl = Long.parseLong(ttlValue);
                } else {
                    //15 * 60 (convert 15 minutes to seconds)
                    ttl = Long.valueOf(900);
                }
            }
            return ttl;
        }
    }

    /**
     * Helper method to add public certificate to JWT_HEADER to signature verification.
     *
     * @param tenantId tenant Id of the certificate to add to the header
     * @return Header
     * @throws APIManagementException if an error occurs while building header
     */
    protected String addCertToHeader(int tenantId) throws APIManagementException {

        if (!tenantBasedSigningEnabled) {
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
        }
        return APIUtil.generateHeader(SigningUtil.getPublicCertificate(tenantId), signatureAlgorithm);
    }

    public String buildBody(JWTInfoDto jwtInfoDto) {

        JWTClaimsSet.Builder jwtClaimSetBuilder = new JWTClaimsSet.Builder();
        Map<String, Object> claims = populateStandardClaims(jwtInfoDto);
        Map<String, Object> customClaims = populateCustomClaims(jwtInfoDto);
        for (Map.Entry<String, Object> claimEntry : customClaims.entrySet()) {
            if (!claims.containsKey(claimEntry.getKey())) {
                claims.put(claimEntry.getKey(), claimEntry.getValue());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Claim key " + claimEntry.getKey() + " already exist");
                }
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        for (Map.Entry<String, Object> claimEntry : claims.entrySet()) {
            Object claimVal = claimEntry.getValue();
            if (claimVal instanceof String && claimEntry.toString().contains("{")) {
                try {
                    Map<String, String> map = mapper.readValue(claimVal.toString(), Map.class);
                    jwtClaimSetBuilder.claim(claimEntry.getKey(), map);
                } catch (IOException e) {
                    // Exception isn't thrown in order to generate jwt without claim, even if an error is
                    // occurred during the retrieving claims.
                    log.error(String.format("Error while reading claim values for %s", claimVal), e);
                }
            } else if (Boolean.parseBoolean(System.getProperty(FORMAT_JSON_ARRAY_PROPERTY)) &&
                    claimVal instanceof String && claimVal.toString().contains("[\"")
                    && claimVal.toString().contains("\"]")) {
                try {
                    List<String> arrayList = mapper.readValue(claimVal.toString(), List.class);
                    jwtClaimSetBuilder.claim(claimEntry.getKey(), arrayList);
                } catch (IOException e) {
                    // Exception isn't thrown in order to generate jwt without claim, even if an error is
                    // occurred during the retrieving claims.
                    log.error("Error while reading claim values", e);
                }
            } else if (APIConstants.JwtTokenConstants.EXPIRY_TIME.equals(claimEntry.getKey())) {
                jwtClaimSetBuilder.claim(claimEntry.getKey(), new Date(Long.parseLong((String) claimEntry.getValue())));
            } else if (APIConstants.JwtTokenConstants.ISSUED_TIME.equals(claimEntry.getKey())) {
                jwtClaimSetBuilder.claim(claimEntry.getKey(), new Date(Long.parseLong((String) claimEntry.getValue())));
            } else {
                jwtClaimSetBuilder.claim(claimEntry.getKey(), claimEntry.getValue());
            }
            //Adding JTI standard claim to the backend JWT
            jwtClaimSetBuilder.jwtID(UUID.randomUUID().toString());
        }
        JWTClaimsSet jwtClaimsSet = jwtClaimSetBuilder.build();
        return jwtClaimsSet.toJSONObject().toString();
    }

    public String encode(byte[] stringToBeEncoded) throws APIManagementException {
        return java.util.Base64.getUrlEncoder().encodeToString(stringToBeEncoded);
    }

    public String getDialectURI() {
        return dialectURI;
    }

    public abstract Map<String, Object> populateStandardClaims(JWTInfoDto jwtInfoDto);

    public abstract Map<String, Object> populateCustomClaims(JWTInfoDto jwtInfoDto);
}
