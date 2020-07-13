/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.notification.handlers;

import com.nimbusds.jose.util.StandardCharset;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.util.Base64;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.basicauth.BasicAuthCredentialValidator;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.BasicAuthValidationInfoDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;

/**
 * This class used to authenticate notify API with basic auth.
 */
public class BasicAuthNotificationHandler extends AbstractHandler implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(BasicAuthNotificationHandler.class);
    private BasicAuthCredentialValidator basicAuthCredentialValidator;
    private final String basicAuthKeyHeaderSegment = "Basic";

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

    }

    private void initializeCredentialValidator() {

        if (basicAuthCredentialValidator == null) {
            synchronized (this) {
                try {
                    basicAuthCredentialValidator = new BasicAuthCredentialValidator();
                } catch (APISecurityException e) {
                    log.error("Error while initializing BasicAuthCredentialValidator", e);
                }
            }
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean handleRequest(MessageContext messageContext) {

        initializeCredentialValidator();
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        Map<String, String> headers =
                (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers.containsKey(APIConstants.AUTHORIZATION_HEADER_DEFAULT)) {
            try {
                String[] credentials =
                        extractBasicAuthCredentials(headers.get(APIConstants.AUTHORIZATION_HEADER_DEFAULT));
                String username = getEndUserName(credentials[0]);
                String password = credentials[1];
                BasicAuthValidationInfoDTO basicAuthValidationInfoDTO =
                        basicAuthCredentialValidator.validate(username, password);
                if (basicAuthValidationInfoDTO.isAuthenticated()) {
                    return true;
                }
            } catch (APISecurityException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext) {

        return true;
    }

    /**
     * Extract the basic authentication credentials from the basic authorization header via Base64 decoding.
     *
     * @param basicAuthHeader the basic authorization header
     * @return a String array containing username and password
     * @throws APISecurityException in case of invalid authorization header or no header
     */
    private String[] extractBasicAuthCredentials(String basicAuthHeader) throws APISecurityException {

        if (basicAuthHeader == null) {
            if (log.isDebugEnabled()) {
                log.debug("Basic Authentication: No Basic Auth Header found");
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                    APISecurityConstants.API_AUTH_MISSING_CREDENTIALS_MESSAGE);
        } else {
            if (basicAuthHeader.contains(basicAuthKeyHeaderSegment)) {
                try {
                    String basicAuthKey = new String(
                            Base64.decode(basicAuthHeader.substring(basicAuthKeyHeaderSegment.length() + 1).trim()),
                            StandardCharset.UTF_8);
                    if (basicAuthKey.contains(":")) {
                        return basicAuthKey.split(":");
                    } else {
                        log.error("Basic Authentication: Invalid Basic Auth token");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                    }
                } catch (WSSecurityException e) {
                    log.error("Error occured during Basic Authentication: Invalid Basic Auth token");
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Basic Authentication: No Basic Auth Header found");
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                        APISecurityConstants.API_AUTH_MISSING_CREDENTIALS_MESSAGE);
            }
        }
    }

    private String getEndUserName(String username) {

        return MultitenantUtils.getTenantAwareUsername(username) + "@" + MultitenantUtils.getTenantDomain(username);
    }
}
