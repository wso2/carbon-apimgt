/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.apimgt.gateway.handlers.security.jwt.transformer;

import com.nimbusds.jwt.JWTClaimsSet;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.dto.ClaimMappingDto;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.TokenIssuerDto;

import java.util.Map;
import java.util.Properties;

public class DefaultJWTTransformer implements JWTTransformer {

    private JWTConfigurationDto jwtConfigurationDto;
    private Properties defaultClaimMappings;

    public DefaultJWTTransformer(JWTConfigurationDto jwtConfigurationDto, Properties defaultClaimMappings) {

        this.defaultClaimMappings = defaultClaimMappings;
        this.jwtConfigurationDto = jwtConfigurationDto;
    }

    @Override
    public JWTClaimsSet transform(JWTClaimsSet jwtClaimsSet) {

        String issuer = jwtClaimsSet.getIssuer();
        TokenIssuerDto tokenIssuerDto = jwtConfigurationDto.getTokenIssuerDtoMap().get(issuer);
        JWTClaimsSet.Builder transformedJWT = new JWTClaimsSet.Builder();
        if (tokenIssuerDto != null) {
            if (!tokenIssuerDto.isDisableDefaultClaimMapping()) {
                for (Map.Entry<String, Object> claimEntry : jwtClaimsSet.getClaims().entrySet()) {
                    String claimKey = claimEntry.getKey();
                    if (defaultClaimMappings.containsKey(claimEntry.getKey())) {
                        claimKey = (String) defaultClaimMappings.get(claimKey);
                    }
                    transformedJWT.claim(claimKey, claimEntry.getValue());
                }
            } else {
                transformedJWT = new JWTClaimsSet.Builder(jwtClaimsSet);
            }

            Map<String, ClaimMappingDto> claimConfigurations = tokenIssuerDto.getClaimConfigurations();
            JWTClaimsSet.Builder jwtBuilder = new JWTClaimsSet.Builder();
            for (Map.Entry<String, Object> claimEntry : transformedJWT.build().getClaims().entrySet()) {
                ClaimMappingDto claimMappingDto = claimConfigurations.get(claimEntry.getKey());
                String claimKey = claimEntry.getKey();
                if (claimMappingDto != null) {
                    claimKey = claimMappingDto.getLocalClaim();
                }
                jwtBuilder.claim(claimKey, claimEntry.getValue());
            }
            return jwtBuilder.build();
        } else {
            for (Map.Entry<String, Object> claimEntry : jwtClaimsSet.getClaims().entrySet()) {
                String claimKey = claimEntry.getKey();
                if (defaultClaimMappings.containsKey(claimEntry.getKey())) {
                    claimKey = (String) defaultClaimMappings.get(claimKey);
                }
                transformedJWT.claim(claimKey, claimEntry.getValue());
            }
            return transformedJWT.build();
        }

    }

    @Override
    public String getIssuer() {

        return APIMgtGatewayConstants.DEFAULT_JWT_TRANSFORMER_ISSUER;
    }
}
