package org.wso2.carbon.apimgt.gateway.common.jwtGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.common.constants.JWTConstants;
import org.wso2.carbon.apimgt.gateway.common.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.gateway.common.dto.JWTInfoDto;
import org.wso2.carbon.apimgt.gateway.common.exception.JWTGeneratorException;
import org.wso2.carbon.apimgt.gateway.common.util.JWTUtil;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractAPIMgtGatewayJWTGenerator {
    private static final Log log = LogFactory.getLog(AbstractAPIMgtGatewayJWTGenerator.class);
    public static final String NONE = "NONE";
    public static final String SHA256_WITH_RSA = "SHA256withRSA";
    public static final String API_GATEWAY_ID = "wso2.org/products/am";
    public static JWTConfigurationDto jwtConfigurationDto;

    private static volatile long ttl = -1L;
    public String dialectURI;

    public String signatureAlgorithm;

    public AbstractAPIMgtGatewayJWTGenerator() {
    }

    public void setJWTConfigurationDto(JWTConfigurationDto jwtConfigurationDto) {
        this.jwtConfigurationDto = jwtConfigurationDto;
        dialectURI = jwtConfigurationDto.getConsumerDialectUri();
        if (dialectURI == null) {
            dialectURI = ClaimsRetriever.DEFAULT_DIALECT_URI;
        }
        signatureAlgorithm = jwtConfigurationDto.getSignatureAlgorithm();
        if (signatureAlgorithm == null || !(NONE.equals(signatureAlgorithm)
                || SHA256_WITH_RSA.equals(signatureAlgorithm))) {
            signatureAlgorithm = SHA256_WITH_RSA;
        }

    }

    public String generateToken(JWTInfoDto jwtInfoDto) throws JWTGeneratorException {

        String jwtHeader = buildHeader();
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
            byte[] signedAssertion = signJWT(assertion);

            if (log.isDebugEnabled()) {
                log.debug("signed assertion value : " + new String(signedAssertion, Charset.defaultCharset()));
            }
            String base64UrlEncodedAssertion = encode(signedAssertion);

            return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.' + base64UrlEncodedAssertion;
        } else {
            return base64UrlEncodedHeader + '.' + base64UrlEncodedBody + '.';
        }
    }

    public String buildHeader() throws JWTGeneratorException {
        String jwtHeader = null;

        if (NONE.equals(signatureAlgorithm)) {
            StringBuilder jwtHeaderBuilder = new StringBuilder();
            jwtHeaderBuilder.append("{\"typ\":\"JWT\",");
            jwtHeaderBuilder.append("\"alg\":\"");
            jwtHeaderBuilder.append(JWTUtil.getJWSCompliantAlgorithmCode(NONE));
            jwtHeaderBuilder.append('\"');
            jwtHeaderBuilder.append('}');

            jwtHeader = jwtHeaderBuilder.toString();

        } else if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            jwtHeader = addCertToHeader();
        }
        return jwtHeader;
    }

    public byte[] signJWT(String assertion) throws JWTGeneratorException {

        try {
            PrivateKey privateKey = jwtConfigurationDto.getPrivateKey();
            return JWTUtil.signJwt(assertion, privateKey, signatureAlgorithm);
        } catch (Exception e) {
            throw new JWTGeneratorException(e);
        }
    }

   /* protected long getTTL() {
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
    }*/

    /**
     * Helper method to add public certificate to JWT_HEADER to signature verification.
     *
     * @throws JWTGeneratorException
     */
    protected String addCertToHeader() throws JWTGeneratorException {

        try {
            Certificate publicCert = jwtConfigurationDto.getPublicCert();
            return JWTUtil.generateHeader(publicCert, signatureAlgorithm);
        } catch (Exception e) {
            String error = "Error in obtaining keystore";
            throw new JWTGeneratorException(error, e);
        }
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
            } else if (claimVal instanceof String && claimVal.toString().contains("[\"")
                    && claimVal.toString().contains("\"]")) {

                try {
                    List<String> arrayList = mapper.readValue(claimVal.toString(), List.class);
                    jwtClaimSetBuilder.claim(claimEntry.getKey(), arrayList);
                } catch (IOException e) {
                    // Exception isn't thrown in order to generate jwt without claim, even if an error is
                    // occurred during the retrieving claims.
                    log.error("Error while reading claim values", e);
                }
            } else if (JWTConstants.EXPIRY_TIME.equals(claimEntry.getKey())) {
                jwtClaimSetBuilder.claim(claimEntry.getKey(), new Date(Long.parseLong((String) claimEntry.getValue())));
            } else if (JWTConstants.ISSUED_TIME.equals(claimEntry.getKey())) {
                jwtClaimSetBuilder.claim(claimEntry.getKey(), new Date(Long.parseLong((String) claimEntry.getValue())));
            } else {
                jwtClaimSetBuilder.claim(claimEntry.getKey(), claimEntry.getValue());
            }
        }
        //Adding JWT standard claim
        jwtClaimSetBuilder.jwtID(UUID.randomUUID().toString());
        JWTClaimsSet jwtClaimsSet = jwtClaimSetBuilder.build();
        return jwtClaimsSet.toJSONObject().toString();
    }

    public String encode(byte[] stringToBeEncoded) throws JWTGeneratorException {
        return java.util.Base64.getUrlEncoder().encodeToString(stringToBeEncoded);
    }

    public String getDialectURI() {
        return dialectURI;
    }

    public abstract Map<String, Object> populateStandardClaims(JWTInfoDto jwtInfoDto);

    public abstract Map<String, Object> populateCustomClaims(JWTInfoDto jwtInfoDto);
}

