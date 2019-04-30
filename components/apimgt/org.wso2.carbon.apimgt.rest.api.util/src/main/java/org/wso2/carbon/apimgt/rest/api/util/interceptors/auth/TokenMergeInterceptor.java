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

import java.util.ArrayList;
import java.util.TreeMap;

public class TokenMergeInterceptor extends AbstractPhaseInterceptor {

    private static final Log logger = LogFactory.getLog(TokenMergeInterceptor.class);

    public TokenMergeInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }

    public void handleMessage(Message message) throws Fault {
        //If Authorization headers are present anonymous URI check will be skipped

        ArrayList authHeaders = (ArrayList) ((TreeMap) (message.get(Message.PROTOCOL_HEADERS)))
                .get(RestApiConstants.AUTH_HEADER_NAME);

        ArrayList tokenCookie = (ArrayList) ((TreeMap) (message.get(Message.PROTOCOL_HEADERS)))
                .get(RestApiConstants.COOKIE_NAME);

        TreeMap headers = (TreeMap) message.get(Message.PROTOCOL_HEADERS);
        if (authHeaders != null && tokenCookie != null) {
            String headerTokenString = authHeaders.get(0).toString();
            String cookieTokenString = tokenCookie.get(0).toString();
            int startingIndexOfToken =
                    cookieTokenString.lastIndexOf(RestApiConstants.AUTH_COOKIE_NAME) + RestApiConstants.AUTH_COOKIE_NAME
                            .length() + 1;
            String tokenStringPartTwo = cookieTokenString.substring(startingIndexOfToken);
            String accessToken = headerTokenString + tokenStringPartTwo;
            ArrayList AuthorizationHeader = new ArrayList<>();
            AuthorizationHeader.add(0, accessToken);
            headers.put(RestApiConstants.AUTH_HEADER_NAME, AuthorizationHeader);
            message.put(Message.PROTOCOL_HEADERS, headers);
        } else {
            return;
        }
    }
}