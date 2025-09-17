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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.MethodStats;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.TreeMap;

import static org.wso2.carbon.apimgt.rest.api.common.RestApiConstants.REFERER_COOKIE_NAME;
import static org.wso2.carbon.apimgt.rest.api.common.RestApiConstants.REST_API_GOVERNANCE_CONTEXT;

/**
 * TokenMergeInterceptor class merge the token parts received from the request and from the cookies,
 * and then re-create the bearer authentication header in the request.
 */
public class TokenMergeInterceptor extends AbstractPhaseInterceptor {

    private static final Log logger = LogFactory.getLog(TokenMergeInterceptor.class);
    private static final String QUERY_STRING = "org.apache.cxf.message.Message.QUERY_STRING";

    public TokenMergeInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }

    @MethodStats
    public void handleMessage(Message message) throws Fault {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting token merge process");
        }
        
        //If Authorization headers are present anonymous URI check will be skipped
        String accessToken = RestApiUtil
                .extractOAuthAccessTokenFromMessage(message, RestApiConstants.REGEX_BEARER_PATTERN,
                        RestApiConstants.AUTH_HEADER_NAME);
        if (accessToken == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No access token found in authorization header, checking query parameters");
            }
            String queryString = (String) message.get(QUERY_STRING);
            if (StringUtils.isNotEmpty(queryString)) {
                String[] queryParams = queryString.split(APIConstants.SEARCH_AND_TAG);
                String tokenFromQueryParam = Arrays.stream(queryParams)
                        .filter(name -> name.contains(APIConstants.AccessTokenConstants.ACCESS_TOKEN))
                        .findFirst().orElse(StringUtils.EMPTY);
                String[] tokenParts = tokenFromQueryParam.split("=");
                if (tokenParts.length == 2) {
                    accessToken = tokenParts[1];
                    if (logger.isDebugEnabled()) {
                        logger.debug("Access token extracted from query parameters");
                    }
                }
            }
        }
        if (accessToken == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No access token found in request");
            }
            return;
        }

        TreeMap<Object, Object> protocolMap = (TreeMap<Object, Object>) message.get(Message.PROTOCOL_HEADERS);
        if (protocolMap == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No protocol headers found, skipping token merge");
            }
            return;
        }

        ArrayList tokenCookie = (ArrayList) (protocolMap).get(RestApiConstants.COOKIE_HEADER_NAME);
        if (tokenCookie == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No cookies found in headers");
            }
            return;
        }

        String cookie = tokenCookie.get(0).toString();
        if (cookie == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cookie value is null");
            }
            return;
        }
        cookie = cookie.trim();
        String[] cookies = cookie.split(";");
        String tokenFromCookie = "";
        if (message.get("jaxrs.template.uri") != null &&
                message.get("jaxrs.template.uri").toString().contains(REST_API_GOVERNANCE_CONTEXT)) {

            String referer = protocolMap.getOrDefault(REFERER_COOKIE_NAME, "").toString();

            if (referer.contains("/admin")) {
                tokenFromCookie = Arrays.stream(cookies)
                        .filter(name -> name.contains(RestApiConstants.GOVERNANCE_ADMIN_AUTH_COOKIE_NAME))
                        .findFirst().orElse("");
            } else if (referer.contains("/publisher")) {
                tokenFromCookie = Arrays.stream(cookies)
                        .filter(name -> name.contains(RestApiConstants.GOVERNANCE_PUBLISHER_AUTH_COOKIE_NAME))
                        .findFirst().orElse("");
            } else {
                tokenFromCookie = Arrays.stream(cookies)
                        .filter(name -> name.contains(RestApiConstants.AUTH_COOKIE_NAME))
                        .findFirst().orElse("");
            }
        } else {
            tokenFromCookie = Arrays.stream(cookies)
                    .filter(name -> name.contains(RestApiConstants.AUTH_COOKIE_NAME))
                    .findFirst().orElse("");
        }
        String[] tokenParts = tokenFromCookie.split("=");
        if (tokenParts.length == 2) {
            accessToken += tokenParts[1]; // Append the token section from cookie to token part from Auth header
            if (logger.isDebugEnabled()) {
                logger.debug("Successfully merged token parts from header and cookie");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Cookie token format invalid, using original token");
            }
        }
        TreeMap headers = (TreeMap) message.get(Message.PROTOCOL_HEADERS);
        ArrayList authorizationHeader = new ArrayList<>();
        authorizationHeader.add(0, String.format("Bearer %s", accessToken));
        headers.put(RestApiConstants.AUTH_HEADER_NAME, authorizationHeader);
        message.put(Message.PROTOCOL_HEADERS, headers);
        if (logger.isDebugEnabled()) {
            logger.debug("Authorization header updated with merged token");
        }
    }
}
