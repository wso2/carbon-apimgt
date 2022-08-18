/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.ext.contexthandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.apimgt.common.gateway.extensionlistener.ContextHandler;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class is the inbound implementation of ContextHandler interface. This handle websocket specific logic to consume
 * the payload from the related message context.
 */
public class InboundContextHandler implements ContextHandler {

    private static final Log log = LogFactory.getLog(InboundContextHandler.class);

    InboundMessageContext inboundMessageContext;

    public InboundContextHandler(InboundMessageContext inboundMessageContext) {
        this.inboundMessageContext = inboundMessageContext;
    }

    @Override
    public Object getProperty(String key) {
        return inboundMessageContext.getProperty(key);
    }

    @Override
    public void setProperty(String key, Object value) {
        if (value != null) {
            if (key.equals(APISecurityUtils.API_AUTH_CONTEXT)) {
                this.inboundMessageContext.setAuthContext((AuthenticationContext) value);
            } else if (key.equals(APIConstants.JwtTokenConstants.SIGNED_JWT_INFO)) {
                this.inboundMessageContext.setSignedJWTInfo((SignedJWTInfo) value);
            } else if (key.equals(APIConstants.JwtTokenConstants.KEY_TYPE)) {
                this.inboundMessageContext.setKeyType((String) value);
            } else if (key.equals(APIConstants.JwtTokenConstants.KEY_VALIDATION_INFO)) {
                this.inboundMessageContext.setInfoDTO((APIKeyValidationInfoDTO) value);
            } else if (key.equals(APIConstants.JwtTokenConstants.TOKEN)) {
                this.inboundMessageContext.setToken((String) value);
            } else if (key.equals(WebsocketUtil.authorizationHeader)) {
                this.inboundMessageContext.getHeadersToRemove().add((String) value);
            }
            this.inboundMessageContext.setProperty(key, value);
        }
    }

    @Override
    public String getAPIKeyAsQueryParam() {
        String apiKey = null;

        try {
            String fullUrl = "http://" + inboundMessageContext.getFullRequestPath();
            URL url=new URL(fullUrl);
            Map<String, String> query_pairs = new LinkedHashMap<>();
            String query = url.getQuery();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                        URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
            return query_pairs.get("apiKey");
        } catch (UnsupportedEncodingException e) {
            log.error("Error occurred while reading a apiKey query param", e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return apiKey;
    }
}
