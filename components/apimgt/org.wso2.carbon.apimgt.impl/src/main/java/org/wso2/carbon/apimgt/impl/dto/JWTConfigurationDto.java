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
package org.wso2.carbon.apimgt.impl.dto;

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
    private boolean tenantBasedSigningEnabled = false;
    private boolean enableUserClaimRetrievalFromUserStore;
    public boolean isTenantBasedSigningEnabled() {

        return tenantBasedSigningEnabled;
    }

    public void setTenantBasedSigningEnabled(boolean tenantBasedSigningEnabled) {

        this.tenantBasedSigningEnabled = tenantBasedSigningEnabled;
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

    public boolean isEnableUserClaimRetrievalFromUserStore() {

        return enableUserClaimRetrievalFromUserStore;
    }

    public void setEnableUserClaimRetrievalFromUserStore(boolean enableUserClaimRetrievalFromUserStore) {

        this.enableUserClaimRetrievalFromUserStore = enableUserClaimRetrievalFromUserStore;
    }
}
