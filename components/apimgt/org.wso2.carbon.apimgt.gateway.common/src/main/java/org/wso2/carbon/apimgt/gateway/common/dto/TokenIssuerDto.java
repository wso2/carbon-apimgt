package org.wso2.carbon.apimgt.gateway.common.dto;

import java.util.HashMap;
import java.util.Map;
import javax.security.cert.Certificate;

/**
 * Holds data related to token issuers.
 */
public class TokenIssuerDto {
    private String issuer;
    private boolean disableDefaultClaimMapping = false;
    private Map<String, ClaimMappingDto> claimConfigurations = new HashMap<>();
    private JWKSConfigurationDTO jwksConfigurationDTO = new JWKSConfigurationDTO();
    private Certificate certificate;
    private String consumerKeyClaim;
    private String scopesClaim;

    public TokenIssuerDto(String issuer) {

        this.issuer = issuer;
    }

    public String getIssuer() {

        return issuer;
    }

    public void setIssuer(String issuer) {

        this.issuer = issuer;
    }

    public Map<String, ClaimMappingDto> getClaimConfigurations() {

        return claimConfigurations;
    }

    public void addClaimMapping(ClaimMappingDto claimMappingDto) {
        claimConfigurations.put(claimMappingDto.getRemoteClaim(), claimMappingDto);
    }

    public JWKSConfigurationDTO getJwksConfigurationDTO() {

        return jwksConfigurationDTO;
    }

    public boolean isDisableDefaultClaimMapping() {

        return disableDefaultClaimMapping;
    }

    public void setDisableDefaultClaimMapping(boolean disableDefaultClaimMapping) {

        this.disableDefaultClaimMapping = disableDefaultClaimMapping;
    }

    public void setJwksConfigurationDTO(JWKSConfigurationDTO jwksConfigurationDTO) {

        this.jwksConfigurationDTO = jwksConfigurationDTO;
    }

    public String getConsumerKeyClaim() {

        return consumerKeyClaim;
    }

    public void setConsumerKeyClaim(String consumerKeyClaim) {

        this.consumerKeyClaim = consumerKeyClaim;
    }

    public String getScopesClaim() {

        return scopesClaim;
    }

    public void setScopesClaim(String scopesClaim) {

        this.scopesClaim = scopesClaim;
    }

    public void addClaimMappings(ClaimMappingDto[] claimMappingDto) {

        for (ClaimMappingDto mappingDto : claimMappingDto) {
            addClaimMapping(mappingDto);
        }
    }

    public Certificate getCertificate() {

        return certificate;
    }

    public void setCertificate(Certificate certificate) {

        this.certificate = certificate;
    }
}
