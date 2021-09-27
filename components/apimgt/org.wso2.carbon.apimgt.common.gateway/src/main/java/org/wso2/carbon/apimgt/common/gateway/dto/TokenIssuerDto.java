/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.common.gateway.dto;

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
    private String audience;

    public TokenIssuerDto() {
    }

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

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }
}
