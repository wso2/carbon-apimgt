/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.util.interceptors.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * TokenMergeInterceptor class merge the token parts received from the request and from the cookies,
 * and then re-create the bearer authentication header in the request.
 */
public class TokenMergeInterceptor extends AbstractPhaseInterceptor {

    private static final Log logger = LogFactory.getLog(TokenMergeInterceptor.class);

    public TokenMergeInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }

    public void handleMessage(Message message) throws Fault {
        //If Authorization headers are present anonymous URI check will be skipped
        String accessToken = RestApiUtil
                .extractOAuthAccessTokenFromMessage(message, RestApiConstants.REGEX_BEARER_PATTERN,
                        RestApiConstants.AUTH_HEADER_NAME);
        if (accessToken == null) {
            return;
        }

        ArrayList tokenCookie = (ArrayList) ((TreeMap) (message.get(Message.PROTOCOL_HEADERS)))
                .get(RestApiConstants.COOKIE_HEADER_NAME);
        if (tokenCookie == null) {
            return;
        }
        
        String cookie = tokenCookie.get(0).toString();
        if (cookie == null) {
            return;
        }

        cookie = cookie.trim();
        String[] cookies = cookie.split(";");
        String tokenFromCookie = Arrays.stream(cookies)
                .filter(name -> name.contains(RestApiConstants.AUTH_COOKIE_NAME)).findFirst().orElse("");
        String[] tokenParts = tokenFromCookie.split("=");
        if (tokenParts.length == 2) {
            accessToken += tokenParts[1]; // Append the token section from cookie to token part from Auth header
        }
        TreeMap headers = (TreeMap) message.get(Message.PROTOCOL_HEADERS);
        ArrayList authorizationHeader = new ArrayList<>();
        authorizationHeader.add(0, String.format("Bearer %s", accessToken));
        headers.put(RestApiConstants.AUTH_HEADER_NAME, authorizationHeader);
        message.put(Message.PROTOCOL_HEADERS, headers);
    }
}
