/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.apimgt.multitenant.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.JITProvisioningPostAuthenticationHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.SUPER_TENANT_DOMAIN;
import static org.wso2.carbon.apimgt.multitenant.auth.MultiTenantAuthenticatorConstants.USER_SELECTED_TENANT_DOMAIN;

/**
 * Custom JIT Provisioning Post Authentication Handler for Multi Tenant Authenticator.
 * <p>
 * This handler extends the {@link JITProvisioningPostAuthenticationHandler} to conditionally
 * execute JIT provisioning based on the tenant domain selected by the user.
 * <p>
 * JIT provisioning is only performed when:
 * <ul>
 *   <li>The {@code USER_SELECTED_TENANT_DOMAIN} context property is not set and the
 *       authentication context tenant domain is not "carbon.super", OR</li>
 *   <li>The {@code USER_SELECTED_TENANT_DOMAIN} equals "carbon.super"</li>
 * </ul>
 * <p>
 * For other tenant domains, JIT provisioning is skipped as the user is already authenticated
 * against the target tenant's identity provider.
 */
public class MultiTenantJITProvisioningHandler extends JITProvisioningPostAuthenticationHandler {

    private static final Log LOG = LogFactory.getLog(MultiTenantJITProvisioningHandler.class);

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest request, HttpServletResponse response,
                                             AuthenticationContext context)
            throws PostAuthenticationFailedException {

        String userSelectedTenantDomain = (String) context.getProperty(USER_SELECTED_TENANT_DOMAIN);

        // Execute JIT provisioning only if:
        // 1. USER_SELECTED_TENANT_DOMAIN is null AND the context tenant domain is not "carbon.super", OR
        // 2. USER_SELECTED_TENANT_DOMAIN equals "carbon.super"
        if ((userSelectedTenantDomain == null &&
                !SUPER_TENANT_DOMAIN.equals(context.getTenantDomain()))
                || SUPER_TENANT_DOMAIN.equals(userSelectedTenantDomain)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("User selected tenant domain is '" + userSelectedTenantDomain +
                        "'. Proceeding with JIT provisioning.");
            }
            return super.handle(request, response, context);
        }

        // Skip JIT provisioning for non-super tenant domains
        if (LOG.isDebugEnabled()) {
            LOG.debug("Skipping JIT provisioning for tenant domain: " + userSelectedTenantDomain);
        }

        // Return UNSUCCESS_COMPLETED to indicate this handler has skipped
        // without performing any provisioning action
        return PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED;
    }

    @Override
    public String getName() {

        return "MultiTenantJITProvisioningHandler";
    }
}
