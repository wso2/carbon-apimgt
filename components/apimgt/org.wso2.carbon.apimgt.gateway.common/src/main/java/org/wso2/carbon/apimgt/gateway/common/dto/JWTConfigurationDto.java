package org.wso2.carbon.apimgt.gateway.common.dto;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JWTConfigurationDto {
    private boolean enabled = false;
    private String jwtHeader = "X-JWT-Assertion";
    private String consumerDialectUri = "http://wso2.org/claims";
    private String signatureAlgorithm = "SHA256withRSA";
    private String jwtGeneratorImplClass = "org.wso2.carbon.apimgt.keymgt.token.JWTGenerator";
    private String claimRetrieverImplClass;
    private boolean enableUserClaims;
    private String gatewayJWTGeneratorImpl;
    private Map<String, TokenIssuerDto> tokenIssuerDtoMap = new HashMap();
    private Set<String> jwtExcludedClaims = new HashSet<>();
    private Certificate publicCert;
    private PrivateKey privateKey;
    private long ttl;

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public String getJwtHeader() {

        return jwtHeader;
    }

    public void setJwtHeader(String jwtHeader) {

        this.jwtHeader = jwtHeader;
    }

    public String getConsumerDialectUri() {

        return consumerDialectUri;
    }

    public void setConsumerDialectUri(String consumerDialectUri) {

        this.consumerDialectUri = consumerDialectUri;
    }

    public String getSignatureAlgorithm() {

        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {

        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getJwtGeneratorImplClass() {

        return jwtGeneratorImplClass;
    }

    public void setJwtGeneratorImplClass(String jwtGeneratorImplClass) {

        this.jwtGeneratorImplClass = jwtGeneratorImplClass;
    }

    public String getClaimRetrieverImplClass() {

        return claimRetrieverImplClass;
    }

    public void setClaimRetrieverImplClass(String claimRetrieverImplClass) {

        this.claimRetrieverImplClass = claimRetrieverImplClass;
    }

    public void setGatewayJWTGeneratorImpl(String gatewayJWTGeneratorImpl) {
        this.gatewayJWTGeneratorImpl = gatewayJWTGeneratorImpl;
    }

    public String getGatewayJWTGeneratorImpl() {

        return gatewayJWTGeneratorImpl;
    }

    public Map<String, TokenIssuerDto> getTokenIssuerDtoMap() {

        return tokenIssuerDtoMap;
    }

    public void setTokenIssuerDtoMap(
            Map<String, TokenIssuerDto> tokenIssuerDtoMap) {

        this.tokenIssuerDtoMap = tokenIssuerDtoMap;
    }

    public Set<String> getJWTExcludedClaims() {

        return jwtExcludedClaims;
    }

    public void setJwtExcludedClaims(Set<String> jwtClaims) {

        this.jwtExcludedClaims = jwtClaims;
    }

    public boolean isEnableUserClaims() {

        return enableUserClaims;
    }

    public void setEnableUserClaims(boolean enableUserClaims) {

        this.enableUserClaims = enableUserClaims;
    }

    public void setPublicCert(Certificate publicCert) {
        this.publicCert = publicCert;
    }

    public Certificate getPublicCert() {
        return publicCert;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public long getTTL() {
        return ttl;
    }
}
