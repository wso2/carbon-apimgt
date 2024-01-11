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

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Holds configs related to jwt generation.
 */
public class JWTConfigurationDto {

    private boolean enabled = false;
    private String jwtHeader = "X-JWT-Assertion";
    private String consumerDialectUri = "http://wso2.org/claims";
    private String signatureAlgorithm = "SHA256withRSA";
    private String jwtDecoding = "base64";
    private boolean enableUserClaims;
    private String gatewayJWTGeneratorImpl;
    private Map<String, TokenIssuerDto> tokenIssuerDtoMap = new HashMap();
    private Set<String> jwtExcludedClaims = new HashSet<>();
    private Certificate publicCert;
    private PrivateKey privateKey;
    private long ttl;

    private boolean useKid;

    public boolean useKid() {
        return useKid;
    }

    public void setUseKid(boolean useKid) {
        this.useKid = useKid;
    }

    public JWTConfigurationDto(JWTConfigurationDto jwtConfigurationDto) {

        this.enabled = jwtConfigurationDto.enabled;
        this.jwtHeader = jwtConfigurationDto.jwtHeader;
        this.consumerDialectUri = jwtConfigurationDto.consumerDialectUri;
        this.signatureAlgorithm = jwtConfigurationDto.signatureAlgorithm;
        this.jwtDecoding = jwtConfigurationDto.jwtDecoding;
        this.enableUserClaims = jwtConfigurationDto.enableUserClaims;
        this.gatewayJWTGeneratorImpl = jwtConfigurationDto.gatewayJWTGeneratorImpl;
        this.tokenIssuerDtoMap = jwtConfigurationDto.tokenIssuerDtoMap;
        this.jwtExcludedClaims = jwtConfigurationDto.jwtExcludedClaims;
        this.ttl = jwtConfigurationDto.ttl;
    }

    public JWTConfigurationDto() {

    }

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

    public String getGatewayJWTGeneratorImpl() {

        return gatewayJWTGeneratorImpl;
    }

    public void setGatewayJWTGeneratorImpl(String gatewayJWTGeneratorImpl) {

        this.gatewayJWTGeneratorImpl = gatewayJWTGeneratorImpl;
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

    public String getJwtDecoding() {
        return jwtDecoding;
    }

    public void setJwtDecoding(String jwtDecoding) {
        this.jwtDecoding = jwtDecoding;
    }

    public boolean isEnableUserClaims() {

        return enableUserClaims;
    }

    public void setEnableUserClaims(boolean enableUserClaims) {

        this.enableUserClaims = enableUserClaims;
    }

    public Certificate getPublicCert() {

        return publicCert;
    }

    public void setPublicCert(Certificate publicCert) {

        this.publicCert = publicCert;
    }

    public PrivateKey getPrivateKey() {

        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {

        this.privateKey = privateKey;
    }

    public void setTtl(long ttl) {

        this.ttl = ttl;
    }

    public long getTTL() {

        return ttl;
    }

}
