/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.UserAuthContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;

import java.text.ParseException;

/**
 * This Util can be used to get access token sent from UI.
 */
public class UserTokenUtil {

    private static final Log log = LogFactory.getLog(UserTokenUtil.class);
    private static final UserAuthContext userContext = new UserAuthContext();
    private static final ThreadLocal<UserAuthContext> tokenThreadLocal = ThreadLocal.withInitial(() -> userContext);

    private UserTokenUtil() {
    }

    public static String getToken() {
        return tokenThreadLocal.get().getToken();
    }

    public static void setToken(String token) {

        UserAuthContext context = tokenThreadLocal.get();
        context.setToken(token);
        tokenThreadLocal.set(context);
    }

    public static void clear() {
        tokenThreadLocal.remove();
    }

    public static String getOrgHandler() throws APIManagementException {
        UserAuthContext context = tokenThreadLocal.get();
        return getOrgHandleFromJwt(context.getToken());
    }

    private static String getOrgHandleFromJwt(String token) throws APIManagementException {
        try {
            SignedJWTInfo signedToken = getSignedJwt(token);
            JSONObject organizationClaim =
                    signedToken.getJwtClaimsSet().getJSONObjectClaim(APIConstants.OperationPolicyConstants.ORGANIZATION);
            if (organizationClaim != null &&
                    organizationClaim.containsKey(APIConstants.OperationPolicyConstants.HANDLE)) {
                return organizationClaim.getAsString(APIConstants.OperationPolicyConstants.HANDLE);
            }
        } catch (ParseException e) {
            throw new APIManagementException("Failed to parse organization handle claim from JWT claims", e);
        }
        return null;
    }

    private static SignedJWTInfo getSignedJwt(String accessToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(accessToken);
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        return new SignedJWTInfo(accessToken, signedJWT, jwtClaimsSet);
    }

}
