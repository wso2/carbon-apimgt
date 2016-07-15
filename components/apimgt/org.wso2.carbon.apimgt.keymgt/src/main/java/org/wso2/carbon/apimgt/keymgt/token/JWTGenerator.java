/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.apimgt.keymgt.token;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.token.ClaimsRetriever;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import java.util.*;

public class JWTGenerator extends AbstractJWTGenerator {

    private static final Log log = LogFactory.getLog(JWTGenerator.class);


    @Override
    public Map<String, String> populateStandardClaims(TokenValidationContext validationContext)
            throws APIManagementException {

        //generating expiring timestamp
        long currentTime = System.currentTimeMillis() ;
        long expireIn = currentTime + getTTL() * 1000;

        String dialect;
        ClaimsRetriever claimsRetriever = getClaimsRetriever();
        if (claimsRetriever != null) {
            dialect = claimsRetriever.getDialectURI(validationContext.getValidationInfoDTO().getEndUserName());
        } else {
            dialect = getDialectURI();
        }

        String subscriber = validationContext.getValidationInfoDTO().getSubscriber();
        String applicationName = validationContext.getValidationInfoDTO().getApplicationName();
        String applicationId = validationContext.getValidationInfoDTO().getApplicationId();
        String tier = validationContext.getValidationInfoDTO().getTier();
        String endUserName = validationContext.getValidationInfoDTO().getEndUserName();
        String keyType = validationContext.getValidationInfoDTO().getType();
        String userType = validationContext.getValidationInfoDTO().getUserType();
        String applicationTier = validationContext.getValidationInfoDTO().getApplicationTier();
        String enduserTenantId = String.valueOf(APIUtil.getTenantId(endUserName));

        Map<String, String> claims = new LinkedHashMap<String, String>(20);

        claims.put("iss", API_GATEWAY_ID);
        claims.put("exp", String.valueOf(expireIn));
        claims.put(dialect + "/subscriber", subscriber);
        claims.put(dialect + "/applicationid", applicationId);
        claims.put(dialect + "/applicationname", applicationName);
        claims.put(dialect + "/applicationtier", applicationTier);
        claims.put(dialect + "/apicontext", validationContext.getContext());
        claims.put(dialect + "/version", validationContext.getVersion());
        claims.put(dialect + "/tier", tier);
        claims.put(dialect + "/keytype", keyType);
        claims.put(dialect + "/usertype", userType);
        claims.put(dialect + "/enduser",
                   UserCoreUtil.removeDomainFromName(APIUtil.getUserNameWithTenantSuffix(endUserName)));
        claims.put(dialect + "/enduserTenantId", enduserTenantId);

        return claims;
    }

    @Override
    public Map<String, String> populateCustomClaims(TokenValidationContext validationContext)
            throws APIManagementException {
        ClaimsRetriever claimsRetriever = getClaimsRetriever();
        if (claimsRetriever != null) {
            String tenantAwareUserName = validationContext.getValidationInfoDTO().getEndUserName();
            try {
                return claimsRetriever.getClaims(tenantAwareUserName);

            } catch (APIManagementException e) {
                log.error("Error while retrieving claims ", e);
            }
        }
        return null;
    }
}
