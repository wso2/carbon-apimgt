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


package org.wso2.carbon.apimgt.impl.jwt.transformer;

import com.nimbusds.jwt.JWTClaimsSet;
import org.wso2.carbon.apimgt.impl.dto.ClaimMappingDto;
import org.wso2.carbon.apimgt.impl.dto.TokenIssuerDto;

import java.util.Map;

public class DefaultJWTTransformer implements JWTTransformer {
    protected TokenIssuerDto tokenIssuer;

    @Override
    public JWTClaimsSet transform(JWTClaimsSet jwtClaimsSet) {
        JWTClaimsSet.Builder transformedJWT = new JWTClaimsSet.Builder();
        if (tokenIssuer != null) {
            Map<String, ClaimMappingDto> claimConfigurations = tokenIssuer.getClaimConfigurations();
            for (Map.Entry<String, Object> claimEntry : jwtClaimsSet.getClaims().entrySet()) {
                ClaimMappingDto claimMappingDto = claimConfigurations.get(claimEntry.getKey());
                String claimKey = claimEntry.getKey();
                if (claimMappingDto != null) {
                    claimKey = claimMappingDto.getLocalClaim();
                }
                transformedJWT.claim(claimKey, claimEntry.getValue());
            }
            return transformedJWT.build();
        }
        return jwtClaimsSet;

    }

    @Override
    public String getIssuer() {

        return "";
    }

    @Override
    public void loadConfiguration(TokenIssuerDto tokenIssuerConfiguration) {
        this.tokenIssuer = tokenIssuerConfiguration;
    }

}
