/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.common.impl;

import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.api.RESTAPIAuthenticator;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

import java.nio.charset.Charset;
import java.util.Base64;

/**
 * Basic auth implementation class
 */
public class BasicAuthAuthenticator implements RESTAPIAuthenticator {

    /*
    * basic auth authentication logic is executed here
    * @pqram Request
    * @param Response
    * @param ServiceMethodInfo
    * @return authentication status. true if authentication is successful; else false
    * */
    @Override
    public boolean authenticate(Request request, Response responder, ServiceMethodInfo serviceMethodInfo)
            throws APIMgtSecurityException {
        String authHeader = request.getHeader(RestApiConstants.AUTHORIZATION_HTTP_HEADER);
        if (authHeader != null) {
            String authType = authHeader.substring(0, RestApiConstants.AUTH_TYPE_BASIC_LENGTH);
            String authEncoded = authHeader.substring(RestApiConstants.AUTH_TYPE_BASIC_LENGTH).trim();


            //If Basic auth header is not found returning true to check the other interceptors(for other auth types)
            if (RestApiConstants.AUTH_TYPE_BASIC.equalsIgnoreCase(authType) && !authEncoded.isEmpty()) {
                byte[] decodedByte = authEncoded.getBytes(Charset.forName(RestApiConstants.CHARSET_UTF_8));
                String authDecoded = new String(Base64.getDecoder().decode(decodedByte),
                        Charset.forName(RestApiConstants.CHARSET_UTF_8));
                String[] authParts = authDecoded.split(":");
                String username = authParts[0];
                String password = authParts[1];
                if (authenticate(username, password)) {
                    return true;
                }
            } else {
                throw new APIMgtSecurityException("Missing 'Authorization : Basic' header in the request.`",
                        ExceptionCodes.MALFORMED_AUTHORIZATION_HEADER_BASIC);
            }

        } else {
            throw new APIMgtSecurityException("Missing Authorization header in the request.`",
                    ExceptionCodes.MALFORMED_AUTHORIZATION_HEADER_BASIC);

        }
        return false;
    }

    private boolean authenticate(String username, String password) {
        //todo improve
        return username.equals(password);

    }
}
