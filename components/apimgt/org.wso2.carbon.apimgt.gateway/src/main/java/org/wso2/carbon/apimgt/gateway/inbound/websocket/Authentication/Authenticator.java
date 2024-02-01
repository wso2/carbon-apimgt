/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.inbound.websocket.Authentication;

import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;

import java.text.ParseException;

/**
 * This interface is used to authenticate web socket API requests, and it contains methods to validate the received
 * token and authenticate the request if the validation is successful.
 */
public interface Authenticator {

    /**
     * This method is used to validate the received token.
     *
     * @param inboundMessageContext InboundMessageContext object containing the request details
     * @return A boolean value indicating whether the token is valid or not
     * @throws APISecurityException if an error occurs while validating the token
     */
    boolean validateToken(InboundMessageContext inboundMessageContext) throws APISecurityException;

    /**
     * This method is used to authenticate the token.
     *
     * @param inboundMessageContext InboundMessageContext object containing the request details
     * @return An InboundProcessorResponseDTO object containing the authentication status
     * @throws APISecurityException if an error occurs while authenticating the token
     */
    InboundProcessorResponseDTO authenticate(InboundMessageContext inboundMessageContext) throws APISecurityException;
}
