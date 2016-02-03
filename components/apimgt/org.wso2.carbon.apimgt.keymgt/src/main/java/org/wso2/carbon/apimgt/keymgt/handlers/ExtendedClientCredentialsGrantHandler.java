/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.handlers.ScopesIssuer;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.model.RequestParameter;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.ClientCredentialsGrantHandler;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;

public class ExtendedClientCredentialsGrantHandler extends ClientCredentialsGrantHandler {
    private static final Log log = LogFactory.getLog(ExtendedClientCredentialsGrantHandler.class);
    private static final String VALIDITY_PERIOD = "validity_period";

    @Override
    public boolean authorizeAccessDelegation(OAuthTokenReqMessageContext tokReqMsgCtx) {

        RequestParameter[] parameters = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getRequestParameters();

        long validityPeriod = 0;

        if (parameters == null) {
            return true;
        }

        // find out validity period
        for (RequestParameter parameter : parameters) {
            if (VALIDITY_PERIOD.equals(parameter.getKey())) {
                if (parameter.getValue() != null && parameter.getValue().length > 0) {
                    validityPeriod = Long.valueOf(parameter.getValue()[0]);
                    //set validity time
                    tokReqMsgCtx.setValidityPeriod(validityPeriod);

                }
            }
        }

        return true;
    }


    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {

        boolean validateResult = super.validateGrant(tokReqMsgCtx);
        AuthenticatedUser user = tokReqMsgCtx.getAuthorizedUser();
        String tenantDomain = user.getTenantDomain();
        String username = user.getUserName(); 
        user.setUserName(username);
        tokReqMsgCtx.setAuthorizedUser(user);

        return validateResult;
    }

    @Override
    public boolean issueRefreshToken() throws IdentityOAuth2Exception {
        return super.issueRefreshToken();
    }

    @Override
    public boolean isOfTypeApplicationUser() throws IdentityOAuth2Exception {
        return super.isOfTypeApplicationUser();
    }

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx) {
        // Execute ScopeIssuer
        boolean state = ScopesIssuer.getInstance().setScopes(tokReqMsgCtx);

        // If ScopeIssuer returns true, then see if application scope is set.
        if (state) {
            String[] scopes = tokReqMsgCtx.getScope();

            String applicationScope = APIKeyMgtDataHolder.getApplicationTokenScope();
            if (scopes != null) {

                // Arrays.asList won't work here, because list.add cannot be called on the returned list.
                ArrayList<String> scopeList = new ArrayList<String>(scopes.length);
                for (String scope : scopes) {
                    scopeList.add(scope);
                }
                // Forcefully add application scope if it's not included in the list.
                if (!scopeList.contains(applicationScope)) {
                    scopeList.add(applicationScope);
                    tokReqMsgCtx.setScope(scopeList.toArray(new String[scopeList.size()]));
                }
            }
        }

        return state;
    }
}
