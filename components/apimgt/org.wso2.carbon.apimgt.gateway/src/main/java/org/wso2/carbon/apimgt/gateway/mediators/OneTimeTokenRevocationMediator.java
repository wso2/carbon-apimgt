/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.commons.lang3.StringUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service Implementation for One Time Token Revocation Mediator
 */
public class OneTimeTokenRevocationMediator extends AbstractMediator {

    private String scope;
    private ExecutorService oneTimeTokenExecutorService;

    public OneTimeTokenRevocationMediator() {

        oneTimeTokenExecutorService = Executors.newSingleThreadExecutor();
    }

    /**
     * This method checks whether the JWT token has the One Time Token Scope that is defined in the One Time Token
     * Revocation policy. If so, key manager is called to revoke the JWT token.
     *
     * @param messageContext This message context contains the request message properties of the relevant
     *                       API which was enabled the one time token revocation mediation in flow.
     */
    public boolean mediate(MessageContext messageContext) {

        AuthenticationContext authContext = (AuthenticationContext)
                messageContext.getProperty(APISecurityUtils.API_AUTH_CONTEXT);
        String tenantDomain = GatewayUtils.getTenantDomain();
        if (authContext != null) {
            String issuer = authContext.getIssuer();
            List<String> scopes = authContext.getRequestTokenScopes();
            if (StringUtils.isNotBlank(issuer)) {
                List<KeyManagerDto> keyManagerDtoList = KeyManagerHolder.getKeyManagerByIssuer(tenantDomain, issuer);
                if (keyManagerDtoList != null) {
                    KeyManagerDto keyManagerDto = keyManagerDtoList.get(0); // TODO : Does not support multiple km with same
                    // issuer

                    if (keyManagerDto != null && StringUtils.isNotBlank(scope) && scopes.contains(scope)) {
                        String token = authContext.getAccessToken();
                        String consumerKey = authContext.getConsumerKey();
                        oneTimeTokenExecutorService.execute(() ->
                                keyManagerDto.getKeyManager().revokeOneTimeToken(token, consumerKey));
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean isContentAware() {

        return false;
    }

    @Override
    public String getType() {

        return null;
    }

    public void setScope(String scope) {

        this.scope = scope;
    }

    public String getScope() {

        return scope;
    }
}