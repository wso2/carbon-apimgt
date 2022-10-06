/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.user.ctx.builder.impl;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.wso2.carbon.apimgt.user.ctx.builder.UserContextBuilder;
import org.wso2.carbon.apimgt.user.ctx.util.UserContextConstants;
import org.wso2.carbon.apimgt.user.exceptions.UserException;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class OAuthJWTUserContextBuilderImpl implements UserContextBuilder {
    private final String accessToken;

    public OAuthJWTUserContextBuilderImpl(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public Map<String, Object> getProperties() throws UserException {
        Map<String, Object> props = new HashMap<>();
        JWTClaimsSet claimsSet;
        try {
            SignedJWT jwt = SignedJWT.parse(accessToken);
            claimsSet = jwt.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new UserException("Cannot parse the JWT token", e);
        }

        if (claimsSet == null) {
            throw new UserException("Cannot parse the JWT token");
        }

        Map<String, Object> claims = new HashMap<>();
        for (Map.Entry<String, Object> claim : claimsSet.getClaims().entrySet()) {
            switch (claim.getKey()) {
                case UserContextConstants.JWT_CLAIM_USERNAME:
                    props.put(UserContextConstants.ATTRIB_USERNAME, claim.getValue());
                    break;
                case UserContextConstants.JWT_CLAIM_ORGANIZATION:
                    props.put(UserContextConstants.ATTRIB_ORGANIZATION, claim.getValue());
                    break;
                case UserContextConstants.JWT_CLAIM_ROLES:
                    props.put(UserContextConstants.ATTRIB_ROLES, claim.getValue());
                    break;
                default:
                    claims.put(claim.getKey(), claim.getValue());
                    break;
            }
        }
        props.put(UserContextConstants.ATTRIB_CLAIMS, claims);
        return props;
    }
}
