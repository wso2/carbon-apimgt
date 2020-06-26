/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.tokenmgt;

import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;

/**
 * Helper DTO to pass information to the utility method "APIMTokenIssuerUtil.generateToken()"
 */
public class JWTAccessTokenIssuerDTO {


    private String clientId;
    private long validityPeriod;
    private String[] scopeList;
    private AuthenticatedUser user;
    private OAuthAuthzReqMessageContext oauthAuthzMsgCtx;
    private OAuthTokenReqMessageContext tokReqMsgCtx;

    public String getClientId() {

        return clientId;
    }

    public void setClientId(String clientId) {

        this.clientId = clientId;
    }

    public long getValidityPeriod() {

        return validityPeriod;
    }

    public void setValidityPeriod(long validityPeriod) {

        this.validityPeriod = validityPeriod;
    }

    public String[] getScopeList() {

        return scopeList;
    }

    public void setScopeList(String[] scopeList) {

        this.scopeList = scopeList;
    }

    public AuthenticatedUser getUser() {

        return user;
    }

    public void setUser(AuthenticatedUser user) {

        this.user = user;
    }

    public void setAuthzMessageContext(OAuthAuthzReqMessageContext oauthAuthzMsgCtx) {
        this.oauthAuthzMsgCtx  = oauthAuthzMsgCtx;

    }

    public OAuthAuthzReqMessageContext getOauthAuthzMsgCtx() {

        return oauthAuthzMsgCtx;
    }

    public void setTokenReqMessageContext(OAuthTokenReqMessageContext tokReqMsgCtx) {
        this.tokReqMsgCtx = tokReqMsgCtx;

    }

    public OAuthTokenReqMessageContext getTokReqMsgCtx() {

        return tokReqMsgCtx;
    }
}
