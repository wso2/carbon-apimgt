/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.ClientCredentialsGrantHandler;

public class OpenKeyManagerGrantHandler extends ClientCredentialsGrantHandler {

    private static Log log = LogFactory.getLog(OpenKeyManagerGrantHandler.class);

    @Override
    public boolean authorizeAccessDelegation(OAuthTokenReqMessageContext tokReqMsgCtx){
        return true;
    }

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {
        //return super.validateGrant(tokReqMsgCtx);

        log.info("Mobile Grant handler is hit");

        boolean authStatus = true;

        // extract request parameters
        //OAuth2Parameters[] parameters = tokReqMsgCtx.getOauth2AccessTokenReqDTO().

//        String mobileNumber = null;
//
//        // find out mobile number
//        for(RequestParameter parameter : parameters){
//            if(MOBILE_GRANT_PARAM.equals(parameter.getKey())){
//                if(parameter.getValue() != null && parameter.getValue().length > 0){
//                    mobileNumber = parameter.getValue()[0];
//                }
//            }
//        }
//
//        if(mobileNumber != null) {
//            //validate mobile number
//            authStatus =  isValidMobileNumber(mobileNumber);
//
//            if(authStatus) {
//                // if valid set authorized mobile number as grant user
//                oAuthTokenReqMessageContext.setAuthorizedUser(mobileNumber);
//                oAuthTokenReqMessageContext.setScope(oAuthTokenReqMessageContext.getOauth2AccessTokenReqDTO().getScope());
//            }
//        }

        return authStatus;
    }

    @Override
    public boolean issueRefreshToken() throws IdentityOAuth2Exception{
        return super.issueRefreshToken();
    }

    @Override
    public boolean isOfTypeApplicationUser() throws IdentityOAuth2Exception{
        return super.isOfTypeApplicationUser();
    }

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx){
        ScopesIssuer scopesIssuer = new ScopesIssuer();
        return scopesIssuer.setScopes(tokReqMsgCtx);
    }

}
