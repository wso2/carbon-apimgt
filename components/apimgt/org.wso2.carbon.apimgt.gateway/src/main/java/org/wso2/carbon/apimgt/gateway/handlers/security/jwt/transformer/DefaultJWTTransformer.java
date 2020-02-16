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


public class DefaultJWTTransformer implements JWTTransformer {

    private JWTConfigurationDto jwtConfigurationDto;

    public DefaultJWTTransformer(JWTConfigurationDto jwtConfigurationDto) {

        this.jwtConfigurationDto = jwtConfigurationDto;
    }

    @Override
    public JWTClaimsSet transform(JWTClaimsSet jwtClaimsSet) {

        String issuer = jwtClaimsSet.getIssuer();
        TokenIssuerDto tokenIssuerDto = jwtConfigurationDto.getTokenIssuerDtoMap().get(issuer);
        if (tokenIssuerDto != null) {
            Map<String, ClaimMappingDto> claimConfigurations = tokenIssuerDto.getClaimConfigurations();
            JWTClaimsSet.Builder jwtBuilder = new JWTClaimsSet.Builder();
            for (Map.Entry<String, Object> claimEntry : jwtClaimsSet.getClaims().entrySet()) {
                ClaimMappingDto claimMappingDto = claimConfigurations.get(claimEntry.getKey());
                String claimKey = claimEntry.getKey();
                if (claimMappingDto != null) {
                    claimKey = claimMappingDto.getLocalClaim();
                }
                jwtBuilder.claim(claimKey, claimEntry.getValue());
            }
            return jwtBuilder.build();
        }
        return jwtClaimsSet;
    }

    @Override
    public String getIssuer() {

        return APIMgtGatewayConstants.DEFAULT_JWT_TRANSFORMER_ISSUER;
    }
}
