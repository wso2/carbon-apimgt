package org.wso2.carbon.apimgt.impl.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JWTConfigurationDto {

    private boolean enabled = false;
    private String jwtHeader = "X-JWT-Assertion";
    private String consumerDialectUri = "http://wso2.org/claims";
    private String signatureAlgorithm = "SHA256withRSA";
    private String jwtGeneratorImplClass = "org.wso2.carbon.apimgt.keymgt.token.JWTGenerator";
    private String claimRetrieverImplClass;
    private String gatewayJWTGeneratorImpl;
    private Set<String> claimConfigurations = new HashSet<>();

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

    public Set<String> getClaimConfigurations() {

        return claimConfigurations;
    }

    public void setClaimConfigurations(Set<String> claimConfigurations) {

        this.claimConfigurations = claimConfigurations;
    }
}
