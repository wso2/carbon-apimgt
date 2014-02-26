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

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;

import java.util.Map;

/**
 *  Mediator class used to add custom header containing API subscriber's name (caller)
 *  to request message being forwarded to actual endpoint.
 */
public class TokenPasser extends AbstractMediator {

    public boolean mediate(MessageContext synCtx) {
        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);
        addHTTPHeader(synCtx,authContext);
        return true;
    }

    private void addHTTPHeader(MessageContext synCtx, AuthenticationContext authContext) {
        Map transportHeaders = (Map)((Axis2MessageContext) synCtx).getAxis2MessageContext()
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        transportHeaders.put("assertion", authContext.getCallerToken());
    }
    
    public boolean isContentAware(){
        return false;
    }
}
