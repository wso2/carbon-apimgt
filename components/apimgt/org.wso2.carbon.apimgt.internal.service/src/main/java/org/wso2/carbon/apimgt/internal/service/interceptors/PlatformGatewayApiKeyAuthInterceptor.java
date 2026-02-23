/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service.interceptors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.List;
import java.util.Map;

/**
 * Interceptor for Internal Data Service that authenticates requests using the
 * platform gateway registration token (api-key header). When present and valid,
 * sets REQUEST_AUTHENTICATION_SCHEME and ORGANIZATION so the request is treated
 * as authenticated (e.g. for GET /apis/{apiId} with Accept: application/zip).
 * Must run before PostAuthenticationInterceptor.
 */
public class PlatformGatewayApiKeyAuthInterceptor extends AbstractPhaseInterceptor<Message> {

    private static final Log log = LogFactory.getLog(PlatformGatewayApiKeyAuthInterceptor.class);
    private static final String API_KEY_HEADER = "api-key";

    public PlatformGatewayApiKeyAuthInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
        if (headers == null) {
            return;
        }
        String apiKey = getFirstHeader(headers, API_KEY_HEADER);
        if (StringUtils.isEmpty(apiKey)) {
            return;
        }
        PlatformGatewayDAO.PlatformGateway gateway;
        try {
            gateway = PlatformGatewayTokenUtil.verifyToken(apiKey);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway api-key verification failed", e);
            }
            throw new AuthenticationException("Unauthenticated request");
        }
        if (gateway == null) {
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway api-key invalid or expired");
            }
            throw new AuthenticationException("Unauthenticated request");
        }
        message.put(RestApiConstants.REQUEST_AUTHENTICATION_SCHEME, RestApiConstants.PLATFORM_GATEWAY_API_KEY);
        String org = StringUtils.isNotBlank(gateway.organizationId) ? gateway.organizationId : "carbon.super";
        message.put(RestApiConstants.ORGANIZATION, org);

        // Set CarbonContext so getLoggedInUserProvider() and Registry/tenant lookups use this org (avoids "organizationnull").
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(org);
        try {
            int tenantId = APIUtil.getTenantIdFromTenantDomain(org);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not resolve tenant id for org " + org + ", using super tenant", e);
            }
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        }
        // Username format expected by getAPIProvider: e.g. admin@carbon.super so tenant is derived correctly.
        String systemUser = "admin@" + org;
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(systemUser);

        if (log.isDebugEnabled()) {
            log.debug("Request authenticated via platform gateway api-key for gateway: " + gateway.id);
        }
    }

    private static String getFirstHeader(Map<String, List<String>> headers, String name) {
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            if (name.equalsIgnoreCase(e.getKey()) && e.getValue() != null && !e.getValue().isEmpty()) {
                return e.getValue().get(0);
            }
        }
        return null;
    }
}
