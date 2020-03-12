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

package org.wso2.carbon.apimgt.keymgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.keymgt.ScopesIssuer;
import org.wso2.carbon.identity.oauth.callback.AbstractOAuthCallbackHandler;
import org.wso2.carbon.identity.oauth.callback.OAuthCallback;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

public class APIManagerOAuthCallbackHandler extends AbstractOAuthCallbackHandler {
    
    private static final Log log = LogFactory.getLog(APIManagerOAuthCallbackHandler.class);

    public boolean canHandle(Callback[] callbacks) throws IdentityOAuth2Exception {
        return true;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        if (callbacks != null && callbacks.length > 0){
            OAuthCallback oauthCallback = (OAuthCallback) callbacks[0];
            if (OAuthCallback.OAuthCallbackType.ACCESS_DELEGATION_AUTHZ.equals(
                    oauthCallback.getCallbackType())){
                oauthCallback.setAuthorized(true);
            }
            if (OAuthCallback.OAuthCallbackType.ACCESS_DELEGATION_TOKEN.equals(
                    oauthCallback.getCallbackType())){
                oauthCallback.setAuthorized(true);
            }
            if (OAuthCallback.OAuthCallbackType.SCOPE_VALIDATION_AUTHZ.equals(
                    oauthCallback.getCallbackType())){
                //Validate scopes in callback using scope issuers
                ScopesIssuer.getInstance().setScopes(oauthCallback);
                oauthCallback.setValidScope(true);
            }
            if (OAuthCallback.OAuthCallbackType.SCOPE_VALIDATION_TOKEN.equals(
                    oauthCallback.getCallbackType())){
                String[] scopes = oauthCallback.getRequestedScope();
                //If no scopes have been requested.
                if(scopes == null || scopes.length == 0){
                   //Issue a default scope. The default scope can only be used to access resources which are
                   // not associated to a scope
                   scopes = new String[]{APIConstants.OAUTH2_DEFAULT_SCOPE};
                }
                oauthCallback.setApprovedScope(scopes);
                oauthCallback.setValidScope(true);
            }
        }
    }
}
