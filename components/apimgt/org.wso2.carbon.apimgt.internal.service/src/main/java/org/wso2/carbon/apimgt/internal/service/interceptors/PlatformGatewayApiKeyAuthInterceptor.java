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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;
import org.wso2.carbon.apimgt.impl.dto.ConnectGatewayConfig;
import org.wso2.carbon.apimgt.impl.dto.PlatformGatewayConnectConfig;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.servlet.http.HttpServletRequest;
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

    /** Message property set when this interceptor started the tenant flow; cleanup interceptor uses it to call endTenantFlow(). */
    public static final String MESSAGE_PROPERTY_TENANT_FLOW_STARTED = "PlatformGatewayTenantFlowStarted";
    /** Set when request is allowed using config registration_token (gateway will be created on first REGISTER). */
    public static final String MESSAGE_PROPERTY_CONNECT_WITH_TOKEN = "PlatformGatewayConnectWithToken";
    /** ThreadLocal set when auth used config token so notify impl can create gateway on first REGISTER. */
    public static final ThreadLocal<Boolean> CONNECT_WITH_TOKEN_AUTH = new ThreadLocal<>();
    /** ThreadLocal set to the connect config that matched (for multiple gateways). */
    public static final ThreadLocal<ConnectGatewayConfig> CONNECT_WITH_TOKEN_MATCHED_ENTRY = new ThreadLocal<>();

    /**
     * Path segments for which platform gateway api-key auth is allowed (no leading slash; matched
     * as path.endsWith("/" + segment) or path.contains("/" + segment + "/")). Requests to other
     * paths are not authenticated by this interceptor.
     */
    private static final String[] PLATFORM_GATEWAY_ALLOWED_PATH_SEGMENTS = {
            "apis",
            "api-keys",
            "subscription-plans",
            "notify-gateway",
            "notify-api-deployment-status",
            "deployments"
    };

    public PlatformGatewayApiKeyAuthInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) {
        if (!isPlatformGatewayAllowedPath(message)) {
            return;
        }
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
            log.warn("Platform gateway api-key verification failed with unexpected error", e);
            throw new AuthenticationException("Unauthenticated request");
        }
        if (gateway == null) {
            PlatformGatewayConnectConfig connectConfig = null;
            try {
                connectConfig = ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfigurationService().getAPIManagerConfiguration()
                        .getPlatformGatewayConnectConfig();
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not get platform gateway connect config", e);
                }
            }
            ConnectGatewayConfig matchedEntry = null;
            if (connectConfig != null) {
                for (ConnectGatewayConfig entry : connectConfig.getConnectGateways()) {
                    if (entry != null && StringUtils.isNotBlank(entry.getRegistrationToken())
                            && apiKey.trim().equals(entry.getRegistrationToken().trim())) {
                        matchedEntry = entry;
                        break;
                    }
                }
            }
            if (matchedEntry != null) {
                message.put(MESSAGE_PROPERTY_CONNECT_WITH_TOKEN, Boolean.TRUE);
                CONNECT_WITH_TOKEN_AUTH.set(Boolean.TRUE);
                CONNECT_WITH_TOKEN_MATCHED_ENTRY.set(matchedEntry);
                String org = getCurrentOrganization();
                message.put(RestApiConstants.REQUEST_AUTHENTICATION_SCHEME, RestApiConstants.PLATFORM_GATEWAY_API_KEY);
                message.put(RestApiConstants.ORGANIZATION, org);
                PrivilegedCarbonContext.startTenantFlow();
                try {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(org);
                    try {
                        int tenantId = APIUtil.getTenantIdFromTenantDomain(org);
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
                    } catch (Exception e) {
                        log.error("Could not resolve tenant id for org " + org + "; rejecting request", e);
                        PrivilegedCarbonContext.endTenantFlow();
                        throw new AuthenticationException("Unauthenticated request");
                    }
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername("admin@" + org);
                    message.put(MESSAGE_PROPERTY_TENANT_FLOW_STARTED, Boolean.TRUE);
                } finally {
                    // endTenantFlow in cleanup interceptor
                }
                if (log.isDebugEnabled()) {
                    log.debug("Request allowed via connect-with-token config; gateway will be created on REGISTER");
                }
                return;
            }
            throw new AuthenticationException("Unauthenticated request");
        }
        message.put(RestApiConstants.REQUEST_AUTHENTICATION_SCHEME, RestApiConstants.PLATFORM_GATEWAY_API_KEY);
        String org = StringUtils.trimToNull(gateway.organizationId);
        if (org == null) {
            log.error("Gateway token verification returned empty organization; rejecting request for gateway: "
                    + gateway.id);
            throw new AuthenticationException("Unauthenticated request");
        }
        message.put(RestApiConstants.ORGANIZATION, org);

        // Set CarbonContext so getLoggedInUserProvider() and Registry/tenant lookups use this org (avoids "organizationnull").
        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(org);
            try {
                int tenantId = APIUtil.getTenantIdFromTenantDomain(org);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            } catch (Exception e) {
                log.error("Could not resolve tenant id for org " + org + "; rejecting request", e);
                PrivilegedCarbonContext.endTenantFlow();
                throw new AuthenticationException("Unauthenticated request");
            }
            // Username format expected by getAPIProvider: e.g. admin@carbon.super so tenant is derived correctly.
            String systemUser = "admin@" + org;
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(systemUser);

            message.put(MESSAGE_PROPERTY_TENANT_FLOW_STARTED, Boolean.TRUE);

            if (log.isDebugEnabled()) {
                log.debug("Request authenticated via platform gateway api-key for gateway: " + gateway.id);
            }
        } finally {
            // endTenantFlow() is called in PlatformGatewayTenantFlowCleanupInterceptor (POST_INVOKE) so tenant remains set for resource invocation
        }
    }

    /**
     * When config does not provide an explicit org, use the current APIM tenant (from CarbonContext).
     */
    private static String getCurrentOrganization() {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        return StringUtils.isNotBlank(tenantDomain) ? tenantDomain : APIConstants.GatewayNotification.WSO2_ALL_TENANTS;
    }

    /**
     * Returns true only if the request path is one of the allow-listed platform gateway routes.
     * This prevents api-key header from authenticating requests to other servlet endpoints.
     */
    private static boolean isPlatformGatewayAllowedPath(Message message) {
        // CXF may or may not expose the servlet request via "http.request" (depends on transport/container).
        // Prefer CXF's PATH_INFO first, then fall back to servlet request if available.
        String path = (String) message.get(Message.PATH_INFO);
        if (StringUtils.isBlank(path)) {
            Object req = message.get("http.request");
            if (req instanceof HttpServletRequest) {
                path = ((HttpServletRequest) req).getRequestURI();
            }
        }
        if (StringUtils.isBlank(path)) {
            return false;
        }
        for (String segment : PLATFORM_GATEWAY_ALLOWED_PATH_SEGMENTS) {
            // Match ".../<segment>" or ".../<segment>/..."
            if (path.endsWith("/" + segment) || path.contains("/" + segment + "/")) {
                return true;
            }
        }
        return false;
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
