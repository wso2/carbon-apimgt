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

package org.wso2.carbon.apimgt.rest.api.util.interceptors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

/**
 * This class will handle the post authentication steps in incoming requests.
 * This will check whether both OAuthAuthenticationInterceptor and BasicAuthenticationInterceptor were skipped and 
 * throws a 401 unauthenticated error.
 */
public class PostAuthenticationInterceptor extends AbstractPhaseInterceptor {

    private static final Log log = LogFactory.getLog(PostAuthenticationInterceptor.class);

    public PostAuthenticationInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }

    /**
     * Handles the incoming message after post authentication. Validate the authentication scheme of the incoming request
     * based on the properties set by previous interceptors. If non of the authentication scheme is set, return a 401
     * unauthenticated response.
     * 
     * @param inMessage cxf incoming message
     */
    @Override
    public void handleMessage(Message inMessage) {
        //by-passes the interceptor if user calls an anonymous api
        if (RestApiUtil.checkIfAnonymousAPI(inMessage)) {
            return;
        }
        String authScheme = (String) inMessage.get(RestApiConstants.REQUEST_AUTHENTICATION_SCHEME);
        //check if the request does not have either the bearer or basic auth header. If so, throw 401 
        //unauthenticated error.
        if (!StringUtils.equals(authScheme, RestApiConstants.OAUTH2_AUTHENTICATION)
                && !StringUtils.equals(authScheme, RestApiConstants.BASIC_AUTHENTICATION)) {
            log.error("Authentication failed: Bearer/Basic authentication header is missing");
            throw new AuthenticationException("Unauthenticated request");
        }
    }
}
